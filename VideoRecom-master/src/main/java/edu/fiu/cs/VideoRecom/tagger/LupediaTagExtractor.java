package edu.fiu.cs.VideoRecom.tagger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class LupediaTagExtractor extends TagExtractorBase {

  private static Logger logger = LoggerFactory
      .getLogger(LupediaTagExtractor.class);
  private static String dbUrlBase = "http://lupedia.ontotext.com/lookup/text2json";

  private String query;

  // = "Keegan-Michael Key and Jordan Peele sit "
  // + "down with Peter Rubin to talk about their love of Game of Thrones, "
  // + "their favorite comedy sketches";

  private String sendRequest() {
    String rtnJson;
    try {
      rtnJson = Jsoup.connect(dbUrlBase).data("lookupText", query)
          .method(Method.POST).ignoreContentType(true).execute().body();
//      logger.info("sendRequest complete...");
      return rtnJson;
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      logger
          .error("error happens when sending request to database,skip this task....");
      return "[]";
    }
  }

  private List<YouTubeTag> parseJsonToTag(String json) {
    JsonArray rawTags = parser.parse(json).getAsJsonArray();
    List<YouTubeTag> tags = new ArrayList<YouTubeTag>();

    HashSet<String> checkNameDup = new HashSet<String>();
    for (JsonElement e : rawTags) {
      YouTubeTag tag = new YouTubeTag();
      JsonObject jsonTag = e.getAsJsonObject();
      int startOffset = jsonTag.get("startOffset").getAsInt();
      int endOffset = jsonTag.get("endOffset").getAsInt();
      String instanceClass = jsonTag.get("instanceClass").getAsString();

      // check duplicate
      String name = query.substring(startOffset, endOffset);
      if (checkNameDup.contains(name) || name.split(" ").length < 2) {
        continue;
      } else {
        checkNameDup.add(name);
      }

      tag.setName(query.substring(startOffset, endOffset));
      tag.setType(instanceClass.substring(instanceClass.lastIndexOf("/") + 1));
      tags.add(tag);
    }

    return tags;
  }

  @Override
  public List<YouTubeTag> tag(String lookupText) {
    this.query = lookupText;
    return parseJsonToTag(sendRequest());
  }

}
