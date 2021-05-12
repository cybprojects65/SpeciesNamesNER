package it.cnr.asfa.textprocessing.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class CheckSpeciesValidity {

	static String googleSearch = "https://www.google.com/search?q=";

	public static void main(String[] args) throws Exception {

		//String species = "Latimeria chalumnae";
		//System.out.println("Valid: " + checkSpecies(species));
		List<String> missingSpecies = Files.readAllLines(new File("FalsePositives.txt").toPath());
		HashSet<String> missingSpeciesSet = new HashSet<>(missingSpecies);
		
		StringBuffer sb = new StringBuffer(); 
		for (String m:missingSpeciesSet) {
			if (m.length()==0)
				 continue;
			
			boolean v = false;
			
			if (m.contains(" "))
				v = true;
			else
				v = checkSpecies(m);
			
			String e = "KO";
			if (v)
				e = "OK";
			String result = m+","+e;
			sb.append(result+"\n");
			System.out.println(result);
		}
		
		FileWriter fw = new FileWriter(new File("FalsePositivesChecked.csv"));
		fw.write(sb.toString());
		fw.close();
	}

	public static boolean checkSpecies(String species) throws Exception {

		boolean valid = false;
		//String got = getHTML(googleSearch + URLEncoder.encode(species, "UTF-8"));
		//String got = googleSearch(URLEncoder.encode(species, "UTF-8"));
		String got = googleSearch(species);
		//System.out.println(got);
		got = got.toLowerCase();
		if (got.contains("species"))
			valid = true;
		
		return valid;

	}

	public static String googleSearch(String domain) throws Exception {
		String response = null;
		//String userAgent = "Mozilla/5.0 (Windows; U; Windows NT 6.0;en-EN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		String userAgent = "Mozilla/6.0 (Windows; U; Windows NT 6.0;en-EN; rv:1.9.2) Gecko/20100115 Firefox/3.6";
		Map<String, String> map = new HashMap<String, String>();
		map.put("User-Agent", userAgent);
		map.put("Host", "www.google.com");
		map.put("Cache-Control", "no-cache");
		//URL url = new URL("http://www.google.com/search?num=100&start=0&hl=en&meta=&q=%40%22" + domain + "%22");
		URL url = new URL("https://en.wikipedia.org/wiki/"+domain.replace(" ", "_"));
		response = response + GETRequest(url, map);

		return response;
	}

	public static String GETRequest(URL url, Map<String, String> map) {
		String readLine = null;
		String responseGET = null;
		HttpURLConnection con = null;
		try {
			con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(5000);
			if (map != null) {
				for (Map.Entry<String, String> entry : map.entrySet()) {
					//System.out.println(entry.getKey() + "/" + entry.getValue());
					con.setRequestProperty(entry.getKey(), entry.getValue());
				}
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuffer response = new StringBuffer();
				while ((readLine = in.readLine()) != null) {
					response.append(readLine);
					response.append('\r');
				}
				in.close();
				responseGET = response.toString();
			} else {
				System.out.println("responseCode = " + responseCode);
			}
		} catch (Exception e) {
			System.out.println("Error");
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
		return responseGET;
	}

	public static String getHTML(String urlToRead) throws Exception {
		StringBuilder result = new StringBuilder();
		URL url = new URL(urlToRead);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
			for (String line; (line = reader.readLine()) != null;) {
				result.append(line);
			}
		}
		return result.toString();
	}

}
