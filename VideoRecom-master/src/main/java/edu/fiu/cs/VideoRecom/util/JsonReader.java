package edu.fiu.cs.VideoRecom.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.Closeables;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * 
 * JsonReader Responsible for reading and parse Json file to lists.
 * 
 */
public class JsonReader {

  public static String DATA_INPUT_PATH = "./src/test/resources/CodeAssignmentDataSet.json";

  private static Logger logger = LoggerFactory.getLogger(JsonReader.class);
  private String path;

  public JsonReader(String path) {
    // TODO Auto-generated constructor stub
    this.path = path;
  }

  public JsonElement parse() {

    StringBuffer sb = new StringBuffer();
    BufferedReader br;
    
    try {
      br = new BufferedReader(new FileReader(path));
      String line = br.readLine();
      while (line != null) {
        sb.append(line);
        line = br.readLine();
      }
      Closeables.close(br, true);
      logger.info("file reading done...");
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      logger.error("reading file error...");
      //when error happens, empty StringBuffer,set it to empty json array
      sb.delete(0, sb.length());
      sb.append("[]");
    }
    
    JsonParser parser = new JsonParser();
    JsonElement rtn = parser.parse(sb.toString());
    return rtn;
  }

}
