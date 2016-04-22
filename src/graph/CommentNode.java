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
	
	public CommentNode(int vertexID, String name, String communityName,
					   int commentID, int rawScore, String body,
					   int authorUserID, int parentPostID, int viewCount) {
		super(vertexID, name, communityName, commentID, 
			  rawScore, body, authorUserID, viewCount);
		
		this.parentPostID = parentPostID;
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
