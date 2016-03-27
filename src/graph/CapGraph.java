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
	private List<Graph> SCCList;
	
	public CapGraph() {
		
		this("Default Graph Name");
	}
	
	public CapGraph(String name) {
		
		this.name = name;
		this.vertices = new HashMap<Integer,Vertex>();
		// might be inefficient because list will keep doubling
		this.SCCList = new ArrayList<Graph>();
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
			// add the out vertex and the edge between it and center
			egonet.addVertex(outVertex.getID());
			egonet.addEdge(center, outVertex.getID());
		}
		
		for (Vertex outVertex : centOutVertsInParent) {
			
			int outVertexID = outVertex.getID();
			
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
		
		System.out.println("doing first pass");
		Stack<Integer> finishOrder = allDFS(this, vertexIDStack, false);
		System.out.println("doing transpose");
		CapGraph thisTranspose = getTranspose();
		// don't need the finishing order after second pass
		System.out.println("doing second pass");
		allDFS(thisTranspose, finishOrder, true);
		
		return SCCList;
	}
	
	public Stack<Integer> allDFS(CapGraph graph, Stack<Integer> verticesToVisit,
								 boolean secondPass) {
		
		Stack<Integer> finished = new Stack<Integer>();
		Set<Integer> visited = new HashSet<Integer>(graph.vertices.size()*2,1);
		
		while (!verticesToVisit.isEmpty()) {
			
			int vertexToVisit = verticesToVisit.pop();
			
			System.out.println("starting a search from " + vertexToVisit);
			
			if (!visited.contains(vertexToVisit)) {
				
				System.out.println(vertexToVisit + "not visited so exploring");
				CapGraph SCC = null;
				
				if (secondPass) {
					// if second pass, need to create an SCC
					// the SCC will include all vertexes reachable
					// from this vertex
					// TODO: copy other info (e.g. vertex name)
					SCC = new CapGraph("SCC with Parent '" + name + "' and " +
									   "Root " + vertexToVisit);
					SCC.addVertex(vertexToVisit);
					System.out.println("creating an SCC with root " + vertexToVisit);
				}

				singleDFS(graph, vertexToVisit, vertexToVisit, 
						  visited, finished, secondPass, SCC);
				
				if (secondPass) {
					
					SCCList.add(SCC);
				}
			}
		}

		return finished;
	}
	
	public void singleDFS(CapGraph graph, int vertexID, int root,
						  Set<Integer> visited, Stack<Integer> finished,
						  boolean secondPass, CapGraph SCC) {
		
		visited.add(vertexID);
		
		if (secondPass) {
		System.out.println("SCC contains " + SCC.getVertices().keySet() + " vertices");
		}
		
		Vertex vertex = graph.vertices.get(vertexID);
		
		if (secondPass && !SCC.getVertices().keySet().contains(vertexID)) {
			// TODO: copy other info (e.g. vertex name)
			System.out.println("adding " + vertexID + " to current SCC");
			SCC.addVertex(vertexID);
		}
		
		for (Vertex neighbor : vertex.getOutEdges()) {
			
			int neighborID = neighbor.getID();
			
			if (secondPass) {
				// TODO: copy other info (e.g. vertex name, edge weights)
				// if we haven't already visited it and
				// it isn't already in this SCC
				if (!visited.contains(neighborID) &&
					!SCC.getVertices().keySet().contains(neighborID)) {
					
					System.out.println("adding " + neighborID + " to current SCC");
					SCC.addVertex(neighborID);
				}
				
				// if we added the neighbor to the SCC, add the edge
				if (SCC.getVertices().keySet().contains(neighborID)) {
					System.out.println("adding edge from " + vertexID + " to " + neighborID + " to current SCC");
					SCC.addEdge(vertexID, neighborID);
					List<Vertex> thisList = SCC.getVertices().get(vertexID).getOutEdges();
					for (Vertex outNeighbor : thisList) {
						System.out.println(outNeighbor.getID());
					}	
				}
			}
			
			if (!visited.contains(neighborID)) {
				
				System.out.println("continuing DFS with " + neighborID);
				singleDFS(graph, neighborID, root, visited, finished,
						  secondPass, SCC);
			}
		}
		
		System.out.println("finished with " + vertexID);
		finished.push(vertexID);
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
