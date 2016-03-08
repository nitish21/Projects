package com.mr.project.popularity;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Partitioner;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;

public class PopularityFinder {
	
	
	
	public static class PopularityMapper extends
	Mapper<Object, Text, IntWritable, Text> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString();
			
			String [] strArray = line.split("\t");
			
			if (strArray.length > 6 && strArray[0].length() > 4) {
				
				String userID = strArray[0];
				String userName = strArray[1];
				
				int numOfFollowers = Integer.parseInt(strArray[3]);

				context.write(new IntWritable(numOfFollowers), new Text(userID+ " " + userName));
				
			}

		}

	}
	
	public static class PopularityReducer extends
		Reducer<IntWritable, Text, Text, IntWritable> {
	
	
		public void reduce(IntWritable noOfFollowers, Iterable<Text> values,
				Context context) throws IOException, InterruptedException {
			
			for (Text val : values) {
				
				context.write(val,noOfFollowers);
		
			}
			
		}
	}
	
	
	public static class PopularityPartitioner extends
	Partitioner<IntWritable, Text> {

		@Override
		public int getPartition(IntWritable noOfFollowers, Text user, int noOfReducers) {
			// TODO Auto-generated method stub

			int numFollowers = noOfFollowers.get();

			if (numFollowers < 23) {
				return 0;
			}
			
			if ( (numFollowers >= 23) && (numFollowers < 81 ) ){
				return 1;
			}
			
			if ( (numFollowers >= 81) && (numFollowers < 222 ) ){
				return 1;
			}
			
			if ( (numFollowers >= 222) && (numFollowers < 663 ) ) {
				return 3;
			} else
				return 4;
			
		}

	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args)  throws Exception {
		// TODO Auto-generated method stub

		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err.println("Usage: wordcount <in> [<in>...] <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "Popularity Finder");
		job.setJarByClass(PopularityFinder.class);
		job.setMapperClass(PopularityMapper.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);

		
		// job.setCombinerClass(IntSumReducer.class);
		job.setPartitionerClass(PopularityPartitioner.class);
		job.setNumReduceTasks(5);
		job.setReducerClass(PopularityReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		
		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		}
		
		FileOutputFormat.setOutputPath(job, new Path(
				otherArgs[otherArgs.length - 1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}
		
		
}


