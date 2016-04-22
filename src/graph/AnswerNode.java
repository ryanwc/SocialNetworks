/** A node that represents an answer in a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package graph;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AnswerNode extends Post implements Commentable {

	private int parentQuestionPostID;
	private List<CommentNode> comments;
	
	public AnswerNode(int vertexID, String name, String communityName, 
					  int postID, int rawScore, String body,
					  int authorUserID, int commentCount,
					  int parentQuestionPostID, int viewCount) {
		super(vertexID, name, communityName, postID,
			  rawScore, body, authorUserID, viewCount);
		
		this.parentQuestionPostID = parentQuestionPostID;
		this.comments = new ArrayList<CommentNode>(commentCount);
	}

	/*
	 * "Getters" for important/useful derived values
	 */

	public double calculateCommentsPerViews() {
		return ((double)comments.size()) / ((double)this.getViewCount());
	}
	
	/*
	 * Normal getters and setters
	 */
	
	public int getParentQuestionPostID() {
		return parentQuestionPostID;
	}

	public void setParentQuestion(int parentQuestionPostID) {
		this.parentQuestionPostID = parentQuestionPostID;
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
		
		DecimalFormat derivedScoreFormat = new DecimalFormat("###,###.###");
	    String commentsPerViews = derivedScoreFormat.format(calculateCommentsPerViews());

	    returnString += "Parent Question Post ID: " + parentQuestionPostID;
		returnString += "\n";
		returnString += "Comment Vertex IDs: ";
		for (CommentNode comment : comments) {
			returnString += comment.getVertexID() + ", ";
		}
		returnString += "\n";
		returnString += "Comments Per Views: " + commentsPerViews;
		returnString += "\n";
		
		return returnString;
	}
}
