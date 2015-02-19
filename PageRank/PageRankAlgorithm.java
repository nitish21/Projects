package assignment2package;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Comparator;

class PageDetails {
	String pageName="";
	int positionInFile = 0;
	boolean sink = true;
	double pageRank;
	List<String> M_inlinks = new ArrayList<String>();
	List<String> L_outlinks = new ArrayList<String>();
	double L_numberOfOutlinks = 0;
}

public class PageRankAlgorithm {

	/**
	 * @param args
	 */
	// static HashMap hm = new HashMap();

	static Map<String, PageDetails> hm = new HashMap<String, PageDetails>();
	static Map<String, Double> pageRanksHM = new HashMap<String, Double>();
	
	static final double d = 0.85;

	public static void readFileAndPrepareDetails(String filename) {
		FileReader fr = null;

		try {
			//"C:\\Users\\NITISH\\Desktop\\wt2g_inlinks.txt"
			fr = new FileReader(filename);// wt2g_inlinks.txt //sixNodeGraph.txt
						
			BufferedReader br = new BufferedReader(fr);
			String line;
			int lineCount = 0;
			while ((line = br.readLine()) != null) {
				lineCount++;
				// process each line
				String[] strArray = line.split(" ");
				
				//if first is not present in the hashmap..insert it..else continue
				String first = strArray[0];
				if (!hm.containsKey(first)){
					PageDetails pfirst = new PageDetails();
					pfirst.pageName = first;
					pfirst.positionInFile = lineCount;
					hm.put(first, pfirst);}
				else{
					PageDetails pfirst = (PageDetails) hm.get(first);
					pfirst.positionInFile = lineCount;
				}

				//for remaining string on the line..
				for (int i = 1; i < strArray.length; i++) {
					
					//check if it is a key in hashmap.. if not - then insert it
					if (!hm.containsKey(strArray[i])){
						PageDetails p1 = new PageDetails();
						p1.pageName = strArray[i];
						hm.put(strArray[i], p1);
					}
					
					boolean flag = false;
					// adding inlinks to first
					PageDetails p = (PageDetails) hm.get(first);
					List<String> m_links = p.M_inlinks;
					if (!m_links.contains(strArray[i]))
						m_links.add(strArray[i]);

					// adding first to the outlinks of the page..
					p = (PageDetails) hm.get(strArray[i]);
					List<String> l_links = p.L_outlinks;
					if (!l_links.contains(first)) {
						p.sink = false;
						l_links.add(first);
					}
				}

			}

			System.out.println(hm.size());

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
//				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static double getEntropy() {
		double entropy = 0.0;
		for (Object value : hm.values()) {
			double pr = ((PageDetails) value).pageRank;
			entropy = entropy + pr * (Math.log(pr) / (Math.log(2)));
		}
		return (-entropy);
	}

	public static void printPageRanks() {
		// TODO Auto-generated method stub
		for (Object key : hm.keySet()) {
			PageDetails page = ((PageDetails) hm.get(key));
			System.out.println("Page = " + key + ", PageRank = "
					+ page.pageRank);
		}

	}

	public static void calculatePageRank() {
		// TODO Auto-generated method stub
		double N = hm.size();
		int count = 0;

		// assign initial values of Page Ranks
		assignInitialPRValues();

		double sinkPR = 0;
		double perplexity = Math.pow(2, getEntropy());
		int perplexCount = 0;
		
//		sortByPositionInFile();

		do {

			count++;
			sinkPR = 0;
			sinkPR = calculateTotalSinkPR();
			double newPR = 0;
			/////////////////////////////////////////////////////////////////////////
			
			List<PageDetails> pages = getPages();

			
			for (PageDetails page : pages) {
				newPR = (1 - d) / N; /* teleportation */
				newPR = newPR + d * sinkPR / N; /*spread remaining sink PR evenly*/

				for (String s : page.M_inlinks) { /* pages pointing to page */
					PageDetails q = (PageDetails) hm.get(s);
					newPR = newPR + d * q.pageRank / q.L_outlinks.size();
				}

				pageRanksHM.put(page.pageName,newPR);
			}

			for (Object key : hm.keySet()) {
				PageDetails page = ((PageDetails) hm.get(key));
				page.pageRank = pageRanksHM.get(key);
			}
			
			double changeInPerplexity = perplexity - Math.pow(2, getEntropy());

			System.out.println("Perplexity is : " + perplexity);
			perplexity = Math.pow(2, getEntropy());
			
			if(Math.abs(changeInPerplexity) <= 1)
				perplexCount++;
			else
				perplexCount = 0;
			
//			 if (count == 1) {
//			 System.out.println("PRs for 1st iteration");
//			 printPageRanks();
//			 }
//			 if (count == 10) {
//			 System.out.println("PRs for 10th iteration");
//			 printPageRanks();
//			 }
//			 if (count == 100) {
//			 System.out.println("PRs for 100th iteration");
//			 printPageRanks();
//			 break;
//			 }
		}while(perplexCount <= 4);//perplexCount <= 4
		
		System.out.println("total number of iterations = " + count);

	}

	public static List<PageDetails> getPages() {
		// TODO Auto-generated method stub
		//sorting as per position in file...
		List<PageDetails> pages = new ArrayList<PageDetails>(hm.values());
		
		Collections.sort(pages, new Comparator<PageDetails>() {

	        public int compare(PageDetails p1, PageDetails p2) {
	            return (p1.positionInFile - p2.positionInFile);
	        }
	    });
		
		return pages;
	}

	public static void assignInitialPRValues() {
		// TODO Auto-generated method stub
		for (Object value : hm.values()) {
			PageDetails page = ((PageDetails) value);
			page.pageRank = 1.0 / hm.size();
		}

	}

	public static double calculateTotalSinkPR() {
		// TODO Auto-generated method stub
		double totalSinkPR = 0;
		for (Object value : hm.values()) {
			PageDetails page = ((PageDetails) value);
			if (page.sink) {
				double pr = page.pageRank;
				totalSinkPR = totalSinkPR + pr;
			}
		}
		return totalSinkPR;
	}

	public static double getTotalSinkNodes() {
		// TODO Auto-generated method stub
		int sinkCount = 0;
		for (Object value : hm.values()) {
			PageDetails page = ((PageDetails) value);
			if (page.sink) {
				sinkCount++;
			}
		}
		return sinkCount;
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		"C:\\Users\\NITISH\\Desktop\\wt2g_inlinks.txt"
		
		readFileAndPrepareDetails(args[0]);
		
		calculatePageRank();
		System.out.println("Number of pages : " + hm.size());
		System.out.println("*****************************************");
		System.out.println("*****************************************");
		System.out.println("Total Sink Nodes = " + getTotalSinkNodes());
		sortByInlinksAndDisplay();
		sortByPRandDisplay();
		
	}

	public static void sortByInlinksAndDisplay() {
		// TODO Auto-generated method stub
		
		List<PageDetails> pages = new ArrayList<PageDetails>(hm.values());
		
		Collections.sort(pages, new Comparator<PageDetails>() {

	        public int compare(PageDetails p1, PageDetails p2) {
	            return (- (p1.M_inlinks.size() - p2.M_inlinks.size()));
	        }
	    });
		
		int count = 1;
		
		System.out.println("Sorted as per number of in links.....");
		
		for (PageDetails p : pages) {
	        if (count < 51) {
				System.out.println(p.pageName + "\t" + "number of inlinks : " + 
	        p.M_inlinks.size());
				count++;
			}
	        else break;
	    }
	}
	
	public static void sortByPRandDisplay() {
		// TODO Auto-generated method stub
		
		List<PageDetails> pages = new ArrayList<PageDetails>(hm.values());
		
		Collections.sort(pages, new Comparator<PageDetails>() {

	        public int compare(PageDetails p1, PageDetails p2) {
	            return (- Double.compare(p1.pageRank, p2.pageRank));
	        }
	    });
		
		int count = 1;
		
		System.out.println("Sorted as per Page Rank.....");
		
		for (PageDetails p : pages) {
			if(count < 51){
				System.out.println(p.pageName + "\t PageRank : " + p.pageRank
						+ "\t Number of Inlinks : " + p.M_inlinks.size()
						+ "\t sink node?  " + p.sink + " no of outlinks:  " + p.L_outlinks.size());
				count++;
			}
			else
				break;
	    }
	}

}
