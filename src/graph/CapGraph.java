/** A graph that represents a social network.
 * 
 * Provides methods to extract egonets and
 * discover strongly connected components.
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */
package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class CapGraph implements Graph {
	
	private String name;
	private Map<Integer,Vertex> vertices;
	private Map<Integer,Graph> rootToSCC;
	
	public CapGraph() {
		
		this("Default Graph Name");
	}
	
	public CapGraph(String name) {
		
		this.name = name;
		this.vertices = new HashMap<Integer,Vertex>();
		
		// might be inefficient because list will keep doubling
		this.rootToSCC = new HashMap<Integer,Graph>();
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

	/** Construct the egonet for a particular vertex.
	 * 
	 * An egonet is a subgraph that includes 1) the vertex center c,
	 * 2) all of vertices v that are directly connected by an edge 
	 * from c to v, 3) all of the edges that connect c to each v,
	 * and 4) and all of the edges between each v.
	 * 
	 * The returned graph does not share any objects with the original graph.
	 * 
	 * @param center is the vertex at the center of the egonet
	 * 
	 * @return the egonet centered at center, including center
	 * 
	 * @see graph.Graph#getEgonet(int)
	 */
	@Override
	public Graph getEgonet(int center) {
		
		Graph egonet = new CapGraph("Egonet for vertex " + center + 
									" within " + name); 
		
		Vertex centVertInParent = vertices.get(center);
		List<Vertex> centOutVertsInParent = centVertInParent.getOutEdges();
		
		// add the center to the egonet
		egonet.addVertex(center);
		
		// create map here or we'll have an inner loop iterating over all
		// of center's adjacency list for each of center's out verts
		Set<Vertex> centOutVertsInParentSet = 
				new HashSet<Vertex>(centOutVertsInParent.size()*2,1);
		
		for (Vertex outVertex : centOutVertsInParent) {
			
			centOutVertsInParentSet.add(outVertex);
		}
		
		for (Vertex outVertex : centOutVertsInParent) {
			
			int outVertexID = outVertex.getID();
			
			// add the out vertex and the edge between it and center
			egonet.addVertex(outVertexID);
			egonet.addEdge(center, outVertexID);
			
			List<Vertex> outVertOutVertsInParent = outVertex.getOutEdges();
			
			for (Vertex outVertOutVert : outVertOutVertsInParent) {
				
				// add edges between out verts if center is connected to both
				// need to use parent adjacency set because
				// we created new verts for the egonet
				if (centOutVertsInParentSet.contains(outVertOutVert)) {
					
					egonet.addEdge(outVertexID, outVertOutVert.getID());
				}
			}
		}

		return egonet;
	}

	/** Find all strongly connected components (SCCs) in a directed graph.
	 * 
	 * The returned graph(s) do not share any objects with the original graph.
	 * 
	 * @return a list of subgraphs that comprise the strongly connected components
	 * of this graph.
	 * 
	 * @see graph.Graph#getSCCs()
	 */
	@Override
	public List<Graph> getSCCs() {

		Stack<Integer> vertexIDStack = new Stack<Integer>();
		
		for (int vertexID : vertices.keySet()) {
			
			vertexIDStack.push(vertexID);
		}
		
		Stack<Integer> magicOrder = allDFS(this, vertexIDStack, false);
		CapGraph thisTranspose = getTranspose();
		Stack<Integer> finalOrder = allDFS(thisTranspose, magicOrder, true);
		
		List<Graph> SCCList = new ArrayList<Graph>(rootToSCC.size());
		
		for (Graph SCC : rootToSCC.values()) {
			
			SCCList.add(SCC);
		}
		return SCCList;
	}
	
	public Stack<Integer> allDFS(CapGraph graph, Stack<Integer> verticesToVisit,
								 boolean secondPass) {
		
		Stack<Integer> finished = new Stack<Integer>();
		Set<Integer> visited = new HashSet<Integer>(graph.vertices.size()*2,1);
		
		while (!verticesToVisit.isEmpty()) {
			
			int vertexToVisit = verticesToVisit.pop();
			
			if (!visited.contains(vertexToVisit)) {
				
				// if second pass, this vertex is the root of the SCC
				singleDFS(graph, vertexToVisit, vertexToVisit, 
						  visited, finished, secondPass);
			}
		}

		return finished;
	}
	
	public void singleDFS(CapGraph graph, int vertexID, int root,
						  Set<Integer> visited, Stack<Integer> finished,
						  boolean secondPass) {
		
		visited.add(vertexID);
		CapGraph thisSCC = null;
		
		if (secondPass && vertexID == root) {
			
			thisSCC = new CapGraph("SCC; Parent: " + name + "; "+ 
								   "Root:" + vertexID);
			thisSCC.addVertex(vertexID);
		}
		
		Vertex vertex = graph.vertices.get(vertexID);
		
		
		for (Vertex neighbor : vertex.getOutEdges()) {
			
			int neighborID = neighbor.getID();
			
			if (secondPass) {
			
				thisSCC.addVertex(neighborID);
				thisSCC.addEdge(vertexID, neighborID);
			}
			
			if (!visited.contains(neighborID)) {
				
				singleDFS(graph, neighborID, root, visited, finished, secondPass);
			}
		}
		
		finished.push(vertexID);
		
		if (secondPass && vertexID == root) {
			
			rootToSCC.put(vertexID, thisSCC);
		}
	}
	
	/** Reverse the edges of this graph.
	 * 
	 * Returns a new graph.
	 * 
	 * @param graph the graph to be transposed
	 * @return a new CapGraph with all original graph edges reversed.
	 */
	public CapGraph getTranspose() {
		
		CapGraph transposeGraph = new CapGraph(name + " (Transpose)");
		
		Map<Integer,Vertex> transposeVertices = transposeGraph.getVertices();
		
		for (int vertexID : this.vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			if (!transposeVertices.keySet().contains(vertexID)) {
				
				transposeGraph.addVertex(vertexID);
			}
			
			List<Vertex> oldOutEdges = vertex.getOutEdges();
			
			// adjacency matrix representation may be useful
			// to avoid linear inner loop
			for (Vertex oldOutVert : oldOutEdges) {
				
				int oldOutVertID = oldOutVert.getID();
				
				if (!transposeVertices.keySet().contains(oldOutVertID)) {
					
					transposeGraph.addVertex(oldOutVertID);
				}
				
				transposeGraph.addEdge(oldOutVertID, vertexID);
			}
		}
		
		return transposeGraph;
	}
	
	public Map<Integer,Vertex> getVertices() {
		
		return vertices;
	}

	/** Return version of the map readable by UCSD auto-grader.
	 * 
	 * Returns a HashMap where the keys in the HashMap are all the vertices 
	 * in the graph, and the values are the Set of vertices that are reachable 
	 * from the vertex key via a directed edge. 
	 * 
	 * The returned representation ignores edge weights and multi-edges.
	 * 
	 * @see graph.Graph#exportGraph()
	 */
	@Override
	public HashMap<Integer, HashSet<Integer>> exportGraph() {
		
		HashMap<Integer,HashSet<Integer>> exportedGraph = 
				new HashMap<Integer,HashSet<Integer>>(vertices.size()*2,1);
		
		for (int vertexID : vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			List<Vertex> outVertices = vertex.getOutEdges();
			HashSet<Integer> outVertexIDSet = new HashSet<Integer>(outVertices.size()*2,1);
			
			for (Vertex outVertex : outVertices) {
				
				int outVertexID = outVertex.getID();
				outVertexIDSet.add(outVertexID);
			}
			
			exportedGraph.put(vertexID, outVertexIDSet);
		}
		
		return exportedGraph;
	}

	/** Print a text representation of the graph to default output.
	 * 
	 */
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
