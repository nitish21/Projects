import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.Map.Entry;


public class Indexer {

	/**
	 * @param args
	 */

	static Map<String, HashMap<Integer, Integer>> wordDetails = new HashMap<String, HashMap<Integer, Integer>>();
	
	static HashMap<Integer,Integer> HmDocAndLength = new HashMap<Integer,Integer>();

	static List<Integer> listOfDocIds = new ArrayList<Integer>();

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		readFileAndFillWordDetails("C:\\Users\\NITISH\\Desktop\\tccorpus\\tccorpus.txt");
		readFileAndFillWordDetails(args[0]);
		printWordDetails();
	}

	public static void printWordDetails() {
		// TODO Auto-generated method stub
		
		PrintWriter writer = null;
		
		try {
			
			writer = new PrintWriter("index.out", "UTF-8");
			
			for (Entry<String, HashMap<Integer, Integer>> singleWordDetails : wordDetails.entrySet()) {
			     writer.print(singleWordDetails.getKey() + " ->");
				 for(Entry<Integer,Integer> details : singleWordDetails.getValue().entrySet())
					 writer.print(" (" + details.getKey() + "," + details.getValue() + ")");
				 writer.print(" ");
				 writer.println();

			}
			
			writer.println("*********************************");
			for(Entry<Integer,Integer> docAndLength : HmDocAndLength.entrySet()){
				writer.println(docAndLength.getKey() + " " + docAndLength.getValue());
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			writer.close();
		}
		
		
		
	}

	public static void readFileAndFillWordDetails(String fileName) {
		// TODO Auto-generated method stub
		FileReader fr = null;
		try {

			fr = new FileReader(fileName);

			BufferedReader br = new BufferedReader(fr);

			String line;

			
			int currentDocumentId = 0;
			int lengthOfcurrentDoc = 0;
			
			while ((line = br.readLine()) != null) {
				
				if (line.startsWith("#")) {
					
					if(currentDocumentId>0)
						HmDocAndLength.put(currentDocumentId, lengthOfcurrentDoc);
					
					currentDocumentId = Integer.parseInt(line.substring(2,line.length()));
					listOfDocIds.add(currentDocumentId);
					lengthOfcurrentDoc = 0;
				} else {
					String[] wordsOnLine = line.split(" ");

					lengthOfcurrentDoc = lengthOfcurrentDoc + wordsOnLine.length;
					
					// for each word on the line..
					for (int i = 0; i < wordsOnLine.length; i++) {
						// System.out.println(wordsOnLine[i]);
						if (!wordDetails.containsKey(wordsOnLine[i])) {
							// HashMap wordHm = wordDetails.get(wordsOnLine[i]);

							HashMap wordHm = new HashMap<Integer, Integer>();
							// inserting or updating the term frequency...
							wordHm.put(currentDocumentId, 1);

							wordDetails.put(wordsOnLine[i], wordHm);
						} else {
							HashMap wordHm = wordDetails.get(wordsOnLine[i]);
							// inserting or updating the term frequency...
							if (!wordHm.containsKey(currentDocumentId))
								wordHm.put(currentDocumentId, 1);//incrementing the current term frequency
							else {
								wordHm.put(currentDocumentId, (Integer)wordHm.get(currentDocumentId) + 1);//incrementing the current term frequency
							}
						}
					}
				}
			}
			
			HmDocAndLength.put(currentDocumentId, lengthOfcurrentDoc);
			

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fr.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

}
