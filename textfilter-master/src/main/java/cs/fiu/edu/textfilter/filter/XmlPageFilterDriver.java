package cs.fiu.edu.textfilter.filter;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;

import cs.fiu.edu.textfilter.custom.WholeFileInputFormat;

public class XmlPageFilterDriver extends Configured implements Tool {

  public static class FilterMapper extends Mapper<Text, Text, Text, Text> {

    private Document document;
    private SAXReader saxReader = new SAXReader();
    private String[] keywords;
    private Configuration conf;
    private int count = 1;

    @Override
    protected void setup(Context context) throws IOException,
        InterruptedException {
      // TODO Auto-generated method stub
      conf = context.getConfiguration();
      keywords = conf.get("textfilter.query").trim().split("@");
      System.out.println("keywords:length: " + keywords.length);
      System.out.println(conf.get("textfilter.query"));
    }

    @Override
    protected void map(Text key, Text value, Context context)
        throws IOException, InterruptedException {
      // TODO Auto-generated method stub

      
      try {

//        System.out.println(value.toString());

        document = saxReader.read(new StringReader(value.toString().trim()));
        document.normalize();
        Map<String, String> namespaceUris = new HashMap<String, String>();
        namespaceUris.put("mediawiki",
            "http://www.mediawiki.org/xml/export-0.3/");

        XPath xPath = DocumentHelper.createXPath("//mediawiki:page");
        xPath.setNamespaceURIs(namespaceUris);

        List list = xPath.selectNodes(document);
        Iterator iter = list.iterator();

        System.out.println(list.size());

        while (iter.hasNext()) {
          Element page = (Element) iter.next();
          Element textEle = page.element("revision").element("text");
          Element timeEle = page.element("revision").element("timestamp");
          for (String keyword : keywords) {
            if (StringUtils.containsIgnoreCase(textEle.getText(), keyword)) {
              System.out.println(count++);
//              System.out.println(textEle.getText());
//              System.out.println(page.asXML());
              context.write(new Text(timeEle.getText()),
                  new Text(page.asXML()));
              break;
            }
          }
        }

      } catch (DocumentException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

    }

  }

  public static class FilterReducer extends
      Reducer<Text, Text, NullWritable, Text> {

    int count = 1;
    
    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context)
        throws IOException, InterruptedException {
      // TODO Auto-generated method stub
      for (Text value : values) {
        System.out.println(count++);
        context.write(NullWritable.get(), value);
      }
    }
  }

  public int run(String[] args) throws Exception {
    // TODO Auto-generated method stub

    Configuration conf = getConf();
    String query = "";
    for(int i = 2; i < args.length - 1; i++){
      query += args[i]+"@";
    }
    query += args[args.length - 1];
    conf.set("textfilter.query", query);

    Job filterJob = new Job(conf, "Wikipedia page filter job");
    filterJob.setJarByClass(XmlPageFilterDriver.class);
    filterJob.setMapperClass(FilterMapper.class);
    filterJob.setReducerClass(FilterReducer.class);

    // FileSystem fs = FileSystem.get(conf);
    // FileStatus[] list = fs.listStatus(new Path(args[1]));
    // if (list != null) {
    // for (FileStatus status : list) {
    // FileInputFormat.addInputPath(filterJob, status.getPath());
    // }
    // }

    // FileSystem fs = FileSystem.get(conf);
    // fs.delete(new Path(args[2]), true);

    FileInputFormat.setInputPaths(filterJob, new Path(args[0]));
    FileOutputFormat.setOutputPath(filterJob, new Path(args[1]));

    // WholeFileInputFormat
    filterJob.setInputFormatClass(WholeFileInputFormat.class);
    filterJob.setMapOutputKeyClass(Text.class);
    filterJob.setMapOutputValueClass(Text.class);

    filterJob.setNumReduceTasks(1);

    filterJob.waitForCompletion(true);
    return 0;
  }

  public static void main(String[] args) throws Exception {
    int exit = ToolRunner.run(new XmlPageFilterDriver(), args);
    System.exit(exit);
  }
}
