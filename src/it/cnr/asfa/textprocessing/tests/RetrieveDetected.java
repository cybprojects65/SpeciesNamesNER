package it.cnr.asfa.textprocessing.tests;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class RetrieveDetected {

public static void main(String [] args) throws Exception{
	
		File f = new File("output.txt");
		//File f = new File("output_nostro_gs.txt");
	
		String testooriginale = new String(Files.readAllBytes(f.toPath()),"UTF-8");
		int i1 = testooriginale.indexOf("[");
		List<String> allAnnotationsList = new ArrayList<String>();
		
		while (i1>=0) {
			int i2 = testooriginale.indexOf("]");
			String sp = testooriginale.substring(i1+1,i2).trim();
			System.out.println(sp);
			testooriginale = testooriginale.substring(i2+1);
			i1 = testooriginale.indexOf("[");
			allAnnotationsList.add(sp);
			
		}
		
		String allAnnotations = allAnnotationsList.toString().replace(", ", ",").replace(",", "\n").replace("[","").replace("]", "");
		
		FileWriter fw = new FileWriter(new File(f.getName().replace(".txt", "_species.txt")));
		fw.write(allAnnotations);
		fw.close();
		
	}

}
