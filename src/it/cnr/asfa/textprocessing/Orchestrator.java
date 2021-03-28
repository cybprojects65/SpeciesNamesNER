package it.cnr.asfa.textprocessing;




public class Orchestrator {

	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");

		String inputFile = "sampleTextTaxa.txt";
		
		ASFAResearchObject researchObject = new ASFAResearchObject();
		

		researchObject.capture(inputFile);
		researchObject.get();
		researchObject.enrich("TAXON");
		researchObject.materializeText() ;
		researchObject.materializeJSON();
		
		System.out.println("Done");

		
		
	}

}