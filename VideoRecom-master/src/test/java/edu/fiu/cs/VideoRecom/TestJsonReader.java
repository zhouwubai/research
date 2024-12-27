package edu.fiu.cs.VideoRecom;

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import edu.fiu.cs.VideoRecom.util.JsonReader;

public class TestJsonReader {

  private static Logger logger = LoggerFactory.getLogger(TestJsonReader.class);
  private JsonReader jr;

  @Before
  public void setup() {
    this.jr = new JsonReader(JsonReader.DATA_INPUT_PATH);
  }

  @Test
  public void test() throws IOException {

    logger.info("Test JsonReader begins...");
    JsonElement je = jr.parse();
    JsonArray videos = je.getAsJsonArray();
    logger.info(String.format("video size is %d", videos.size()));
    assertTrue(videos.size() == 474);
    
  }

}
