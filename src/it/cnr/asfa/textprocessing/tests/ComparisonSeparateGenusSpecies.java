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

public class ComparisonSeparateGenusSpecies {

	static String[] orchestatorAnnotation;
	static String[] botanicalNerAnnotation;
	static String[] botanicalNerAnnotationChecked;

	// Variabili per il conteggio delle statitiche con le occorreze NON controllate
	// in GBIF

	static String[] annotazioniComuniSpecies;
	static String[] annotazioniComuniGenus;

	static String[] unicheOrchestatorAnnotationGenus;
	static String[] unicheOrchestatorAnnotationSpecies;

	static String[] unicheBotanicalNerAnnotationGenus;
	static String[] unicheBotanicalNerAnnotationSpecies;

	static int TPGenus = 0;
	static int TPSpecies = 0;

	static int FPGenus = 0;
	static int FPSpecies = 0;

	static int FNGenus = 0;
	static int FNSpecies = 0;

	static double precisionGenus;
	static double precisionSpecies;

	static double recallGenus;
	static double recallSpecies;

	static double f1Genus;
	static double f1Species;

	// Variabili per il conteggio delle statitiche con le occorreze CONTROLLATE in
	// GBIF

	static String[] annotazioniComuniChecked;
	static String[] annotazioniComuniCheckedGenus;
	static String[] annotazioniComuniCheckedSpecies;

	static String[] unicheOrchestatorAnnotationChecked;
	static String[] unicheOrchestatorAnnotationCheckedGenus;
	static String[] unicheOrchestatorAnnotationCheckedSpecies;

	static String[] unicheBotanicalNerAnnotationChecked;
	static String[] unicheBotanicalNerAnnotationCheckedGenus;
	static String[] unicheBotanicalNerAnnotationCheckedSpecies;

	static int TPChecked = 0;
	static int TPCheckedGenus = 0;
	static int TPCheckedSpecies = 0;

	static int FPChecked = 0;
	static int FPCheckedGenus = 0;
	static int FPCheckedSpecies = 0;

	static int FNChecked = 0;
	static int FNCheckedGenus = 0;
	static int FNCheckedSpecies = 0;

	static double precisionChecked;
	static double precisionCheckedGenus;
	static double precisionCheckedSpecies;

	static double recallChecked;
	static double recallCheckedGenus;
	static double recallCheckedSpecies;

