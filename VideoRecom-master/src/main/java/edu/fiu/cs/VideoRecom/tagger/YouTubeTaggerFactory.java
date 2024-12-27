package edu.fiu.cs.VideoRecom.tagger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.fiu.cs.VideoRecom.common.YouTubeVideo;
import edu.fiu.cs.VideoRecom.util.JsonReader;
import edu.fiu.cs.VideoRecom.util.JsonWriter;

public class YouTubeTaggerFactory {

  private static Logger logger = LoggerFactory.getLogger(YouTubeTaggerFactory.class);

  private String inputPath;
  private String outputPath;
  private List<TagExtractorBase> taggers;

  public YouTubeTaggerFactory() {
    // TODO Auto-generated constructor stub
    taggers = new ArrayList<TagExtractorBase>();
  }

  public void run() {

    List<YouTubeVideo> videos = YouTubeVideo.parseJsonToVideos((new JsonReader(
        inputPath)).parse().getAsJsonArray());

    // Here we can try to speedup using multi thread
    int count = 1;
    for (YouTubeVideo video : videos) {
      String lookupText = video.getTitle() + video.getDescription();

      List<List<YouTubeTag>> tagGroups = new ArrayList<List<YouTubeTag>>();
      for (TagExtractorBase tagger : taggers) {
        tagGroups.add(tagger.tag(lookupText));
      }

      video.setTags(mergeTagGroups(tagGroups));
      logger.info(String.format("done tagging %d video", count++));

    }

    // write to output
    (new JsonWriter(outputPath)).write(videos);
  }

  public static void main(String[] args) {

    YouTubeTaggerFactory taggers = new YouTubeTaggerFactory();
    taggers.setInputPath(JsonReader.DATA_INPUT_PATH);
    taggers.setOutputPath(JsonWriter.DATA_OUT_PATH);
//    taggers.addTagger(new AlchemyTagExtractor());
    taggers.addTagger(new LupediaTagExtractor());
    taggers.addTagger(new SpotlightTagExtractor());
    taggers.run();

  }

  private List<YouTubeTag> mergeTagGroups(List<List<YouTubeTag>> tagGroups) {
    HashSet<String> checkDup = new HashSet<String>();
    List<YouTubeTag> tags = new ArrayList<YouTubeTag>();
    for (List<YouTubeTag> oneGroup : tagGroups) {
      for (YouTubeTag tag : oneGroup) {
        //latter I might realize filter condition into filter class.
        if (!checkDup.contains(tag.getName()) && tag.getName().split(" ").length > 1) {
          tags.add(tag);
          checkDup.add(tag.getName());
        }
      }
    }
    return tags;
  }

  public void addTagger(TagExtractorBase tagger) {
    taggers.add(tagger);
  }

  public String getInputPath() {
    return inputPath;
  }

  public void setInputPath(String inputPath) {
    this.inputPath = inputPath;
  }

  public String getOutputPath() {
    return outputPath;
  }

  public void setOutputPath(String outputPath) {
    this.outputPath = outputPath;
  }

}
