package application;

import graph.CapGraph;
import util.GraphLoader;

public class GraphTest {

	public static void main(String[] args) {
		
		CapGraph test = new CapGraph("Test");
		
		GraphLoader.loadGraph(test, "data/small_test_graph.txt");
		
		test.printGraph();
	}
}
