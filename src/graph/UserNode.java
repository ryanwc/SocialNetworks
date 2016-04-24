/** A node that represents a user in a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package graph;

import java.util.ArrayList;
import java.util.List;

public class UserNode extends Vertex {

	private int userID;
	private int reputation;
	private Integer age; // nullable
	private int upvotes;
	private int downvotes;
	private int accountID;
	
	private List<QuestionNode> questions;
	private List<AnswerNode> answers;
	private List<CommentNode> comments;
	// for an extension that includes badge information
	// private List<Badge> badges;
	
	public UserNode(int vertexID, String name, int userID, int reputation, 
					Integer age, int upvotes, int downvotes, int accountID) {
		super(vertexID, name);
		
		this.userID = userID;
		this.reputation = reputation;
		this.age = age;
		this.upvotes = upvotes;
		this.downvotes = downvotes;
		this.accountID = accountID;
		
		this.questions = new ArrayList<QuestionNode>();
		this.answers = new ArrayList<AnswerNode>();
		this.comments = new ArrayList<CommentNode>();
	}
	
	/** Makes a copy of this UserNode
	 * 
	 * Creates a new UserNode with all object values that are initially
	 * passed to the UserNode's constructor equal to the same values 
	 * from the UserNode's current state.
	 * 
	 * This means, for example, that the new Vertex will have the same
	 * vertexID as this UserNode because those values are 
	 * passed to the constructor, but not the same list of out edges 
	 * because the list of outEdges is not passed to the constructor.
	 * 
	 * @return a new UserNode with values described above
	 */
	@Override
	public UserNode makeCopy() {
		
		return new UserNode(this.getVertexID(), this.getName(), userID,
							reputation, age, upvotes, downvotes, accountID);
	}

	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public int getReputation() {
		return reputation;
	}

	public void setReputation(int reputation) {
		this.reputation = reputation;
	}

	public Integer getAge() {
		return age;
	}

	public void setAge(Integer age) {
		this.age = age;
	}

	public int getUpvotes() {
		return upvotes;
	}

	public void setUpvotes(int upvotes) {
		this.upvotes = upvotes;
	}

	public int getDownvotes() {
		return downvotes;
	}

	public void setDownvotes(int downvotes) {
		this.downvotes = downvotes;
	}

	public int getAccountID() {
		return accountID;
	}

	public void setAccountID(int accountID) {
		this.accountID = accountID;
	}

	public List<QuestionNode> getQuestions() {
		return questions;
	}

	public void setQuestions(List<QuestionNode> questions) {
		this.questions = questions;
	}

	public List<AnswerNode> getAnswers() {
		return answers;
	}

	public void setAnswers(List<AnswerNode> answers) {
		this.answers = answers;
	}

	public List<CommentNode> getComments() {
		return comments;
	}

	public void setComments(List<CommentNode> comments) {
		this.comments = comments;
	}
	
	@Override
	public String toString() {
		
		String returnString = super.toString();
		
		returnString += "User ID: " + userID;
		returnString += "\n";
		returnString += "Reputation: " + reputation;
		returnString += "\n";
		returnString += "Age: " + age;
		returnString += "\n";
		returnString += "Upvotes: " + upvotes;
		returnString += "\n";
		returnString += "Downvotes: " + downvotes;
		returnString += "\n";
		returnString += "Account ID: " + accountID;
		returnString += "\n";
		returnString += "Question Vertex IDs: ";
		for (QuestionNode question : questions) {
			returnString += question.getVertexID() + ", ";
		}
		returnString += "\n";
		returnString += "Answer Vertex IDs: ";
		for (AnswerNode answer : answers) {
			returnString += answer.getVertexID() + ", ";
		}
		returnString += "\n";
		returnString += "Comment Vertex IDs: ";
		for (CommentNode comment : comments) {
			returnString += comment.getVertexID() + ", ";
		}
		returnString += "\n";
		
		return returnString;
	}
}
