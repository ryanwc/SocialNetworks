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

import java.util.ArrayList;
import java.util.List;

public class QuestionNode extends Post {
	
	private Integer acceptedAnswerId; // can be null
	private String title;
	private List<String> tags;
	private int favoriteCount;
	
	private List<AnswerNode> answers;
	
	public QuestionNode(int vertexID, String name, String communityName,
						int postID, int rawScore, String body,
						int authorUserId, int commentCount, int viewCount,
						int acceptedAnswerId, String title, List<String> tags,
						int answerCount, int favoriteCount) {
		super(vertexID, name, communityName, postID, rawScore,
			  body, authorUserId, commentCount, viewCount);

		this.acceptedAnswerId = acceptedAnswerId;
		this.title = title;
		this.tags = tags;
		this.favoriteCount = favoriteCount;
		
		this.answers = new ArrayList<AnswerNode>(answerCount);
	}
	
	/*
	 * "Getters" for important/useful derived values
	 */
	
	public double calculateAnswersPerViews() {
		return answers.size() / this.getViewCount();
	}

	public double calculateFavoritesPerViews() {
		return favoriteCount / this.getViewCount();
	}
	
	/*
	 * Normal getters and setters
	 */

	public Integer getAcceptedAnswerId() {
		return acceptedAnswerId;
	}

	public void setAcceptedAnswerId(Integer acceptedAnswerId) {
		this.acceptedAnswerId = acceptedAnswerId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public int getFavoriteCount() {
		return favoriteCount;
	}

	public void setFavoriteCount(int favoriteCount) {
		this.favoriteCount = favoriteCount;
	}

	public List<AnswerNode> getAnswers() {
		return answers;
	}

	public void setAnswers(List<AnswerNode> answers) {
		this.answers = answers;
	}
}
