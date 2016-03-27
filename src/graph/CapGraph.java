/** A graph that represents a social network.
 * 
 * Provides methods to extract egonets and
 * discover strongly connected components.
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */
package graph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CapGraph implements Graph {
	
	String name;
	Map<Integer,Vertex> vertices;
	
	public CapGraph() {
		
		this("Default Graph Name");
	}
	
	public CapGraph(String name) {
		
		this.name = name;
		this.vertices = new HashMap<Integer,Vertex>();
	}

	/** Add a vertex to the graph.
	 * 
	 * @see graph.Graph#addVertex(int)
	 * 
	 * @param num is the numerical id of the vertex to add
	 */
	@Override 
	public void addVertex(int num) {
		
		Vertex vertex = new Vertex(num);
		vertices.put(num,vertex);
	}

	/** Add a directed edge to the graph.
	 * 
	 * @see graph.Graph#addEdge(int, int)
	 * 
	 * @param from is the id of the edge's starting vertex
	 * @param to is the id of the edge's ending vertex
	 */
	@Override
	public void addEdge(int from, int to) {
		
		Vertex fromVertex = vertices.get(from);
		Vertex toVertex = vertices.get(to);
		
		fromVertex.createEdge(toVertex);
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getEgonet(int)
	 */
	@Override
	public Graph getEgonet(int center) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#getSCCs()
	 */
	@Override
	public List<Graph> getSCCs() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see graph.Graph#exportGraph()
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		// TODO Auto-generated method stub
		return null;
	}

	public void printGraph() {
		
		System.out.println("This is a text representation of the graph " + 
						   name + ":");
		
		for (int vertexID : vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			System.out.print("Vertex ID/Name: " + vertex.getID() + "/" +
							 vertex.getName() + "; adjacency list: ");
			
			for (Vertex toVertex : vertex.getOutEdges()) {
				
				System.out.print(toVertex.getID() + ",");
			}
			
			System.out.println();
		}
	}
}
