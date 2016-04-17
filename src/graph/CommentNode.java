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

	private int commentID;
	private Post parentPost; // currently, a QuestionNode or AnswerNode
	
	public CommentNode(int vertexID, String name, String communityName,
					   int commentID, int parentPostID, int rawScore, 
					   String body, int viewCount, int authorUserId,
					   Post parentPost) {
		super(vertexID, name, communityName, commentID, 
			  rawScore, body, authorUserId, viewCount);
		
		this.commentID = commentID;
		this.parentPost = parentPost;
	}

	public int getCommentID() {
		return commentID;
	}

	public void setCommentID(int commentID) {
		this.commentID = commentID;
	}

	public Post getParentPost() {
		return parentPost;
	}

	public void setParentPost(Post parentPost) {
		this.parentPost = parentPost;
	}
	
	@Override
	public int getViewCount() {
		// returns views of the parent post
		return parentPost.getViewCount();
	}
	
	@Override
	public void setViewCount(int viewCount) {
		// sets the views of the parent post
		parentPost.setViewCount(viewCount);
	}
	
}
