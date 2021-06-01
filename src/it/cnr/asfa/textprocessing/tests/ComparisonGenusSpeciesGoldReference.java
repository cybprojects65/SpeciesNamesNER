package it.cnr.asfa.textprocessing.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.*;;

public class ComparisonGenusSpeciesGoldReference {

	static String[] referenceGenus;
	static String[] referenceSpecies;

	static String[] orchestatorAnnotation;

	static String[] botanicalNerAnnotation;
	static String[] botanicalNerAnnotationChecked;

	static String[] annotazioniComuniSpecies;
	static String[] annotazioniComuniGenus;

	static String[] unicheMeranerAnnotationGenus;
	static String[] unicheMeranerAnnotationSpecies;

	static String[] unicheBotanicalNerAnnotationGenus;
	static String[] unicheBotanicalNerAnnotationSpecies;

	static List<String> wikipedia = new ArrayList<String>();
	static List<String> allSpecies = new ArrayList<String>();
	static List<String> allGenus = new ArrayList<String>();

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

	// Variabili per il conteggio delle statitiche con le occorreze CONTROLLATE
	// in
	// GBIF

	static String[] annotazioniComuniChecked;
	static String[] annotazioniComuniCheckedGenus;
	static String[] annotazioniComuniCheckedSpecies;

	static String[] unicheMeranerAnnotationChecked;
	static String[] unicheMeranerAnnotationCheckedGenus;
	static String[] unicheMeranerAnnotationCheckedSpecies;

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

