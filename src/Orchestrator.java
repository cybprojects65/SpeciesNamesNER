import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.json.JSONArray;

import utils.EfficientSearchInText;
import utils.FileTools;

public class Orchestrator {

	static String dataMinerURL = "dataminer-prototypes.d4science.org";
	static String token = "fea75a5a-d84c-495f-b0ca-09cdd95bacce-843339462";

	public static void main(String[] args) throws Exception {
		System.out.println("ASFA Text Miner has started...");
		String annotationsParsed[] = null;
		List<String> annotations = null;
		String language = null;
		File inputText = null; // NOTE: INPUT MUST BE UTF-8

		if (args.length < 3) {
			System.out.println("Using sample input");
			args = new String[3];
			args[0] = "Location#Person#Organization#Keyword";
			args[1] = "en";
			args[2] = "sampleTextBBC.txt";
		}

		annotationsParsed = args[0].split("#");
		annotations = Arrays.asList(annotationsParsed);
		language = args[1]; // it en de fr es
		inputText = new File(args[2]);

		NLPHubCaller caller = new NLPHubCaller(dataMinerURL, token);
		caller.run(language, inputText, annotations);

		System.out.println("JSON output is in: " + caller.getOutputJsonFile());
		System.out.println("Annotated text is in: " + caller.getOutputAnnotationFile());
		// example of output
		String allAnnotations = FileTools.loadString(caller.getOutputAnnotationFile().getAbsolutePath(), "UTF-8");
		String allAnnotationsNotExtracted = allAnnotations.replace("##", "");
		int nAnn = ((allAnnotations.length() - allAnnotationsNotExtracted.length()) / 4) - 1;
		// Fix small bug that occurs if there is only one output algorithm regarding
		// algorithm counting
		if (nAnn == 0) {
			nAnn = 1;
		}
		String alllogs = FileTools.loadString(caller.getLogFile().getAbsolutePath(), "UTF-8");

		System.out.println(allAnnotations);
		System.out.println("N of algorithms that extracted information " + nAnn);

		if (alllogs.contains("Unparsable"))
			throw new Exception("Something went wrong");

		// Second part of the execution dedicated to the annotation of taxonomies

		ASFAResearchObject ResearchObject = new ASFAResearchObject();
		String[] tokens = ResearchObject.caputure(caller.getOutputJsonFile().toString());
		// il metodo sottostante (enrich) è ancora in fase di sviluppo
		ResearchObject.enrich(tokens);
//		ResearchObject.save();

	}

}
