package mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class RecommendationJob2 {

	public static class UserRelationMapper extends Mapper<Object, Text, Text, Text> {
		
		private static HashMap<String, String> UsersRelationMap = new HashMap<String, String>();

		@Override
		protected void setup(Context context) throws IOException, InterruptedException {
			super.setup(context);
			@SuppressWarnings("deprecation")
			Path[] path = context.getLocalCacheFiles();
			fillUsersRelationHashMap(path[0]);
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException {
			super.cleanup(context);
			UsersRelationMap.clear();
			UsersRelationMap = null;
		}

		public void fillUsersRelationHashMap(Path file) throws IOException {

			String line = "";
			BufferedReader reader = new BufferedReader(new FileReader(file.toString()));

			while ((line = reader.readLine()) != null) {
				String userNetwork[] = line.split("\t");
				UsersRelationMap.put(userNetwork[0].trim(), userNetwork[1].trim());
			}
			reader.close();
		}
		
		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] data = value.toString().split("\t");
			String userId = data[0];
			HashSet<String> recommendations = new HashSet<String>(Arrays.asList(data[1].split(",")));
			
			HashSet<String> uniqueRecommendations = new HashSet<String>();
			for(String rec : recommendations){
				if(UsersRelationMap.containsKey(rec)){
					uniqueRecommendations.addAll(Arrays.asList(UsersRelationMap.get(rec).split(",")));
				}
			}
			uniqueRecommendations.removeAll(recommendations);
			if(!uniqueRecommendations.isEmpty()){
				String val = uniqueRecommendations.toString().replaceAll("\\[","").replaceAll("\\]", "");
				context.write(new Text(userId), new Text(val));
			}
		}
	}
	
	public static class UniqueRecommendationReducer extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			
			StringBuilder sb = new StringBuilder();
			for(Text value:values){
				sb.append(",").append(value.toString());
			}
			context.write(key, new Text(sb.toString().substring(1)));
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length < 2) {
	      System.err.println("Usage: RecommendationJob2 <in> <out>");
	      System.exit(2);
	    }
	    Job job = Job.getInstance(conf, "Recommendation Job-2");
	    job.addCacheFile(new URI("s3://mapreduceproj/Job1Output/Job1MergedOutput.txt"));
	    FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
	    FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		job.setJarByClass(RecommendationJob2.class);
		job.setMapperClass(UserRelationMapper.class);
		job.setReducerClass(UniqueRecommendationReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		int sucessCode = job.waitForCompletion(true) ? 0 : 3;
		
		System.exit(sucessCode);
	}
}
