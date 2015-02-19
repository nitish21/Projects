import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Bm25 {

	/**
	 * @param args
	 */

	static Map<String, HashMap<Integer, Integer>> wordDetails = new HashMap<String, HashMap<Integer, Integer>>();
	
	static HashMap<Integer,Integer> HmDocAndLength = new HashMap<Integer,Integer>();
	
	static int displayCount;
	
	static double avdl = 0.0;

	static ArrayList<String> listOfQueries = new ArrayList<String>();
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		readFileAndFillWordDetails("C:\\Users\\NITISH\\Desktop\\tccorpus\\index.out","C:\\Users\\NITISH\\Desktop\\tccorpus\\queries.txt",100);
		
		readFileAndFillWordDetails(args[0],args[1],Integer.parseInt(args[2]));
		
		calculateAvdl();
		
		beginBm25ForAllTheQueries();
		
	}

	public static void beginBm25ForAllTheQueries() {
		// TODO Auto-generated method stub
		
		PrintWriter writer = null;
		try {
//			writer = new PrintWriter("C:\\Users\\NITISH\\Desktop\\tccorpus\\results.eval", "UTF-8");
			
			writer = new PrintWriter("results.eval", "UTF-8");
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        String [] listOfWordsInQuery = null;
        
        
		//for each query...
		for(int i=0;i<listOfQueries.size();i++){
			//get all the words in the current query...
			listOfWordsInQuery = listOfQueries.get(i).split(" ");
			
			//get list of all the document IDs for a particular query
			ArrayList<Integer> listOfDocIdsInQuery = getAllDocIdsForQuery(listOfWordsInQuery);
			HashMap<Integer, Double> HmDocAndBmScore = new HashMap<Integer, Double>();
			
			
			for(int doc : listOfDocIdsInQuery){
				double bm25Score = 0.0;
				
				for(int j=0;j<listOfWordsInQuery.length;j++){
					double k1 = 1.2;
					double k2 = 100.0;
					double b = 0.75;
//					//////////////////////////////////////////////////////////////////////////////
					double N = (double)HmDocAndLength.size();
					double ni = (double)(wordDetails.get(listOfWordsInQuery[j]).size());//no of documents in which the term is present
					double logNum = 1;
					double logDen = (ni + 0.5)/(N - ni + 0.5);
					
					double t1 = Math.log(logNum/logDen);
					
					//////////////////////////////////////////////////////////////////////////////
					double K = k1 * ((1-b) + b * (double)HmDocAndLength.get(doc) / avdl);
					
					double fi = 0.0;
					
					if(((HashMap<Integer, Integer>) wordDetails.get(listOfWordsInQuery[j])).containsKey(doc))
						fi = ((HashMap<Integer, Integer>) wordDetails.get(listOfWordsInQuery[j])).get(doc);
					
					
					double t2 = (k1 + 1)*fi/(K+fi);
					
					//////////////////////////////////////////////////////////////////////////////
					double qfi = 1.0;
					double t3 = (k2 + 1)*qfi/(k2+qfi);
					
					bm25Score = bm25Score + t1*t2*t3;
										
				}
				
				HmDocAndBmScore.put(doc,bm25Score);
				
				
			}

			sortAndDisplayBmScores(HmDocAndBmScore,writer,i+1);
										
		}
		
		writer.close();
		
	}

	public static void sortAndDisplayBmScores(HashMap<Integer, Double> hm, PrintWriter writer, int queryNumber) {
		// TODO Auto-generated method stub

		List<Map.Entry<Integer, Double>> list =
	            new LinkedList<Map.Entry<Integer, Double>>( hm.entrySet() );
		
		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {

	        public int compare(Map.Entry<Integer, Double> p1, Map.Entry<Integer, Double> p2) {
	            return - (p1.getValue().compareTo(p2.getValue()));
	        }
	    });
		
		int count = 1;
		
		for (Map.Entry<Integer, Double> entry : list) {
	        if (count < 101) {
	        	writer.println(queryNumber + " Q0 " + entry.getKey() + " " + count + " " + entry.getValue() + " Nitish_Nadkarni");
				count++;
			}
	        else break;
	    }
		
	}

	public static void calculateAvdl() {
		// TODO Auto-generated method stub
		
		double sum = 0.0;
		for (Entry<Integer, Integer> docAndLength : HmDocAndLength.entrySet()) {
			sum = sum + (double)docAndLength.getValue();
		}
		
		avdl = sum/(double)HmDocAndLength.size();
		
	}

	public static ArrayList<Integer> getAllDocIdsForQuery(String [] listOfWordsInQuery){
		
		ArrayList<Integer> listOfDocIdsInQuery = new ArrayList<Integer>();
		for(int j=0;j<listOfWordsInQuery.length;j++){
			HashMap<Integer, Integer> innerHm = wordDetails.get(listOfWordsInQuery[j]);
			
			
			for (Entry<Integer, Integer> singleDocTermFreq : innerHm.entrySet()) {
				if(!listOfDocIdsInQuery.contains(singleDocTermFreq.getKey()))
					listOfDocIdsInQuery.add(singleDocTermFreq.getKey());
			}
		}
		
		return listOfDocIdsInQuery;
		
	}

	public static void readFileAndFillWordDetails(String indexFileName, String queriesFileName,int displayCount) {
		// TODO Auto-generated method stub
		
		Bm25.displayCount = displayCount;
		FileReader fr = null;
		FileReader fr1 = null;

		try {

			fr = new FileReader(queriesFileName);
			fr1 = new FileReader(indexFileName);

			BufferedReader br = new BufferedReader(fr);
			
			BufferedReader br_index = new BufferedReader(fr1);

			String queryLine = "";

			while ((queryLine = br.readLine()) != null) {
				listOfQueries.add(queryLine);
			}
			
			String indexLine = "";
			while (!(indexLine = br_index.readLine()).startsWith("*")){
				
				int i = indexLine.indexOf(' ');
				String word = indexLine.substring(0, i);
				
				if(wordPresentInAnyQuery(word) == true)
				{	
				    //Splitting the index line
				    String [] invertedLists = indexLine.split("\\(");
				    
				    //inverted lists will start from index 1, index 0 will contain the word and -> 
				    int count = 0;
				    
				    HashMap wordHm = new HashMap<Integer, Integer>();
				    for(int j=1;j<invertedLists.length;j++){
				    	
				    	String [] furtherSplits = invertedLists[j].split(",");
				    	
				    	String documentId = furtherSplits[0];
				    	String queryTermFrequencyInDocument = furtherSplits[1].substring(0, furtherSplits[1].length() - 2);

						// inserting or updating the term frequency...
						wordHm.put(Integer.parseInt(documentId), Integer.parseInt(queryTermFrequencyInDocument));

				    	count++;
				    }
				    
				    //enter the word details in data structure
				    wordDetails.put(word, wordHm);
 
				
				}
				
			}
			
			//read the document Ids and corresponding lengths
			while((indexLine = br_index.readLine()) != null){
				
				String [] lineSplits = indexLine.split(" ");				
				HmDocAndLength.put(Integer.parseInt(lineSplits[0]), Integer.parseInt(lineSplits[1]));
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
				fr1.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static boolean wordPresentInAnyQuery(String word) {
		// TODO Auto-generated method stub
		
		String [] listOfWordsInQuery = null;
		
		//for each query...
		for(int i=0;i<listOfQueries.size();i++){
			
			//get all the words in the current query...
			listOfWordsInQuery = listOfQueries.get(i).split(" ");
		
			for(int j=0;j<listOfWordsInQuery.length;j++){
				if(word.equalsIgnoreCase(listOfWordsInQuery[j]))
					return true;
			}
		}
		return false;
	}

}
