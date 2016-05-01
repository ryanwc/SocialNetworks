package application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.CapGraph;
import graph.Graph;
import graph.StackExchangeTopicGraph;
import graph.UserNode;
import graph.Vertex;
import util.GraphLoader;

public class GraphTest {

	public static void main(String[] args) {
		
		//CapGraph testGraphOne = new CapGraph("Test One");
		//CapGraph testGraph = new CapGraph("Testing");
		
		//GraphLoader.loadGraph(testGraph, "data/facebook_ucsd.txt");
		//GraphLoader.loadGraph(testGraph, "data/small_test_graph.txt");
		
		StackExchangeTopicGraph graph = new StackExchangeTopicGraph("Buddhism");
		GraphLoader.populateStackExchangeTopicGraph(graph, "data/stack_exchange/buddhism.stackexchange.com/");
		
		//StackExchangeTopicGraph graph = new StackExchangeTopicGraph("TestGraph1");
		//GraphLoader.populateStackExchangeTopicGraph(graph, "data/stack_exchange/TestGraph1/");
		
		///* regression format export test
		try {
			graph.exportQuestionUsefulnessRegressionFormat();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//*/
		
		/* detect communities test
		try {
			graph.detectAndGetCommunities();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		for (int level : graph.getCommunities().keySet()) {
			
			Map<Integer,StackExchangeTopicGraph> communityMap = graph.getCommunities().get(level);
			
			for (int communityID : communityMap.keySet()) {
				
				communityMap.get(communityID).printGraph();
			}
		}
		//*/
		
		/* Print test
		graph.printGraph();
		//*/
		
		/* SCC test
		// test how many users don't have any posts (and will therefore be their own SCC)
		int unconnectedUsers = 0;
		for (UserNode user : graph.getUsers().values()) {
			if (user.getOutEdges().size() < 1) {
				unconnectedUsers++;
			}
		}
		System.out.println(graph.getTopic() + " has " + unconnectedUsers + " unconnected users "
				+ "out of " + graph.getUsers().size() + " total users" );
		List<Graph> SCCs = graph.getSCCs();
		int biggestSCC = 0;
		System.out.println(graph.getTopic() + " has " + SCCs.size() + " SCCs");
		for (Graph SCC : SCCs) {
			if (((StackExchangeTopicGraph)SCC).getVertices().size() > biggestSCC) {
				biggestSCC = ((StackExchangeTopicGraph)SCC).getVertices().size();
			};
			if (((StackExchangeTopicGraph)SCC).getVertices().size() > 1) {
				
				((StackExchangeTopicGraph)SCC).printStats();
			}
		}
		System.out.println("Biggest SCC has " + biggestSCC + " vertices");
		//*/
		
		/* egonet test
		int center = 1;
		System.out.println("Building egonet for vertex " + center);
		((StackExchangeTopicGraph)graph.getEgonet(center)).printStats();
		//*/
		
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
		
		/*SCCs test
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
