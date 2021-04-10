package it.cnr.asfa.textprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;

public class ASFAResearchObject {

	// Array dove viene estratto il testo parla per parola per fare i match
	String[] words;

	// Lista di corrispondenze riscontrate nel testo espresse in booleani
	boolean[] matches;

	// Stringa che indica la cateogoria delle annotazioni
	String category;

	// Stringa che indica la path e il nome del file di testo in analisi
	String filename;

	LinkedHashMap<String, String> annotationstext = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> annotationsjson = new LinkedHashMap<String, String>();

	public void capture(String fileName) throws FileNotFoundException {
		// Salvo nella variabile "filename" la path e nome del file di testo in analisi
		this.filename = fileName;
		ArrayList<String> wordsArrayList = new ArrayList<String>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {

				String[] token = line.replaceAll("[^a-zA-Z ]", "").split("\\s+");
				for (int i = 0; i < token.length; i++) {
					wordsArrayList.add(token[i]); // genus should be upper case
					// read next line
				}
				line = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert ArrayList to Array
		words = new String[wordsArrayList.size()];
		words = wordsArrayList.toArray(words);

		// Comando per stampare l'array
		// System.out.println(Arrays.toString(words));

	}

	// Metodo identifica le tassonomie e nel testo e le salva nella
	// variabile "matches"
	public void get() throws Exception {

		EfficientSearchInText est = new EfficientSearchInText();
		File referenceTaxa = new File("epithet_genus.csv");
		int nthreads = 8;
		System.out.println("Searching");
		boolean found[] = est.searchParallel(words, referenceTaxa, nthreads);
		matches = found;
		// Versione con doppia search
//		boolean genusfound[] = est.searchParallel(words, genus, nthreads);
//		boolean epithetfound[] = est.searchParallel(words, epithet, nthreads);
//		int size = genusfound.length;
//		boolean[] temp = new boolean[size];
//		for (int i = 0; i < genusfound.length; i++) {
//			 if (genusfound[i] == true) {
//				 temp[i]=true;
//			}	
//			if (epithetfound[i] == true) {
//				temp[i]=true;
//			} 
//		}
//		matches = temp;
		System.out.println("Searching end");
	}

	public void enrich(String annotationName) throws IOException {
		System.out.println("Enriching...");
		category = annotationName;
		HashSet<String> allAnnotationsequences = new HashSet<>();
		String annotationseq = new String();
		for (int i = 0; i < words.length; i++) {
			if (matches[i] == true) {
				// Questa condizione serva per controllare che la tassonomia sia seguita da
				// un'abbreviazione
				// della parola precedente come nel caso ad esempio di "L. chalumnae". Viene
				// controllata la lunghezza della parola precedente che deve essere di
				// 1 ovvero 0+1 e quandi accorpata all'interno del vettore contenente le parole
				if (i > 0 && words[i - 1].length() == 1) {
					words[i] = words[i - 1] + ". " + words[i];
				}
				// se il match attuale è positivo e anche quello precedente allora le due parole
				// vengono accorpate all'interno di una stringa
				// temporanea chiamata "annotationseq" che viene resettata ad ogni passo del for
				if (i > 0 && matches[i - 1] == true) {
					annotationseq = annotationseq + " " + words[i];
				} else {
					// a questo punto se questa stringa esiste e quindi se il match è stato fatto
					// allora questa stringa "annotationseq" viene aggiunta
					// ad un vetore contenente i match elaborati chiamato ""annotationseq""
					if (annotationseq.length() > 0) {
						allAnnotationsequences.add(annotationseq);
					}
					// se invece questa stringa è vuota allora se siamo qui cmq il match è positivo
					// e quindi si deve agggiungere la singola parola
					// alla sringa temporanea che così viene resettata alla parola con la quale il
					// match ha dato valore positivo
					annotationseq = (words[i]);
				}
			}
		}
		// A questo punto leggo il file originale in modo da stamparlo nuovamente con le
		// annotazioni
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		StringBuilder jsonIndex = new StringBuilder();
		// Rimuovo i line breaks
		testooriginale = testooriginale.replace("\n", " ").replace("\r", "");
		List<Integer> index = new ArrayList<>();
		for (String annot : allAnnotationsequences) {
			if (annot.contains(" ")) {
				int s1 = testooriginale.indexOf(annot);
				int s2 = annot.length() + s1;
				index.add(s1);
				index.add(s2);
				testooriginale = testooriginale.replace(annot, "[" + annot + "]");
			}
		}
		Collections.sort(index);
		for (int i = 0; i < index.size(); i = i + 2) {
			jsonIndex.append("{\"indices\": [" + index.get(i) + "," + index.get(i + 1) + "]},");
		}

		annotationstext.put(annotationName, testooriginale);
		annotationsjson.put(annotationName, jsonIndex.toString());
	}

	// creo il file di output in formato TXT e stampo le annotazioni ottenute
	public void materializeText() throws IOException {
		File file = new File("output.txt");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));

		writer.append("##" + category.toUpperCase() + "##" + "\n");
		writer.print(annotationstext.get(category));
		writer.close();
	}

	// creo il file di output in formato JSON e stampo le annotazioni ottenute

	public void materializeJSON() throws IOException {

		File file = new File("output.json");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("{ \"text\": \"");
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		writer.append(testooriginale.replace("\n", " ").replace("\r", ""));
		writer.append("\", \"entities\": [");
		writer.append(annotationsjson.get(category) + "]}");
		writer.close();

	}
}