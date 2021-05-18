package it.cnr.asfa.textprocessing.tests;

import java.io.BufferedReader;
import java.util.HashMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparisonJohn {

	static String[] orchestatorAnnotation;
	static String[] botanicalNerAnnotation;

	static String[] annotazioniComuni;
	static String[] unicheOrchestatorAnnotation;
	static String[] unicheBotanicalNerAnnotation;

	static int TP = 0;
	static int FP = 0;
	static int TN = 0;
	static int FN = 0;

	static double accuracy;
	static double precision;
	static double recall;
	static double f1;

	// Estraggo tutte le annotazioni di Orchestator
	public static void GetOrchestatorAnnotation(String filename) throws IOException {
		ArrayList<String> contenitore = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
				Matcher m = Pattern.compile("\\[(.*?)\\]").matcher(line);
				while (m.find()) {
					// Qui " 1 " serve a rimuovere la parentesi dai match
					contenitore.add(m.group(1));

				}

			}

		}

		orchestatorAnnotation = new String[contenitore.size()];
		orchestatorAnnotation = contenitore.toArray(orchestatorAnnotation);

	}

	// Estraggo le annotazioni designate di BotanicalNer
	public static void GetBotanicalNerAnnotation(String filename) throws IOException {

		ArrayList<String> contenitore = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			boolean dentro = false;
			String temp = "";
			while ((line = br.readLine()) != null) {
				if (line.contains("B-lat_species")) {
					dentro = true;
				}
				if ((line.contains("B-lat_genus"))) {
					String line_clean = "";
					line_clean = line.replace(",B-lat_genus", "");
					contenitore.add(line_clean);
					dentro = false;
				}
				if ((line.contains(",O")) && (dentro == true)) {
					// Aggiungo la striga array di appoggio senza lo spazio finale al mio array
					contenitore.add(temp.substring(0, temp.length() - 1));
					// Resetto la stringa
					temp = "";
					// Resetto il flag
					dentro = false;
				}
				if (dentro == true) {
					String line_clean = "";
					// pulisco la stringa prima di inserirla nell'array
					if (line.contains("B-lat_species")) {
						line_clean = line.replace(",B-lat_species", " ");
					}
					if (line.contains("I-lat_species")) {
						line_clean = line.replace(",I-lat_species", " ");
					}
					temp = temp + line_clean;
				}

			}
		}
		
		//eliminare specie della lista john vs Coro
		//aggiungere specie della lista Coro OK - Wikipedia
		botanicalNerAnnotation = new String[contenitore.size()];
		botanicalNerAnnotation = contenitore.toArray(botanicalNerAnnotation);

	}

	public static void GetStatistics(String orchestrator_file, String botanical_file) throws IOException {

		ArrayList<String> contenitore1 = new ArrayList<String>();

		// Prendo le annotazioni presenti sia in orchestrator che in botanical
		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotation[i].equals(orchestatorAnnotation[j])) {
					contenitore1.add(botanicalNerAnnotation[i]);
					break;
				}

			}

		}

		annotazioniComuni = new String[contenitore1.size()];
		annotazioniComuni = contenitore1.toArray(annotazioniComuni);

		// Prendo le anotazioni presenti in orchestrator e non in botanical
		ArrayList<String> contenitore2 = new ArrayList<String>();

		for (int i = 0; i < orchestatorAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < botanicalNerAnnotation.length; j++) {
				if (orchestatorAnnotation[i].equals(botanicalNerAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				contenitore2.add(orchestatorAnnotation[i]);
			}
		}
		unicheOrchestatorAnnotation = new String[contenitore2.size()];
		unicheOrchestatorAnnotation = contenitore2.toArray(unicheOrchestatorAnnotation);

		// Prendo le anotazioni presenti in botanical e non in orchestrator

		ArrayList<String> contenitore3 = new ArrayList<String>();

		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotation[i].equals(orchestatorAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				contenitore3.add(botanicalNerAnnotation[i]);
			}
		}
		unicheBotanicalNerAnnotation = new String[contenitore3.size()];
		unicheBotanicalNerAnnotation = contenitore3.toArray(unicheBotanicalNerAnnotation);

		// Adesso ho tutti i dati per generare le statistiche

		// Inizializzo queste 3 variabili che mi serviranno (riga 164) a contare il
		// numero di TRUE NEGATIVE
		int TPtotal = 0;
		int FPtotal = 0;
		int FNtotal = 0;

		// I TRUE POSITIVE sono il numero di elememti in comune tra i due insiemi
		for (int i = 0; i < annotazioniComuni.length; i++) {
			TP++;
			String[] conteggio1 = annotazioniComuni[i].split("\\s+");
			TPtotal = TPtotal + conteggio1.length;

		}
		TP = TPtotal;
		
		// I FALSE POSITIVE sono il numero di match che sono unici di orchestrator

		for (int i = 0; i < unicheOrchestatorAnnotation.length; i++) {
			FP++;
			String[] conteggio2 = unicheOrchestatorAnnotation[i].split("\\s+");
			FPtotal = FPtotal + conteggio2.length;
		}

		FP = FPtotal;
		// I FALSE NEGATIVE sono il numero di match che sono unici di botanical

		for (int i = 0; i < unicheBotanicalNerAnnotation.length; i++) {
			FN++;
			String[] conteggio3 = unicheBotanicalNerAnnotation[i].split("\\s+");
			FNtotal = FNtotal + conteggio3.length;
		}
		
		FP = FNtotal;
		
//		I TRUE NEGATIVE sono calcolati sottraendo dal numero totale di parole analizzate 
//		da orchestrator le altre categorie ovvero TP, FP e FN 
//		Per ottenere il numero totale di parole analizzate da orchestrator prendo il testo originale
//		e ri divido le parole utilizzando il medodo di suddivisione originale di orchestrator preso 
//		dalla specifica classe "ASFAResearchObjectSpecies.java"

		int paroleOrchestrator = 0;

		File fileDir = new File("gold_only_text_rev.txt");
		ArrayList<String> wordsArrayList = new ArrayList<String>();

		try (FileInputStream fis = new FileInputStream(fileDir);
				InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
				BufferedReader reader = new BufferedReader(isr)) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] token = line.replaceAll("[^a-zA-Z ]", " ").split("\\s+");
				for (int i = 0; i < token.length; i++) {
					paroleOrchestrator++;
				}
			}
			reader.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Ottengo il numero dei TRUE NEGATIVE
		TN = paroleOrchestrator - TPtotal - FPtotal - FNtotal;

		precision = (double) TP / (double) (TP + FP);
		recall = (double) TP / (double) (TP + FN);
		f1 = 2d * (precision * recall) / (precision + recall);
		accuracy = (double) (TP + TN) / (double) (TP + TN + FP + FN);

		System.out.println("\n");

		System.out.println("TP: " + TP);
		System.out.println("TN: " + TN);
		System.out.println("FP: " + FP);
		System.out.println("FN: " + FN);
		System.out.println("\n");

		System.out.println("Accuracy: " + accuracy);
		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
		System.out.println("F1 Score: " + f1);

	}

	public static void main(String[] args) throws IOException {

		GetOrchestatorAnnotation("output.txt");
		GetBotanicalNerAnnotation("Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatistics("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv");

		System.out.println("\n\n DONE");

	}

}
