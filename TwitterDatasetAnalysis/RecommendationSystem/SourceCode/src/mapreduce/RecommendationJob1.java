package mapreduce;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

public class RecommendationJob1 {

	public static class UserRelationMapper extends Mapper<Object, Text, Text, Text> {
		
		private Text UserKey = new Text();
		private Text Value = new Text();
		
		private static HashSet<String> UsersSet = new HashSet<String>();

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
			UsersSet.clear();
			UsersSet = null;
		}

		public void fillUsersHashMap(Path file) throws IOException {

			String line = "";
			BufferedReader reader = new BufferedReader(new FileReader(file.toString()));

			while ((line = reader.readLine()) != null) {
				String userInfo[] = line.split("\t");
				if (userInfo.length == 8)
					UsersSet.add(userInfo[0].trim());
			}
			reader.close();
		}

		@Override
		public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
			String[] users = value.toString().split("\t");
			if (users.length != 0 && UsersSet.contains(users[0]) && UsersSet.contains(users[1])) {

				// If Relation is : A [tab] B -> Emit [A, B]
				UserKey.set(users[0]);
				Value.set(users[1]);
				context.write(UserKey, Value);

				UserKey.clear();
				Value.clear();
			}
		}
	}
	
	public static class UserRelationReducer extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			StringBuilder sb = new StringBuilder();
			for (Text value : values) {	
				sb.append(",").append(value.toString());
			}
			context.write(key, new Text(sb.toString().substring(1)));
		}
	}
	
	public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, URISyntaxException {
		Configuration conf = new Configuration();
	    String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
	    if (otherArgs.length < 2) {
	      System.err.println("Usage: RecommendationJob1 <in> <out>");
	      System.exit(2);
	    }
	    Job job = Job.getInstance(conf, "Recommendation Job-1");
		FileInputFormat.addInputPath(job, new Path(otherArgs[0]));
		FileOutputFormat.setOutputPath(job, new Path(otherArgs[1]));
		job.addCacheFile(new URI("s3://mapreduceproj/users.txt"));
		job.setJarByClass(RecommendationJob1.class);
		job.setMapperClass(UserRelationMapper.class);
		job.setReducerClass(UserRelationReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		int sucessCode = job.waitForCompletion(true) ? 0 : 3;
		
		System.exit(sucessCode);
	}
}