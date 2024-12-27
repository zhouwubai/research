package edu.fiu.cs.VideoRecom.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import edu.fiu.cs.VideoRecom.common.YouTubeVideo;

public class JsonWriter {

  public static String DATA_OUT_PATH = "./src/test/resources/TaggedDataSet.json";
  public static String RECOM_DATA_OUT_PATH = "./src/test/resources/RecommDataSet.json";
  public static String TEST_SAMPLE = "./src/test/resources/TaggedDataSetTestSample.json";
  
  private static Logger logger = LoggerFactory.getLogger(JsonWriter.class);
  private static Gson gson = new GsonBuilder()
                             .setPrettyPrinting()
                             .create();
  private String path;

  public JsonWriter(String path) {
    // TODO Auto-generated constructor stub
    this.path = path;
  }

  public boolean write(Object obj){
    return write(gson.toJson(obj));
  }
  
  public boolean write(List<YouTubeVideo> videos){
    return write(gson.toJson(videos));
  }
  
   public boolean write(JsonElement je) {
     return write(gson.toJson(je));
   }

  public boolean write(String json){

    BufferedWriter bw = null;
    try {
      bw = new BufferedWriter(new FileWriter(path));
      bw.write(json);
      return true;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      logger.error("error happens when writing...");
      return false;
    } finally {
      try {
        Closeables.close(bw, true);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

  }

}
