package edu.fiu.cs.VideoRecom.tagger;

import com.google.common.base.Objects;

public class YouTubeTag {

  private String name;
  private String type;

  public YouTubeTag() {
    // TODO Auto-generated constructor stub
  }
  
  public YouTubeTag(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }
  
  
  @Override
  public String toString() {
    // TODO Auto-generated method stub
    return Objects.toStringHelper(this.getClass())
          .add("tag name", this.name)
          .add("tag type", this.type)
          .toString();
  }

}
