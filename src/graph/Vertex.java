/** A vertex in a graph.
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Vertex {

	/* The project requires egonets/SCCs to be new graphs (no object reuse)
	 * if this were not a requirement, it might be worth, instead of 
	 * declaring a single list of edges, declaring a map like
	 * "HashMap<CapGraph,List<Edge>> parentGraphs" to keep track of 
	 * all of the subgraphs a particular Vertex is a part of and the edges
	 * the Vertex has in each subgraph.
	 * To do this we also need to modify several other methods.
	 * 
	 * Could also change "List<Vertex> outEdges" to 
	 * "List<Map<Vertex,Integer>> outEdges" to store info about edge weights.
	 */
	private int vertexID; // unique ID for this community graph
	private String name;
	
	private List<Integer> outEdges;
	
	// map from hierarchy level to id of community this vertex is a member of
	private Map<Integer,Integer> communityMembership;
	
	public Vertex(int vertexID) {
		
		this(vertexID, Integer.toString(vertexID));
	}
	
	public Vertex(int vertexID, String name) {
		
		this.vertexID = vertexID;
		this.name = name;
		
		this.outEdges = new ArrayList<Integer>();
		this.communityMembership = new HashMap<Integer,Integer>();
	}
	
	/** Create an edge between this vertex and another vertex.
	 * 
	 * @param toVertex the vertex object the edge goes to
	 */
	public void createEdge(Vertex toVertex) {
		
		outEdges.add(toVertex.getVertexID());
	}
	
	/** Makes a copy of this Vertex
	 * 
	 * Creates a new Vertex with all object values that are initially
	 * passed to the Vertex's constructor equal to the same values 
	 * from the Vertex's current state.
	 * 
	 * This means, for example, that the new Vertex will have the same
	 * vertexID as this Vertex because those values are 
	 * passed to the constructor, but not the same list of out edges 
	 * because the list of outEdges is not passed to the constructor.
	 * 
	 * @return a copy of the given Vertex
	 */
	public Vertex makeCopy() {
		
		return new Vertex(vertexID, name);
	}
	
	public int getVertexID() {
		
		return vertexID;
	}
	
	public String getName() {
		
		return name;
	}
	
	public List<Integer> getOutEdges() {
		
		return outEdges;
	}
	
	public void setOutEdges(List<Integer> outEdges) {
		
		this.outEdges = outEdges;
	}
	
	public void setName(String name) {
		
		this.name = name;
	}
	
	public Map<Integer,Integer> getCommunityMembership() {
		
		return communityMembership;
	}
	
	public String toString() {
		
		String returnString = "";
		
		returnString += "Vertex ID: " + vertexID;
		returnString += "\n";
		returnString += "Name: " + name;
		returnString += "\n";
		returnString += "Out edge vertex IDs: ";
		for (int outEdgeVertexID : outEdges) {
			returnString += outEdgeVertexID + ", ";
		}
		returnString += "\n";
		
		return returnString;
	}
}
