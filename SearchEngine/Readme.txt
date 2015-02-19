Readme.txt

keep the tccorpus.txt and queries.txt file in the same folder as the executable jars indexer.jar and bm25.jar

Go to this folder from terminal and execute the indexer.jar with the following command
java -jar indexer.jar "tccorpus.txt"
This will generate the file "index.out" in the same folder.

Now execute the bm25.jar which is also in the same folder using the following command
java -jar bm25.jar "index.out" "queries.txt" "100"
This will generate the file "results.eval" which will have the expected output.

The implementation report is present in the file "Report_of_implementation.doc"
The codes can be found in indexer.java and bm25.java


