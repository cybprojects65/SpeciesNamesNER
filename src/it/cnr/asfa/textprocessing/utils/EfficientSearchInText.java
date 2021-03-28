package it.cnr.asfa.textprocessing.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EfficientSearchInText {

	
	public static void main(String[] args) throws Exception {
		EfficientSearchInText est = new EfficientSearchInText();
		File ref = new File("taxons.csv");
		long t0 = System.currentTimeMillis();

		BufferedReader br = new BufferedReader(new FileReader(ref));
			String line = br.readLine();
			int n = 6000;
			ArrayList<String> lines = new ArrayList<>();
			for (int i=0;i<n;i++) {
				lines.add(line);
				line = br.readLine();
			}
			String toSearch [] = new String[lines.size()];
			toSearch = lines.toArray(toSearch);
			//System.out.println(Arrays.toString(toSearch));
		br.close();
		int nthreads = 8;
		
		est.searchParallel(toSearch, ref,nthreads);
		long t1 = System.currentTimeMillis();
		System.out.println("Elapsed P "+(t1-t0)+"ms");
		
		t0 = System.currentTimeMillis();
		est.searchBruteForce(toSearch, ref);
		t1 = System.currentTimeMillis();
		System.out.println("Elapsed Brute "+(t1-t0)+"ms");
		
	}
	
	public boolean[] searchBruteForce(String[] toSearch, File referenceFile) throws Exception{
		return searchParallel(toSearch, referenceFile, 1);
	}
	
	public boolean[] searchParallel(String[] toSearch, File referenceFile, int nThreads) throws Exception{

		
		int maxLines = 6244995;
		
		ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
		int chunkLength = maxLines/nThreads;
		comparisons = 0;
		finished = new boolean[nThreads];
		found = new boolean[toSearch.length];
		for (int j = 0; j < nThreads; j++) {
			
			int nElements = chunkLength;
			int start = j*chunkLength;
			if (j==nThreads-1) {
				nElements +=maxLines%nThreads;
			}
			System.out.println("S:"+start+" E:"+(start+nElements-1)+" MaxL:"+(maxLines-1));
			ThreadSearch ts = new ThreadSearch(toSearch, referenceFile, start, nElements,j);
			executorService.submit(ts);
		}
		
		while(!hasFinished()) {
			Thread.sleep(10);
		}
		executorService.shutdown();
		//System.out.println("Comparisons "+comparisons);
		//System.out.println("Found "+Arrays.toString(found));
		return found;
	}

	boolean[] found;
	boolean[] finished;
	
	Integer comparisons;
	public synchronized boolean hasFinished() {
		
		for(boolean f: finished) {
			if (!f)
				return false;
		}
		return true;
	}
	
	public synchronized void addComp() {
		comparisons=comparisons+1;
	}
	
	public synchronized boolean found(int i) {
		return found[i];
	}
	
	public synchronized void setFound(int i) {
		found[i]=true;
	}
	
	public synchronized void setFinished(int i) {
		finished[i]=true;
	}
	
	public class ThreadSearch implements Callable<Integer> {

		String[] toSearch;
		File inputFile;
		int start;
		int maxLines;
		int threadN;
		public ThreadSearch(String[] toSearch, File inputFile, int start, int maxLines, int threadN) {
			this.toSearch = toSearch;
			this.inputFile = inputFile;
			this.start = start;
			this.maxLines = maxLines;
			this.threadN=threadN;
		}

		@Override
		public Integer call() {

			try { 
			
			FileInputStream fs= new FileInputStream(inputFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fs));
			for(int i = 0; i < start; ++i)
			  br.readLine();
			
			String line = br.readLine();
			
			for (int i = start; i < (start + maxLines); i++) {
					//addComp();
					//System.out.println("Searching line "+line);
					int j = 0;
					for (String s : toSearch) {
						//if (!found(j)) {
						if (!found[j] && line.equals(s)) {
								//setFound(j);
								found[j]=true;
						}
						j++;
					}
					line = br.readLine();
				}
				
				br.close();
			
		}catch(Exception e) {
			e.printStackTrace();
		}
			setFinished(threadN);
			return 1;
		}

	}
}
