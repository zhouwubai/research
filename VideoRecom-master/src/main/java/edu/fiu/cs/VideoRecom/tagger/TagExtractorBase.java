package edu.fiu.cs.VideoRecom.tagger;

import java.util.List;

import com.google.gson.JsonParser;

/**
 * Base class for tagger
 * @author zhouwubai
 *
 */
public abstract class TagExtractorBase {


	protected static JsonParser parser = new JsonParser();
	
	public abstract List<YouTubeTag> tag(String text);
	
}
