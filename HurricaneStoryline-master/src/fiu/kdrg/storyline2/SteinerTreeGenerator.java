package fiu.kdrg.storyline2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jblas.DoubleMatrix;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fiu.kdrg.storyline.Edge;
import fiu.kdrg.storyline.SteinerTree;
import fiu.kdrg.storyline.event.Event;

public class SteinerTreeGenerator {

	private static Logger logger = LoggerFactory
			.getLogger(SteinerTreeGenerator.class);

	private List<Event> events;
	private double[][] simGraph = null;
	private List<Integer> doms = null;
	private int approx = 12;
	private int domMaxNum = 120;
	private List<Map<Integer, DijkstraShortestPath<Integer, Edge>>> shortestPathsFromAllNodesToDoms;
	private SteinerTree steinerTree = null;
	private DirectedGraph<Integer, Edge> connGraph = null;

	public SteinerTreeGenerator(List<Event> events) {
		// TODO Auto-generated constructor stub
		this.events = events;
	}

	public static void main(String[] args) {
		List<Event> events = null;
		// events = EventLoader.loadEventByDisaster(1, "2005-08-20",
		// "2005-10-01");
		// events = EventLoader.loadEventByDisaster(2, "2012-10-01",
		// "2012-12-01");
		events = EventLoader.loadEventByDisaster(3, "2011-08-01", "2011-09-01");

		SteinerTreeGenerator treeGenerator = new SteinerTreeGenerator(events);
		treeGenerator.computeSteinerTree(1);
		treeGenerator.printSteinerTree();
	}

	private void computeSimGraph() {
		Map<String, Integer> idf = new HashMap<String, Integer>();
		List<Map<String, Double>> X = new ArrayList<Map<String, Double>>();
		simGraph = new double[events.size()][events.size()];
		for (Event event : events) {
			for (String ngram : new HashSet<String>(event.getNGramsOfContent())) {
				Integer df = idf.get(ngram);
				if (df == null)
					df = 0;
				idf.put(ngram, df + 1);// 其实这个是Collection Frequency,不是document
										// frequency
			}
		}

		logger.info("idf done");

		for (Event event : events) {
			Map<String, Double> x = new HashMap<String, Double>();
			for (String ngram : event.getNGramsOfContent()) {
				Integer ngramidf = idf.get(ngram);
				if (ngramidf != null && ngram.equals(1))
					continue;

				Double tf = x.get(ngram);
				if (tf == null)
					tf = 0.0;
				x.put(ngram, tf + 1);
			}

			double norm = 0;

			for (Entry<String, Double> en : x.entrySet()) {
				String ngram = en.getKey();
				Integer ngramidf = idf.get(ngram);
				Double tf = en.getValue();
				tf *= Math.log((events.size() + 1.0) / ngramidf);
				en.setValue(tf);
				norm += tf * tf;// tf 即是tf-idf
			}
			norm = Math.sqrt(norm);
			for (Entry<String, Double> en : x.entrySet()) {
				en.setValue(en.getValue() / norm);
			}
			X.add(x);
		}

		logger.info("vectorization done");
		long start = new Date().getTime();

		for (int i = 0; i < events.size(); i++) {
			simGraph[i][i] = 1;
			for (int j = i + 1; j < events.size(); j++) {
				simGraph[i][j] = simGraph[j][i] = innerProduct(X.get(i),
						X.get(j));
			}
		}
		logger.info("sim graph done, " + (new Date().getTime() - start) / 1000);
	}

