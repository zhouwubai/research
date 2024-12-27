package fiu.kdrg.storyline2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jblas.DoubleMatrix;

import fiu.kdrg.storyline.event.Event;
import fiu.kdrg.storyline.event.SerializeFactory;
import fiu.kdrg.util.EventUtil;
import fiu.kdrg.util.Util;

/**
 * get dominating set
 * @author zhouwubai
 * @date Mar 27, 2014
 * @email zhouwubai@gmail.com
 * Apache Licence 2.0
 */
public class DocFilter {
	
	List<Event> events;
	double miniSim;
	double maxDist;
	
	public DocFilter(List<Event> events) {
		// TODO Auto-generated constructor stub
		this.events = events;
//		setEventID();
		
		miniSim = 0.5;
		maxDist = 5;
	}
	
	
	
	public ArrayList<Event> filter(int k){
		
		DoubleMatrix connGraph = genConnGraph();
		int n = events.size();
		DoubleMatrix uncovered = DoubleMatrix.ones(1,n);
		ArrayList<Event> results = new ArrayList<Event>();
		
		int i = 0;
		while(i < k) {
			DoubleMatrix covering = DoubleMatrix.ones(1,n);
			covering.copy(uncovered);
			covering = covering.mmul(connGraph);
			int sel = covering.argmax();
			double maxdeg = covering.get(sel);
			
			if (maxdeg < 0.5)
				break;
			
			results.add(events.get(sel));
			
			DoubleMatrix ind = connGraph.getRow(sel).ge(0.5);			
			uncovered = uncovered.put(0, ind, 0);
			
			i++;
		}
		
		return results;
	}
	
	
	
	
	
	private DoubleMatrix genConnGraph(){
		
		double[][] simGraph = StoryUtil.computeSimilarity(events);
		int n = events.size();
		DoubleMatrix connGraph = DoubleMatrix.zeros(n, n);
		for(int i = 0; i < n; i++) {
			for(int j = i+1; j < n; j++) {
				if (simGraph[i][j] > miniSim &&
						events.get(i).hasDistanceLe(events.get(j), maxDist)){
					connGraph.put(i, j, 1);
					connGraph.put(j, i, 1);
				}
			}
		}
		
		
		return connGraph;
	}
	

	public void setMiniSim(double miniSim) {
		this.miniSim = miniSim;
	}
	
	

	public void setMaxDist(double maxDist) {
		this.maxDist = maxDist;
	}
	
	/**
	 * set id for events, after this, all events will identifies by its id.
	 */
//	private void setEventID()
//	{
//		for(int id = 0; id < events.size(); id++){
//			events.get(id).setId(id);
//		}
//	}


	public static void main(String[] args) {
		
		DocFilter filter = new DocFilter(
				EventLoader.loadEventByDisaster(1, "2005-01-01","2006-01-01"));
		
		filter.setMiniSim(0.5);
		filter.setMaxDist(3);
		ArrayList<Event> filteredEvents = filter.filter(200);
		
		System.out.println(filteredEvents.size());
		
		try {
			SerializeFactory.serialize(Util.rootDir + "filterEvents1.out", filteredEvents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ArrayList<Event> filterEvents = null;
		
		try {
			filterEvents = (ArrayList<Event>) SerializeFactory.deSerialize(Util.rootDir + "filterEvents1.out");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println(filterEvents.size());
		EventUtil.displayEvents(EventUtil.sortEventByDate(filterEvents));
		for(Event event : filterEvents)
		{
			System.out.println(event.getId());
		}
		
	}
	
}
