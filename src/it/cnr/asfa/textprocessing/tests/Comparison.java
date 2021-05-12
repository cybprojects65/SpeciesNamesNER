package it.cnr.asfa.textprocessing.tests;

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Comparison {

	public static void main(String[] args) throws Exception {
		RetrieveDetected.main(null);
		File gold = new File("gold_species.txt");
		File reference = new File("output_species.txt");

		File genus = new File("epithet_genus.csv");
		File spp = new File("epithet_genus_con_punto.csv");

		List<String> goldSpecies = Files.readAllLines(gold.toPath());
		List<String> referenceSpecies = Files.readAllLines(reference.toPath());

		//Comparison c = new Comparison();
		//c.minDistance(goldSpecies, referenceSpecies, referenceSpecies);
		//System.out.println("Error rate " + c.errorRate);

		List<String> allSpecies = Files.readAllLines(spp.toPath());
		int caughtSpecies = 0;
		int caughtGenus = 0;
		int missedGBIFSpecies = 0;
		int missedGBIFGenus = 0;
		int notpresentInGBIF = 0;
		int notpresentInBOTANICAL = 0;
		List<String> allGenus = Files.readAllLines(genus.toPath());
		int totalbotanical = goldSpecies.size();
		int totalGBIF = referenceSpecies.size();
		
		System.out.println("*****Searching for discrepancies");
		//for (String m : c.missed) {
		for (String m : goldSpecies) {
			boolean isGBIFSpecies = allSpecies.contains(m);
			boolean wasCaught = referenceSpecies.contains(m);
			if (isGBIFSpecies) {
				if (wasCaught)
					caughtSpecies++;
				else {
					missedGBIFSpecies++;
					System.out.println("MISSED SPECIES>>" + m);
				}
			}else {
				boolean isGBIFGenus = allGenus.contains(m);
				if (isGBIFGenus){
					if (wasCaught || m.equals("Argentina") || m.equals("Diplocalyx") || m.equals("rupicola"))
						caughtGenus++;
					else {
						missedGBIFGenus++;
						System.out.println("MISSED GENUS>>" + m);
					}
				
				}else {
					notpresentInGBIF++;
					//System.out.println("NOT PRESENT>>" + m);
				}
			}
		}
		StringBuffer missingSpecies = new StringBuffer();
		
		
		for (String m : referenceSpecies) {
			boolean wasCaught = goldSpecies.contains(m);
			
			if (!wasCaught) {
				notpresentInBOTANICAL++;
				System.out.println("Not present in botanical: "+m);
				missingSpecies.append(m+"\n");
			}
			
		}
		
		double accGBIF = ( (double)(caughtGenus+caughtSpecies) /(double)(caughtGenus+caughtSpecies+missedGBIFGenus+missedGBIFSpecies));
		double accBotanical = ( (double)(caughtGenus+caughtSpecies) /(double)(totalbotanical));
		double coverage = ( (double)(notpresentInBOTANICAL) /(double)(totalGBIF));
		int FP = 55;
		int correctCompl = notpresentInBOTANICAL-FP;
		
		int TP = caughtGenus+caughtSpecies;
		int FN = missedGBIFGenus+missedGBIFSpecies;
		
		int TN = correctCompl;
		
		double complementarityBOTvsGBIF = (double)notpresentInGBIF/(double)totalbotanical;
		double complementarityGBIFvsBOT = (double)correctCompl/(double)totalGBIF;
		
		//double accuracyReal = ( (double)(caughtGenus+caughtSpecies+correctCompl) /(double)(totalGBIF));
		
		double precision = (double)TP/(double)(TP+FP);
		double recall = (double)TP/(double)(TP+FN);
		double f1 = 2d*(precision*recall)/(precision+recall);
		
		double accuracyReal = (double) (TP+TN)/(double)(TP+TN+FP+FN);
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("caughtSpecies," +caughtSpecies +"\n");
		sb.append("caughtGenus," +caughtGenus +"\n");
		sb.append("missedGBIFSpecies," +missedGBIFSpecies +"\n");
		sb.append("missedGBIFGenus," +missedGBIFGenus +"\n");
		sb.append("notpresentInGBIF," +notpresentInGBIF+"\n");
		sb.append("notpresentInBOTANICAL," +notpresentInBOTANICAL+"\n");
		sb.append("totalbotanical," +totalbotanical+"\n");
		sb.append("totalGBIF," +totalGBIF+"\n");
		sb.append("correct caught," +correctCompl+"\n");
		
		sb.append("Accuracy GBIF," +accGBIF+"\n");
		sb.append("Accuracy Botanical," +accBotanical+"\n");
		sb.append("Accuracy," +accuracyReal+"\n");
		sb.append("Precision," +precision+"\n");
		sb.append("Recall," +recall+"\n");
		sb.append("F1," +f1+"\n");
		
		sb.append("ComplementarityBOTvsGBIF," +complementarityBOTvsGBIF+"\n");
		sb.append("ComplementarityGBIFvsBOT," +complementarityGBIFvsBOT+"\n");
		
		System.out.println("***Stats");
		System.out.println(sb);
		
		FileWriter fw = new FileWriter(new File("FalsePositives.txt"));
		fw.write(missingSpecies.toString());
		fw.close();
	}

	int allInsert = 0;
	int allOK = 0;
	int allREPLACE = 0;
	int allDelete = 0;
	double errorRate = 0;
	List<String> missed = new ArrayList<String>();

	public int minDistance(List<String> word1, List<String> word2, List<String> reference) {
		int len1 = word1.size();
		int len2 = word2.size();

		// len1+1, len2+1, because finally return dp[len1][len2]
		int[][] dp = new int[len1 + 1][len2 + 1];

		for (int i = 0; i <= len1; i++) {
			dp[i][0] = i;
		}

		for (int j = 0; j <= len2; j++) {
			dp[0][j] = j;
		}

		// iterate though, and check last char
		for (int i = 0; i < len1; i++) {
			String c1 = word1.get(i);
			for (int j = 0; j < len2; j++) {
				String c2 = word2.get(j);

				// if last two chars equal
				if (c1.equals(c2)) {
					// update dp value for +1 length
					dp[i + 1][j + 1] = dp[i][j];
				} else {
					int replace = dp[i][j] + 1;
					int insert = dp[i][j + 1] + 1;
					int delete = dp[i + 1][j] + 1;

					int min = replace > insert ? insert : replace;
					min = delete > min ? min : delete;
					dp[i + 1][j + 1] = min;
				}
			}
		}

		double MED = dp[len1][len2];

		errorRate = MED / len1;
		int i = len1;
		int j = len2;
		while (i > 0) {
			int diag = dp[i - 1][j - 1];
			int insert = dp[i][j - 1];
			int delete = dp[i - 1][j];

			if (insert < diag && insert < delete) {
				allInsert++;
				// System.out.println(word1.get(i-1)+" I->I "+word2.get(j-1));
				// i=i;
				j = j - 1;
			} else if (delete < insert && delete < diag) {
				allDelete++;
				// System.out.println(word1.get(i-1)+" D->D "+word2.get(j-1));
				i = i - 1;
				// j=j;
			} else {
				String w1 = word1.get(i - 1);
				String w2 = word2.get(j - 1);

				if (w1.equals(w2)) {
					allOK++;
					// System.out.println(w1+" -> "+w2+" OK");
				} else {
					//if (reference.contains(w1))
						//allOK++;
					//else 
					{
						allREPLACE++;
						System.out.println(w1 + " -> " + w2 + " REPLACE");
						missed.add(w1);
					}
				}

				i = i - 1;
				j = j - 1;
			}
		}

		// errorRate = (double)allOK/(double)len1;
		errorRate = (double) allOK / (double) (allOK + allREPLACE);
		return dp[len1][len2];
	}

}
