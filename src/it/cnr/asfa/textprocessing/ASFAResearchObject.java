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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.regex.Matcher;

import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;

public class ASFAResearchObject {

	// Testo estratto in un array
	String[] words;

	// Lista di corrispondenze riscontrate nel testo espresse in booleani
	boolean[] matches;

	// variabile che indica la cateogoria delle annotazioni
	String category;


	LinkedHashMap<String, String> annotationstext = new LinkedHashMap<String, String>();
	LinkedHashMap<String, String> jsonAnnotations = new LinkedHashMap<String, String>();

	// Metodo che salva il tetso del txt in una variabile chiamata "puretext"
	public void capture(String fileName) throws FileNotFoundException {
		ArrayList<String> wordsArrayList = new ArrayList<String>();

		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(fileName));
			String line = reader.readLine();
			while (line != null) {
//				System.out.println(line);
//				String pattern = "\\s+|(?=\\p{Punct})|(?<=\\p{Punct})";
				String[] parol = line.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
				for (int i = 0; i < parol.length; i++) {
					wordsArrayList.add(parol[i].toLowerCase());
					// read next line
				}
				line = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Convert ArrayList to Array
		words = wordsArrayList.toArray(new String[0]);

	}

	// Metodo identifica le tassonomie e nel testo e le salva nella
	// variabile "matches"
	public void get() throws Exception {

		EfficientSearchInText est = new EfficientSearchInText();
		File referenceTaxa = new File("epithet_genus.csv");
		int nthreads = 8;

		boolean found[] = est.searchParallel(words, referenceTaxa, nthreads);
		matches = found;
	}

	public void enrich(String annotationName) throws IOException {

		// Salvo la stringa che indica la categoria delle annotazioni in una variabile
		// che utilizzerò successiamente
		category = annotationName;
		// Utilizzando l'espressione regolare qui sotto i doppi spazi sono considerati
		// come uno unico
		// Variabile per il file TXT
		StringBuffer sb = new StringBuffer();
		// Variabile per il file JSON
		StringBuffer annotation = new StringBuffer();
		// Coppia di variabili per il conteggio degli indici delle posizioni di inizio e
		// fine delle parole nel testo (quelli che servono per il file JSON)
		int s0 = 0;
		int s1 = 0;
		annotation.append("\"" + annotationName + "\":[");
		for (int i = 0; i < words.length; i++) {
			if (matches[i] == true) {
				sb.append(" [" + words[i] + "]");
				s1 = s1 + words[i].length();
				annotation.append("{\"indices\":[" + s0 + "," + s1 + "]},");
				s0 = s0 + words[i].length() + 1;
				s1 = s1 + 1;
			} else {
				sb.append(" " + words[i]);
			s1 = s1 + words[i].length() + 1;
			s0 = s0 + words[i].length() + 1;
			}
		}

		String speciesAnnotation = sb.toString();
//		System.out.println(speciesAnnotation);
		annotationstext.put(annotationName, speciesAnnotation);
		jsonAnnotations.put(annotationName, annotation.toString());

	}

	// creo il file di output in formato TXT e stampo le annotazioni ottenute
	public void materializeText() throws IOException {
		File file = new File("output.txt");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("##" + category.toUpperCase() + ":##" + "\n");
		// il substring rimuove lo spazio iniziale che si genera automaticamente
		writer.print(annotationstext.get(category).substring(1));
		writer.close();
	}

	// creo il file di output in formato JSON e stampo le annotazioni ottenute

	public void materializeJSON() throws IOException {

		File file = new File("output.json");
		String charset = "UTF-8";
		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
		writer.append("{ \"text\": \"");
		for (String s : words) {
			writer.append(s + " ");
		}
		writer.append("\", \"entities\": {");

		writer.append(jsonAnnotations.get(category).toString() + "]} } ");
		writer.close();
	}

}