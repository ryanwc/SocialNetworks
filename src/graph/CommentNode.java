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

public class CommentNode extends Post {

	private int parentPostID; // currently, a QuestionNode or AnswerNode
	
	public CommentNode(int vertexID, String name, String topic,
					   int commentID, int rawScore, String body,
					   int authorUserID, int parentPostID, int viewCount) {
		super(vertexID, name, topic, commentID, 
			  rawScore, body, authorUserID, viewCount);
		
		this.parentPostID = parentPostID;
	}
	
	/** Makes a copy of this CommentNode
	 * 
	 * Creates a new CommentNode with all object values that are initially
	 * passed to the CommentNode's constructor equal to the same values 
	 * from the CommentNode's current state.
	 * 
	 * This means, for example, that the new CommentNode will have the same
	 * vertexID as this CommentNode because those values are 
	 * passed to the constructor, but not the same list of out edges 
	 * because the list of outEdges is not passed to the constructor.
	 * 
	 * @return a new CommentNode with the values described above
	 */
	@Override
	public CommentNode makeCopy() {
		
		return new CommentNode(this.getVertexID(), this.getName(),
							   this.getTopic(), this.getPostID(),
							   this.getRawScore(), this.getBody(),
							   this.getAuthorUserID(), parentPostID,
							   this.getViewCount());
	}

	public int getParentPostID() {
		return parentPostID;
	}

	public void setParentPostID(int parentPostID) {
		this.parentPostID = parentPostID;
	}
	
	@Override
	public String toString() {
		
		String returnString = super.toString();
		
	    returnString += "Parent Post Post ID: " + parentPostID;
		returnString += "\n";
		// returnString += "Comment ID: " + commentID;
		// returnString += "\n";
		returnString += "NOTE: A comment may have the same postID as a "
				+ "Question or Answer, but the comment's postID is unique "
				+ "to all comments. [Dev should add commentID]";
		
		return returnString;
	}
}
