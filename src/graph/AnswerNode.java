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

public class AnswerNode extends Post {

	private QuestionNode parentQuestion;
	
	public AnswerNode(int vertexID, String name, String communityName, 
					  int postId, int rawScore, String body,
					  int authorUserId, int commentCount, int viewCount,
					  QuestionNode parentQuestion) {
		super(vertexID, name, communityName, postId, rawScore, 
			  body, authorUserId, commentCount, viewCount);
		
		this.parentQuestion = parentQuestion;
	}

	public QuestionNode getParentQuestion() {
		return parentQuestion;
	}

	public void setParentQuestion(QuestionNode parentQuestion) {
		this.parentQuestion = parentQuestion;
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
