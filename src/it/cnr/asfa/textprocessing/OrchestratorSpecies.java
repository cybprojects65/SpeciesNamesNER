package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;

public class OrchestratorSpecies {
//java -cp asfaspeciesner.jar it.cnr.asfa.textprocessing.Orchestrator sampleTextTaxa.txt

	@SuppressWarnings("null")
	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		/*
		long total = 0;
		for (int i = 0; i < 10000000; i++) {
			total += i;
		}
*/
		System.out.println("ASFA Text Miner has started...");

		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; // NOTE: INPUT MUST BE UTF-8

		if (args.length < 1) {
			System.out.println("Using sample input");
			args = new String[1];
			//args[0] = "testText.txt";
			args[0] = "gold_only_text_rev.txt";
			//args[0] = "gold_test.txt";
		}

		ASFAResearchObjectSpecies researchObjectSpecies = new ASFAResearchObjectSpecies();

//		Operazioni per Species
		researchObjectSpecies.capture(args[0]);
		researchObjectSpecies.get();
		researchObjectSpecies.enrich("Taxon");
		researchObjectSpecies.materializeText();
		researchObjectSpecies.materializeJSON();
		System.out.println("Species operations completed");

		System.out.println("Done");

		long stopTime = System.currentTimeMillis();
		long elapsedTime = stopTime - startTime;

		System.out.println("Elapsed time");
		System.out.println(elapsedTime);
	}

}