	static double f1Checked;
	static double f1CheckedGenus;
	static double f1CheckedSpecies;

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
		contenitore.removeAll(Collections.singleton("Clematis ?? jackmanii"));
		contenitore.removeAll(Collections.singleton("L. pyrenaicus subsp. pyrenaicus"));
		contenitore.removeAll(Collections.singleton("Micromeria ?? angosturae"));
		contenitore.removeAll(Collections.singleton("M. tenuis subsp. linkii"));
		contenitore.removeAll(Collections.singleton("M. varia subsp. canariensis"));
		contenitore.removeAll(Collections.singleton("Musa ?? paradisiaca"));
		contenitore.removeAll(Collections.singleton("Panicum italicum L."));
		contenitore.removeAll(Collections.singleton("Iris halophila var. sogdiana"));
		contenitore.removeAll(Collections.singleton("Bothriochloa ischaemum var. ischaemum"));
		contenitore.removeAll(Collections.singleton("Daucus carota subsp. sativus"));
		contenitore.removeAll(Collections.singleton("A. xalapensis var. texana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. halophila"));
		contenitore.removeAll(Collections.singleton("Iris spuria ssp. sogdiana"));
		contenitore.removeAll(Collections.singleton("Iris halophile subsp. sogdiana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. notha"));
		contenitore.removeAll(Collections.singleton("Taxus ?? media"));
		contenitore.removeAll(Collections.singleton("Dendromecon rigida subsp. harfordii"));
		contenitore.removeAll(Collections.singleton("M. spicata var. longifolia"));
		contenitore.removeAll(Collections.singleton("Fritillaria lusitanica subsp. oranensis"));
		contenitore.removeAll(Collections.singleton("Didymopanax morototoni var. angustipetalum"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. demetrii"));
		contenitore.removeAll(Collections.singleton("Matthiola longipetala subsp. bicornis"));
		contenitore.removeAll(Collections.singleton("O. maculata subsp. pterocarpa"));
		contenitore.removeAll(Collections.singleton("T. ?? cultorum"));
		contenitore.removeAll(Collections.singleton("A. fissuratus var. intermedius"));
		contenitore.removeAll(Collections.singleton("Zea mays L."));
		contenitore.removeAll(Collections.singleton("Cypella aquatilis Ravenna"));
		contenitore.removeAll(Collections.singleton("Erica abietina subsp. perfoliosa"));
		contenitore.removeAll(Collections.singleton("Quercus mongolica var. grosseserrata"));
		contenitore.removeAll(Collections.singleton("Rosa ?? centifolia"));
		contenitore.removeAll(Collections.singleton("Vachellia nilotica subsp. adstringens"));
		contenitore.removeAll(Collections.singleton("S. x herrenhusana"));
		contenitore.removeAll(Collections.singleton("Raphanus raphanistrum subsp. sativus"));
		contenitore.removeAll(Collections.singleton("Q ?? beaumontiana"));
		contenitore.removeAll(Collections.singleton("U. montana pendula nova"));
		contenitore.removeAll(Collections.singleton("D. gigantea var. geniculata"));
		contenitore.removeAll(Collections.singleton("Clematis virginiana L. var. missouriensis"));
		contenitore.removeAll(Collections.singleton("Taxus baccata var. wallichiana"));
		contenitore.removeAll(Collections.singleton("Iris spuria subsp. musulmanica"));
		contenitore.removeAll(Collections.singleton("Rubus fruticosus L."));

		// contenitore.add("varietiesBidens tripartita");
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

		// Creo un altro array che conterr?? solo le specie presenti in
		// "epithet_genus.csv" che sono nel gold

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
		ArrayList<String> contenitore1Genus = new ArrayList<String>();
		ArrayList<String> contenitore1Species = new ArrayList<String>();

		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotation[i].equals(orchestatorAnnotation[j])) {
					String[] conteggio = botanicalNerAnnotation[i].split("\\s+");
					if (conteggio.length == 1) {
						TPGenus = TPGenus + 1;
						contenitore1Genus.add(botanicalNerAnnotation[i]);

					} else {

						TPSpecies = TPSpecies + 1;
						contenitore1Species.add(botanicalNerAnnotation[i]);

					}
					break;
				}

			}

		}

		annotazioniComuniGenus = new String[contenitore1Genus.size()];
		annotazioniComuniGenus = contenitore1Genus.toArray(annotazioniComuniGenus);

		annotazioniComuniSpecies = new String[contenitore1Species.size()];
		annotazioniComuniSpecies = contenitore1Species.toArray(annotazioniComuniSpecies);

		// Prendo le anotazioni presenti in orchestrator e non in botanical
		// I FALSE POSITIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di orchestrator

		ArrayList<String> contenitore2Genus = new ArrayList<String>();
		ArrayList<String> contenitore2Species = new ArrayList<String>();

		for (int i = 0; i < orchestatorAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < botanicalNerAnnotation.length; j++) {
				if (orchestatorAnnotation[i].equals(botanicalNerAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				String[] conteggio = orchestatorAnnotation[i].split("\\s+");
				if (conteggio.length == 1) {
					FPGenus = FPGenus + 1;
					contenitore2Genus.add(orchestatorAnnotation[i]);

				} else {
					FPSpecies = FPSpecies + 1;
					contenitore2Species.add(orchestatorAnnotation[i]);

				}
			}
		}

		unicheOrchestatorAnnotationGenus = new String[contenitore2Genus.size()];
		unicheOrchestatorAnnotationGenus = contenitore2Genus.toArray(unicheOrchestatorAnnotationGenus);

		unicheOrchestatorAnnotationSpecies = new String[contenitore2Species.size()];
		unicheOrchestatorAnnotationSpecies = contenitore2Species.toArray(unicheOrchestatorAnnotationSpecies);

		// Prendo le anotazioni presenti in botanical e non in orchestrator
		// I FALSE NEGATIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di botanical

		ArrayList<String> contenitore3Genus = new ArrayList<String>();
		ArrayList<String> contenitore3Species = new ArrayList<String>();

		for (int i = 0; i < botanicalNerAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotation[i].equals(orchestatorAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				String[] conteggio = botanicalNerAnnotation[i].split("\\s+");
				if (conteggio.length == 1) {
					FNGenus = FNGenus + 1;
					contenitore3Genus.add(botanicalNerAnnotation[i]);

				} else {
					FNSpecies = FNSpecies + 1;
					contenitore3Species.add(botanicalNerAnnotation[i]);

				}

			}
		}

		unicheBotanicalNerAnnotationGenus = new String[contenitore3Genus.size()];
		unicheBotanicalNerAnnotationGenus = contenitore3Genus.toArray(unicheBotanicalNerAnnotationGenus);

		unicheBotanicalNerAnnotationSpecies = new String[contenitore3Species.size()];
		unicheBotanicalNerAnnotationSpecies = contenitore3Species.toArray(unicheBotanicalNerAnnotationSpecies);

		precisionGenus = (double) TPGenus / (double) (TPGenus + FPGenus);
		recallGenus = (double) TPGenus / (double) (TPGenus + FNGenus);
		f1Genus = 2d * (precisionGenus * recallGenus) / (precisionGenus + recallGenus);

		precisionSpecies = (double) TPSpecies / (double) (TPSpecies + FPSpecies);
		recallSpecies = (double) TPSpecies / (double) (TPSpecies + FNSpecies);
		f1Species = 2d * (precisionSpecies * recallSpecies) / (precisionSpecies + recallSpecies);

		System.out.println("Risultati SENZA	 il controllo su GBIF");
		System.out.println("\n");

		System.out.println("GENUS");
		System.out.println("\n");

		System.out.println("TP: " + TPGenus);
		System.out.println("FP: " + FPGenus);
		System.out.println("FN: " + FNGenus);

		System.out.println("\n");

		System.out.println("Precision: " + precisionGenus);
		System.out.println("Recall: " + recallGenus);
		System.out.println("F1 Score: " + f1Genus);
		System.out.println("\n");

		System.out.println("SPECIES");

		System.out.println("\n");

		System.out.println("TP: " + TPSpecies);
		System.out.println("FP: " + FPSpecies);
		System.out.println("FN: " + FNSpecies);

		System.out.println("\n");

		System.out.println("Precision: " + precisionSpecies);
		System.out.println("Recall: " + recallSpecies);
		System.out.println("F1 Score: " + f1Species);
		System.out.println("\n");

	}

	public static void GetStatisticsChecked(String orchestrator_file, String botanical_file) throws IOException {

		// Prendo le annotazioni presenti sia in orchestrator che in botanical
		// I TRUE POSITIVE sono il numero di elememti (considerati come numero di
		// parole) in comune tra i due insiemi
		ArrayList<String> contenitore1Genus = new ArrayList<String>();
		ArrayList<String> contenitore1Species = new ArrayList<String>();

		for (int i = 0; i < botanicalNerAnnotationChecked.length; i++) {
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotationChecked[i].equals(orchestatorAnnotation[j])) {
					String[] conteggio = botanicalNerAnnotationChecked[i].split("\\s+");
					if (conteggio.length == 1) {
						TPCheckedGenus = TPCheckedGenus + 1;
						contenitore1Genus.add(botanicalNerAnnotationChecked[i]);

					} else {
						TPCheckedSpecies = TPCheckedSpecies + 1;
						contenitore1Species.add(botanicalNerAnnotationChecked[i]);

					}
					break;
				}

			}

		}

		annotazioniComuniCheckedGenus = new String[contenitore1Genus.size()];
		annotazioniComuniCheckedGenus = contenitore1Genus.toArray(annotazioniComuniCheckedGenus);

		annotazioniComuniCheckedSpecies = new String[contenitore1Species.size()];
		annotazioniComuniCheckedSpecies = contenitore1Species.toArray(annotazioniComuniCheckedSpecies);

		// Prendo le anotazioni presenti in orchestrator e non in botanical
		// I FALSE POSITIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di orchestrator

		ArrayList<String> contenitore2Genus = new ArrayList<String>();
		ArrayList<String> contenitore2Species = new ArrayList<String>();

		for (int i = 0; i < orchestatorAnnotation.length; i++) {
			boolean match = false;
			for (int j = 0; j < botanicalNerAnnotationChecked.length; j++) {
				if (orchestatorAnnotation[i].equals(botanicalNerAnnotationChecked[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {

				String[] conteggio = orchestatorAnnotation[i].split("\\s+");
				if (conteggio.length == 1) {
					FPCheckedGenus = FPCheckedGenus + 1;
					contenitore2Genus.add(orchestatorAnnotation[i]);

				} else {
					FPCheckedSpecies = FPCheckedSpecies + 1;
					contenitore2Species.add(orchestatorAnnotation[i]);

				}
			}
		}

		unicheOrchestatorAnnotationCheckedGenus = new String[contenitore2Genus.size()];
		unicheOrchestatorAnnotationCheckedGenus = contenitore2Genus.toArray(unicheOrchestatorAnnotationCheckedGenus);

		unicheOrchestatorAnnotationCheckedSpecies = new String[contenitore2Species.size()];
		unicheOrchestatorAnnotationCheckedSpecies = contenitore2Species
				.toArray(unicheOrchestatorAnnotationCheckedSpecies);

		// Prendo le anotazioni presenti in botanical e non in orchestrator
		// I FALSE NEGATIVE sono il numero di match (considerati come numero di parole)
		// che sono unici di botanical

		ArrayList<String> contenitore3Genus = new ArrayList<String>();
		ArrayList<String> contenitore3Species = new ArrayList<String>();

		for (int i = 0; i < botanicalNerAnnotationChecked.length; i++) {
			boolean match = false;
			for (int j = 0; j < orchestatorAnnotation.length; j++) {
				if (botanicalNerAnnotationChecked[i].equals(orchestatorAnnotation[j])) {
					match = true;
					break;
				}

			}
			if (match == false) {
				String[] conteggio = botanicalNerAnnotationChecked[i].split("\\s+");
				if (conteggio.length == 1) {
					FNCheckedGenus = FNCheckedGenus + 1;
					contenitore3Genus.add(botanicalNerAnnotationChecked[i]);

				} else {
					FNCheckedSpecies = FNCheckedSpecies + 1;
					contenitore3Species.add(botanicalNerAnnotationChecked[i]);

				}

			}
		}

		unicheBotanicalNerAnnotationCheckedGenus = new String[contenitore3Genus.size()];
		unicheBotanicalNerAnnotationCheckedGenus = contenitore3Genus.toArray(unicheBotanicalNerAnnotationCheckedGenus);

		unicheBotanicalNerAnnotationCheckedSpecies = new String[contenitore3Species.size()];
		unicheBotanicalNerAnnotationCheckedSpecies = contenitore3Species
				.toArray(unicheBotanicalNerAnnotationCheckedSpecies);

		precisionCheckedGenus = (double) TPCheckedGenus / (double) (TPCheckedGenus + FPCheckedGenus);
		recallCheckedGenus = (double) TPCheckedGenus / (double) (TPCheckedGenus + FNCheckedGenus);
		f1CheckedGenus = 2d * (precisionCheckedGenus * recallCheckedGenus)
				/ (precisionCheckedGenus + recallCheckedGenus);

		precisionCheckedSpecies = (double) TPCheckedSpecies / (double) (TPCheckedSpecies + FPCheckedSpecies);
		recallCheckedSpecies = (double) TPCheckedSpecies / (double) (TPCheckedSpecies + FNCheckedSpecies);
		f1CheckedSpecies = 2d * (precisionCheckedSpecies * recallCheckedSpecies)
				/ (precisionCheckedSpecies + recallCheckedSpecies);

		System.out.println("------------------------------------------------------------------");
		System.out.println("\n");
		System.out.println("Risultati CON il controllo su GBIF");
		System.out.println("\n");

		System.out.println("GENUS");
		System.out.println("\n");

		System.out.println("TP: " + TPCheckedGenus);
		System.out.println("FP: " + FPCheckedGenus);
		System.out.println("FN: " + FNCheckedGenus);

		System.out.println("\n");

		System.out.println("Precision: " + precisionCheckedGenus);
		System.out.println("Recall: " + recallCheckedGenus);
		System.out.println("F1 Score: " + f1CheckedGenus);

		System.out.println("\n");
		System.out.println("SPECIES");
		System.out.println("\n");

		System.out.println("TP: " + TPCheckedSpecies);
		System.out.println("FP: " + FPCheckedSpecies);
		System.out.println("FN: " + FNCheckedSpecies);

		System.out.println("\n");

		System.out.println("Precision: " + precisionCheckedSpecies);
		System.out.println("Recall: " + recallCheckedSpecies);
		System.out.println("F1 Score: " + f1CheckedSpecies);

	}

	public static void main(String[] args) throws IOException {

		GetOrchestatorAnnotation("output.txt");
		GetBotanicalNerAnnotation("Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatistics("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatisticsChecked("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv");
		System.out.println("\n\n DONE");

	}

}
