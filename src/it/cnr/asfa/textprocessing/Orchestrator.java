package it.cnr.asfa.textprocessing;

import java.io.File;
import java.util.List;

public class Orchestrator {

	@SuppressWarnings("null")
	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");

			
		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; //NOTE: INPUT MUST BE UTF-8

		if (args.length < 3) {
			System.out.println("Using sample input");
			args= new String[3];
			args[0] = "";
			args[1] = "en";
			args[2] = "sampleTextTaxa.txt";
		}
		
		
		ASFAResearchObject researchObject = new ASFAResearchObject();
		

		researchObject.capture(args[2]);
		researchObject.get();
		researchObject.enrich("TAXON");
		researchObject.materializeText();
		researchObject.materializeJSON();
		
		System.out.println("Done");

		
		
	}

}