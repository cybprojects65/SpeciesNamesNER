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
import java.util.Iterator;
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

	// Oggetto necessario per avviare la search
	EfficientSearchInText est;

	// Numero di threads
	int threads;

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

		est = new EfficientSearchInText();
		File referenceTaxa = new File("epithet_genus.csv");
		System.out.println("Searching");
		threads = 8;
		boolean found[] = est.searchParallel(words, referenceTaxa, threads);
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

	public void enrich(String annotationName) throws Exception {
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
			} else {
				if (annotationseq.length() > 0) {
					allAnnotationsequences.add(annotationseq);
					annotationseq = "";
				}
			}
		}

		if (annotationseq.length() > 0) {
			allAnnotationsequences.add(annotationseq);
			annotationseq = "";
		}

		// NOTA: inserire qui il sistema a regole sui genus e species:
		/*
		 * 1 - se l'annotazione contiene un SOLO genus e ALMENO UN epithet oppure una
		 * lettera puntata e ALMENO UN epithet allora -> se non c'è nella lista dei
		 * "genus epithet" allora scartiamo -> altrimenti accettiamo 2 - se contiene 2
		 * genus -> scartiamo 3 - se contiene 0 genus -> scartiamo 4 - se contiene 1
		 * genus ma è una parola inglese (nella lista common english words) -> scartiamo
		 * 
		 * Generazione del dataset "genus epithet": per ogni entry di GBIF, aggiungere
		 * G. epithet Genus epithet
		 */
		System.out.println("Annotaioni trovate inizialmente");
		for (String strCurrentNumber : allAnnotationsequences) {
			System.out.println(strCurrentNumber);
		}

		HashSet<String> checkedallAnnotationsequences = new HashSet<>();

		for (String annotation : allAnnotationsequences) {
//    		1 - se l'annotazione contiene un SOLO genus e ALMENO UN epithet
			if (annotation.contains(" ")) {
				if (Character.isUpperCase(annotation.charAt(0))
						&& Character.isLowerCase(annotation.split(" ")[1].charAt(0))) {
//    				-> se non c'è nella lista dei "genus epithet" allora scartiamo
//    				-> altrimenti accettiamo
					File checkTaxa = new File("epithet_genus_con_punto.csv");
					String[] temp = { annotation };
					boolean found[] = est.searchParallel(temp, checkTaxa, threads);
					if (found[0] == true) {
						checkedallAnnotationsequences.add(annotation);
					}
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
			if (annotation.contains(" ") == false) {
				if (Character.isUpperCase(annotation.charAt(0))) {
					File checkTaxa = new File("MostCommonEnglishWords.csv");
					// Trasformo la stringa in lowercase perché il file "MostCommonEnglishWords" non
					// ha lettere maiuscole
					String[] temp = { annotation.toLowerCase() };
					boolean found[] = est.searchParallel(temp, checkTaxa, 8);
					if (found[0] == false) {
						checkedallAnnotationsequences.add(annotation);
					}
				}
			}
		}
		// A questo punto leggo il file originale in modo da stamparlo nuovamente con le
		// annotazioni
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		StringBuilder jsonIndex = new StringBuilder();
		// Rimuovo i line breaks
		testooriginale = testooriginale.replace("\n", " ").replace("\r", "");
		// Rimuovo le possibili parentesi quadre presenti nel testo di input
		testooriginale = testooriginale.replace("[", " ").replace("]", " ");
		for (String annot : checkedallAnnotationsequences) {
			if (annot.contains(" ")) {
				testooriginale = testooriginale.replace(annot, "[" + annot + "]");
			}
		}

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