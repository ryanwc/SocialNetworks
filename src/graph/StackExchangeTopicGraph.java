/** A graph that represents a StackExchange topic.
 * 
 * Nodes are questions, answers, comments, and users.
 * Edges connect users to their questions, answers, and comments.
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */
package graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class StackExchangeTopicGraph implements Graph {
	
	public static final int QUESTION = 1;
	public static final int ANSWER = 2;
	public static final int COMMENT = 3;
	public static final int USER = 4;

	private String topic;
	private Map<Integer,Vertex> vertices;
	
	private List<Graph> SCCList;
	private Map<Integer,Map<Integer,StackExchangeTopicGraph>> levelToCommunities;
	private Integer highestLevelCommunity;
	// should egonets have a map of vertID --> its egonet?
	
	// maps from specific node type ID (e.g., userID, postID) 
	// so children can access parents from DOM data
	private Map<Integer,QuestionNode> questions;
	private Map<Integer,AnswerNode> answers;
	private Map<Integer,CommentNode> comments;
	private Map<Integer,UserNode> users;
	
	// is there a better way to do this?  need the String map for fast access
	// when creating QuestionNodes but ID map is less fragile.
	// if we concede a Tag object cannot exist without a Question that has it
	// as a tag, we could create Tags upon Question creation instead of
	// separately.  But StackExchange allows Tags with 0 related questions.
	private Map<Integer,Tag> tagIDMap;
	private Map<String,Tag> tagStringMap;
	
	// to enforce unique vertex IDs
	// addVertex methods fail if the ID is already mapped and
	// increment the counter after putting the vertex in the map
	// (ensuring unique IDs like this seems fragile)
	private int uniqueVertexIDCounter = 1;
	
	public StackExchangeTopicGraph() {
		
		this("Default Topic Name");
	}
	
	public StackExchangeTopicGraph(String topic) {
		
		this.topic = topic;
		
		this.vertices = new HashMap<Integer,Vertex>();
		
		this.users = new HashMap<Integer,UserNode>();
		this.questions = new HashMap<Integer,QuestionNode>();
		this.answers = new HashMap<Integer,AnswerNode>();
		this.comments = new HashMap<Integer,CommentNode>();
		
		this.highestLevelCommunity = null;
		
		this.tagIDMap = new HashMap<Integer,Tag>();
		this.tagStringMap = new HashMap<String,Tag>();
		
		this.SCCList = new ArrayList<Graph>();
		this.levelToCommunities = 
				new HashMap<Integer,Map<Integer,StackExchangeTopicGraph>>();
	}
	
	/** Add a vertex with dummy info to the graph.
	 * 
	 * This method will add a question, answer, comment, or user vertex
	 * to the graph with "default" values in all of the vertex's fields.
	 * 
	 * @see graph.Graph#addVertex(int)
	 * 
	 * @param vertexID is the id of the top vertex in the created 
	 * dummy vertex chain.  For example, if the type to add is a 
	 * user, vertexID is the id of the user.  If the type to add is
	 * a comment, vertexID is the id of the author of the parent
	 * post of the comment.
	 * @param vertexType is the type of vertex to add.
	 */
	@Override
	public void addVertex(int vertexID, int vertexType) {
		
		if (vertexType != QUESTION || vertexType != ANSWER || 
			vertexType != COMMENT || vertexType != USER) {
			
			throw new IllegalArgumentException("Nodes in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user");
		}
		
		Vertex vertex;
		
		// it's probably bad to add lots of dummy posts,
		// especially if they are comments or answers because these will
		// also create a corresponding parent dummy question and 
		// dummy author users)
		if (vertexID == USER) {
			
			vertex = createDummyUser(vertexID);
		}
		else if (vertexID == QUESTION){
				
			vertex = createDummyQuestion(vertexID);
		}
		else if (vertexType == ANSWER) {
				
			vertex = createDummyAnswer(vertexID);
		}
		else if (vertexType == COMMENT){
			
			// random parent type
			int coinFlip = (int)Math.random();
			int parentType;
			
			if (coinFlip == 0) {
				parentType = QUESTION;
			}
			else {
				parentType = ANSWER;
			}
			vertex = createDummyComment(vertexID, parentType);
		}
		else {
			
			throw new IllegalArgumentException("Vertices in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user.");
		}
		
		addVertex(vertex);
	}
	
	/** Add a vertex to the graph.
	 * 
	 * Adds an already-created question, answer, comment, or user node 
	 * to the graph (and puts it in the proper maps).
	 * 
	 * @param vertex is the Vertex object to add
	 */
	public void addVertex(Vertex vertex) {
		
		if (vertex instanceof QuestionNode) {
			
			addQuestionToGraph((QuestionNode)vertex);
		}
		else if (vertex instanceof AnswerNode) {
			
			addAnswerToGraph((AnswerNode)vertex);
		}
		else if (vertex instanceof CommentNode) {

			addCommentToGraph((CommentNode)vertex);
		}
		else if (vertex instanceof UserNode) {
		
			addUserToGraph((UserNode)vertex);
		}
		else {
			
			throw new IllegalArgumentException("Vertices in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user.");
		}
	}
	
	/** Create a Vertex from a DOM Node and add it to the graph.
	 * 
	 * Adds a question, answer, comment, or user to the graph and sets
	 * that vertex's state (e.g., view count).
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node is a DOM XML node that contains the vertex's data
	 * @param vertexType indicates the type of the vertex (question, answer,
	 * comment, or user)
	 */
	public void addVertex(int vertexID, Node node, int vertexType) {
		
		Vertex vertex;
		
		if (vertexType == QUESTION) {
			
			vertex = createQuestionFromDOMNode(vertexID, node);
		}
		else if (vertexType == ANSWER) {
			
			vertex = createAnswerFromDOMNode(vertexID, node);
		}
		else if (vertexType == COMMENT) {
			
			vertex = createCommentFromDOMNode(vertexID, node);
		}
		else if (vertexType == USER) {
		
			vertex = createUserFromDOMNode(vertexID, node);
		}
		else {
			
			throw new IllegalArgumentException("Vertices in a "
					+ "StackExchangeTopicGraph must be a question, "
					+ "answer, comment, or user.");
		}
		
		addVertex(vertex);
	}
	
	/** Create a QuestionNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange question per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * NOTE: This will throw a null pointer exception if the given Node
	 * has no author. You must add an author to the node before passing it as 
	 * an argument (see util.GraphLoader.loadQuestionsIntoGraph()
	 * for an example of how to do so).
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node is the DOM node that contains the question's data
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

		Integer acceptedAnswerID;
		if (nodeAttributes.getNamedItem("AcceptedAnswerId") != null) {
			
			acceptedAnswerID = Integer.parseInt(nodeAttributes.
				getNamedItem("AcceptedAnswerId").getNodeValue());
		}
		else {
			
			acceptedAnswerID = null;
		}
		
		String title = nodeAttributes.getNamedItem("Title").getNodeValue();
		int answerCount = Integer.parseInt(nodeAttributes.
				getNamedItem("AnswerCount").getNodeValue());
		
		int favoriteCount = 0;
		if (nodeAttributes.getNamedItem("FavoriteCount") != null) {
			favoriteCount = Integer.parseInt(nodeAttributes.
					getNamedItem("FavoriteCount").getNodeValue());
		}

		String tagsString = nodeAttributes.getNamedItem("Tags").getNodeValue();
		
		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		List<String> tagStrings = parseRawTags(tagsString);
		List<Integer> thisQuestionTagIDList = 
				new ArrayList<Integer>(tagStringMap.size());
		
		for (String tagString : tagStrings) {
			
			if (!tagStringMap.containsKey(tagString)) {
				throw new IllegalArgumentException("Tag " + tagString + " in "
						+ "Question with postID " + postID + " does not exist"
								+ " in the topic.");
			}
			
			thisQuestionTagIDList.add(tagStringMap.get(tagString).getTagID());
		}
		
		// name is possibly not needed
		String name = "Question " + (questions.size()+1);
		
		QuestionNode question = new QuestionNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, commentCount, viewCount, 
				acceptedAnswerID, title, thisQuestionTagIDList, 
				answerCount, favoriteCount);
		
		return question;
	}
	
	/** Add a question to the graph.
	 * 
	 * Adds a question as a vertex to the graph (and adds it
	 * to the question map).
	 * 
	 * @param question is the QuestionNode to add to the graph
	 */
	public void addQuestionToGraph(QuestionNode question) {
		
		vertices.put(question.getVertexID(), question);
		questions.put(question.getPostID(), question);
		
		for (int tagID : question.getTags()) {
			
			Tag tag = tagIDMap.get(tagID);
			tag.setThisGraphTagCount(tag.getThisGraphTagCount()+1);
		}
		
		uniqueVertexIDCounter++;
	}
	
	/** Create an AnswerNode with data from a DOM Node.
	 * 
	 * This method relies on the given node being an XML DOM representation
	 * of a StackExchange answer per the schema described at:
	 * http://meta.stackexchange.com/questions/2677/database-schema-documentation-for-the-public-data-dump-and-sede
	 * 
	 * NOTE: This will throw a null pointer exception if the given Node
	 * has no author.  See util.GraphLoader.loadAnswersIntoGraph() for an
	 * example of how to add an author to an answer DOM node.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node is the DOM node that contains the answer's data
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
			throw new IllegalArgumentException("Parent question with postID "
					+ parentQuestionID + " must be added before Answer with "
					+ " postID " + postID);
		}

		if (vertices.containsKey(vertexID)) {
			throw new IllegalArgumentException("Graph already contains "
					+ "vertex with vertexID " + vertexID);
		}
		
		QuestionNode parentQuestion = questions.get(parentQuestionID);
		
		// name is possibly not needed
		String name = "Answer " + (answers.size()+1);
		
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
	 * NOTE: This will throw a null pointer exception if the given Node
	 * has no author.  See util.GraphLoader.loadAnswersIntoGraph() for an
	 * example of how to add an author to an answer DOM node.
	 * 
	 * @param vertexID is the unique id of the vertex in this graph
	 * @param node is a DOM node that contains the comments's data
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
		String name = "Comment " + (comments.size()+1);
		
		CommentNode comment = new CommentNode(vertexID, name, topic, postID,
				rawScore, body, authorUserID, parentPost.getPostID(), 
				parentPost.getViewCount());
		
		return comment;
	}
	
	/** Add a CommentNode as a vertex to the graph.
	 * 
	 * @param comment is the CommentNode to add to the graph
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
	 * @param node is the DOM node that contains the user's data
	 */
	public UserNode createUserFromDOMNode(int vertexID, Node node) {
		
		NamedNodeMap nodeAttributes = node.getAttributes();
		
		if (nodeAttributes.getNamedItem("DisplayName") == null) {
			throw new IllegalArgumentException("Given node does not represent "
					+ "a user");
		}
		
		String name = nodeAttributes.getNamedItem("DisplayName").
				getNodeValue();
		int userID = Integer.parseInt(nodeAttributes.
				getNamedItem("Id").getNodeValue());
		int reputation = Integer.parseInt(nodeAttributes.
				getNamedItem("Reputation").getNodeValue());
		
		Integer age;
		
		if (nodeAttributes.getNamedItem("Age") != null) {
			age = Integer.parseInt(nodeAttributes.
					getNamedItem("Age").getNodeValue());
		}
		else {
			age = null;
		}
		
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
	 * @param user is the UserNode to add to the graph
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
	 * @param node is the DOM node that contains the tag's data
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
		
		Tag tag = new Tag(topic, tagID, tagName, tagCount, 0);
		
		return tag;
	}
	
	/** Add a Tag object to the graph metadata.
	 * 
	 * @param the Tag object to add to the graph
	 */
	public void addTagToGraph(Tag tag) {
		
		tagIDMap.put(tag.getTagID(), tag);
		tagStringMap.put(tag.getTagName(), tag);
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
		Vertex toVertex = vertices.get(toVertexID);
		
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
	 * Should only be used if no edges have been added to the graph, or else
	 * unpredicatble results will occur.
	 * 
	 * Works for subgraphs (egonets and communities) because it checks if the
	 * "to vertex" is actually in the subgraph before adding the edge.
	 */
	public void addAllEdges() {
		
		// QuestionNodes only know about their author (user)
		for (QuestionNode question : questions.values()) {

			if (users.containsKey(question.getAuthorUserID())) {
				
				UserNode author = users.get(question.getAuthorUserID());
				addEdge(question.getVertexID(), author.getVertexID());
				addEdge(author.getVertexID(), question.getVertexID());
			}
		}
		
		// AnswerNodes know about their author (user) and parent question
		for (AnswerNode answer : answers.values()) {
			
			if (users.containsKey(answer.getAuthorUserID())) {
				
				UserNode author = users.get(answer.getAuthorUserID());
				addEdge(answer.getVertexID(), author.getVertexID());
				addEdge(author.getVertexID(), answer.getVertexID());
			}

			if (questions.containsKey(answer.getParentQuestionPostID())) {
				
				QuestionNode question = questions.get(answer.getParentQuestionPostID());
				addEdge(answer.getVertexID(), question.getVertexID());
				addEdge(question.getVertexID(), answer.getVertexID());
			}
		}
		
		// CommentNodes know about author (user) and parent question or answer
		for (CommentNode comment : comments.values()) {
			
			if (users.containsKey(comment.getAuthorUserID())) {
				
				UserNode author = users.get(comment.getAuthorUserID());
				
				addEdge(comment.getVertexID(), author.getVertexID());
				addEdge(author.getVertexID(), comment.getVertexID());
			}

			Post parentPost = null;
			
			if (answers.containsKey(comment.getParentPostID())) {
				parentPost = answers.get(comment.getParentPostID());
			}
			else {
				parentPost = questions.get(comment.getParentPostID());
			}
			
			if (parentPost != null) {
				
				addEdge(comment.getVertexID(), parentPost.getVertexID());
				addEdge(parentPost.getVertexID(), comment.getVertexID());
			}
		}
		
		// UserNodes don't know anything about other nodes
		// All edges from UserNodes to other vertices are handled above
	}
	
	/** Create a user with dummy data.
	 * 
	 * One example use of this method is creating a dummy user to act as 
	 * the parent of posts whose real user has been deleted from the
	 * Stack Exchange community.
	 * 
	 * Does not add the create UserNode to the graph.
	 * 
	 * @param vertexID is the id of the vertex in this graph
	 * @return a new UserNode with dummy data
	 */
	public UserNode createDummyUser(int vertexID) {
		
		// does not quite ensure unique userID for each dummy node
		int userID = -(users.size()-1);
		String name = "Default User";
		int reputation = 0;
		Integer age = null;
		int upvotes = 0;
		int downvotes = 0;
		// same accountID for all dummy users
		int accountID = -2;
		
		//System.out.println("Creating dummy user with id " + vertexID + 
			//	" and user id " + userID);
		UserNode user = new UserNode(vertexID, name, userID,reputation, 
				age, upvotes, downvotes, accountID);
		
		return user;
	}
	
	/** Create a question with dummy data.
	 * 
	 * Before creating the question node, creates a dummy UserNode
	 * author and adds it to the graph.
	 * 
	 * Does not add the created dummy question to the graph.
	 * 
	 * @param vertexID is the id of the vertex of the dummy user that will
	 * be the author of this question
	 * @return a new QuestionNode with dummy data (including dummy author)
	 */
	public QuestionNode createDummyQuestion(int vertexID) {
		
		UserNode user = createDummyUser(vertexID);
		addVertex(user);
		
		// does not quite ensure unique questionID
		int postID = -(questions.size()-1);
		String name = "Default Question";
		int rawScore = 0;
		String body = "";
		// need to create dummy user first
		int authorID = user.getUserID();
		int commentCount = 0;
		int viewCount = 0;
		Integer acceptedAnswerID = null;
		String title = "";
		List<Integer> tagIDs = new ArrayList<Integer>(0);
		int answerCount = 0;
		int favoriteCount = 0;
		
		QuestionNode question = new QuestionNode(vertexID, name, topic,
				postID, rawScore, body, authorID, commentCount, viewCount, 
				acceptedAnswerID, title, tagIDs, answerCount, favoriteCount);
		
		return question;
	}
	
	/** Create an AnswerNode with dummy data.
	 * 
	 * Before creating the answer node, creates a dummy UserNode
	 * author and adds it to the graph.
	 * 
	 * Does not add the created dummy answer to the graph.
	 * 
	 * @param vertexID is the id of the vertex of the dummy user that will
	 * be the author of the new dummy answer
	 * @return a new AnswerNode with dummy data (including dummy author)
	 */
	public AnswerNode createDummyAnswer(int vertexID) {
		
		QuestionNode parentQuestion = createDummyQuestion(vertexID);
		addVertex(parentQuestion);
		
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
		
		return answer;
	}
	
	/** Create a dummy comment on a dummy question or answer.
	 * 
	 * In addition to creating a dummy comment, will create a dummy 
	 * question or answer to be the parent, add that parent to the
	 * graph, create a dummy user to be the author of the parent question, 
	 * add that dummy user to the graph, create a dummy user to be the author
	 * of the dummy comment, and add that dummy user to the graph.
	 * 
	 * Does not add the created dummy CommentNode to the graph.
	 * 
	 * @param vertexID is the id of the vertex that will
	 * represent the author of the new dummy comment's parent post
	 * @param parentType is the type of the parent post for the returned
	 * CommentNode.  parentType must be a question or answer or else
	 * an illegal argument exception is thrown.
	 * @return a new CommentNode with dummy data
	 */
	public CommentNode createDummyComment(int parentVertexID, int parentType) {
		
		Post parentPost;
		
		if (parentType == QUESTION) {
			parentPost = createDummyQuestion(parentVertexID);
		}
		else if (parentType == ANSWER) {
			parentPost = createDummyAnswer(parentVertexID);
		}
		else {
			throw new IllegalArgumentException("Parent post of a comment "
					+ "must be a question or an answer");
		}
		
		addVertex(parentPost);
		
		UserNode author = createDummyUser(uniqueVertexIDCounter);
		addVertex(author);
		
		// does not quite ensure unique answerID
		int commentID = -(comments.size()-1);
		String name = "Default Comment";
		int rawScore = 0;
		String body = "";
		
		CommentNode comment = new CommentNode(uniqueVertexIDCounter, name, topic,
				commentID, rawScore, body, author.getUserID(),
				parentPost.getPostID(), parentPost.getViewCount());
		
		return comment;
	}
	
	/** Creates a list of tags from raw tag data in a question DOM node.
	 * 
	 * Each tag is between '&lt;' which is an XML '<', and '&gt;' which
	 * is an XML '>'.  It appears Java (or the Java method that gets text
	 * from XML DOM) converts '&lt;' to '<' and '&gt;' to '>'.
	 * 
	 * This method may break if there is a '<' or '>' in the actual tag name.
	 * 
	 * @param tagsString is the raw tags String from the DOM
	 * @return a List<String> with each item in the list a tag
	 */
	public List<String> parseRawTags(String tagsString) {
		
		List<String> tags = new ArrayList<String>(4);
		
		for (int i = 0; i < tagsString.length()-1; i++) {

			if (tagsString.charAt(i) == '<') {
				int j = i+1;
				while (tagsString.charAt(j) != '>') {
					j++;
				}

				tags.add(tagsString.substring(i+1,j));
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
					
					// make a copy of each tag in the parent graph
					// add it to the SCC but with 0 count for the SCC
					for (int tagID : graph.getTagIDMap().keySet()) {
						
						Tag tag = graph.getTagIDMap().get(tagID);
						Tag tagCopy = tag.makeCopy();
						SCC.getTagIDMap().put(tagCopy.getTagID(), tagCopy);
					}
					
					Vertex vertexSuper = graph.vertices.get(vertexToVisitID);
					Vertex vertexSCC = vertexSuper.makeCopy();
					vertexSCC.setName(vertexSCC.getName() + " in " + SCC.getTopic());
					SCC.addVertex(vertexSCC);
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
		
		if (secondPass == false && SCC != null) {
			throw new IllegalArgumentException("SCC should be null "
					+ "on first pass");
		}
		else if (secondPass == true && SCC == null) {
			throw new IllegalArgumentException("SCC should not be null "
					+ "on second pass");
		}

		visited.add(vertexID);
		
		Vertex vertex = graph.vertices.get(vertexID);
		
		if (secondPass && !SCC.getVertices().keySet().contains(vertexID)) {

			Vertex vertexCopy = vertex.makeCopy();
			vertexCopy.setName(vertexCopy.getName() + " in " + SCC.getTopic());
			SCC.addVertex(vertexCopy);
		}
		
		for (Integer neighborID : vertex.getOutEdges()) {
			
			Vertex neighbor = vertices.get(neighborID);
			
			if (secondPass) {
				// if we haven't already visited it and
				// it isn't already in this SCC
				if (!visited.contains(neighborID) &&
					!SCC.getVertices().keySet().contains(neighborID)) {

					Vertex neighborCopy = neighbor.makeCopy();
					neighborCopy.setName(neighborCopy.getName() 
							+ " in " + SCC.getTopic());
					SCC.addVertex(neighborCopy);
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
	 * @return a new StackExchangeTopicGraph with all original 
	 * graph edges reversed.
	 */
	public StackExchangeTopicGraph getTranspose() {
		
		StackExchangeTopicGraph transposeGraph = 
				new StackExchangeTopicGraph(topic + " (Transpose)");
		
		// make a complete copy of each tag in the parent graph
		// add it to the transpose graph
		for (int tagID : this.getTagIDMap().keySet()) {
			
			Tag tag = this.getTagIDMap().get(tagID);
			Tag tagCopy = tag.makeCopy();
			tagCopy.setThisGraphTagCount(tagCopy.getHighestLevelGraphTagCount());
			transposeGraph.getTagIDMap().put(tagCopy.getTagID(), tagCopy);
		}
		
		Map<Integer,Vertex> transposeVertices = transposeGraph.getVertices();
		
		for (int vertexID : this.vertices.keySet()) {
			
			Vertex vertex = vertices.get(vertexID);
			
			if (!transposeVertices.keySet().contains(vertexID)) {
				
				Vertex vertexCopy = vertex.makeCopy();
				vertexCopy.setName(vertexCopy.getName() 
						+ " in " + transposeGraph.getTopic());
				transposeGraph.addVertex(vertexCopy);
			}
			
			List<Integer> oldOutEdges = vertex.getOutEdges();
			
			// adjacency matrix representation may be useful
			// to avoid linear inner loop
			for (Integer oldOutVertID : oldOutEdges) {
				
				Vertex oldOutVert = vertices.get(oldOutVertID);
				
				if (!transposeVertices.keySet().contains(oldOutVertID)) {
					
					Vertex oldOutVertCopy = oldOutVert.makeCopy();
					oldOutVert.setName(oldOutVert.getName()
							+ " in " + transposeGraph.getTopic());
					transposeGraph.addVertex(oldOutVertCopy);
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
	 * NOTE: Each vertex in the egonet will have the same stats (views, 
	 * usefulness, etc) as in the parent graph.
	 * 
	 * @param center is the vertex at the center of the egonet
	 * @return the egonet centered at center, including center
	 * 
	 * @see graph.Graph#getEgonet(int)
	 */
	@Override
	public Graph getEgonet(int center) {
		
		//System.out.println("Getting egonet for vertex " + center);
		
		StackExchangeTopicGraph egonet = 
				new StackExchangeTopicGraph("Egonet for vertex " + center + 
						" within " + topic);
		
		// make a copy of each tag in the parent graph
		// add it to the egonet but with 0 count for the egonet
		for (int tagID : this.getTagIDMap().keySet()) {
			
			Tag tag = this.getTagIDMap().get(tagID);
			Tag tagCopy = tag.makeCopy();
			egonet.getTagIDMap().put(tagCopy.getTagID(), tagCopy);
		}

		//TODO: Should egonet include "spoke" comments and answers?  currently, the egonet
		// will include answers and comments that "spoke" off of a vertex on a "main" path
		// between users in the egonet (the "spoke" comments and answers do not lead to 
		// a user). E.g., User1 and User2 are in the egonet.  User1 asks Question1 
		// and User2 answers Question1 with Answer1.  A hundred other users, which are NOT
		// in the egonet, make hundreds of other answers to Question1 and hundreds of
		// comments to Question1, Answer1, and the other answers. These hundreds of 
		// answers and comments are currently included in the egonet.
		Vertex cVertParentGraph = vertices.get(center);
		
		// question: should egonet be different for a post?
		// maybe it should include its author but not necessarily
		// all the other posts the author made?
		// as written, (egonet of post) == (egonet of post's author)
		if (!(cVertParentGraph instanceof UserNode)) {
			//System.out.println("Finding author");
			cVertParentGraph = users.get(((Post)cVertParentGraph).getAuthorUserID());
		}
		
		// add the center to the egonet
		//System.out.println("author, so center, is " + cVertParentGraph.getVertexID());
		Vertex cVertParentGraphCopy = cVertParentGraph.makeCopy();
		cVertParentGraphCopy.setName(cVertParentGraphCopy.getName()
				+ " in " + egonet.getTopic());
		egonet.addVertex(cVertParentGraphCopy);
		
		// populate egonet with vertices and edges up to
		// (and including) one user away from center
		egonet.DFSEgoNet(this, egonet, cVertParentGraph.getVertexID(),
						 cVertParentGraph.getVertexID(), null);
		
		// to avoid concurrent modification exception in for loop just below
		Set<Integer> vertIDsFoundByCenter = 
				new HashSet<Integer>(egonet.getVertices().keySet());
		Map<Integer,Integer[]> vertsNotFoundByCenterToFinder = 
				new HashMap<Integer,Integer[]>();
		
		for (int vertexID : vertIDsFoundByCenter) {
			
			Vertex vertex = egonet.getVertices().get(vertexID);
			
			// if the vertex found by center is a user and is not the center,
			// do a DFS from it to add vertices directly linking other users
			if (vertex instanceof UserNode &&
				vertex.getVertexID() != cVertParentGraph.getVertexID()) {

				egonet.DFSEgoNet(this, egonet, vertex.getVertexID(),
						vertex.getVertexID(), vertsNotFoundByCenterToFinder);
			}
		}
		
		egonet.addAllEdges();
		
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
	 * @param userDFSInitiatorVertIS is the vertex id of the UserNode that
	 * initiated the original call to DFS
	 * @param vertexID the vertex from which to do this DFS
	 * @param vertsNotFoundByCenterToFinder is a map from
	 * vertID --> {firstFinderVertID, secondFinderVertID}, where vertID is
	 * the vertex id of a vertex that was found by a DFS call starting at a
	 * non-center user, firstFinderVertID is the vertex id of the first
	 * non-center user who "found" the vertex (if any), and secondFinderVertID
	 * is the vertex id of the second non-center user who "found" the vertex
	 * (if any)
	 */
	private void DFSEgoNet(StackExchangeTopicGraph parent, 
				 		   StackExchangeTopicGraph egonet, 
						   int userDFSInitiatorVertID, int vertexID, 
						   Map<Integer,Integer[]> vertsNotFoundByCenterToFinder) {
		
		Vertex vertex = parent.getVertices().get(vertexID);
		List<Integer> outVertexIDs = vertex.getOutEdges();
		
		for (Integer outVertexID : outVertexIDs) {
			
			Vertex outVertex = parent.getVertices().get(outVertexID);
			Vertex outVertexCopy = outVertex.makeCopy();
			outVertexCopy.setName(outVertexCopy.getName()
					+ " in " + egonet.getTopic());

			// if this vertex is not already in the egonet
			if (!egonet.getVertices().containsKey(outVertexCopy.getVertexID())) {
					
				// if we started from the center
				if (vertsNotFoundByCenterToFinder == null) {
				
					egonet.addVertex(outVertexCopy);
				}
				else {
					// if we did not start from center, and if this 
					// vertex is not a user, we might want to add it 
					// to the egonet
					if (!(outVertexCopy instanceof UserNode)) {
						
						// if it was not already found by another user
						// mark it as found and do a DFS from it
						if (!vertsNotFoundByCenterToFinder.keySet().
								contains(outVertexCopy.getVertexID())) {
							
							// index 0 = first finder, index 1 = second finder
							Integer[] firstAndSecondFinders = {userDFSInitiatorVertID,null};
							
							vertsNotFoundByCenterToFinder.put(outVertexCopy.getVertexID(),
															  firstAndSecondFinders);
							
							DFSEgoNet(parent, egonet, userDFSInitiatorVertID,
									  outVertexCopy.getVertexID(), 
									  vertsNotFoundByCenterToFinder);
						}
						else if (vertsNotFoundByCenterToFinder.
								 get(outVertexCopy.getVertexID())[0] != userDFSInitiatorVertID) {
							// above is "if it was already found once *another* user"
							
							if (vertsNotFoundByCenterToFinder.
								 get(outVertexCopy.getVertexID())[1] == null) {
								// found for the second time, so add to egonet
								egonet.addVertex(outVertexCopy);
								// set as found second time by this user caller
								vertsNotFoundByCenterToFinder.
								 get(outVertexCopy.getVertexID())[1] = userDFSInitiatorVertID;
							}
							
							// if the user that discovered this vertex for the second time
							// was this user, we need to continue this user's DFS
							if (vertsNotFoundByCenterToFinder.get(outVertexCopy.getVertexID())[1] 
									== userDFSInitiatorVertID) {
								
								DFSEgoNet(parent, egonet, userDFSInitiatorVertID,
										  outVertexCopy.getVertexID(), 
										  vertsNotFoundByCenterToFinder);
							}
						}
					}
				}
				
				// do another DFS is not a user and it's the first pass
				if (!(outVertex instanceof UserNode) && 
					vertsNotFoundByCenterToFinder == null) {
					
					DFSEgoNet(parent, egonet, userDFSInitiatorVertID,
							  outVertexID, vertsNotFoundByCenterToFinder);
				}	
			}
		}
	}
	
	/** Detect communities in this graph.
	 * 
	 * Uses the Louvain method for detecting communities, freely available for 
	 * download and described here:
	 * https://perso.uclouvain.be/vincent.blondel/research/louvain.html
	 * 
	 * This method's helper methods interface with C++ code found at the link
	 * above.  Instructions to ensure all files are in the correct directory
	 * should be found at this project's GitHub page
	 * (https://github.com/ryanwc/SocialNetworks)
	 * 
	 * @return {map Integer --> {map Integer --> StackExchangeTopicGraph}}, 
	 * with each key in the upper map corresponding to a level of graph 
	 * hierarchy resulting from execution of the Louvain method, each key 
	 * in the lower map corresponding to a community within that level, and 
	 * each StackExchangeTopicGraph in the final value set corresponding to
	 * a community found at that level of the hierarchy. 0 is the "leaf" level 
	 * of the hierarchy, which means that for a return value returnMap, 
	 * returnMap.get(0) will return a Map<Integer,StackExchangeTopicGraph>>
	 * of size equal to the number of vertices in this graph, with each Integer
	 * key getting a StackExchangeTopicGraph containing only itself. 
	 * The key for the "highest" level of the hierarchy will be 
	 * returnMap(returnMap.size()-1), which will return a 
	 * Map<Integer,StackExchangeTopicGraph> with size equal the number 
	 * of communities at which the modularity score calculated by the Louvain 
	 * method no longer increased from the last level.
	 * @throws IOException 
	 */
	public File exportCommunities() throws IOException {
		
		File linkedListFile = null;
		
		try {
			linkedListFile = exportToLinkedListPlainText();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return runLouvain(linkedListFile);
	}
	
	/** Run the Louvain method for detecting communities of the given file.
	 * 
	 * Go to the following link to read about and download the freely 
	 * available C++ for the Louvain method:
	 * https://perso.uclouvain.be/vincent.blondel/research/louvain.html
	 * 
	 * @throws IOException 
	 */
	public File runLouvain(File linkedListFile) throws IOException {
		
		//System.out.println("running louvain");
		
		File communityMetadata = new File("Louvain_CPlusPlus/"+topic+"communityHierarchyInfo.txt");
		PrintWriter clearWriter = new PrintWriter(communityMetadata);
		clearWriter.close();
		String levelMetadataMarker = "**** starting level info ****";
		
		// discover communities and write metadata to text file
		ProcessBuilder buildCommunityHierarchy = new ProcessBuilder("bash", 
				"-c", "cd Louvain_CPlusPlus ; "
						+ "./convert -i '" + linkedListFile.getAbsolutePath() + "' -o graph.bin -r ; "
						+ "./community graph.bin -l -1 -v > graph.tree ; "
						+ "echo '" + levelMetadataMarker + "' ; "
						+ "./hierarchy graph.tree");
		buildCommunityHierarchy.redirectErrorStream(true);
		buildCommunityHierarchy.redirectOutput(Redirect.to(communityMetadata));
		
		Process buildingCommunityHierarchy = buildCommunityHierarchy.start();
		while (buildingCommunityHierarchy.isAlive()) {
			
		}

		// read output to determine number of levels
		InputStream metaDataIn = new FileInputStream(communityMetadata.getAbsolutePath());
		Scanner metaDataScanner = new Scanner(metaDataIn);
        
        String metaDataLine;
        int levels = -1;
        boolean inLevelMetaData = false;
        
        // probably a better way to do this, like reading just the last line
        // to get highest level number
        while (metaDataScanner.hasNextLine()) {
        	
        	//System.out.println("looking for level info");
        	metaDataLine = metaDataScanner.nextLine();
        	
        	if (inLevelMetaData) {
        		levels++;
        	}
        	else {
        		if (metaDataLine.equals(levelMetadataMarker)) {
        			inLevelMetaData = true;
        		}
        	}
        }
        
        metaDataScanner.close();
        
        //System.out.println("levels: " + levels);
        
        // write all level mappings to a single file
        // (a single level mapping contains a line for each vertex 
        // with the tuple "vertexID communityNum")
        ProcessBuilder writeLevelMapping;
		File levelMappings = new File("Louvain_CPlusPlus/"+topic+"LevelMappings.txt");
		clearWriter = new PrintWriter(levelMappings);
		clearWriter.close();
        
		for (int level = 0; level < levels; level++) {
       
			//System.out.println("writing a mapping");
			writeLevelMapping = 
    				new ProcessBuilder("bash", "-c", 
    						"cd Louvain_CPlusPlus ; "
    							+ "./hierarchy graph.tree -l " + level + " >> " + levelMappings.getName());
    		writeLevelMapping.redirectErrorStream(true);
    		Process writeLevelMappingProcess = writeLevelMapping.start();
    		while (writeLevelMappingProcess.isAlive()) {
    			
    		}
        }
		
		highestLevelCommunity = levels-1;
		
		return levelMappings;
	}
	
	/** Populate this graph's communities with the file output
	 * from running the Louvain method.
	 * 
	 * Go to the following link to read about and download the freely 
	 * available C++ for the Louvain method:
	 * https://perso.uclouvain.be/vincent.blondel/research/louvain.html
	 * 
	 * @param levelMappings is the file that contains all of the mappings
	 * from vertex to community for each level of the hierarchy discovered
	 * by running the Louvain method on this graph.
	 * @throws IOException 
	 */
	public void buildLevelToCommunityMap(File levelMappings, int level) throws IOException {
		
		InputStream levelMappingsIn = new FileInputStream(levelMappings.getAbsolutePath()); 
		BufferedReader levelMappingsReader = new BufferedReader(new InputStreamReader(levelMappingsIn));
		
		String vertexMapping = "";
		// only build given level
		int thisLevel = 0;
		int lineCounter = 0;
		int vertexID;
		int louvainVertexID;
		int communityID;
		StackExchangeTopicGraph community;
		
		Map<Integer,StackExchangeTopicGraph> levelCommunities;
		
		Object[] sortedOriginalIDs = vertices.keySet().toArray();
		Arrays.sort(sortedOriginalIDs);
		
		while ((vertexMapping = levelMappingsReader.readLine()) != null) {
			
			//System.out.println("in level " + thisLevel);
			//System.out.println(vertexMapping);
			
			if (thisLevel == level) {
				
				String[] vertexAndCommunity = vertexMapping.split(" ");
				communityID = Integer.parseInt(vertexAndCommunity[1]);
				// need to add one because the Louvain method indexes at zero
				// will not work for egonets, SCCs, or for other graphs where 
				// vertexIDs are not guaranteed to be numbered [1-numVertices]
				louvainVertexID = Integer.parseInt(vertexAndCommunity[0]);
				vertexID = (int)sortedOriginalIDs[louvainVertexID];
				
				// everything should go here
				
				// put the a community map in the current level if not there
				if (!levelToCommunities.containsKey(thisLevel)) {
				
					levelToCommunities.put(thisLevel, 
							new HashMap<Integer,StackExchangeTopicGraph>());
				}
				
				levelCommunities = levelToCommunities.get(thisLevel);
				
				
				if (levelCommunities.containsKey(communityID)) {
					
					community = levelCommunities.get(communityID);
				}
				else {

					community =
							new StackExchangeTopicGraph("Community " + communityID +
									" of level " + thisLevel + " of " + topic);
					// make a copy of each tag in the parent graph
					// add it to the community but with 0 count for the community
					for (int tagID : this.getTagIDMap().keySet()) {
						
						
						Tag tag = this.getTagIDMap().get(tagID);
						Tag tagCopy = tag.makeCopy();
						community.getTagIDMap().put(tagCopy.getTagID(), tagCopy);
					}
					
					levelCommunities.put(communityID, community);
				}
				
				Vertex parentVertex = vertices.get(vertexID);
				Vertex vertexCopy = parentVertex.makeCopy();
				vertexCopy.setName(vertexCopy.getName()
						+ " in " + community.getTopic());
				
				community.addVertex(vertexCopy);
				
				lineCounter++;
				// done with this level
				if (lineCounter % vertices.size() == 0) {
					// is it better to have new var for community here?
					// add all edges to each community at this level
					for (int communityNum : levelCommunities.keySet()) {
						
						community = levelCommunities.get(communityNum);
						community.addAllEdges();
					}
					
					thisLevel++;
				}
			}
			else {
				lineCounter++;
				if (lineCounter % vertices.size() == 0) {
					thisLevel++;
				}
			}
		}
		
		levelMappingsReader.close();
	}
	
	public String getTopic() {
		return topic;
	}
	
	public Map<Integer,Map<Integer,StackExchangeTopicGraph>> getCommunities() {

		return levelToCommunities;	
	}
	
	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public Map<Integer,Tag> getTagIDMap() {
		return tagIDMap;
	}
	
	public void setTagIDMap(Map<Integer,Tag> tagIDMap) {
		this.tagIDMap = tagIDMap;
	}
	
	public Map<String,Tag> getTagStringMap() {
		return tagStringMap;
	}
	
	public void setTags(Map<String,Tag> tagStringMap) {
		this.tagStringMap = tagStringMap;
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
	
	public List<Graph> getSCCList() {
		
		if (SCCList.size() < 1) {
			return getSCCs();
		}
		else {
			return SCCList;
		}
	}
	
	/** Return a version of the map that is potentially more friendly 
	 * to other systems.
	 * 
	 * The returned representation ignores edge weights and multi-edges.
	 * 
	 * @return a HashMap of all vertexIDs v in the graph --> set of vertexIDs 
	 * reachable from v via a directed edge.
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
		
		System.out.println("---------------------");
		System.out.println("Vertices:");

		for (Vertex vertex : vertices.values()) {
	
			System.out.println("**********");
			System.out.println(vertex.toString());
			System.out.println("**********");
		}
		
		System.out.println("---------------------");
		
		System.out.println("Tags:");
		for (Tag tag : tagIDMap.values()) {
			
			System.out.println("**********");
			System.out.println(tag.toString());
			System.out.println("**********");
		}
		
		System.out.println("---------------------");
	}
	
	//TODO: what should this print?
	/** Print basic stats about the graph.
	 * 
	 */
	public void printStats() {
		
		System.out.println("********************");
		System.out.println("Pertinent stats for graph " + topic + ":");
		System.out.println("Num vertices: " + vertices.size());
		System.out.println("Num users: " + users.size());
		System.out.println("Num questions: " + questions.size());
		System.out.println("Num answers: " + answers.size());
		System.out.println("Num comments: " + comments.size());
		System.out.println("Num tags: " + tagIDMap.size());
		System.out.println("********************");
	}
	
	/** Converts the graph to linked list format.
	 * 
	 * Required format for processing with the Louvain method found
	 * at https://perso.uclouvain.be/vincent.blondel/research/louvain.html
	 * 
	 * An example graph with three vertices and four edges 
	 * in linked list format:
	 * 
	 * 1 2
	 * 1 3
	 * 2 3
	 * 3 1
	 * . .
	 * 
	 * Each line has the form {"fromVertexID" "toVertexID"}
	 * which indicates an edge between the vertices with specified IDs.
	 * 
	 * Discards edge weights, if any.
	 * 
	 * If a vertex has no out edges, adds one out edge to itself (a loop) so
	 * the edge is included in a graph.  This is so the Louvain method found at
	 * https://perso.uclouvain.be/vincent.blondel/research/louvain.html
	 * still processes the vertex.  In the context of a Stack Exchange graph, 
	 * it is actually quite common for a vertex to have no out edges, as many
	 * users register but never make any posts.
	 * @return a file with linked list representation of the graph 
	 */
	public File exportToLinkedListPlainText() throws IOException {
		
		File linkedListFile = new File("data/stack_exchange/"+topic+"_LinkedList.txt");
		FileWriter fileWriter = new FileWriter(linkedListFile, false);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		
		// to play nicely with Louvain method, which requires re-indexing to 0
		// we are now guaranteed an ascending ordering of vertex IDs
		// so we can convert back from index 0 ids even if the graph's vertex ids
		// do not follow [1-numVertices], without touching the Louvain code.
		Object[] sortedIDs = vertices.keySet().toArray();
		Arrays.sort(sortedIDs);
		
		for (int i = 0; i < sortedIDs.length; i++) {
			
			List<Integer> outEdges = vertices.get(sortedIDs[i]).getOutEdges();
			
			if (outEdges.size() < 1) {
				printWriter.printf( "%s" + "%n" , sortedIDs[i] + " " + sortedIDs[i]);
			}
			for (int outEdgeVertID : outEdges) {
			
				printWriter.printf( "%s" + "%n" , sortedIDs[i] + " " + outEdgeVertID);
			}
		}
		
		printWriter.close();
		fileWriter.close();
		
		return linkedListFile;
	}
	
	/** Converts the graph to a format easily fed into regression analysis of 
	 * drivers of a question's "usefulness" score.
	 * 
	 * Variable names are in row 1, each sample is in a subsequent row.
	 * All values separated by whitespace
	 * 
	 * An example graph with four questions and three independent variables:
	 * 
	 * Score D1 D2 D3
	 * 87 2 54 65
	 * 43 3 23 20
	 * 2 3 100 15
	 * 10 1 4 10
	 * 
	 * @return a file with version of the graph easily manipulated by a 
	 * language suited for numerical calculation, like R or MATLAB, for 
	 * regression analysis of drivers of a question in that graph's 
	 * "usefulness" score.
	 * @throws IOException 
	 */
	public File exportQuestionUsefulnessRegressionFormat() throws IOException {
		
		File regressionQFile = new File("data/stack_exchange/"+topic+"_Regression.txt");
		FileWriter fileWriter = new FileWriter(regressionQFile, false);
		PrintWriter printWriter = new PrintWriter(fileWriter);
		
		//TODO: make this who method more maintainable
		double usefulness;
		
		int askerReputation, numAskerQuestions, 
			numAskerAnswers, numAskerCmnts, views, questionCharLength, 
			numTags, numVertsInEgonet,
			numVertsInHighestLevelCommunity;
		
		int acceptedAnswer; // 0 for no, 1 for yes
		
		double avgTagQuestions, usefulnessOfTopAnswer, favoritesPerViews, 
			   cmntsPerViews, answersPerViews,
			   avgRepOfAnswerers, avgUsefulnessAllAnswers, 
			   askerCmntsOnThisQPerViews;
		
		printWriter.printf("%s" + "%n", "usefulness askerReputation "
				+ "usefulnessOfTopAnswer numAskerQuestions numAskerAnswers "
				+ "numAskerCmnts questionCharLength numTags "
				+ "avgTagQuestions numVertsInEgonet "
				+ "numVertsInHighestLevelCommunity "
				+ "acceptedAnswer favoritesPerViews cmntsPerViews "
				+ "answersPerViews avgRepOfAnswerers "
				+ "avgUsefulnessAllAnswers askerCmntsOnThisQPerViews");

		int numAccepted = 0;
		int numNotAccepted = 0;
		
		for (QuestionNode question : this.getQuestions().values()) {
			
			// discard if has no answers
			
			if (question.getAnswers().size() > 0) {
			
				UserNode asker = users.get(question.getAuthorUserID());
			
				usefulness = question.calculateUsefulness();
				askerReputation = asker.getReputation();
			
				usefulnessOfTopAnswer = 0;
				int totalRepOfAnswerers = 0;
				double totalUsefulnessOfAnswers = 0;
				int askerCmntsOnThisQ = 0;
				for (AnswerNode answer : question.getAnswers()) {
				
					UserNode answerer = users.get(answer.getAuthorUserID());
					totalRepOfAnswerers += answerer.getReputation();
					totalUsefulnessOfAnswers += answer.calculateUsefulness();
					if (answer.calculateUsefulness() > usefulnessOfTopAnswer) {
					
						usefulnessOfTopAnswer = answer.calculateUsefulness();
					}
					for (CommentNode comment : answer.getComments()) {
						if (comment.getAuthorUserID() == question.getAuthorUserID()) {
							askerCmntsOnThisQ++;
						}
					}
				}
				avgRepOfAnswerers = ( ((double)totalRepOfAnswerers) / 
						((double)question.getAnswers().size()  + 
								Double.parseDouble(".00001") ) );
				avgUsefulnessAllAnswers = totalUsefulnessOfAnswers / 
						((double)question.getAnswers().size()  + 
								Double.parseDouble(".00001") );
			
				// possibly colinear with SCC/egonet/community size
				numAskerQuestions = asker.getQuestions().size();
				numAskerAnswers = asker.getAnswers().size();
				numAskerCmnts = asker.getComments().size();
				//
			
				views = question.getViewCount();
				questionCharLength = question.getBody().length();
				numTags = question.getTags().size();
			
				int totalTagQuestions = 0;
				for (int tagID : question.getTags()) {
				
					totalTagQuestions += tagIDMap.get(tagID).getThisGraphTagCount();
				}
			
				avgTagQuestions = ((double)totalTagQuestions) / 
						((double)question.getTags().size());
			
				StackExchangeTopicGraph egonet = 
						(StackExchangeTopicGraph)getEgonet(question.getVertexID());
				numVertsInEgonet = egonet.getVertices().size();
			
				numVertsInHighestLevelCommunity = 0;
				// clean this up
				if (!levelToCommunities.containsKey(highestLevelCommunity)) {
				
					File levelMappings = exportCommunities();
					buildLevelToCommunityMap(levelMappings, highestLevelCommunity);
					//System.out.println("finished building: " + highestLevelCommunity +
						//	" is highest level community");
				}
				Map<Integer,StackExchangeTopicGraph> highestLevelCommunities = 
						levelToCommunities.get(highestLevelCommunity);
				for (int communityID : highestLevelCommunities.keySet()) {
				
					StackExchangeTopicGraph community = 
							highestLevelCommunities.get(communityID);
				
					if (community.getVertices().keySet().
							contains(question.getVertexID())) {
					
						numVertsInHighestLevelCommunity = 
								community.getVertices().size();
						break;
					}
				}
			
				// should this be numAnswers instead?
				if (question.getAcceptedAnswerId() != null) {
					acceptedAnswer = 1;
					numAccepted++;
				}
				else {
					acceptedAnswer = 0;
					numNotAccepted++;
				}
			
				favoritesPerViews = question.calculateFavoritesPerViews();
				cmntsPerViews = question.calculateCommentsPerViews();
				answersPerViews = question.calculateAnswersPerViews();
			
				for (CommentNode comment : question.getComments()) {
					if (comment.getAuthorUserID() == question.getAuthorUserID()) {
						askerCmntsOnThisQ++;
					}
				}
			
				askerCmntsOnThisQPerViews = ((double)askerCmntsOnThisQ) / 
						((double)views);
			
				printWriter.printf("%s" + "%n", usefulness + " " + askerReputation
						+ " " + usefulnessOfTopAnswer + " " + numAskerQuestions + 
						" " + numAskerAnswers + " " + numAskerCmnts + " " +
						questionCharLength + " " + numTags + " " +
						avgTagQuestions + " " + numVertsInEgonet + " " + 
						numVertsInHighestLevelCommunity + " "
						+ acceptedAnswer + " " + favoritesPerViews + " " +
						cmntsPerViews + " " + answersPerViews + " " +
						avgRepOfAnswerers + " " + avgUsefulnessAllAnswers + " " + 
						askerCmntsOnThisQPerViews);
			}
		
		}
		
		System.out.println("num accepted " + numAccepted);
		System.out.println("num not accepted " + numNotAccepted);
		printWriter.close();
		
		return regressionQFile;
	}
}
