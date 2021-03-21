package it.cnr.asfa.textprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;

import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;
import org.json.*;

public class ASFAResearchObject {

	// Metodo che prende il testo dal file json (output di nlphub) e lo tokenizza
	// per salvare poi l'ouput in un array
	public String[] caputure(String filename) throws IOException {
		FileReader file = new FileReader(filename);
		BufferedReader reader = new BufferedReader(file);

		String key = "";
		String line = reader.readLine();

		while (line != null) {
			key += line;
			line = reader.readLine();
		}

		String jsonString = key;
		JSONObject obj = new JSONObject(jsonString);
		String jsonText = obj.getString("text");
		String[] tokens = jsonText.replaceAll("[^a-zA-Z ]", "").toLowerCase().split("\\s+");
		return tokens;

	}

	// Metodo arricchisce il testo identificando le tassonomie
	public boolean[] get(String[] toSearch) throws Exception {

		EfficientSearchInText est = new EfficientSearchInText();
		File referenceTaxa = new File("taxons.csv");
		int nthreads = 8;
//		long t0 = System.currentTimeMillis();
//		String toSearch [] = {
//				"mentha gentilis agardhiana",
//				"theclopsis mycon",
//				"korscheltellus ganna",
//				"afromarengo coriacea",
//				"calycopis gizela",
//				"strymon crambusa",
//				"colgar asperum",
//				"phaeostrymon alcestis oslari",
//				"colgar",
//				"hello"};

		boolean found[] = est.searchParallel(toSearch, referenceTaxa, nthreads);
		System.out.println("Found " + Arrays.toString(found));
		return(found);
//		long t1 = System.currentTimeMillis();
//		System.out.println("Elapsed Parallel " + (t1 - t0) + "ms");
//		t0 = System.currentTimeMillis();
//		est.searchBruteForce(toSearch, referenceTaxa);
//		t1 = System.currentTimeMillis();
//		System.out.println("Elapsed Brute " + (t1 - t0) + "ms");

	}

	public String[] enrich(boolean[] match, String filename) throws IOException {
		FileReader file = new FileReader(filename);
		BufferedReader reader = new BufferedReader(file);

		String key = "";
		String line = reader.readLine();

		while (line != null) {
			key += line;
			line = reader.readLine();
		}

		String jsonString = key;
		JSONObject obj = new JSONObject(jsonString);
		String jsonText = obj.getString("text");
		String[] TokenizedjsonText = jsonText.split("\\s+");
		for (int i = 0; i <= TokenizedjsonText.length; i++) {
			if (match[i] == true) {
				TokenizedjsonText[i] = "[" + TokenizedjsonText[i] + "]";
				  System.out.println(TokenizedjsonText[i]);
			}
		}
		return(TokenizedjsonText);
	}
	
	
	
	public void save(String[] enriched) {
		for (int i = 0; i < enriched.length; i++) {
			  System.out.println(enriched[i]);
			}


	}

}
