package it.cnr.asfa.textprocessing.tests;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RetrieveDetected {

public static void main(String [] args) throws Exception{
		
		String testooriginale = new String(Files.readAllBytes(new File("output.txt").toPath()),"UTF-8");
		int i1 = testooriginale.indexOf("[");
		
		while (i1>=0) {
			int i2 = testooriginale.indexOf("]");
			System.out.println(testooriginale.substring(i1+1,i2));
			testooriginale = testooriginale.substring(i2+1);
			i1 = testooriginale.indexOf("[");
		}
		
	}

}
