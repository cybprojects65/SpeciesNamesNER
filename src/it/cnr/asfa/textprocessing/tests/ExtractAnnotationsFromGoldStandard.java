package it.cnr.asfa.textprocessing.tests;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class ExtractAnnotationsFromGoldStandard {

	public static void main(String[] args) throws Exception {

		String file = new String(Files.readAllBytes(new File("./Tests/obiettivi 11 maggio/2/gold_due_colonne.csv").toPath()), "UTF-8");
		String[] lines = file.split("\n");
		List<String> list = new ArrayList<>();
		int i = 0;
		while (i<lines.length) {
			String l=lines[i];
			//System.out.println(">"+l);
			String s = l.substring(0,l.indexOf(",")).replace("×", "").trim();
			String a = l.substring(l.indexOf(","));
			if (a.length()>1) {
				if (a.contains("B-lat_genus")) 
					list.add(s);
				else if (a.contains("B-lat_species")) {
					i++;
					l=lines[i];
					String sp = l.substring(0,l.indexOf(",")).trim();
					if (sp.toLowerCase().equals(sp) && sp.length()>2)
						list.add(s+" "+sp);
				}
			}
			i++;
		}
	
		String allAnnotations = list.toString().replace(", ", ",").replace(",", "\n").replace("[","").replace("]", "");
		
		
		System.out.println(allAnnotations);
		FileWriter fw = new FileWriter(new File("gold_species.txt"));
		fw.write(allAnnotations);
		fw.close();
		
	}

}
