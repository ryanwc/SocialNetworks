/** An abstract node that represents a post in a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package graph;

import java.util.List;

public abstract class Post extends Vertex {

	private String communityName;
	private int postID;
	private int rawScore;
	private String body; // (as rendered HTML)
	private int authorUserId; // nullable in original data set; we exclude null
	private int commentCount;
	private int viewCount;
	
	private List<CommentNode> comments;
	
	public Post(int vertexID, String name, String communityName, int postID, 
				int rawScore, String body,  int authorUserId, 
				int commentCount, int viewCount) {
		super(vertexID, name);
		
		this.communityName = communityName;
		this.postID = postID;
		this.rawScore = rawScore;
		this.body = body;
		this.authorUserId = authorUserId;
		this.commentCount = commentCount;
		this.viewCount = viewCount;
	}
	
	/*
	 * "Getters" for important/useful derived values
	 */
	
	public double calculateUsefulness() {
		return rawScore / viewCount;
	}

	public double calculateCommentsPerViews() {
		return comments.size() / viewCount;
	}
	
	/*
	 * Normal getters and setters
	 */

	public int getPostID() {
		return postID;
	}

	public void setPostID(int postID) {
		this.postID = postID;
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

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public List<CommentNode> getComments() {
		return comments;
	}

	public void setComments(List<CommentNode> comments) {
		this.comments = comments;
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
}
