package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;

public class OrchestratorSpecies {
//java -cp asfaspeciesner.jar it.cnr.asfa.textprocessing.Orchestrator sampleTextTaxa.txt

	@SuppressWarnings("null")
	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");

		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; // NOTE: INPUT MUST BE UTF-8

		if (args.length < 1) {
			System.out.println("Using sample input");
			args = new String[1];
			args[0] = "sampleTextTaxa.txt";
		}

		ASFAResearchObjectSpecies researchObjectSpecies = new ASFAResearchObjectSpecies();

//		Operazioni per Species
		researchObjectSpecies.capture(args[0]);
		researchObjectSpecies.get();
		researchObjectSpecies.enrich("Taxon");
		researchObjectSpecies.materializeText();
		researchObjectSpecies.materializeJSON();
		System.out.println("Operazioni species completate");


		System.out.println("Done");

	}

}