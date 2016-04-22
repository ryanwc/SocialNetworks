package application;

import java.util.HashMap;
import java.util.List;

import graph.CapGraph;
import graph.Graph;
import graph.StackExchangeTopicGraph;
import graph.Vertex;
import util.GraphLoader;

public class GraphTest {

	public static void main(String[] args) {
		
		//CapGraph testGraphOne = new CapGraph("Test One");
		//CapGraph testGraph = new CapGraph("Testing");
		
		//GraphLoader.loadGraph(testGraph, "data/facebook_ucsd.txt");
		//GraphLoader.loadGraph(testGraph, "data/small_test_graph.txt");
		
		StackExchangeTopicGraph graph = new StackExchangeTopicGraph("Test Topic 1");
		GraphLoader.populateStackExchangeTopicGraph(graph, "data/stack_exchange/TestGraph1/");
		graph.printGraph();
		List<Graph> SCCs = graph.getSCCs();
		for (Graph SCC : SCCs) {
			if (SCC instanceof StackExchangeTopicGraph) {
				((StackExchangeTopicGraph) SCC).printGraph();
			}
		}
		
		//testGraph.printGraph();
		
		//CapGraph testTranspose = testGraph.getTranspose();
		
		//testTranspose.printGraph();
		
		//System.out.println();
		
		/*
		List<Vertex> testList = testGraphOne.getVertices().get(8195).getOutEdges();
		
		//System.out.println(testList.size());
		
		for (Vertex vertex : testList) {
			System.out.println(vertex.getID());
		}
		*/
		
		/*
		//Egonet test
		for (int vertexID : smallTestGraph.getVertices().keySet()) {
			
			Graph egonet = smallTestGraph.getEgonet(vertexID);
			System.out.println("Egonet for " + vertexID + ":");
			((CapGraph)egonet).printGraph();
		}
		//*/
		
		/*
		//SCCs test
		List<Graph> SCCs = testGraph.getSCCs();
		for (Graph graph : SCCs) {
			((CapGraph)graph).printGraph();
		}
		CapGraph anotherTest = new CapGraph("SCC Test");
		GraphLoader.loadGraph(anotherTest, "data/ryanTestSCC.txt");
		SCCs = anotherTest.getSCCs();
		for (Graph graph : SCCs) {
			((CapGraph)graph).printGraph();
		}
		//*/
	}
}
