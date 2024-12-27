package edu.fiu.cs.VideoRecom;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.jblas.DoubleMatrix;
import org.junit.Before;
import org.junit.Test;

import edu.fiu.cs.VideoRecom.common.YouTubeVideo;
import edu.fiu.cs.VideoRecom.recommend.Recommender;
import edu.fiu.cs.VideoRecom.util.JsonReader;
import edu.fiu.cs.VideoRecom.util.JsonWriter;

public class TestRecommender {

  private Recommender recom;
  
  @Before
  public void setUp(){
    recom = new Recommender();
    Map<String,Double> conf = new HashMap<String, Double>();
    conf.put(YouTubeVideo.TAG_W, 2d);
    conf.put(YouTubeVideo.TITLE_W,1d);
    conf.put(YouTubeVideo.CATEG_W, 1d);
    conf.put(YouTubeVideo.DESC_W, 1d);
    conf.put(YouTubeVideo.N_GRAM, 3d);
    
    JsonReader jr = new JsonReader(JsonWriter.DATA_OUT_PATH);
    recom.setVideos(YouTubeVideo.parseJsonToTaggedVideos(jr.parse().getAsJsonArray()));
    recom.setConf(conf);
    
    recom.recommend();
  }
  
  @Test
  public void test() {
    DoubleMatrix sim = new DoubleMatrix(recom.getSimGraph());
    int[][] sortIdx = sim.rowSortingPermutations();
    
    for(int i = 0; i < sortIdx.length; i++){
      assertTrue(i == sortIdx[i][sortIdx[i].length - 1]);
      for(int j = 0; j < sortIdx[i].length - 1; j++){
        //
//        assertTrue(sim.get(i, sortIdx[i][j]) <= sim.get(i, sortIdx[i][j+1]));
//        System.out.print(recom.getSimGraph()[i][sortIdx[i][j]] + "\t");
      }
      System.out.println();
    }
    
    double[][] testM = {{1,2,3},{3,4,1},{6,7,3}};
    DoubleMatrix testDM = new DoubleMatrix(testM);
    int[][] sortM = testDM.rowSortingPermutations();
    
    for(int i = 0; i < testM.length; i++){
      for(int j = 0; j < testM[i].length; j++){
        System.out.print(testM[i][j] + "\t");
      }
      System.out.println();
    }
    
    for(int i = 0; i < sortM.length; i++){
      for(int j = 0; j < sortM[i].length; j++){
        System.out.print(sortM[i][j] + "\t");
      }
      System.out.println();
    }
    
  }

}
