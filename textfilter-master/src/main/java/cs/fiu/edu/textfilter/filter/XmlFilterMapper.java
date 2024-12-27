package cs.fiu.edu.textfilter.filter;

import java.io.IOException;

import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.dom4j.Document;

public class XmlFilterMapper extends Mapper<NullWritable, Text, Text, Text>{

	private Document document;
	
	@Override
	protected void map(NullWritable key, Text value,Context context)
			throws IOException, InterruptedException {
		
	}
	
}
