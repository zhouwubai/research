package edu.fiu.cs.VideoRecom.tagger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class AlchemyTagExtractor extends TagExtractorBase {

  private static Logger logger = LoggerFactory
      .getLogger(AlchemyTagExtractor.class);
  // private static String dbUrlBase =
  // "http://spotlight.dbpedia.org/rest/spot/";

  private String query;

  // = "Keegan-Michael Key and Jordan Peele sit "
  // + "down with Peter Rubin to talk about their love of Game of Thrones, "
  // + "their favorite comedy sketches";

  public static void main(String[] args) {

    String query = "Beverly D\u0027Angelo reads a copy of "
        + "More in a meeting with Ari on the HBO Series Entourage.";
    TagExtractorBase te = new AlchemyTagExtractor();
    te.tag(query);

  }

  private String sendRequest() {
    HttpClient httpClient = new DefaultHttpClient();
    URIBuilder builder = new URIBuilder();
    builder.setScheme("http").setHost("access.alchemyapi.com")
        .setPath("/calls/text/TextGetRankedNamedEntities")
        .setParameter("text", query).setParameter("outputMode", "json")
        .setParameter("apikey", "aa8c66b5d414a424ba87713a5bd8f02a1420a679");
    URI uri = null;
    try {
      uri = builder.build();
//      logger.info(uri.toString());

    } catch (URISyntaxException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }

    HttpGet getRequest = new HttpGet(uri);
    getRequest.addHeader("accept", "application/json");
    try {
      HttpResponse response = httpClient.execute(getRequest);
      if (response.getStatusLine().getStatusCode() != 200) {
        logger.info("Failed: Http error code : "
            + response.getStatusLine().getStatusCode());
        return "{\"entities\":[]}";
      }

      BufferedReader br = new BufferedReader(new InputStreamReader(response
          .getEntity().getContent()));
      StringBuffer sb = new StringBuffer();
      String line = "";
      while ((line = br.readLine()) != null) {
        sb.append(line);
      }
      return sb.toString();
    } catch (Exception e) {
      e.printStackTrace();
      logger
          .error("error happens when sending request to database,skip this task....");
      return "{\"entities\":[]}";
    }
  }

  private List<YouTubeTag> parseJsonToTag(String json) {
    JsonArray rawTags = parser.parse(json).getAsJsonObject().get("entities")
        .getAsJsonArray();
    List<YouTubeTag> tags = new ArrayList<YouTubeTag>();

    HashSet<String> checkNameDup = new HashSet<String>();
    for (JsonElement e : rawTags) {
      YouTubeTag tag = new YouTubeTag();
      JsonObject jsonTag = e.getAsJsonObject();

      // check duplicate
      String name = jsonTag.get("text").getAsString();
      if (checkNameDup.contains(name)) {
        continue;
      } else {
        checkNameDup.add(name);
      }

      tag.setName(name);
      tag.setType(jsonTag.get("type").getAsString());
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
