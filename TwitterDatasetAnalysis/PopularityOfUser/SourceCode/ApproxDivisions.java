package com.mr.project.approxDivisions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

public class ApproxDivisions {

	public static class ApproxDivisionsMapper extends
			Mapper<Object, Text, Text, IntWritable> {

		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException {

			String line = value.toString();

			double p = Math.random();

			if (p <= 0.5) {

				String[] strArray = line.split("\t");

				if (strArray.length > 6 && strArray[0].length() > 4) {

					int numOfFollowers = Integer.parseInt(strArray[3]);

					context.write(new Text("dummy"), new IntWritable(
							numOfFollowers));
				}

			}

		}

	}

	public static class ApproxDivisonsReducer extends
			Reducer<Text, IntWritable, IntWritable, IntWritable> {

		public void reduce(Text dummy, Iterable<IntWritable> values,
				Context context) throws IOException, InterruptedException {

			List<Integer> valuesAL = new ArrayList<Integer>();
			
			for (IntWritable val : values) {

				valuesAL.add(val.get());

			}
			
			Collections.sort(valuesAL);
			
			int size = valuesAL.size();
			
			System.out.println(valuesAL.get(0));
			System.out.println(valuesAL.get((int)((double)size * 0.2)));
			System.out.println(valuesAL.get((int)((double)size * 0.4)));
			System.out.println(valuesAL.get((int)((double)size * 0.6)));
			System.out.println(valuesAL.get((int)((double)size * 0.8)));
			System.out.println(valuesAL.get(valuesAL.size()-1));
			
			context.write(new IntWritable(0), new IntWritable(valuesAL.get(0)));
			context.write(new IntWritable(1), new IntWritable(valuesAL.get((int)((double)size * 0.2))));
			context.write(new IntWritable(2), new IntWritable(valuesAL.get((int)((double)size * 0.4))));
			context.write(new IntWritable(3), new IntWritable(valuesAL.get((int)((double)size * 0.6))));
			context.write(new IntWritable(4), new IntWritable(valuesAL.get((int)((double)size * 0.8))));
			context.write(new IntWritable(5), new IntWritable(valuesAL.get(valuesAL.size()-1)));
			

		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		Configuration conf = new Configuration();
		String[] otherArgs = new GenericOptionsParser(conf, args)
				.getRemainingArgs();
		if (otherArgs.length < 2) {
			System.err.println("Usage: wordcount <in> [<in>...] <out>");
			System.exit(2);
		}
		Job job = new Job(conf, "approx divisions");
		job.setJarByClass(ApproxDivisions.class);
		job.setMapperClass(ApproxDivisionsMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);

//		job.setCombinerClass(ApproxDivisonsReducer.class);
//		job.setPartitionerClass(PopularityPartitioner.class);
//		job.setNumReduceTasks(1);
		job.setReducerClass(ApproxDivisonsReducer.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setOutputValueClass(IntWritable.class);

		for (int i = 0; i < otherArgs.length - 1; ++i) {
			FileInputFormat.addInputPath(job, new Path(otherArgs[i]));
		}

		FileOutputFormat.setOutputPath(job, new Path(
				otherArgs[otherArgs.length - 1]));
		System.exit(job.waitForCompletion(true) ? 0 : 1);
	}

}
