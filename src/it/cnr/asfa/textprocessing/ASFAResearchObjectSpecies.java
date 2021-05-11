package it.cnr.asfa.textprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;

import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;

public class ASFAResearchObjectSpecies {

	// Array dove viene estratto il testo parla per parola per fare i match
	String[] words;

	// Lista di corrispondenze riscontrate nel testo espresse in booleani
	boolean[] matches;

	// Stringa che indica la cateogoria delle annotazioni
	String category;

	// Stringa che indica la path e il nome del file di testo in analisi
	String filename;

	// Oggetto necessario per avviare la search
	EfficientSearchInText est;

	// Numero di threads
	int threads;

	// Codifica del file
	String charset = "UTF-8";

	LinkedHashMap<String, String> annotationstext = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> annotationsjson = new LinkedHashMap<String, String>();

	public void capture(String fileName) throws FileNotFoundException {
		// Salvo nella variabile "filename" la path e nome del file di testo in analisi
		this.filename = fileName;
		File fileDir = new File(filename);
		ArrayList<String> wordsArrayList = new ArrayList<String>();

		try (FileInputStream fis = new FileInputStream(fileDir);
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(isr)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] token = line.replaceAll("[^a-zA-Z ]", " ").split("\\s+");
				for (int i = 0; i < token.length; i++) {
					wordsArrayList.add(token[i].trim()); // genus should be upper case
				}
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert ArrayList to Array
		words = new String[wordsArrayList.size()];
		words = wordsArrayList.toArray(words);

	}

	// Metodo identifica le tassonomie e nel testo e le salva nella
	// variabile "matches"
	public void get() throws Exception {

		est = new EfficientSearchInText();
		File referenceTaxa = new File("epithet_genus.csv");
		System.out.println("Searching");
		threads = 8;
		boolean found[] = est.searchParallel(words, referenceTaxa, threads);
		matches = found;
		System.out.println("Searching end");
	}