	public static void GetMeranerAnnotation(String filename) throws IOException {
		ArrayList<String> genus = new ArrayList<String>();
		ArrayList<String> species = new ArrayList<String>();
		String text = "";
		try {
			text = new String(Files.readAllBytes(Paths.get(filename)));
			String jsonString = text; // assign your JSON String here
			JSONObject obj = new JSONObject(jsonString);
			JSONArray arr = obj.getJSONArray("plant_names"); // notice that `"posts": [...]`
			for (int i = 0; i < arr.length(); i++) {
				String type = arr.getJSONObject(i).getString("taxon_rank");
				if (type.contains("Genus")) {
					String genusString = arr.getJSONObject(i).getString("entity_candidate").trim();
					genusString = genusString.replaceAll("[^a-zA-Z0-9]", " ");
					genus.add(genusString.trim());
				}
				if (type.contains("Species")) {
					String speciesString = arr.getJSONObject(i).getString("entity_candidate").trim();
					speciesString = speciesString.replaceAll("[^a-zA-Z0-9]", " ");
					species.add(speciesString.trim());
				}
//				String post_id = arr.getJSONObject(i).getString("entity_candidate");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} // JSONParser jsonParser = new JSONParser();

		referenceGenus = new String[genus.size()];
		referenceGenus = genus.toArray(referenceGenus);

		referenceSpecies = new String[species.size()];
		referenceSpecies = species.toArray(referenceSpecies);

	}

	
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

		ArrayList<String> genus = new ArrayList<String>();
		ArrayList<String> species = new ArrayList<String>();

		for (String spp : contenitore) {
			spp = spp.trim();
			if (spp.contains(" "))
				species.add(spp);
			else
				genus.add(spp);

		}

		referenceGenus = new String[genus.size()];
		referenceGenus = genus.toArray(referenceGenus);

		referenceSpecies = new String[species.size()];
		referenceSpecies = species.toArray(referenceSpecies);

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
					// Aggiungo la striga array di appoggio senza lo spazio
					// finale al mio array
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

		// Rimuovo dalla lista del gold le entry di: "Argentina",
		// "Diplocalyx","rupicola"

		contenitore.removeAll(Collections.singleton("Argentina"));
		contenitore.removeAll(Collections.singleton("Diplocalyx"));
		contenitore.removeAll(Collections.singleton("rupicola"));

		// aggiungere specie della lista Coro OK - Wikipedia
		try (BufferedReader br = new BufferedReader(new FileReader("Tests/File_per_la_valutazione/FalsePositivesChecked.csv"))) {
			String line;
			while ((line = br.readLine()) != null) {
				if (line.contains(",OK")) {
					wikipedia.add(line.replace(",OK", ""));
				}
			}
		}

		// Converto "contenitore" che e' in formato ArrayList<String> in un
		// array di
		// tipo String[] e lo salvo come variabile globale
		botanicalNerAnnotation = new String[contenitore.size()];
		botanicalNerAnnotation = contenitore.toArray(botanicalNerAnnotation);

		File species = new File("epithet_genus_con_punto.csv");
		allSpecies = Files.readAllLines(species.toPath());
		File genus = new File("epithet_genus.csv");
		allGenus = Files.readAllLines(genus.toPath());

	}

	public static void GetStatistics(String meraner_file, String botanical_file, boolean gbifcheck) throws IOException {

		int totalGenusBotanical = 0;
		int totalSpeciesBotanical = 0;

		for (int j = 0; j < botanicalNerAnnotation.length; j++) {

			if (botanicalNerAnnotation[j].contains(" "))
				totalSpeciesBotanical++;
			else
				totalGenusBotanical++;

		}

		// TP
		ArrayList<String> contenitore1Genus = new ArrayList<String>();
		ArrayList<String> contenitore1Species = new ArrayList<String>();
		ArrayList<String> contenitoreFP = new ArrayList<String>();

		for (int i = 0; i < referenceGenus.length; i++) {
			boolean matched = false;
			for (int j = 0; j < botanicalNerAnnotation.length; j++) {
				if (referenceGenus[i].equals(botanicalNerAnnotation[j])) {
					TPGenus = TPGenus + 1;
					contenitore1Genus.add(referenceGenus[i]);
					botanicalNerAnnotation[j] = "";
					matched = true;
					break;
				}
			}
			if (!matched) {
				contenitoreFP.add(referenceGenus[i]);
				if (wikipedia.contains(referenceGenus[i]))
					TPGenus++;
				else
					FPGenus++;
			}
		}

		for (int i = 0; i < referenceSpecies.length; i++) {
			boolean matched = false;
			for (int j = 0; j < botanicalNerAnnotation.length; j++) {
				if (referenceSpecies[i].equals(botanicalNerAnnotation[j])) {
					TPSpecies = TPSpecies + 1;
					contenitore1Species.add(referenceSpecies[i]);
					botanicalNerAnnotation[j] = "";
					matched = true;
					break;
				}
			}

			if (!matched) {
				contenitoreFP.add(referenceSpecies[i]);
				if (allSpecies.contains(referenceSpecies[i]) && gbifcheck)
					TPSpecies++;
				else if (!gbifcheck && wikipedia.contains(referenceSpecies[i]))
					TPSpecies++;
				else
					FPSpecies++;
			}
		}

		boolean writeFP = false;
		if (writeFP) {
			FileWriter fw = new FileWriter(new File("FalsePositives.txt"));
			for (String s : contenitoreFP) {
				fw.write(s + "\n");
			}
			fw.close();
		}

		for (int j = 0; j < botanicalNerAnnotation.length; j++) {
			if (botanicalNerAnnotation[j].length() > 0) {
				if (botanicalNerAnnotation[j].contains(" ")) {
					if (gbifcheck && allSpecies.contains(botanicalNerAnnotation[j]))
						FNSpecies++;
					else if (!gbifcheck)
						FNSpecies++;
				} else {
					if (gbifcheck && allGenus.contains(botanicalNerAnnotation[j]))
						FNGenus++;
					else if (!gbifcheck)
						FNGenus++;
				}
			}
		}

		precisionGenus = (double) TPGenus / (double) (TPGenus + FPGenus);
		recallGenus = (double) TPGenus / (double) (TPGenus + FNGenus);
		f1Genus = 2d * (precisionGenus * recallGenus) / (precisionGenus + recallGenus);

		precisionSpecies = (double) TPSpecies / (double) (TPSpecies + FPSpecies);
		recallSpecies = (double) TPSpecies / (double) (TPSpecies + FNSpecies);
		f1Species = 2d * (precisionSpecies * recallSpecies) / (precisionSpecies + recallSpecies);

		if (!gbifcheck)
			System.out.println("Risultati SENZA	 il controllo su GBIF");
		else
			System.out.println("Risultati CON il controllo su GBIF");
		System.out.println("\n");

		System.out.println("GENUS");
		System.out.println("Total Genus found: " + referenceGenus.length);
		System.out.println("Total Genus botanical: " + totalGenusBotanical);
		System.out.println("TP: " + TPGenus);
		System.out.println("FP: " + FPGenus);
		System.out.println("FN: " + FNGenus);

		System.out.println("\n");

		System.out.println("Precision: " + precisionGenus);
		System.out.println("Recall: " + recallGenus);
		System.out.println("F1 Score: " + f1Genus);
		System.out.println("\n");

		System.out.println("SPECIES");
		System.out.println("Total Species: " + referenceSpecies.length);
		System.out.println("Total Species botanical: " + totalSpeciesBotanical);
		System.out.println("TP: " + TPSpecies);
		System.out.println("FP: " + FPSpecies);
		System.out.println("FN: " + FNSpecies);

		System.out.println("\n");

		System.out.println("Precision: " + precisionSpecies);
		System.out.println("Recall: " + recallSpecies);
		System.out.println("F1 Score: " + f1Species);
		System.out.println("\n");

	}

	public static void main(String[] args) throws IOException {

		GetOrchestatorAnnotation("output.txt");
		//GetMeranerAnnotation("Tests/output_meraner_su_gold.json");
		GetBotanicalNerAnnotation("Tests/File_per_la_valutazione/gold_due_colonne.csv");
		GetStatistics("output.txt", "Tests/File_per_la_valutazione/gold_due_colonne.csv", false);

		System.out.println("\n\n DONE");

	}

}
