package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OrchestratorThesaurus {
//java -cp asfaspeciesner.jar it.cnr.asfa.textprocessing.Orchestrator sampleTextTaxa.txt

	@SuppressWarnings("null")
	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");

		/*
		String annotLc = "a pond compilation";
		 Pattern p = Pattern.compile("(( |^)on( |$))");
		 Matcher m = p.matcher(annotLc);
		 boolean b = m.find();
		 System.out.println(b);
		 System.exit(0);
		 */
		/*
		String test = " through more [[[chemical] analysis] methods] efficient use of resources. ";
		System.out.println(ASFAResearchObjectSpecies.cleanupNested(test));
		System.exit(0);
		*/
		
		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; // NOTE: INPUT MUST BE UTF-8

		if (args.length < 1) {
			System.out.println("Using sample input");
			args = new String[1];
			args[0] = "sampleTextThesaurus.txt";	
		}

		ASFAResearchObjectThesaurus researchObjectThesaurus = new ASFAResearchObjectThesaurus();

		//Operazioni per Thesaurus
		researchObjectThesaurus.capture(args[0]);
		researchObjectThesaurus.get();
		researchObjectThesaurus.enrich("Asfa");
		researchObjectThesaurus.materializeText();
		researchObjectThesaurus.materializeJSON();
		System.out.println("Operazioni thesaurus completate");

		System.out.println("Done");

	}

}