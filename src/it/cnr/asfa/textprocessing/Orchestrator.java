package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;

public class Orchestrator {
//java -cp asfaspeciesner.jar it.cnr.asfa.textprocessing.Orchestrator sampleTextTaxa.txt
	
	@SuppressWarnings("null")
	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");

			
		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; //NOTE: INPUT MUST BE UTF-8

		if (args.length < 1) {
			System.out.println("Using sample input");
			args= new String[1];
			args[0] = "sampleTextTaxa.txt";
		}
		
		
		ASFAResearchObject researchObject = new ASFAResearchObject();
		

		researchObject.capture(args[0]);
		researchObject.get();
		researchObject.enrich("TAXON");
		researchObject.materializeText();
		researchObject.materializeJSON();
		
		System.out.println("Done");

		
		
	}

}