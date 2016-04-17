package graph;

import java.util.List;

public interface Commentable {

	public List<CommentNode> getComments();
	
	public double calculateCommentsPerViews();
}