	public void enrich(String annotationName) throws Exception {
		System.out.println("Enriching...");
		category = annotationName;
		HashSet<String> allAnnotationsequences = new HashSet<>();
		String annotationseq = new String();
		String testo = new String(Files.readAllBytes(new File(filename).toPath()));
				System.out.println("Detecting viable words");
		for (int i = 0; i < words.length; i++) {
			if (matches[i] == true) {
				// Questa condizione serve per controllare che la tassonomia sia seguita da
				// un'abbreviazione
				// della parola precedente come nel caso ad esempio di "L. chalumnae". Viene
				// controllata la lunghezza della parola precedente che deve essere di
				// 1 ovvero 0+1 e quandi accorpata all'interno del vettore contenente le parole
				if (i > 0 && words[i - 1].length() == 1 && Character.isUpperCase(words[i-1].charAt(0))) {
					String toSearch = words[i - 1] + ". " + words[i];
					if (Character.isLowerCase(words[i].charAt(0)) && testo.contains(toSearch))
						words[i] = words[i - 1] + ". " + words[i];
					
				}
				// se il match attuale è positivo e anche quello precedente allora le due parole
				// vengono accorpate all'interno di una stringa
				// temporanea chiamata "annotationseq" che viene resettata ad ogni passo del for
				if (i > 0 && matches[i - 1] == true) {
					if (Character.isUpperCase(words[i].charAt(0))) {
						allAnnotationsequences.add(annotationseq);
						annotationseq = (words[i]);
					}
					else if (!annotationseq.contains(" ")){
						annotationseq = annotationseq + " " + words[i];
					}
					
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
			} else {
				if (annotationseq.length() > 0) {
					allAnnotationsequences.add(annotationseq);
					annotationseq = "";
				}
			}
		}
		System.out.println("Filtering viable words");
		testo = null; System.gc();
		if (annotationseq.length() > 0) {
			allAnnotationsequences.add(annotationseq);
			annotationseq = "";
		}

		// Array che conterrà tutte le annotazioni confermate dai successivi passaggi di
		// controllo
		HashSet<String> checkedallAnnotationsequences = new HashSet<>();

		// Leggo il file "MostCommonEnglishWords.csv" e inserisco ciascuna linea
		// all'inerno di un array
		BufferedReader in = new BufferedReader(new FileReader("MostCommonEnglishWords.csv"));
		String str;
		List<String> MostCommonEnglishWords = new ArrayList<String>();
		while ((str = in.readLine()) != null) {
			MostCommonEnglishWords.add(str.toLowerCase());
		}
		in.close();

		// Array di appoggio necessario per eseguire i confronti successivi
		ArrayList<String> ArrayListGenusEpithet = new ArrayList<String>();

		for (String annotation : allAnnotationsequences) {
//    		1 - se l'annotazione contiene un SOLO genus e ALMENO UN epithet
			if (annotation.contains(" ")) {
				if (Character.isUpperCase(annotation.charAt(0))
						&& Character.isLowerCase(annotation.split(" ")[1].charAt(0))) {
//    				-> se non c'è nella lista dei "genus epithet" allora scartiamo
//    				-> altrimenti accettiamo
//					Per velocizare l'operazione questi riscontri vengono salvati in un array in modo da eseguire la search una volta sola con tutti i riscontri in un secondo momento
					ArrayListGenusEpithet.add(annotation);
				}
				// Se è formato da
				if (Character.isUpperCase(annotation.charAt(0))
						&& Character.isLowerCase(annotation.split(" ")[1].charAt(0))) {

				}
//        		2 - se contiene 2 genus -> scartiamo
				if (Character.isUpperCase(annotation.charAt(0))
						&& Character.isUpperCase(annotation.split(" ")[1].charAt(0))) {
					continue;
				}
			}
//    		3 - se contiene 0 genus -> scartiamo
			if (Character.isLowerCase(annotation.charAt(0))) {
				continue;
			}
//    		4 - se contiene 1 genus ma è una parola inglese (nella lista common english words) -> scartiamo
			if (!annotation.contains(" ")) {
				if (annotation.length()>=3 && Character.isUpperCase(annotation.charAt(0)) && annotation.toLowerCase().equals(annotation.substring(0,1).toLowerCase()+annotation.substring(1))) {
					if (!(MostCommonEnglishWords.contains(annotation.toLowerCase()))) {
						checkedallAnnotationsequences.add(annotation);
					}
				}
			}
		}

		System.out.println("Checking species scientific names");
		// Convert ArrayList to Array
		String[] ArrayGenusEpithet = new String[ArrayListGenusEpithet.size()];
		ArrayGenusEpithet = ArrayListGenusEpithet.toArray(ArrayGenusEpithet);
		// Creo un altro Array dove le occorrenze vengono aggiunte fino alla seconda
		// parola per ciascuna in modo da non invalidare il successivo confronto con
		// l'array "epithet_genus_con_punto.csv"
		String[] ArrayGneusEpithetTroncato = new String[ArrayGenusEpithet.length];
		for (int i = 0; i < ArrayGenusEpithet.length; i++) {
			int count = ArrayGenusEpithet[i].length() - ArrayGenusEpithet[i].replace(" ", "").length();
			if (count == 1) {
				ArrayGneusEpithetTroncato[i] = ArrayGenusEpithet[i];
			}
			if (count >= 2) {
				String elements[] = ArrayGenusEpithet[i].split(" ");
				ArrayGneusEpithetTroncato[i] = elements[0] + " " + elements[1];
			}
		}

		File referenceTaxa = new File("epithet_genus_con_punto.csv");
		System.out.println("Searching");
		// eseguo il confronto con l'array troncato ma aggiungo i risultati ottenuti
		// utilizzando l'array completo
		boolean found[] = est.searchParallel(ArrayGneusEpithetTroncato, referenceTaxa, threads);
		System.out.println("Searching end");
		// Aggiungo tutti i match filtrati dalle condizioni precedenti nell'array
		// "checkedallAnnotationsequences"
		for (int i = 0; i < ArrayGenusEpithet.length; i++) {
			if (found[i] == true) {
				checkedallAnnotationsequences.add(ArrayGenusEpithet[i]);
			}else {
				String genus = ArrayGenusEpithet[i].substring(0,ArrayGenusEpithet[i].indexOf(" "));
				if (genus.length()>=3 && Character.isUpperCase(genus.charAt(0)) && genus.toLowerCase().equals(genus.substring(0,1).toLowerCase()+genus.substring(1))) {
						if (!(MostCommonEnglishWords.contains(genus.toLowerCase()))) {
							checkedallAnnotationsequences.add(genus);
						}
				}
			}
		}
		System.out.println("Annotating...");
		// A questo punto leggo il file originale in modo da stamparlo nuovamente con le
		// annotazioni
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		StringBuilder jsonIndex = new StringBuilder();
		// Rimuovo i line breaks
		testooriginale = testooriginale.replace("\n", " ").replace("\r", "");
		// Rimuovo le possibili parentesi quadre presenti nel testo di input
		testooriginale = testooriginale.replace("[", " ").replace("]", " ");
		
		
		List<String> ss = new ArrayList<>(checkedallAnnotationsequences);
 		ss.sort((s1, s2) -> s2.length() - s1.length());
 		
		for (String annot : ss) {
			// if (annot.contains(" ")) {
			//testooriginale = testooriginale.replaceAll("annot", "[" + annot + "]");
			String regex = "(\\W|^)"+annot+"(\\W|$)";
			
			Pattern p = Pattern.compile(regex);
			Matcher m = p.matcher(testooriginale);
			int le = testooriginale.length();
			StringBuffer sb = new StringBuffer();
			int s0 = 0;
			
		    while (m.find()) {
		      int s = m.start();
		      int e = m.end();
		      if (s==0 && !Pattern.matches("\\p{Punct}", ""+testooriginale.charAt(s)))
		       	s=-1;
		      if (e==le && !Pattern.matches("\\p{Punct}", ""+testooriginale.charAt(e-1)))
		      	e=le+1;
		      	//System.out.println("--->>"+testooriginale.substring(s+1,e-1)+" vs "+annot);
		      	sb.append(testooriginale.substring(s0,s+1));
		      	sb.append("["+testooriginale.substring(s+1,e-1)+"]");
		      	s0 = e-1;
		     }
		    
		    if (s0<le)
		    	sb.append(testooriginale.substring(s0));
		    testooriginale = sb.toString();
		    
			// }
		}
		
		System.out.println("..End annotating");
		testooriginale = ASFAResearchObjectSpecies.cleanupNested(testooriginale);
		// Identificazione indici parentesi di annotazione da inserire nel JSON
		List<Integer> indices = new ArrayList<>();
		String primaparentesi = "[";
		String secondaparentesi = "]";
		int counter_one = 0;
		int index_primaparentesi = testooriginale.indexOf(primaparentesi);
		int counter_two = 0;
		int index_secondaparentesi = testooriginale.indexOf(secondaparentesi);
		while (index_primaparentesi >= 0) {
			indices.add(index_primaparentesi - counter_one);
			indices.add(index_secondaparentesi - counter_two - 1);
			counter_two = counter_two + 2;
			counter_one = counter_one + 2;
			index_primaparentesi = testooriginale.indexOf(primaparentesi, index_primaparentesi + 1);
			index_secondaparentesi = testooriginale.indexOf(secondaparentesi, index_secondaparentesi + 1);
		}
		Collections.sort(indices);
		for (int i = 0; i < indices.size(); i = i + 2) {
			jsonIndex.append("{\"indices\": [" + indices.get(i) + "," + indices.get(i + 1) + "]},");
		}

		// Inserimento delle annotazioni nei rispettivi oggetti
		annotationstext.put(annotationName, testooriginale);
		annotationsjson.put(annotationName, jsonIndex.toString());
	}

	// creo il file di output in formato TXT e stampo le annotazioni ottenute
	public void materializeText() throws IOException {
		File file = new File("output.txt");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));

