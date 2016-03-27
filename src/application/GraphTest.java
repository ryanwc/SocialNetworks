package application;

import graph.CapGraph;
import util.GraphLoader;

public class GraphTest {

	public static void main(String[] args) {
		
		CapGraph testGraph = new CapGraph("Test");
		
		GraphLoader.loadGraph(testGraph, "data/small_test_graph.txt");
		
		testGraph.printGraph();
	}
}
