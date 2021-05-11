package it.cnr.asfa.textprocessing.tests;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	
	
	public static void main(String [] args) {
		
		//String testooriginale = "a Ca a";
		//String testooriginale = "a Ca. sa";
		//String testooriginale = " Ca. a";
		String testooriginale = "AsCa. asasa";
		String annot = "Ca";
		
		testooriginale = "(formerly Mahonia section Horridae)";
		annot = "Mahonia";
		
		//System.out.println(">"+testooriginale.substring(0,1).toLowerCase()+testooriginale.substring(1));
		//String regex = "[\\p{Punct}|^| ]"+annot+"\b";
		//String regex = "(\\W|^)"+annot+"(\\W|$)";
		//String regex = "(\\W)"+annot+"(\\W)";
		
		String regexuni = "^"+annot+"$";
		String regexlast = " "+annot+"($|\\W)";
		String regexfirst = "^"+annot+" ";
		String regexother = "[ ]"+annot+"[ ]";
		//testooriginale = testooriginale.replaceAll(regexuni, "[" + annot + "]");
		//testooriginale = testooriginale.replaceAll(regexlast, " [" + annot + "]");
		//testooriginale = testooriginale.replaceAll(regexfirst, "[" + annot + "] ");
		//testooriginale = testooriginale.replaceAll(regexother, " [" + annot + "] ");
		
		
		System.out.println(testooriginale);
		
		String regex = "( |^)"+annot+"(\\W|$)";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(testooriginale);
		int le = testooriginale.length();
	    while (m.find()) {
	      int s = m.start();
	      int e = m.end();
	      if (s==0 && testooriginale.charAt(s)!=' ')
	       	s=-1;
	      if (e==le && !Pattern.matches("\\p{Punct}", ""+testooriginale.charAt(e-1)))
	      	e=le+1;
	      	testooriginale = testooriginale.substring(0,s+1)+"["+testooriginale.substring(s+1,e-1)+"]"+testooriginale.substring(e-1);
	       }
	        
	    System.out.println(testooriginale);
	}
}
