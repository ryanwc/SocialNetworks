/** A graph that represents a StackExchange topic.
 * 
 * Nodes are questions, answers, comments, and users.
 * Edges connect users to their questions, answers, and comments.
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class StackExchangeTopicGraph implements Graph {
	
	private static final int QUESTION = 1;
	private static final int ANSWER = 2;
	private static final int COMMENT = 3;
	private static final int USER = 4;

	private String topic;
	private Map<Integer,Vertex> vertices;
	private List<Graph> SCCList;
	
	// question: should these map from vertexID to specific node type,
	// or from specific node type ID to node type?
	// right now it's the latter so children can access parents from DOM data
	private Map<Integer,QuestionNode> questions;
	private Map<Integer,AnswerNode> answers;
	private Map<Integer,CommentNode> comments;
	private Map<Integer,UserNode> users;
	
	private Map<Integer,Tag> tags;
	
	// to enforce unique vertex IDs
	// e.g., for deleted and added vertices
	// ensuring unique IDs is kind of fragile (see dummy vertex methods)
	private int uniqueVertexIDCounter = 0;
	
	public StackExchangeTopicGraph() {
		
		this("Default Topic Name");
	}
	
	public StackExchangeTopicGraph(String topic) {
		
		this.topic = topic;
		this.vertices = new HashMap<Integer,Vertex>();
		// might be inefficient because list will keep doubling
		this.SCCList = new ArrayList<Graph>();
	}
	
	/** Add a vertex with dummy info to the graph.
	 * 
	 * This method will add a question, answer, comment, or user
	 * vertex to the graph.  However, there will be no useful information
	 * in the vertex, and the programmer must supply info elsewhere 
	 * via the vertex's setters.
	 * 
	 * To populate the graph with a question, answer, comment, or user
	 * vertex that has useful information, use the method
	 * addVertex(int vertexID, Node vertexType), which will use the
	 * given Node's data to populate the vertex's fields.
	 * 
	 * @see graph.Graph#addVertex(int)
	 * 
	 * @param num is the numerical id of the vertex to add
	 * @param vertexType is the type of vertex to add
	 */
	public void addVertex(int vertexID, int vertexType) {
		
		if (vertexType != QUESTION || vertexType != ANSWER || 
			vertexType != COMMENT || vertexType != USER) {
			
			throw new IllegalArgumentException("Nodes in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user");
		}
		
		if (vertexID == USER) {
			
			addDummyUser(vertexID);
		}
		else {
			
			// it's probably bad to add lots of dummy posts,
			// especially if they are comments or answers because these will
			// also create a corresponding parent dummy question)
			
			if (vertexType == QUESTION) {
				
				addDummyQuestion(vertexID);
			}
			if (vertexType == ANSWER) {
				
				addDummyAnswer(vertexID);
			}
			else {
			
				addDummyComment(vertexID);
			}
		}
	}
	
	/** Add a vertex to the graph.
	 * 
	 * Adds a question, answer, comment, or user to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the vertex's data
	 * @param vertexType indicates the type of the vertex (question, answer,
	 * comment, or user)
	 */
	public void addVertex(int vertexID, Node node, int vertexType) {
		
		if (vertexType == QUESTION) {
			
			QuestionNode question = createQuestionFromDOMNode(vertexID, node);
			addQuestionToGraph(question);
		}
		else if (vertexType == ANSWER) {
			
			AnswerNode answer = createAnswerFromDOMNode(vertexID, node);
			addAnswerToGraph(answer);
		}
		else if (vertexType == COMMENT) {
			
			CommentNode comment = createCommentFromDOMNode(vertexID, node);
			addCommentToGraph(comment);
		}
		else if (vertexType == USER) {
		
			UserNode user = createUserFromDOMNode(vertexID, node);
			addUserToGraph(user);
		}
		else {
			
			throw new IllegalArgumentException("Nodes in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user");
		}
	}
	
	/** Create a QuestionNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange question per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the question's data
	 */
	public QuestionNode createQuestionFromDOMNode(int vertexID, Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("CommentCount") == null ||
			Integer.parseInt(nodeAttributes.
					getNamedItem("PostTypeId").getNodeValue()) != 1) {
				throw new IllegalArgumentException("Given node does not represent "
						+ "a question.");
		}
		
		int postID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int rawScore = Integer.parseInt(nodeAttributes.
				getNamedItem("Score").getNodeValue());
		// does not strip rendered HTML from body
		String body = nodeAttributes.getNamedItem("Body").getNodeValue();
		int authorUserID = Integer.parseInt(nodeAttributes.
				getNamedItem("OwnerUserId").getNodeValue());
		int commentCount = Integer.parseInt(nodeAttributes.
				getNamedItem("CommentCount").getNodeValue());
		int viewCount = Integer.parseInt(nodeAttributes.
				getNamedItem("ViewCount").getNodeValue());
		// need to check null on acceptedAnserID?
		Integer acceptedAnswerID = Integer.parseInt(nodeAttributes.
				getNamedItem("AcceptedAnswerId").getNodeValue());
		String title = nodeAttributes.getNamedItem("Title").getNodeValue();
		int answerCount = Integer.parseInt(nodeAttributes.
				getNamedItem("AnswerCount").getNodeValue());
		int favoriteCount = Integer.parseInt(nodeAttributes.
				getNamedItem("FavoriteCount").getNodeValue());
		String tagsString = nodeAttributes.getNamedItem("Tags").getNodeValue();
		
		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		List<String> tags = parseRawTags(tagsString);
		
		// name is possibly not needed
		String name = "Question " + questions.size();
		
		QuestionNode question = new QuestionNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, commentCount, viewCount, 
				acceptedAnswerID, title, tags, answerCount, favoriteCount);
		
		return question;
	}
	
	/** Add a question to the graph.
	 * 
	 * Adds a question as a vertex to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * @param question is the QuestionNode to add to the graph
	 */
	public void addQuestionToGraph(QuestionNode question) {
		
		vertices.put(question.getVertexID(), question);
		questions.put(question.getPostID(), question);
		
		uniqueVertexIDCounter++;
	}
	
	/** Create an AnswerNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange answer per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the answer's data
	 */
	public AnswerNode createAnswerFromDOMNode(int vertexID, Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("ParentId") == null &&
				nodeAttributes.getNamedItem("CommentCount") == null) {
				throw new IllegalArgumentException("Given node does not represent "
						+ "an answer.");
		}
		
		int postID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int rawScore = Integer.parseInt(nodeAttributes.
				getNamedItem("Score").getNodeValue());
		// does not strip rendered HTML from body
		String body = nodeAttributes.getNamedItem("Body").getNodeValue();
		int authorUserID = Integer.parseInt(nodeAttributes.
				getNamedItem("OwnerUserId").getNodeValue());
		int commentCount = Integer.parseInt(nodeAttributes.
				getNamedItem("CommentCount").getNodeValue());
		int parentQuestionID = Integer.parseInt(nodeAttributes.
				getNamedItem("ParentId").getNodeValue());
		
		// not absolutely necessary but a nice way to ensure
		// the answer has a parent
		if (!questions.containsKey(parentQuestionID)) {
			throw new IllegalArgumentException("Parent question node must "
					+ "be added to the graph before child answer node.");
		}

		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		QuestionNode parentQuestion = questions.get(parentQuestionID);
		
		// name is possibly not needed
		String name = "Answer " + answers.size();
		
		AnswerNode answer = new AnswerNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, commentCount, 
				parentQuestion.getPostID(),parentQuestion.getViewCount());
		
		return answer;
	}
	
	/** Add an AnswerNode to the graph.
	 * 
	 * @param answer is the AnswerNode to add to the graph
	 */
	public void addAnswerToGraph(AnswerNode answer) {
		
		vertices.put(answer.getVertexID(), answer);
		answers.put(answer.getPostID(), answer);
		
		uniqueVertexIDCounter++;
	}
	
	/** Create a CommentNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange comment per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the comments's data
	 */
	public CommentNode createCommentFromDOMNode(int vertexID, Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("Text") == null &&
			nodeAttributes.getNamedItem("Score") == null) {
			throw new IllegalArgumentException("Given node does not represent "
					+ "a comment");
		}
		
		int postID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int rawScore = Integer.parseInt(nodeAttributes.
				getNamedItem("Score").getNodeValue());
		// does not strip rendered HTML from body
		String body = nodeAttributes.getNamedItem("Text").getNodeValue();
		int authorUserID = Integer.parseInt(nodeAttributes.
				getNamedItem("UserId").getNodeValue());
		int parentPostID = Integer.parseInt(nodeAttributes.
				getNamedItem("PostId").getNodeValue());
		
		// not absolutely necessary but a nice way
		// to ensure the comment has a parent
		if (!answers.containsKey(parentPostID) &&
			!questions.containsKey(parentPostID)) {
			throw new IllegalArgumentException("Parent post node must "
					+ "be added to the graph before child comment node.");
		}
		
		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		Post parentPost;
		
		if (answers.containsKey(parentPostID)) {
			parentPost = answers.get(parentPostID);
		}
		else {
			parentPost = questions.get(parentPostID);
		}
		
		// name is possibly not needed
		String name = "Comment " + comments.size();
		
		CommentNode comment = new CommentNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, parentPost.getPostID(), 
				parentPost.getViewCount());
		
		return comment;
	}
	
	/** Add a CommentNode as a vertex to the graph.
	 * 
	 * @param CommentNode is the node to add to the graph
	 */
	public void addCommentToGraph(CommentNode comment) {
		
		vertices.put(comment.getVertexID(), comment);
		comments.put(comment.getPostID(), comment);
		
		uniqueVertexIDCounter++;
	}
	
	/** Create a UserNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange user per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the user's data
	 */
	public UserNode createUserFromDOMNode(int vertexID, Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("Age") == null) {
			throw new IllegalArgumentException("Given node does not represent "
					+ "a user");
		}
		
		String name = nodeAttributes.getNamedItem("DisplayName").
				getNodeValue();
		int userID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int reputation = Integer.parseInt(nodeAttributes.
				getNamedItem("Reputation").getNodeValue());
		int age = Integer.parseInt(nodeAttributes.
				getNamedItem("Age").getNodeValue());
		int upVotes = Integer.parseInt(nodeAttributes.
				getNamedItem("UpVotes").getNodeValue());
		int downVotes = Integer.parseInt(nodeAttributes.
				getNamedItem("DownVotes").getNodeValue());
		int accountID = Integer.parseInt(nodeAttributes.
				getNamedItem("AccountId").getNodeValue());
		
		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		UserNode user = new UserNode(vertexID, name, userID, reputation, 
									 age, upVotes, downVotes, accountID);
		
		return user;
	}
	
	/** Add a UserNode as a vertex to the graph.
	 * 
	 * @param the UserNode to add to the graph
	 */
	public void addUserToGraph(UserNode user) {
		
		vertices.put(user.getVertexID(), user);
		users.put(user.getUserID(), user);
		
		uniqueVertexIDCounter++;
	}
	
	/** Create a Tag object with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange tag per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param node contains the tag's data
	 */
	public Tag createTagFromDOMNode(Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("TagName") == null) {
			throw new IllegalArgumentException("Given node does not "
					+ "represent a tag");
		}
		
		int tagID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		String tagName = nodeAttributes.getNamedItem("TagName").getNodeValue();
		int tagCount = Integer.parseInt(nodeAttributes.
				getNamedItem("Count").getNodeValue());
		
		Tag tag = new Tag(topic, tagID, tagName, tagCount);
		
		return tag;
	}
	
	/** Add a Tag object to the graph metadata.
	 * 
	 * @param the Tag object to add to the graph
	 */
	public void addTagToGraph(Tag tag) {
		
		tags.put(tag.getTagID(), tag);
	}
	
	/** Add a directed edge to the graph.
	 * 
	 * NOTE: An undirected edge is represented by two directed edges.
	 * 
	 * @see graph.Graph#addEdge(int, int)
	 * 
	 * @param fromVertexID is the id of the vertex at the start of the edge
	 * @param toVertexID is the id of the vertex at the end of the edge
	 */
	public void addEdge(int fromVertexID, int toVertexID) {
		
		Vertex fromVertex = vertices.get(fromVertexID);
		Vertex toVertex = vertices.get(fromVertexID);
		
		fromVertex.createEdge(toVertex);
		
		// if needed, add the toVertex to the
		// appropriate list for fromVertex's type
		if (fromVertex instanceof UserNode) {
			if (toVertex instanceof QuestionNode) {
				((UserNode)fromVertex).getQuestions().add((QuestionNode)toVertex);
			}
			else if (toVertex instanceof AnswerNode) {
				((UserNode)fromVertex).getAnswers().add((AnswerNode)toVertex);
			}
			else if (toVertex instanceof CommentNode) {
				((UserNode)fromVertex).getComments().add((CommentNode)toVertex);
			}
		}
		else if (fromVertex instanceof QuestionNode) {
			if (toVertex instanceof AnswerNode) {
				((QuestionNode)fromVertex).getAnswers().add((AnswerNode)toVertex);
			}
			else if (toVertex instanceof CommentNode) {
				((QuestionNode)fromVertex).getComments().add((CommentNode)toVertex);
			}
		}
		else if (fromVertex instanceof AnswerNode) {
			if (toVertex instanceof CommentNode) {
				((AnswerNode)fromVertex).getComments().add((CommentNode)toVertex);
			}
		}
		// no extra work to do if fromVertex is a CommentNode
	}
	
	/** Adds all edges to the graph.
	 * 
	 * Should only be used if no edges have been added to the graph.
	 */
	public void addAllEdges() {
		
		// QuestionNodes only know about their author (user)
		for (QuestionNode question : questions.values()) {
			
			UserNode author = users.get(question.getAuthorUserID());
			
			addEdge(question.getVertexID(), author.getVertexID());
			addEdge(author.getVertexID(), question.getVertexID());
		}
		
		// AnswerNodes know about their author (user) and parent question
		for (AnswerNode answer : answers.values()) {
			
			UserNode author = users.get(answer.getAuthorUserID());
			QuestionNode question = questions.get(answer.getParentQuestionPostID());
			
			addEdge(answer.getVertexID(), author.getVertexID());
			addEdge(author.getVertexID(), answer.getVertexID());
			
			addEdge(answer.getVertexID(), question.getVertexID());
			addEdge(question.getVertexID(), answer.getVertexID());
		}
		
		// CommentNodes know about author (user) and parent question or answer
		for (CommentNode comment : comments.values()) {
			
			UserNode author = users.get(comment.getAuthorUserID());
			Post parentPost;
			
			if (answers.keySet().contains(comment.getParentPostID())) {
				parentPost = answers.get(comment.getParentPostID());
			}
			else {
				parentPost = questions.get(comment.getParentPostID());
			}
			
			addEdge(comment.getVertexID(), author.getVertexID());
			addEdge(author.getVertexID(), comment.getVertexID());
			
			addEdge(comment.getVertexID(), parentPost.getVertexID());
			addEdge(parentPost.getVertexID(), comment.getVertexID());;
		}
		
		// UserNodes don't know anything about other nodes
		// All edges from UserNodes to other vertices are handled above
	}
	
	/** Add a user with dummy data to the graph.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 */
	private UserNode addDummyUser(int vertexID) {
		
		// does not quite ensure unique questionID
		int userID = -(users.size()-1);
		String name = "Default User";
		int reputation = 0;
		int age = 0;
		int upvotes = 0;
		int downvotes = 0;
		int accountID = -2;
		
		UserNode user = new UserNode(vertexID, name, userID,reputation, 
				age, upvotes, downvotes, accountID);
		
		vertices.put(user.getVertexID(), user);
		users.put(user.getUserID(), user);
		
		uniqueVertexIDCounter++;
		
		return user;
	}
	
	/** Add a question with dummy data to the graph.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 */
	private QuestionNode addDummyQuestion(int vertexID) {
		
		// does not quite ensure unique questionID
		int postID = -(questions.size()-1);
		String name = "Default Question";
		int rawScore = 0;
		String body = "";
		int authorID = -2;
		int commentCount = 0;
		int viewCount = 0;
		Integer acceptedAnswerID = null;
		String title = "";
		List<String> tags = new ArrayList<String>(0);
		int answerCount = 0;
		int favoriteCount = 0;
		
		QuestionNode question = new QuestionNode(vertexID, name, topic,
				postID, rawScore, body, authorID, commentCount, viewCount, 
				acceptedAnswerID, title, tags, answerCount, favoriteCount);
		
		vertices.put(question.getVertexID(), question);
		questions.put(question.getPostID(), question);
		
		uniqueVertexIDCounter++;
		
		return question;
	}
	
	/** Add an answer with dummy data to the graph.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 */
	private void addDummyAnswer(int vertexID) {
		
		QuestionNode parentQuestion = addDummyQuestion(vertexID);
		
		// does not quite ensure unique answerID
		int postID = -(answers.size()-1);
		String name = "Default Answer";
		int rawScore = 0;
		String body = "";
		int authorID = -2;
		int commentCount = 0;
		
		AnswerNode answer = new AnswerNode(uniqueVertexIDCounter, name, topic,
				postID, rawScore, body, authorID, commentCount, 
				parentQuestion.getPostID(), parentQuestion.getViewCount());
		
		vertices.put(answer.getVertexID(), answer);
		answers.put(answer.getPostID(), answer);
		
		uniqueVertexIDCounter++;
	}
	
	/** Add a comment with dummy data to the graph.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 */
	private void addDummyComment(int vertexID) {
		
		QuestionNode parentPost = addDummyQuestion(vertexID);
		
		// does not quite ensure unique answerID
		int commentID = -(comments.size()-1);
		String name = "Default Comment";
		int rawScore = 0;
		String body = "";
		int authorID = -2;
		
		CommentNode comment = new CommentNode(uniqueVertexIDCounter, name, topic,
				commentID, rawScore, body, authorID, 
				parentPost.getPostID(), parentPost.getViewCount());
		
		vertices.put(comment.getVertexID(), comment);
		comments.put(comment.getPostID(), comment);
		
		uniqueVertexIDCounter++;
	}
	
	/** Creates a list of tags from raw tag data in a question DOM node.
	 * 
	 * @param tagsString is the raw tags String from the DOM
	 */
	public List<String> parseRawTags(String tagsString) {
		
		List<String> tags = new ArrayList<String>(4);
		
		// each tag is between '&lt;' and '&gt;' get each one and add to list
		// assumes tagsString either begins with '&lt;' and ends with '&gt;'
		// or is empty
		for (int i = 0; i < tagsString.length()-3; i++) {
			
			if (tagsString.substring(i,i+4).equals("&lt;")) {
				int j = i+4;
				while (!tagsString.substring(j,j+4).equals("&gt;")) {
					j++;
				}
				tags.add(tagsString.substring(i+4,j));
			}
		}
		
		return tags;
	}
	
	/** Find all strongly connected components (SCCs) this graph.
	 * 
	 * This method will work for directed graphs and undirected graphs.
	 * A method that finds SCCs for just undirected graphs would be much simpler,
	 * but using this method ensures ability to use directed edges
	 * in a StackExchangeTopicGraph if needed.
	 * 
	 * The returned graph(s) does not share any objects with the original graph.
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
		
		Stack<Integer> finishOrder = allDFS(this, vertexIDStack, false);
		StackExchangeTopicGraph thisTranspose = getTranspose();
		// don't need the finishing order after second pass
		allDFS(thisTranspose, finishOrder, true);
		
		return SCCList;
	}
	
	
	/** Use depth-first search (DFS) to discover all vertices and all strongly
	 * connected components (SCCs) in a StackExchange topic.
	 * 
	 * Returns vertices in a stack with later finishing DFS times on top to
	 * first finishing DFS times on bottom.  A vertex is "finished" with 
	 * DFS when DFS has discovered everything there is to discover from that
	 * vertex.
	 * 
	 * @param graph is the graph in which to do DFS and uncover SCCs.  If
	 *   secondPass is true, this should be a transpose of the original graph.
	 * @param verticesToVisit is the (possibly ordered) list of all vertices to
	 *   visit.  If secondPass is true, the stack should be ordered as if it were
	 *   given by the return value of this method on the first pass.
	 * @param secondPass is a boolean that indicates whether the graph is the
	 *   transpose of the graph in which we want to discover SCCs and whether
	 *   verticesToVisit is ordered according to the ordering mentioned above.
	 * @return 
	 */
	public Stack<Integer> allDFS(StackExchangeTopicGraph graph, 
								 Stack<Integer> verticesToVisit,
								 boolean secondPass) {
		
		Stack<Integer> finished = new Stack<Integer>();
		Set<Integer> visited = new HashSet<Integer>(graph.vertices.size()*2,1);
		
		while (!verticesToVisit.isEmpty()) {
			
			int vertexToVisitID = verticesToVisit.pop();
			
			if (!visited.contains(vertexToVisitID)) {

				StackExchangeTopicGraph SCC = null;
				
				if (secondPass) {
					// if second pass, need to create the SCC (a subgraph)
					SCC = new StackExchangeTopicGraph("SCC with Parent '" + 
								topic + "' and " + "Root " + vertexToVisitID);
					
					addVertexToSCC(graph, vertexToVisitID, SCC);
				}

				singleDFS(graph, vertexToVisitID, vertexToVisitID, 
						  visited, finished, secondPass, SCC);
				
				if (secondPass) {
					// at this point, all vertices are added to the SCC
					// and in their rightful maps within the SCC
					// time to add edges!
					SCC.addAllEdges();
					SCCList.add(SCC);
				}
			}
		}

		return finished;
	}
	
	/** Add a vertex in a graph to an SCC of that graph.
	 * 
	 * Helper method to ensure a graph does not share any objects
	 * with its SCCs while keeping all object IDs in the SCC equal to
	 * the corresponding object IDs in the graph.
	 * 
	 * @param graph is the StackExchangeTopicGraph that contains the vertex
	 * and SCC
	 * @param vertexToAddID is the int ID of the vertex to add to the SCC
	 * @param SCC is the StackExchangeTopicGraph that represents a graph SCC
	 */
	private void addVertexToSCC(StackExchangeTopicGraph graph, 
								int vertexToAddID, 
								StackExchangeTopicGraph SCC) {
		
		Vertex vertexSuper = graph.vertices.get(vertexToAddID);
		Vertex vertexSCC = makeCopy(vertexSuper);
		SCC.putVertexInCorrectMap(vertexSCC);
		SCC.vertices.put(vertexSCC.getVertexID(), vertexSCC);
	}
	
	/**	Add a vertex in a graph to the correct vertex type map.
	 * 
	 * @param vertex is the vertex to add to the correct map
	 * @param graph is the graph in which to add the vertex to the
	 * correct map
	 */
	private void putVertexInCorrectMap(Vertex vertex) {
		
		if (vertex instanceof QuestionNode) {
			
			questions.put(((QuestionNode)vertex).getPostID(),
					(QuestionNode)vertex);
		}
		else if (vertex instanceof AnswerNode) {
			
			answers.put(((AnswerNode)vertex).getPostID(),
					(AnswerNode)vertex);
		}
		else if (vertex instanceof CommentNode) {
			
			comments.put(((CommentNode)vertex).getPostID(),
					(CommentNode)vertex);
		}
		else {
			
			users.put(((UserNode)vertex).getUserID(),
					(UserNode)vertex);
		}
	}
	
	//TODO: Put a makeCopy method in each vertex type instead of here
	/** Makes a copy of a Vertex
	 * 
	 * Creates a new Vertex with all object values that are initially
	 * passed to the Vertex's constructor equal to the same values 
	 * from the Vertex's current state.
	 * 
	 * This means, for example, that the new Vertex will have the same
	 * vertexID and userID as the given Vertex because those values are 
	 * passed to the constructor, but not the same list of out edges 
	 * because the list of outEdges is not passed to the constructor.
	 * 
	 * @param vertex
	 * @return a copy of the given Vertex
	 */
	public Vertex makeCopy(Vertex vertex) {
		
		Vertex vertexCopy;
		
		if (vertex instanceof UserNode) {
			
			vertexCopy = new UserNode(vertex.getVertexID(), vertex.getName(),
					((UserNode)vertex).getUserID(), 
					((UserNode)vertex).getReputation(),
					((UserNode)vertex).getAge(),
					((UserNode)vertex).getUpvotes(), 
					((UserNode)vertex).getDownvotes(),
					((UserNode)vertex).getAccountID());
		}
		else if (vertex instanceof QuestionNode) {
			
			vertexCopy = new QuestionNode(vertex.getVertexID(),
					vertex.getName(),
					((QuestionNode)vertex).getTopic(),
					((QuestionNode)vertex).getPostID(),
					((QuestionNode)vertex).getRawScore(),
					((QuestionNode)vertex).getBody(),
					((QuestionNode)vertex).getAuthorUserID(),
					((QuestionNode)vertex).getComments().size(),
					((QuestionNode)vertex).getViewCount(),
					((QuestionNode)vertex).getAcceptedAnswerId(),
					((QuestionNode)vertex).getTitle(), 
					((QuestionNode)vertex).getTags(),
					((QuestionNode)vertex).getAnswers().size(), 
					((QuestionNode)vertex).getFavoriteCount());
		}
		else if (vertex instanceof AnswerNode) {
			
			vertexCopy = new AnswerNode(vertex.getVertexID(), vertex.getName(),
					((AnswerNode)vertex).getTopic(), 
					((AnswerNode)vertex).getPostID(),
					((AnswerNode)vertex).getRawScore(),
					((AnswerNode)vertex).getBody(),
					((AnswerNode)vertex).getAuthorUserID(),
					((AnswerNode)vertex).getComments().size(),
					((AnswerNode)vertex).getParentQuestionPostID(),
					((AnswerNode)vertex).getViewCount());
		}
		else if (vertex instanceof CommentNode) {
			vertexCopy = new CommentNode(vertex.getVertexID(), vertex.getName(),
					((CommentNode)vertex).getTopic(),
					((CommentNode)vertex).getPostID(),
					((CommentNode)vertex).getRawScore(),
					((CommentNode)vertex).getBody(),
					((CommentNode)vertex).getAuthorUserID(),
					((CommentNode)vertex).getParentPostID(),
					((CommentNode)vertex).getViewCount());
		}
		else {
			throw new IllegalArgumentException("Vertex must be a UserNode, "
					+ "QuestionNode, AnswerNode, or CommentNode");
		}
		
		return vertexCopy;
	}
	
	/** Do a depth-first search as a helper method for discovering SCCs.
	 * 
	 * Do a DFS in a directed graph from a particular vertex as part of 
	 * computing either the "finishing order" of all vertices in the graph 
	 * or assigning vertices to SCCs.
	 * 
	 * If second pass is false, this method is useful because it populates
	 * the "finished" stack passed to it, which gives the ordering that 
	 * should be used with the given graph's transpose to discover SCCs.
	 * 
	 * If secondPass is true, this method will populate the graph's
	 * SCC list.
	 * 
	 * @param graph is the graph in which to do the DFS.
	 * @param vertexID is the vertex from which to do the DFS.
	 * @param root is the root of the current SCC.
	 * @param visited is the set of vertices that have been discovered
	 *   by DFS so far.
	 * @param finished is the list of vertices from which DFS has already
	 *   discovered all vertices there are to discover.
	 * @param secondPass is a boolean that indicates whether the correct
	 *   finishing order has already been calculated and whether the SCC
	 *   list should be populated.
	 * @param SCC is the current SCC (this should be be null if secondPass
	 *   is false).
	 */
	public void singleDFS(StackExchangeTopicGraph graph, int vertexID, 
						  int root, Set<Integer> visited,
						  Stack<Integer> finished, boolean secondPass,
						  StackExchangeTopicGraph SCC) {

		visited.add(vertexID);
		
		Vertex vertex = graph.vertices.get(vertexID);
		
		if (secondPass && !SCC.getVertices().keySet().contains(vertexID)) {

			Vertex vertexCopy = makeCopy(vertex);
			SCC.putVertexInCorrectMap(vertexCopy);
			SCC.vertices.put(vertexCopy.getVertexID(),vertexCopy);
		}
		
		for (Integer neighborID : vertex.getOutEdges()) {
			
			Vertex neighbor = vertices.get(neighborID);
			
			if (secondPass) {
				// if we haven't already visited it and
				// it isn't already in this SCC
				if (!visited.contains(neighborID) &&
					!SCC.getVertices().keySet().contains(neighborID)) {

					Vertex neighborCopy = makeCopy(neighbor);
					SCC.putVertexInCorrectMap(neighborCopy);
					SCC.vertices.put(neighborCopy.getVertexID(),neighborCopy);
				}
				
				// if we added the neighbor to the SCC, add the edge
				if (SCC.getVertices().keySet().contains(neighborID)) {
					SCC.addEdge(vertexID, neighborID);
				}
			}
			
			if (!visited.contains(neighborID)) {
				
				singleDFS(graph, neighborID, root, visited, finished,
						  secondPass, SCC);
			}
		}
		
		finished.push(vertexID);
	}
	
	/** Reverse the edges of this graph.
	 * 
	 * Returns a new graph.  The new graph is identical to the old graph
	 * if the graph is undirected.
	 * 
	 * @param graph the graph to be transposed
	 * @return a new StackExchangeTopicGraph with all original 
	 * graph edges reversed.
	 */
	public StackExchangeTopicGraph getTranspose() {
		
		StackExchangeTopicGraph transposeGraph = 
				new StackExchangeTopicGraph(topic + " (Transpose)");
		
		Map<Integer,Vertex> transposeVertices = transposeGraph.getVertices();
		
		for (int vertexID : this.vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			if (!transposeVertices.keySet().contains(vertexID)) {
				
				Vertex vertexCopy = makeCopy(vertex);
				transposeGraph.putVertexInCorrectMap(vertexCopy);
				transposeGraph.vertices.put(vertexCopy.getVertexID(),
						vertexCopy);
			}
			
			List<Integer> oldOutEdges = vertex.getOutEdges();
			
			// adjacency matrix representation may be useful
			// to avoid linear inner loop
			for (Integer oldOutVertID : oldOutEdges) {
				
				Vertex oldOutVert = vertices.get(oldOutVertID);
				
				if (!transposeVertices.keySet().contains(oldOutVertID)) {
					
					Vertex oldOutVertCopy = makeCopy(oldOutVert);
					transposeGraph.putVertexInCorrectMap(oldOutVertCopy);
					transposeGraph.vertices.put(oldOutVertCopy.getVertexID(),
							oldOutVertCopy);
				}
			}
		}
		
		transposeGraph.addAllEdges();
		
		return transposeGraph;
	}
	
	/** Construct the egonet for a particular vertex.
	 * 
	 * Typically, an egonet is a subgraph that includes 1) the vertex 
	 * center c, 2) all of vertices v that are directly connected by an edge 
	 * from c to v, 3) all of the edges that connect c to each v,
	 * and 4) and all of the edges between each v.
	 * 
	 * However, for a StackExchangeTopicGraph, an egonet is a subgraph that
	 * includes: 1) the vertex center c, which must be a user (see next
	 * paragraph), 2) every vertex that can be reached by traveling from c 
	 * to another user vertex u, inclusive of u, 3) every edge on every path
	 * from c to each u, 4) every vertex that can be reached by traveling from
	 * each u to each other u, and 5) every edge on every path from each u to
	 * each other u.
	 * 
	 * If the given center c is not a user vertex, this method will find, in
	 * constant time, the (only) user vertex u directly connected to c, 
	 * then construct the egonet for u.
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
		
		StackExchangeTopicGraph egonet = 
				new StackExchangeTopicGraph("Egonet for vertex " + center + 
						" within " + topic); 
		
		Vertex cVertParentGraph = vertices.get(center);
		
		// question: should egonet be different for a post?
		// maybe it should include its author but not necessarily
		// all the other posts the author made?
		// as written, (egonet of post) == (egonet of post's author)
		if (!(cVertParentGraph instanceof UserNode)) {
			cVertParentGraph = vertices.get(((Post)cVertParentGraph).
					getAuthorUserID());
		}
		
		// add the center to the egonet
		Vertex cVertParentGraphCopy = makeCopy(cVertParentGraph);
		egonet.getVertices().put(cVertParentGraphCopy.getVertexID(),
				cVertParentGraphCopy);
		egonet.putVertexInCorrectMap(cVertParentGraphCopy);
		
		// populate egonet with vertices and edges up to
		// (and including) one user away from center
		egonet.DFSEgoNet(this, egonet, cVertParentGraph.getVertexID(), null);
		
		Set<Integer> vertIDsFoundByOtherUsers = new HashSet<Integer>();
		
		// add vertices and edges directly linking other users
		for (int otherUserVertexID : egonet.getUsers().keySet()) {
			
			egonet.DFSEgoNet(this, egonet, otherUserVertexID,
							 vertIDsFoundByOtherUsers);
		}

		return egonet;
	}
	
	/** Do DFS from a vertex to populate an egonet.
	 * 
	 * Works by finding all vertices and edges 1 degree of user
	 * separation away from all users one degree of user separation away
	 * from the center user, then adds all vertices and edges found by at
	 * least two user vertices during this exploration.
	 * 
	 * Pass a null value for the argument foundByOtherUser to indicate this
	 * call starts at the center of the egonet.
	 * 
	 * @param parent the StackExchangeTopicGraph that contains the egonet
	 * @param egonet the StackExchangeTopicGraph that represents the egonet
	 * @param vertexID the vertex from which to do DFS
	 * @param foundByOtherUser is the set of vertex IDs found by a call to
	 * DFSEgoNet starting at a user that is not the center of the ego net.
	 * Should be null if this call starts at the center of the egonet.
	 */
	public void DFSEgoNet(StackExchangeTopicGraph parent, 
						  StackExchangeTopicGraph egonet,
						  int vertexID, Set<Integer> foundByOtherUser) {
		
		Vertex vertex = parent.getVertices().get(vertexID);
		List<Integer> outVertexIDs = vertex.getOutEdges();
		
		for (Integer outVertexID : outVertexIDs) {
			
			Vertex outVertex = parent.getVertices().get(outVertexID);
			Vertex outVertexCopy = makeCopy(outVertex);;
			
			// if we started from center, it's OK to immediately add
			// the edges
			// candidate for redesign: separate method for discovering
			// vertices and adding edges. could be cleaner
			if (foundByOtherUser == null) {
				
				egonet.addEdge(vertexID, outVertexCopy.getVertexID());
				egonet.addEdge(outVertexCopy.getVertexID(), vertexID);
			}

			// if this vertex is not already in the egonet
			if (!egonet.getVertices().containsKey(outVertexCopy.getVertexID())) {
					
				// if we started from the center
				if (foundByOtherUser == null) {
				
					egonet.getVertices().put(outVertexCopy.getVertexID(),
						outVertexCopy);
					egonet.putVertexInCorrectMap(outVertexCopy);
				}
				else {
					// if we did not start from center, and if this 
					// vertex is not a user, we might want to add it 
					// to the egonet
					if (!(outVertexCopy instanceof UserNode)) {
						
						// if it was NOT already found by another user
						// mark it as found
						if (!foundByOtherUser.contains(outVertexCopy.getVertexID())) {
							
							foundByOtherUser.add(outVertexCopy.getVertexID());
						}
						else {
							// if it WAS already found by another user, that
							// means there is a direct link between it and another 
							// user directly connected to center user, so add it
							// and its edges to the ego net
							egonet.getVertices().put(outVertexCopy.getVertexID(),
									outVertexCopy);
							egonet.putVertexInCorrectMap(outVertexCopy);
							egonet.addEdge(vertexID, outVertexCopy.getVertexID());
							egonet.addEdge(outVertexCopy.getVertexID(), vertexID);
							
							// still need to connect other users to their immediate
						}
					}
				}
				
				// do another DFS is not a user
				if (!(outVertex instanceof UserNode)) {
					
					DFSEgoNet(parent, egonet, outVertexID, foundByOtherUser);	
				}	
			}
			else if (outVertexCopy instanceof UserNode &&
					 foundByOtherUser != null &&
					 egonet.getVertices().containsKey(vertexID)) {
				
				// at this point everything is in the egonet except for edges 
				// between not-center users and their egonet posts.
				// this is one of those users' posts. so, add the edges
				egonet.addEdge(vertexID, outVertex.getVertexID());
				egonet.addEdge(outVertex.getVertexID(), vertexID);
			}
		}
	}
	
	/** Detect communities in the graph.
	 * 
	 */
	public void detectCommunities() {
	/*
		- Compute “betweenness” of all edges (i.e., calculate shortest path between every pair of vertices and count how many times each edge appears in a path)
			- for each node v (O(v)) (linear at this point)
			- bfs of graph starting at v (O(|V|+|E|)) (quadratic at this point)
			- compute # of shortest paths from v to each other node
			- distribute flow to edges along these paths (increment counter for each edge in each shortest path?)
		- Remove edge(s) of highest betweenness
		- Repeat with graph subsections until there are no more edges, or until have separated graph into desired number of components (O(|E|)) (cubic at this point)
	*/
	}
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public Map<Integer,Tag> getTags() {
		return tags;
	}
	
	public void setTags(Map<Integer,Tag> tags) {
		this.tags = tags;
	}
	
	public Map<Integer,Vertex> getVertices() {
		return vertices;
	}
	
	public Map<Integer,QuestionNode> getQuestions() {
		return questions;
	}

	public Map<Integer,AnswerNode> getAnswers() {
		return answers;
	}
	public Map<Integer,CommentNode> getComments() {
		return comments;
	}
	public Map<Integer,UserNode> getUsers() {
		return users;
	}
	
	public int getUniqueVertexIDCounter() {
		return uniqueVertexIDCounter;
	}
	
	public void setUniqueVertexIDCounter(int uniqueVertexIDCounter) {
		this.uniqueVertexIDCounter = uniqueVertexIDCounter;
	}
	
	/** Return a version of the map that is more friendly to other systems.
	 * 
	 * Returns a HashMap of all vertexIDs v in the graph --> set of vertexIDs 
	 * s reachable from v via a directed edge.
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
			
			List<Integer> outVertices = vertex.getOutEdges();
			HashSet<Integer> outVertexIDSet = new HashSet<Integer>(outVertices.size()*2,1);
			
			for (Integer outVertexID : outVertices) {
				
				outVertexIDSet.add(outVertexID);
			}
			
			exportedGraph.put(vertexID, outVertexIDSet);
		}
		
		return exportedGraph;
	}

	//TODO: Make viz of graph with maybe Swing?
	/** Print a text representation of the graph to default output.
	 * 
	 */
	public void printGraph() {
		
		System.out.println("This is a text representation of the graph " + 
				   topic + ":");

		for (int vertexID : vertices.keySet()) {
	
			Vertex vertex = vertices.get(vertexID);
	
			System.out.print("Vertex ID/Name: " + vertex.getVertexID() + "/" +
					 vertex.getName() + "; adjacency list: ");
	
			for (Integer toVertexID : vertex.getOutEdges()) {
		
				System.out.print(toVertexID + ",");
			}
	
			System.out.println();
		}
	}
}