	private void computeDomSet(int k) {
		computeSimGraph();
		doms = new ArrayList<Integer>();
		Map<Integer, List<Integer>> clusters = new HashMap<Integer, List<Integer>>();

		int n = events.size();
		DoubleMatrix connGraph = DoubleMatrix.zeros(n, n);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (simGraph[i][j] > 0.7) {
					connGraph.put(i, j, 1);
					// System.out.println(1);
				}
			}
		}

		DoubleMatrix uncovered = DoubleMatrix.ones(1, n);
		int i = 0;
		while (i < k) {
			DoubleMatrix covering = DoubleMatrix.ones(1, n);
			covering.copy(uncovered);
			covering = covering.mmul(connGraph);
			int sel = covering.argmax();
			double maxdeg = covering.get(sel);

			if (maxdeg < 0.5)
				break;

			doms.add(sel);

			DoubleMatrix ind = connGraph.getRow(sel).ge(0.5);
			List<Integer> members = new ArrayList<Integer>();
			for (int rowN = 0; rowN < ind.rows; rowN++)
				for (int colN = 0; colN < ind.columns; colN++) {
					if (ind.get(rowN, colN) != 0)
						members.add(colN);
				}

			clusters.put(sel, members);

			int[] neighbors = ind.findIndices();
			uncovered = uncovered.put(0, ind, 0);
			i++;
		}
		logger.info(String.format("dom set size %d", doms.size()));
		logger.info("Generate dom sets done.");
	}

	private DirectedGraph<Integer, Edge> computeConnGraph() {
		if (simGraph == null)
			computeSimGraph();

		DirectedGraph<Integer, Edge> connGraph = new DefaultDirectedGraph<Integer, Edge>(
				Edge.class);
		for (int i = 0; i < simGraph.length; i++)
			connGraph.addVertex(i);

		for (int i = 0; i < simGraph.length; i++) {
			long ti = events.get(i).getEventDate();
			for (int j = 0; j < simGraph.length; j++) {
				if (i == j)
					continue;
				if (simGraph[i][j] <= 0.1) {
					continue;
				}

				// 相差50个小时，相似度大于0.1
				long tj = events.get(j).getEventDate();
				if (ti <= tj && ti > tj - 1000 * 3600 * 50) {
					connGraph.addEdge(i, j);
				}
			}
		}
		logger.info("Generate Directed Graph Done");
		logger.info(String.format("directed graph size is %d", connGraph
				.edgeSet().size()));
		return connGraph;
	}

	public void computeSteinerTree(int approx) {
		if (doms == null)
			computeDomSet(domMaxNum);

		Collections.sort(doms, new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				return (int) (events.get(o1).getEventDate() - events.get(o2)
						.getEventDate());
			}
		});

		connGraph = computeConnGraph();
		shortestPathsFromAllNodesToDoms = new ArrayList<Map<Integer, DijkstraShortestPath<Integer, Edge>>>();
		for (int i = 0; i < simGraph.length; i++) {
			Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = new HashMap<Integer, DijkstraShortestPath<Integer, Edge>>();
			for (int j = 0; j < doms.size(); j++) {
				DijkstraShortestPath<Integer, Edge> path = new DijkstraShortestPath<Integer, Edge>(
						connGraph, i, doms.get(j));
				path.getPath();
				paths.put(doms.get(j), path);
			}
			shortestPathsFromAllNodesToDoms.add(paths);
		}

		int kk = doms.size();
		SteinerTree tree = getSteinerTree(doms, doms.get(0), kk--, approx);
		while (tree == null) {
			tree = getSteinerTree(doms, doms.get(0), kk--, approx);
		}
		steinerTree = tree;
		logger.info("Generate Steiner Tree Done");
	}

	private SteinerTree getSteinerTree(List<Integer> doms, int root,
			int targetSize, int approx) {
		if (approx == 1)
			return getBaseSteinerTree(doms, root, targetSize);
		int n = connGraph.vertexSet().size();
		SteinerTree tree = new SteinerTree(doms);
		while (targetSize > 0) {
			SteinerTree besttree = new SteinerTree(doms);
			besttree.add(root);
			for (Edge e : connGraph.outgoingEdgesOf(root)) {
				int v = connGraph.getEdgeTarget(e);
				for (int kp = 1; kp <= targetSize; kp++) {
					SteinerTree ctree = getSteinerTree(doms, v, kp, approx - 1);
					if (ctree == null)
						continue;
					ctree.add(root);// 这个地方应该加条root到v的边
					ctree.getGraphSteinerTreeInfo().addEdge(root, v);
					if (ctree.cost() < besttree.cost())// cost有问题
						besttree = ctree;
				}
			}
			if (besttree.cover() == 0)
				return null;
			targetSize -= besttree.cover();
			doms = new ArrayList<Integer>(doms);
			doms.removeAll(besttree.getDoms());

			tree.add(besttree);
		}
		return tree;
	}

	private SteinerTree getBaseSteinerTree(List<Integer> doms, final int root,
			int targetSize) {
		logger.info("entering getBaseSteinerTree methods");
		final Map<Integer, DijkstraShortestPath<Integer, Edge>> paths = shortestPathsFromAllNodesToDoms
				.get(root);

		SteinerTree tree = new SteinerTree(doms);
		tree.add(root);
		List<Integer> targets = new ArrayList<Integer>(doms);
		targets.remove(new Integer(root));
		Collections.sort(targets, new Comparator<Integer>() {

			@Override
			// 从root到dom的最短路径排序。
			public int compare(Integer o1, Integer o2) {
				int l1 = 100000;
				int l2 = 100000;
				List<Edge> path = paths.get(o1).getPathEdgeList(); // shortestPath.getPathEdgeList(o1);
				if (path != null)
					l1 = path.size();
				path = paths.get(o2).getPathEdgeList();
				if (path != null)
					l2 = path.size();

				return l1 - l2;
			}
		});

		logger.info("merge shortest path");
		for (int i = 0; i < targets.size(); i++) {
			if (tree.cover() == targetSize)
				break;
			int nodei = targets.get(i);
			List<Edge> edges = paths.get(nodei).getPathEdgeList();
			if (edges == null) {
				break;
			} else {
				logger.info(String.format(
						"root to target %d's path,length is %d", nodei,
						edges.size()));
			}
			for (Edge e : paths.get(nodei).getPathEdgeList()) {// 最短路径生成的不一定是颗树？？？
				Integer target = connGraph.getEdgeTarget(e);
				Integer source = connGraph.getEdgeSource(e);
				tree.add(target);
				tree.getGraphSteinerTreeInfo().addVertex(source);
				tree.getGraphSteinerTreeInfo().addVertex(target);
				tree.getGraphSteinerTreeInfo().addEdge(source, target);

			}
		}

		// if (tree.cover() != targetSize)
		// return null;
		// else
		return tree;
	}

	// private void setEventWeights() {
	//
	// int n = simGraph.length;
	// DoubleMatrix simGraphMatrix = new DoubleMatrix(simGraph);
	// DoubleMatrix addMatrix = DoubleMatrix.ones(1, n);
	// DoubleMatrix weights = DoubleMatrix.zeros(1, n);
	// weights = addMatrix.mmul(simGraphMatrix);
	//
	// for (int i = 0; i < n; i++) {
	// events.get(i).setWeight(weights.get(i));
	// }
	// }

	public void printSteinerTree() {
		for (Integer node : steinerTree.getNodes()) {
			System.out.println(events.get(node).getEventContent());
		}
	}

	static public double innerProduct(Map<String, Double> a,
			Map<String, Double> b) {
		double inner = 0;
		if (a.size() > b.size()) {
			Map<String, Double> c = a;
			a = b;
			b = c;
		}
		for (Entry<String, Double> en : a.entrySet()) {
			Double v = b.get(en.getKey());
			if (v != null)
				inner += en.getValue() * v;
		}
		return inner;
	}

	public List<Event> getEvents() {
		return events;
	}

	public void setEvents(List<Event> events) {
		this.events = events;
	}

	public double[][] getSimGraph() {
		return simGraph;
	}

	public List<Integer> getDoms() {
		return doms;
	}

	public int getApprox() {
		return approx;
	}

	public void setApprox(int approx) {
		this.approx = approx;
	}

	public List<Map<Integer, DijkstraShortestPath<Integer, Edge>>> getShortestPathsFromAllNodesToDoms() {
		return shortestPathsFromAllNodesToDoms;
	}

	public SteinerTree getSteinerTree() {
		return steinerTree;
	}

	public DirectedGraph<Integer, Edge> getConnGraph() {
		return connGraph;
	}

}
