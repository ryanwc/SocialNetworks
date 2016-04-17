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

import java.util.ArrayList;
import java.util.List;

public class AnswerNode extends Post implements Commentable {

	private QuestionNode parentQuestion;
	private List<CommentNode> comments;
	
	public AnswerNode(int vertexID, String name, String communityName, 
					  int postID, int rawScore, String body,
					  int authorUserID, int commentCount,
					  QuestionNode parentQuestion) {
		super(vertexID, name, communityName, postID,
			  rawScore, body, authorUserID, parentQuestion.getViewCount());
		
		this.parentQuestion = parentQuestion;
		this.comments = new ArrayList<CommentNode>(commentCount);
	}

	/*
	 * "Getters" for important/useful derived values
	 */

	public double calculateCommentsPerViews() {
		return comments.size() / this.getViewCount();
	}
	
	/*
	 * Normal getters and setters
	 */
	
	public QuestionNode getParentQuestion() {
		return parentQuestion;
	}

	public void setParentQuestion(QuestionNode parentQuestion) {
		this.parentQuestion = parentQuestion;
	}
	
	public List<CommentNode> getComments() {
		return comments;
	}
	
	public void setComments(List<CommentNode> comments) {
		this.comments = comments;
	}

	@Override
	public int getViewCount() {
		// returns views of the parent question
		return parentQuestion.getViewCount();
	}
	
	@Override
	public void setViewCount(int viewCount) {
		// sets the views of the parent question
		parentQuestion.setViewCount(viewCount);
	}
}
