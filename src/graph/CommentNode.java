/** A node that represents a comment in a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package graph;

public class CommentNode extends Vertex {

	private int commentID; // unique to community
	private int parentPostID;
	private int rawScore;
	private String body;
	private int authorUserId; // nullable in original data set but exclude posts with nulls from our graph
	
	private int viewCount; // same as parentPost viewcount
	
	private double usefulness; // score divided by views; our independent variable
	
	public CommentNode(int vertexID, String name, int commentID,
					   int parentPostID, int rawScore, String body, 
					   int authorUserId) {
		super(vertexID, name);
		
		this.setCommentID(commentID);
		this.setRawScore(rawScore);
		this.setParentPostID(parentPostID);
		this.setBody(body);
		this.setAuthorUserId(authorUserId);
	}

	public int getCommentID() {
		return commentID;
	}

	public void setCommentID(int commentID) {
		this.commentID = commentID;
	}

	public int getParentPostID() {
		return parentPostID;
	}

	public void setParentPostID(int parentPostID) {
		this.parentPostID = parentPostID;
	}

	public int getRawScore() {
		return rawScore;
	}

	public void setRawScore(int rawScore) {
		this.rawScore = rawScore;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getAuthorUserId() {
		return authorUserId;
	}

	public void setAuthorUserId(int authorUserId) {
		this.authorUserId = authorUserId;
	}
}
