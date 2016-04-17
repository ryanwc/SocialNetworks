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
			
			// create a user node
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
			
			addQuestion(vertexID, node);
		}
		else if (vertexType == ANSWER) {
			
			addAnswer(vertexID, node);
		}
		else if (vertexType == COMMENT) {
			
			addComment(vertexID, node);
		}
		else if (vertexType == USER) {
		
			addUser(vertexID, node);
		}
		else {
			
			throw new IllegalArgumentException("Nodes in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user");
		}
	}
	
	/** Add a question to the graph.
	 * 
	 * Adds a question as a vertex to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange question per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the question's data
	 */
	public void addQuestion(int vertexID, Node questionNode) {
		
		NamedNodeMap nodeAttributes = questionNode.getAttributes();
		
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
		String title = nodeAttributes.getNamedItem("ViewCount").getNodeValue();
		int answerCount = Integer.parseInt(nodeAttributes.
				getNamedItem("AnswerCount").getNodeValue());
		int favoriteCount = Integer.parseInt(nodeAttributes.
				getNamedItem("FavoriteCount").getNodeValue());
		String tagsString = nodeAttributes.getNamedItem("Tags").getNodeValue();
		
		List<String> tags = parseRawTags(tagsString);
		
		// name is possibly not needed
		String name = "Question " + questions.size();
		
		QuestionNode question = new QuestionNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, commentCount, viewCount, 
				acceptedAnswerID, title, tags, answerCount, favoriteCount);
		
		vertices.put(question.getVertexID(), question);
		questions.put(question.getPostID(), question);
		
		uniqueVertexIDCounter++;
	}
	
	/** Add an answer to the graph.
	 * 
	 * Adds an answer as a vertex to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange answer per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the answer's data
	 */
	public void addAnswer(int vertexID, Node answerNode) {
		
		NamedNodeMap nodeAttributes = answerNode.getAttributes();
		
		// not absolutely necessary but a nice way to ensure
		// the answer has a parent
		if (!questions.containsKey(Integer.parseInt(nodeAttributes.
				getNamedItem("ParentId").getNodeValue()))) {
			throw new IllegalArgumentException("Parent question node must "
					+ "be added to the graph before child answer node.");
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
		
		QuestionNode parentQuestion = questions.get(parentQuestionID);
		
		// name is possibly not needed
		String name = "Answer " + answers.size();
		
		AnswerNode answer = new AnswerNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, commentCount, parentQuestion);
		
		vertices.put(answer.getVertexID(), answer);
		answers.put(answer.getPostID(), answer);
		
		uniqueVertexIDCounter++;
	}
	
	/** Add a comment to the graph.
	 * 
	 * Adds a comment as a vertex to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange comment per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the comment's data
	 */
	public void addComment(int vertexID, Node commentNode) {
		
		NamedNodeMap nodeAttributes = commentNode.getAttributes();
		
		// not absolutely necessary but a nice way
		// to ensure the comment has a parent
		if (!answers.containsKey(Integer.parseInt(nodeAttributes.
				getNamedItem("PostId").getNodeValue())) &&
			!questions.containsKey(Integer.parseInt(nodeAttributes.
					getNamedItem("PostId").getNodeValue()))) {
			throw new IllegalArgumentException("Parent post node must "
					+ "be added to the graph before child comment node.");
		}
		
		int postID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int rawScore = Integer.parseInt(nodeAttributes.
				getNamedItem("Score").getNodeValue());
		// does not strip rendered HTML from body
		String body = nodeAttributes.getNamedItem("Body").getNodeValue();
		int authorUserID = Integer.parseInt(nodeAttributes.
				getNamedItem("UserId").getNodeValue());
		int parentPostID = Integer.parseInt(nodeAttributes.
				getNamedItem("PostId").getNodeValue());
		
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
				rawScore, body, authorUserID, parentPost);
		
		vertices.put(comment.getVertexID(), comment);
		comments.put(comment.getPostID(), comment);
		
		uniqueVertexIDCounter++;
	}
	
	/** Add a user to the graph.
	 * 
	 * Adds a user as a vertex to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange user per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node contains the user's data
	 */
	public void addUser(int vertexID, Node userNode) {
		
		NamedNodeMap nodeAttributes = userNode.getAttributes();
		
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
		
		UserNode user = new UserNode(vertexID, name, userID, reputation, 
					age, upVotes, downVotes, accountID);
		
		vertices.put(user.getVertexID(), user);
		users.put(user.getUserID(), user);
		
		uniqueVertexIDCounter++;
	}
	
	public void addEdge(Vertex from, Vertex to) {
		
		// to implement
	}
	
	/** Add a question with dummy data to the graph.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 */
	private QuestionNode addDummyQuestion(int vertexID) {
		
		// does not ensure unique questionID
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
		
		// does not ensure unique answerID
		int postID = -(answers.size()-1);
		String name = "Default Answer";
		int rawScore = 0;
		String body = "";
		int authorID = -2;
		int commentCount = 0;
		
		AnswerNode answer = new AnswerNode(uniqueVertexIDCounter, name, topic,
				postID, rawScore, body, authorID, commentCount, parentQuestion);
		
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
		
		// does not ensure unique answerID
		int commentID = -(comments.size()-1);
		String name = "Default Comment";
		int rawScore = 0;
		String body = "";
		int authorID = -2;
		
		CommentNode comment = new CommentNode(uniqueVertexIDCounter, name, topic,
				commentID, rawScore, body, authorID, parentPost);
		
		vertices.put(comment.getVertexID(), comment);
		comments.put(comment.getCommentID(), comment);
		
		uniqueVertexIDCounter++;
	}
	
	/** Helper method for creating a question node that to parses
	 *  raw tag data from the DOM node.
	 * 
	 * @param tagsString is the raw tags String from the DOM
	 */
	private List<String> parseRawTags(String tagsString) {
		
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
	
	public String getTopic() {
		return topic;
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public int getUniqueVertexIDCounter() {
		return uniqueVertexIDCounter;
	}
	
	public void setUniqueVertexIDCounter(int uniqueVertexIDCounter) {
		this.uniqueVertexIDCounter = uniqueVertexIDCounter;
	}
}
