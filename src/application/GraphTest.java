package application;

import java.util.HashMap;
import java.util.List;

import graph.CapGraph;
import graph.Graph;
import graph.Vertex;
import util.GraphLoader;

public class GraphTest {

	public static void main(String[] args) {
		
		//CapGraph testGraphOne = new CapGraph("Test One");
		CapGraph smallTestGraph = new CapGraph("Small Test");
		
		//GraphLoader.loadGraph(testGraph, "data/facebook_ucsd.txt");
		GraphLoader.loadGraph(smallTestGraph, "data/small_test_graph.txt");
		
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
		
		///*
		//SCCs test
		
		//*/
	}
}