		writer.append("##" + category.toUpperCase() + "##" + "\n");
		writer.print(annotationstext.get(category));
		writer.close();
	}

	// creo il file di output in formato JSON e stampo le annotazioni ottenute

	public static String cleanupNested(String annotatedtext) {


		int idx = annotatedtext.indexOf("[[");
		if (idx < 0)
			return annotatedtext;
		else {
			int l = annotatedtext.length();
			char[] newStr = new char[l];
			int level = 0;
			for (int i = 0; i < annotatedtext.length(); ++i) {
				if (annotatedtext.charAt(i) == '[') {
					if (level == 0) // check before incrementing
						newStr[i] = '[';
					level++;
				} else if (annotatedtext.charAt(i) == ']') {
					level--;
					if (level == 0) // check after incrementing
						newStr[i] = ']';
				} else {
					newStr[i] = annotatedtext.charAt(i);
				}
			}
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < l; i++) {
				if (newStr[i] > 0)
					sb.append(newStr[i]);
			}

			return sb.toString();
		}

	}
	
	public static String cleanupNested1(String str) {

		int idx = str.indexOf("[[");
		if (idx < 0)
			return str;
		else {
			int l = str.length();
			char[] newStr = new char[l];
			int level = 0;
			for (int i = 0; i < str.length(); ++i) {
				if (str.charAt(i) == '[') {
					if (level == 0) // check before incrementing
						newStr[i] = '[';
					level++;
				} else if (str.charAt(i) == ']') {
					level--;
					if (level == 0) // check after incrementing
						newStr[i] = ']';
				} else {
					newStr[i] = str.charAt(i);
				}
			}
			StringBuffer sb = new StringBuffer();

			for (int i = 0; i < l; i++) {
				if (newStr[i] > 0)
					sb.append(newStr[i]);
			}

			return sb.toString();
		}

	}

	public void materializeJSON() throws IOException {

		File file = new File("output.json");
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("{ \"text\": \"");
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		writer.append(testooriginale.replace("\n", " ").replace("\r", ""));
		// {"Taxon":[
		// {"indices":[393,399]},{"indices":[400,409]},{"indices":[577,586]},{"indices":[694,701]},{"indices":[745,752]},{"indices":[773,783]}
		// ]
		writer.append("\", \"entities\": " + "{ " + "\"" + category + "\": [");
		String jsonannotation = ("" + annotationsjson.get(category)).trim();
		if (jsonannotation.endsWith(","))
			jsonannotation = jsonannotation.substring(0, jsonannotation.length() - 1);
		writer.append(jsonannotation + "]} }");
		writer.close();

	}

}