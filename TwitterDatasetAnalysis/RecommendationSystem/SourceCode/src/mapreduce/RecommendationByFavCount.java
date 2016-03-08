package mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class RecommendationByFavCount {

	public static class RecommendationsMapper extends Mapper<Object, Text, Text, Text> {

		private Text UserKey = new Text();
		private Text Value = new Text();

		final int TOP_TEN = 10;

		private static HashMap<String, Integer> UsersMap = new HashMap<String, Integer>();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			@SuppressWarnings("deprecation")
			Path[] path = context.getLocalCacheFiles();
			fillUsersHashMap(path[0]);
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			super.cleanup(context);
			UsersMap.clear();
			UsersMap = null;
		}

		public void fillUsersHashMap(Path file) throws IOException {

			String line = "";
			BufferedReader reader = new BufferedReader(new FileReader(file.toString()));

			while ((line = reader.readLine()) != null) {
				String userInfo[] = line.split("\t");
				if (userInfo.length == 8)
					UsersMap.put(userInfo[0].trim(), Integer.parseInt(userInfo[5].trim()));
			}
			reader.close();
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			
			TreeMap<Integer, String> userWithFavCountMap = new TreeMap<Integer, String>(Collections.reverseOrder());

			String[] users = value.toString().split("\t");
			key = users[0];
			String[] recommendations = users[1].split(", ");

			for (String userId : recommendations) {
				
				if(UsersMap.containsKey(userId)){
					int userFavoriteCount = UsersMap.get(userId);
					if (userWithFavCountMap.size() < TOP_TEN) {
						userWithFavCountMap.put(userFavoriteCount, userId);
					} else {
						Integer lowestKey = userWithFavCountMap.firstKey();
						if (lowestKey.intValue() <= userFavoriteCount) {

							// This deletes the smallest element
							@SuppressWarnings("unused")
							Entry<Integer, String> x = userWithFavCountMap.pollFirstEntry();

							// insert the latest element which is greater than smallest element
							userWithFavCountMap.put(userFavoriteCount, userId);
						}
					}
				}
				
			}

			StringBuilder top10Recommendations = new StringBuilder();
			
			for (Map.Entry<Integer, String> entry : userWithFavCountMap.entrySet()) {
				top10Recommendations.append(entry.getValue());
				top10Recommendations.append("|");
			}

			Value.set(top10Recommendations.toString());
			UserKey.set(key.toString());
			context.write(UserKey, Value);
		}
	}

	public static void main(String[] args) throws ClassNotFoundException, IOException, InterruptedException, URISyntaxException {
		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err.println("Usage: RecommendationByFavCount <in> <out>");
			System.exit(2);
		}
		Job job = Job.getInstance(conf, "Recommendation by Max Favorite Count");
		job.addCacheFile(new URI("s3://mapreduceproj/users.txt"));
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		job.setJarByClass(RecommendationByFavCount.class);
		job.setMapperClass(RecommendationsMapper.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);

		int sucessCode = job.waitForCompletion(true) ? 0 : 3;
		System.exit(sucessCode);
	}

}
