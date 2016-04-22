/** A node that represents a question in a stackexchange.com community.
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

public class QuestionNode extends Post implements Commentable {
	
	private Integer acceptedAnswerID; // can be null ("no accepted answer")
	private String title;
	private int favoriteCount;
	
	// would it be better to have these lists of Integers (IDs)?
	private List<AnswerNode> answers;
	private List<CommentNode> comments;
	private List<Tag> tags;
	
	public QuestionNode(int vertexID, String name, String communityName,
						int postID, int rawScore, String body,
						int authorUserID, int commentCount, int viewCount,
						Integer acceptedAnswerID, String title, 
						List<Tag> tags, int answerCount, 
						int favoriteCount) {
		super(vertexID, name, communityName, postID, rawScore,
			  body, authorUserID, viewCount);

		this.acceptedAnswerID = acceptedAnswerID;
		this.title = title;
		this.tags = tags;
		this.favoriteCount = favoriteCount;
		
		this.answers = new ArrayList<AnswerNode>(answerCount);
		this.comments = new ArrayList<CommentNode>(commentCount);
	}
	
	/*
	 * "Getters" for important/useful derived values
	 */

	public double calculateCommentsPerViews() {
		return ((double)comments.size()) / ((double)this.getViewCount());
	}
	
	public double calculateAnswersPerViews() {
		return ((double)answers.size()) / ((double)this.getViewCount());
	}

	public double calculateFavoritesPerViews() {
		return ((double)favoriteCount) / ((double)this.getViewCount());
	}
	
	/*
	 * Normal getters and setters
	 */

	public Integer getAcceptedAnswerId() {
		return acceptedAnswerID;
	}

	public void setAcceptedAnswerId(Integer acceptedAnswerId) {
		this.acceptedAnswerID = acceptedAnswerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}
	
	public List<CommentNode> getComments() {
		return comments;
	}
	
	public void setComments(List<CommentNode> comments) {
		this.comments = comments;
	}

	public List<AnswerNode> getAnswers() {
		return answers;
	}

	public void setAnswers(List<AnswerNode> answers) {
		this.answers = answers;
	}
	
	@Override
	public String toString() {
		
		String returnString = super.toString();
		
		DecimalFormat derivedScoreFormat = new DecimalFormat("###,###.###");
		
	    String answersPerViews = derivedScoreFormat.format(calculateAnswersPerViews());
	    String commentsPerViews = derivedScoreFormat.format(calculateCommentsPerViews());
	    String favoritesPerViews = derivedScoreFormat.format(calculateFavoritesPerViews());
		
	    returnString += "Accepted Answer Post ID: " + acceptedAnswerID;
		returnString += "\n";
		returnString += "Question Title: " + title;
		returnString += "\n";
		returnString += "Favorites: " + favoriteCount;
		returnString += "\n";
		returnString += "Favorites Per Views: " + favoritesPerViews;
		returnString += "\n";
		returnString += "Answer Vertex IDs: ";
		for (AnswerNode answer : answers) {
			returnString += answer.getVertexID() + ", ";
		}
		returnString += "\n";
		returnString += "Answers Per Views: " + answersPerViews;
		returnString += "\n";
		returnString += "Comment Vertex IDs: ";
		for (CommentNode comment : comments) {
			returnString += comment.getVertexID() + ", ";
		}
		returnString += "\n";
		returnString += "Comments Per Views: " + commentsPerViews;
		returnString += "\n";	
		returnString += "Tag IDs: ";
		for (Tag tag : tags) {
			returnString += tag.getTagID() + ", ";
		}
		returnString += "\n";
		
		return returnString;
	}
}
