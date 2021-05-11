package it.cnr.asfa.textprocessing.tests;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;

public class PrepareGoldStandard {

	
	
	public static void main(String [] args) throws Exception{
		
		String file = new String(Files.readAllBytes(new File("gold_only_text.txt").toPath()),"UTF-8");
		
		file = file.replace(" ,",",");
		file = file.replace(" .",".");
		file = file.replace(" )",")");
		file = file.replace("( ","(");
		file = file.replace(" :",":");
		
		file = file.replaceAll(" + "," ");
		FileWriter fw = new FileWriter(new File("gold_only_text_rev.txt"));
		fw.write(file);fw.close();
		
	}
}
