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
	private int highestLevelGraphTagCount;
	private int thisGraphTagCount;
	
	public Tag(String topic, int tagID, String tagName, 
			   int highestLevelGraphTagCount, int thisGraphTagCount) {
		
		this.topic = topic;
		this.tagID = tagID;
		this.tagName = tagName;
		this.highestLevelGraphTagCount = highestLevelGraphTagCount;
		this.thisGraphTagCount = thisGraphTagCount;
	}
	
	/** Make a copy of this tag.
	 * 
	 * Sets all fields equal to the current state of this
	 * Tag, except for thisGrapgTagCount, which is set to 0.
	 * 
	 * @return a new Tag that is a copy of this Tag
	 */
	public Tag makeCopy() {
		
		Tag tagCopy = new Tag(this.getTopic(), this.getTagID(),this.getTagName(), 
				this.getHighestLevelGraphTagCount(), 0);
		
		return tagCopy;
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

	public int getHighestLevelGraphTagCount() {
		return highestLevelGraphTagCount;
	}

	public void setHighestLevelGraphTagCount(int topicGraphTagCount) {
		this.highestLevelGraphTagCount = topicGraphTagCount;
	}
	
	public int getThisGraphTagCount() {
		return thisGraphTagCount;
	}
	
	public void setThisGraphTagCount(int thisGraphTagCount) {
		this.thisGraphTagCount = thisGraphTagCount;
	}

	public String getTopic() {
		return topic;
	}

	public void setCommunityName(String communityName) {
		this.topic = communityName;
	}
	
	@Override
	public String toString() {
		
		String returnString = "";
		
		returnString += "Tag ID: " + tagID;
		returnString += "\n";
		returnString += "Tag Name: " + tagName;
		returnString += "\n";
		returnString += "Topic: " + topic;
		returnString += "\n";
		returnString += "Count in Topic Graph: " + highestLevelGraphTagCount;
		returnString += "\n";
		returnString += "Count in this Graph: " + thisGraphTagCount;
		returnString += "\n";
		
		return returnString;
	}

}
