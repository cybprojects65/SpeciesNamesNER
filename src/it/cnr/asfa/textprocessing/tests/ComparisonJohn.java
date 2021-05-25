package it.cnr.asfa.textprocessing.tests;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparisonJohn {

	static String[] orchestatorAnnotation;
	static String[] botanicalNerAnnotation;
	static String[] botanicalNerAnnotationChecked;

	// Variabili per il conteggio delle statitiche con le occorreze NON controllate
	// in GBIF

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

	// Variabili per il conteggio delle statitiche con le occorreze CONTROLLATE in
	// GBIF
	static String[] annotazioniComuniChecked;
	static String[] unicheOrchestatorAnnotationChecked;
	static String[] unicheBotanicalNerAnnotationChecked;

	static int TPChecked = 0;
	static int FPChecked = 0;
	static int TNChecked = 0;
	static int FNChecked = 0;

	static double accuracyChecked;
	static double precisionChecked;
	static double recallChecked;
	static double f1Checked;

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

		// eliminare specie della lista john vs Coro

		contenitore.removeAll(Collections.singleton("Carapichea ipecacuanhaIpecacuanha"));
		contenitore.removeAll(Collections.singleton("varietiesBidens tripartita subsp. bullatus"));
		contenitore.removeAll(Collections.singleton("Clematis × jackmanii"));
		contenitore.removeAll(Collections.singleton("L. pyrenaicus subsp. pyrenaicus"));
		contenitore.removeAll(Collections.singleton("Micromeria × angosturae"));
		contenitore.removeAll(Collections.singleton("M. tenuis subsp. linkii"));
		contenitore.removeAll(Collections.singleton("M. varia subsp. canariensis"));
		contenitore.removeAll(Collections.singleton("Musa × paradisiaca"));
		contenitore.removeAll(Collections.singleton("Panicum italicum L."));
		contenitore.removeAll(Collections.singleton("Iris halophila var. sogdiana"));
		contenitore.removeAll(Collections.singleton("Bothriochloa ischaemum var. ischaemum"));
		contenitore.removeAll(Collections.singleton("Daucus carota subsp. sativus"));
		contenitore.removeAll(Collections.singleton("A. xalapensis var. texana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. halophila"));
		contenitore.removeAll(Collections.singleton("Iris spuria ssp. sogdiana"));
		contenitore.removeAll(Collections.singleton("Iris halophile subsp. sogdiana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. notha"));
		contenitore.removeAll(Collections.singleton("Taxus × media"));
		contenitore.removeAll(Collections.singleton("Dendromecon rigida subsp. harfordii"));
		contenitore.removeAll(Collections.singleton("M. spicata var. longifolia"));
		contenitore.removeAll(Collections.singleton("Fritillaria lusitanica subsp. oranensis"));
		contenitore.removeAll(Collections.singleton("Didymopanax morototoni var. angustipetalum"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. demetrii"));
		contenitore.removeAll(Collections.singleton("Matthiola longipetala subsp. bicornis"));
		contenitore.removeAll(Collections.singleton("O. maculata subsp. pterocarpa"));
		contenitore.removeAll(Collections.singleton("T. × cultorum"));
		contenitore.removeAll(Collections.singleton("A. fissuratus var. intermedius"));
		contenitore.removeAll(Collections.singleton("Zea mays L."));
		contenitore.removeAll(Collections.singleton("Cypella aquatilis Ravenna"));
		contenitore.removeAll(Collections.singleton("Erica abietina subsp. perfoliosa"));
		contenitore.removeAll(Collections.singleton("Quercus mongolica var. grosseserrata"));
		contenitore.removeAll(Collections.singleton("Rosa × centifolia"));
		contenitore.removeAll(Collections.singleton("Vachellia nilotica subsp. adstringens"));
		contenitore.removeAll(Collections.singleton("S. x herrenhusana"));
		contenitore.removeAll(Collections.singleton("Raphanus raphanistrum subsp. sativus"));
		contenitore.removeAll(Collections.singleton("Q × beaumontiana"));
		contenitore.removeAll(Collections.singleton("U. montana pendula nova"));
		contenitore.removeAll(Collections.singleton("D. gigantea var. geniculata"));
		contenitore.removeAll(Collections.singleton("Clematis virginiana L. var. missouriensis"));
		contenitore.removeAll(Collections.singleton("Taxus baccata var. wallichiana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. musulmanica"));
		contenitore.removeAll(Collections.singleton("Rubus fruticosus L."));

		//contenitore.add("varietiesBidens tripartita");
		contenitore.add("L. pyrenaicus");
		contenitore.add("M. tenuis");
		contenitore.add("M. varia");
		contenitore.add("Panicum italicum");
		contenitore.add("Bothriochloa ischaemum");
		contenitore.add("A. xalapensis");
		contenitore.add("Iris halophile");
		contenitore.add("M. spicata");
		contenitore.add("Fritillaria lusitanica");
		contenitore.add("Didymopanax morototoni");
		contenitore.add("Zea mays");
		contenitore.add("Cypella aquatilis");
		contenitore.add("Erica abietina");
		contenitore.add("Vachellia nilotica");
		contenitore.add("U. montana");
		contenitore.add("Rubus fruticosus");

		// aggiungere specie della lista Coro OK - Wikipedia
		try (BufferedReader br = new BufferedReader(
				new FileReader("Tests/File_per_la_valutazione/FalsePositivesChecked.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(",OK")) {
					contenitore.add(line.replace(",OK", ""));
				}
			}
		}

		// Rimuovo dalla lista del gold le entry di: "Argentina",
		// "Diplocalyx","rupicola"

		contenitore.removeAll(Collections.singleton("Argentina"));
		contenitore.removeAll(Collections.singleton("Diplocalyx"));
		contenitore.removeAll(Collections.singleton("rupicola"));

		// Converto "contenitore" che e' in formato ArrayList<String> in un array di
		// tipo String[] e lo salvo come variabile globale
		botanicalNerAnnotation = new String[contenitore.size()];
		botanicalNerAnnotation = contenitore.toArray(botanicalNerAnnotation);

		// Creo un altro array che conterrà solo le specie presenti in
		// "epithet_genus.csv" che sono nel gold

		ArrayList<String> temp = new ArrayList<String>();

		File genus = new File("epithet_genus.csv");
		File species = new File("epithet_genus_con_punto.csv");
		List<String> allGenus = Files.readAllLines(genus.toPath());
		List<String> allSpecies = Files.readAllLines(species.toPath());

		ArrayList<String> contenitoreChecked = new ArrayList<String>();
		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
//			quelle con solo il genus devono essere in epithet_genus.csv
			String[] words = botanicalNerAnnotation[i].split("\\s+");
			if (words.length == 1) {
				if (allGenus.contains(botanicalNerAnnotation[i])) {
					contenitoreChecked.add(botanicalNerAnnotation[i]);
				}
			}

//				quelle con genus e specie devono essere in epithet_genus_con_punto.csv
			else {
				if (allSpecies.contains(botanicalNerAnnotation[i])) {
					contenitoreChecked.add(botanicalNerAnnotation[i]);

				}
			}
		}

		botanicalNerAnnotationChecked = new String[contenitoreChecked.size()];
		botanicalNerAnnotationChecked = contenitoreChecked.toArray(botanicalNerAnnotationChecked);

	}

	public static void GetStatistics(String orchestrator_file, String botanical_file) throws IOException {

		// Prendo le annotazioni presenti sia in orchestrator che in botanical
		// I TRUE POSITIVE sono il numero di elememti (considerati come numero di
		// parole) in comune tra i due insiemi
		ArrayList<String> contenitore1 = new ArrayList<String>();
		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotation[i].equals(orchestatorAnnotation[j])) {
					contenitore1.add(botanicalNerAnnotation[i]);
					String[] conteggio1 = botanicalNerAnnotation[i].split("\\s+");
					TP = TP + conteggio1.length;
					break;
				}

			}

		}

		annotazioniComuni = new String[contenitore1.size()];
		annotazioniComuni = contenitore1.toArray(annotazioniComuni);

		// Prendo le anotazioni presenti in orchestrator e non in botanical
		// I FALSE POSITIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di orchestrator

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
				String[] conteggio2 = orchestatorAnnotation[i].split("\\s+");
				FP = FP + conteggio2.length;
			}
		}

		unicheOrchestatorAnnotation = new String[contenitore2.size()];
		unicheOrchestatorAnnotation = contenitore2.toArray(unicheOrchestatorAnnotation);

		// Prendo le anotazioni presenti in botanical e non in orchestrator
		// I FALSE NEGATIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di botanical

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
				String[] conteggio3 = botanicalNerAnnotation[i].split("\\s+");
				FN = FN + conteggio3.length;
			}
		}

		unicheBotanicalNerAnnotation = new String[contenitore3.size()];
		unicheBotanicalNerAnnotation = contenitore3.toArray(unicheBotanicalNerAnnotation);

		// Adesso ho tutti i dati per generare le statistiche

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
		TN = paroleOrchestrator - TP - FP - FN;

		precision = (double) TP / (double) (TP + FP);
		recall = (double) TP / (double) (TP + FN);
		f1 = 2d * (precision * recall) / (precision + recall);
		accuracy = (double) (TP + TN) / (double) (TP + TN + FP + FN);

		System.out.println("Risultati SENZA il controllo su GBIF");

		System.out.println("\n");

		System.out.println("TP: " + TP);
		System.out.println("FP: " + FP);
		System.out.println("FN: " + FN);
		System.out.println("TN: " + TN);

		System.out.println("\n");

		System.out.println("Accuracy: " + accuracy);
		System.out.println("Precision: " + precision);
		System.out.println("Recall: " + recall);
		System.out.println("F1 Score: " + f1);

	}

	public static void GetStatisticsChecked(String orchestrator_file, String botanical_file) throws IOException {

		// Prendo le annotazioni presenti sia in orchestrator che in botanical
		// I TRUE POSITIVE sono il numero di elememti (considerati come numero di
		// parole) in comune tra i due insiemi
		ArrayList<String> contenitore1 = new ArrayList<String>();
		for (int i = 0; i < botanicalNerAnnotationChecked.length; i++) {
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotationChecked[i].equals(orchestatorAnnotation[j])) {
					contenitore1.add(botanicalNerAnnotationChecked[i]);
					String[] conteggio1 = botanicalNerAnnotationChecked[i].split("\\s+");
					TPChecked = TPChecked + conteggio1.length;
					break;
				}

			}

		}

		annotazioniComuniChecked = new String[contenitore1.size()];
		annotazioniComuniChecked = contenitore1.toArray(annotazioniComuniChecked);

		// Prendo le anotazioni presenti in orchestrator e non in botanical
		// I FALSE POSITIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di orchestrator

		ArrayList<String> contenitore2 = new ArrayList<String>();

		for (int i = 0; i < orchestatorAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < botanicalNerAnnotationChecked.length; j++) {
				if (orchestatorAnnotation[i].equals(botanicalNerAnnotationChecked[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				contenitore2.add(orchestatorAnnotation[i]);
				String[] conteggio2 = orchestatorAnnotation[i].split("\\s+");
				FPChecked = FPChecked + conteggio2.length;
			}
		}

		unicheOrchestatorAnnotationChecked = new String[contenitore2.size()];
		unicheOrchestatorAnnotationChecked = contenitore2.toArray(unicheOrchestatorAnnotationChecked);

		// Prendo le anotazioni presenti in botanical e non in orchestrator
		// I FALSE NEGATIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di botanical

		ArrayList<String> contenitore3 = new ArrayList<String>();
		for (int i = 0; i < botanicalNerAnnotationChecked.length; i++) {
			boolean match = false;
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotationChecked[i].equals(orchestatorAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				contenitore3.add(botanicalNerAnnotationChecked[i]);
				String[] conteggio3 = botanicalNerAnnotationChecked[i].split("\\s+");
				FNChecked = FNChecked + conteggio3.length;
			}
		}

		unicheBotanicalNerAnnotationChecked = new String[contenitore3.size()];
		unicheBotanicalNerAnnotationChecked = contenitore3.toArray(unicheBotanicalNerAnnotationChecked);

		// Adesso ho tutti i dati per generare le statistiche

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
		TNChecked = paroleOrchestrator - TPChecked - FPChecked - FNChecked;

		precisionChecked = (double) TPChecked / (double) (TPChecked + FPChecked);
		recallChecked = (double) TPChecked / (double) (TPChecked + FNChecked);
		f1Checked = 2d * (precisionChecked * recallChecked) / (precisionChecked + recallChecked);
		accuracyChecked = (double) (TPChecked + TNChecked) / (double) (TPChecked + TNChecked + FPChecked + FNChecked);

		System.out.println("\n");
		System.out.println("\n");

		System.out.println("Risultati CON il controllo su GBIF");

		System.out.println("\n");

		System.out.println("TP: " + TPChecked);
		System.out.println("FP: " + FPChecked);
		System.out.println("FN: " + FNChecked);
		System.out.println("TN: " + TNChecked);

		System.out.println("\n");

		System.out.println("Accuracy: " + accuracyChecked);
		System.out.println("Precision: " + precisionChecked);
		System.out.println("Recall: " + recallChecked);
		System.out.println("F1 Score: " + f1Checked);

	}

	public static void main(String[] args) throws IOException {

		GetOrchestatorAnnotation("output.txt");
		GetBotanicalNerAnnotation("Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatistics("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatisticsChecked("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv");
		System.out.println("\n\n DONE");

	}

}
