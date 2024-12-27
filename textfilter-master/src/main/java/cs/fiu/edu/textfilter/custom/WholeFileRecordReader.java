package cs.fiu.edu.textfilter.custom;

import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.LineRecordReader;

public class WholeFileRecordReader extends RecordReader<Text, Text> {

  private LineRecordReader lineReader;
  private String fileName;

  private boolean fileProcessed = false;
  private StringBuffer valueBuffer = new StringBuffer();
  private Text value = new Text();

  @Override
  public void close() throws IOException {
    // TODO Auto-generated method stub

  }

  @Override
  public Text getCurrentKey() throws IOException, InterruptedException {
    return (new Text(fileName));
  }

  @Override
  public Text getCurrentValue() throws IOException, InterruptedException {
    return value;
  }

  @Override
  public float getProgress() throws IOException, InterruptedException {
    return 0;
  }

  @Override
  public void initialize(InputSplit split, TaskAttemptContext context)
      throws IOException, InterruptedException {
    lineReader = new LineRecordReader();
    lineReader.initialize(split, context);
    fileName = ((FileSplit) split).getPath().getName();
  }

  @Override
  public boolean nextKeyValue() throws IOException, InterruptedException {
    // TODO Auto-generated method stub
    if (fileProcessed) {
      return false;
    }

    while (lineReader.nextKeyValue()) {
      valueBuffer.append(lineReader.getCurrentValue());
    }
    value.set(valueBuffer.toString());
    fileProcessed = true;
    return true;
  }

}