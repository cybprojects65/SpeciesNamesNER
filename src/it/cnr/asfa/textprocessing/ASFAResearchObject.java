package it.cnr.asfa.textprocessing;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;


import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;

public class ASFAResearchObject {

	// Testo pulito estratto dal file JSON
	String puretext;

	// Lista di corrispondenze riscontrate nel testo espresse in booleani
	boolean[] matches;
	
	// Stringa che descrive la tipologia della annotazioni richieste, nel caso specifico Taxon ma che potrebbe diventare Worms o Gibf
	String category;

	// le dichiarazioni precedenti "LinkedHashMap<String,String> annotationstext;"
	// causavano l'errore "Exception in thread "main"
	// java.lang.NullPointerException "
	LinkedHashMap<String, String> annotationstext = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> jsonAnnotations = new LinkedHashMap<String, String>();

	
	// Metodo che salva il tetso del txt in una variabile chiamata "puretext"
	public void capture(String fileName) {
	    String text = "";
	    try {
	      text = new String(Files.readAllBytes(Paths.get(fileName)));
	    } catch (IOException e) {
	      e.printStackTrace();
	    }

	    puretext = text;
	  }
		
		
	

	// Metodo identifica le tassonomie e nel testo e le salva nella
	// variabile "matches"
	public void get() throws Exception {

		String[] TokenizedjsonText = puretext.split("\\s+");
		// Per migliorare il match rimuovo i segni di punteggiatura e trasformo il testo
		// in minuscolo. Questo passaggio viene effettuato dopo la suddivisione del
		// testo in tokens per eviare che si verifichino delle incogruenze a livello di
		// match di tokens nel metodo "enrich" dove il testo deve essere quello
		// originale
		// dell'output del json e non quello trasformato per effettuare al meglio il
		// match
		for (int i = 0; i < TokenizedjsonText.length; i++) {
			TokenizedjsonText[i] = TokenizedjsonText[i].replaceAll("[^a-zA-Z ]", "").toLowerCase();
		}
		EfficientSearchInText est = new EfficientSearchInText();
		File referenceTaxa = new File("epithet_genus.csv");
		int nthreads = 8;

		boolean found[] = est.searchParallel(TokenizedjsonText, referenceTaxa, nthreads);
		matches = found;
	}

	
	
	public void enrich(String annotationName) throws IOException {

		// Salvo la stringa che indica la categoria delle annotazioni in una variabile che utilizzerò successiamente
		category = annotationName;
		// Utilizzando l'espressione regolare qui sotto i doppi spazi sono considerati come uno unico
		String[] TokenizedjsonText = puretext.split("\\s+");
		// Variabile per il file TXT
		StringBuffer sb = new StringBuffer();
		// Variabile per il file JSON
		StringBuffer annotation = new StringBuffer();
		// Coppia di variabili per il conteggio degli indici delle posizioni di inizio e
		// fine delle parole nel testo (quelli che servono per il file JSON)
		int s0 = 0;
		int s1 = 0;
		annotation.append("\"" + annotationName + "\":[");
		for (int i = 0; i < TokenizedjsonText.length; i++) {
			if (matches[i] == true) {
				sb.append(" [" + TokenizedjsonText[i] + "]");
				s1 = s1 + TokenizedjsonText[i].length();
				annotation.append("{\"indices\":[" + s0 + "," + s1 + "]}");
				s0 = s0 + TokenizedjsonText[i].length() + 1;
				s1 = s1 + 1;
			} else
				sb.append(" " + TokenizedjsonText[i]);
			s1 = s1 + TokenizedjsonText[i].length() + 1;
			s0 = s0 + TokenizedjsonText[i].length() + 1;

		}

		String speciesAnnotation = sb.toString();
		System.out.println(speciesAnnotation);
		annotationstext.put(annotationName, speciesAnnotation);
		jsonAnnotations.put(annotationName, annotation.toString());

	}
	

	// creo il file di output in formato TXT e stampo  le annotazioni ottenute
	public void materializeText() throws IOException {
		File file = new File("output.txt");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("Original text:"+ "\n" + puretext+ "\n" );
		writer.append("##" + category.toUpperCase() + ":##" + "\n");
		writer.print(annotationstext.get(category));
		writer.close();
	}

	
	// creo il file di output in formato JSON e stampo le annotazioni ottenute

	public void materializeJSON() throws IOException {

		File file = new File("output.json");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("{ \"text\": \"" + puretext.replace("\n", "").replace("\r", "")+ "\", \"entities\": {" );
		writer.append(jsonAnnotations.get(category).toString() + "]} } ");	
		writer.close();
		}

}