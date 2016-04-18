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

public abstract class Post extends Vertex {

	private String communityName;
	private int postID;
	private int rawScore;
	private String body; // (as rendered HTML)
	private int authorUserID; // nullable in original data set; we exclude null
	private int viewCount;
	
	public Post(int vertexID, String name, String communityName, int postID, 
				int rawScore, String body,  int authorUserID, int viewCount) {
		super(vertexID, name);
		
		this.communityName = communityName;
		this.postID = postID;
		this.rawScore = rawScore;
		this.body = body;
		this.authorUserID = authorUserID;
		this.viewCount = viewCount;
	}
	
	/*
	 * "Getters" for important/useful derived values
	 */
	
	public double calculateUsefulness() {
		return rawScore / viewCount;
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

	public int getAuthorUserID() {
		return authorUserID;
	}

	public void setAuthorUserID(int authorUserID) {
		this.authorUserID = authorUserID;
	}

	public int getViewCount() {
		return viewCount;
	}

	public void setViewCount(int viewCount) {
		this.viewCount = viewCount;
	}

	public String getCommunityName() {
		return communityName;
	}

	public void setCommunityName(String communityName) {
		this.communityName = communityName;
	}
}
