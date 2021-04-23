package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;

public class OrchestratorThesaurus {
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
			args[0] = "textThesaurus.txt";
		}

		ASFAResearchObjectSpecies researchObjectSpecies = new ASFAResearchObjectSpecies();
		ASFAResearchObjectThesaurus researchObjectThesaurus = new ASFAResearchObjectThesaurus();

//		Operazioni per Species
		researchObjectSpecies.capture(args[0]);
		researchObjectSpecies.get();
		researchObjectSpecies.enrich("Taxon");
		researchObjectSpecies.materializeText();
		researchObjectSpecies.materializeJSON();
		System.out.println("Operazioni species completate");

//		Operazioni per Thesaurus
		researchObjectThesaurus.capture(args[0]);
		researchObjectThesaurus.get();
		researchObjectThesaurus.enrich("THESAURUS");
		researchObjectThesaurus.materializeText();
		researchObjectThesaurus.materializeJSON();
		System.out.println("Operazioni thesaurus completate");

		System.out.println("Done");

	}

}