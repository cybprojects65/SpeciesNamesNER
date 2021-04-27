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
import java.util.LinkedHashMap;
import java.util.List;

import it.cnr.asfa.textprocessing.utils.EfficientSearchInText;

public class ASFAResearchObjectThesaurus extends ASFAResearchObjectSpecies{

	
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
					wordsArrayList.add(token[i].toLowerCase()); // genus should be upper case
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
		File referenceTaxa = new File("thesaurus.csv");
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

		// A questo punto leggo il file originale in modo da stamparlo nuovamente con le
		// annotazioni
		String testooriginale = new String(Files.readAllBytes(new File(filename).toPath()));
		StringBuilder jsonIndex = new StringBuilder();
		// Rimuovo i line breaks
		testooriginale = testooriginale.replace("\n", " ").replace("\r", "");
		// Rimuovo le possibili parentesi quadre presenti nel testo di input
		testooriginale = testooriginale.replace("[", " ").replace("]", " ");
		for (String annot : allAnnotationsequences) {
			if (annot.contains(" ")) 
			{
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


}
