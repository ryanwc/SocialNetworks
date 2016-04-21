/** A class that represents a tag in a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package graph;

public class Tag {

	private String topic;
	private int tagID;
	private String tagName;
	private int tagCount;
	
	public Tag(String topic, int tagID,
			   String tagName, int tagCount) {
		
		this.topic = topic;
		this.tagID = tagID;
		this.tagName = tagName;
		this.tagCount = tagCount;
	}

	public int getTagID() {
		return tagID;
	}

	public void setTagID(int tagID) {
		this.tagID = tagID;
	}

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public int getTagCount() {
		return tagCount;
	}

	public void setTagCount(int tagCount) {
		this.tagCount = tagCount;
	}

	public String getTopic() {
		return topic;
	}

	public void setCommunityName(String communityName) {
		this.topic = communityName;
	}

}
