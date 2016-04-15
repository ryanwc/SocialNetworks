/** A utility that populates a graph that represents 
 * a stackexchange.com community.
 * 
 * The class hierarchy and structure is based on the data model provided
 * by the Stack Exchange Network in their "Stack Exchange Data Dump".
 * The data dump can be found here: https://archive.org/details/stackexchange
 * 
 * @author ryanwilliamconnor
 * © 2016 Ryan William Connor
 */

package util;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import graph.CapGraph;

public class GraphPopulator {
	
	CapGraph graph;

	public static void main(String[] args) {
		
		// tell Java the location of an XML file with data from a stackexchange community
		File communityXML = new File("data/StackExchangeData/buddhism.stackexchange.com/Posts.xml");
		// get instance of the factory that can give us a document builder
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		Document communityDOM = null;
		
		// try to create a new document builder
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
		// try to parse the XML file into a DOM using the document builder
		try {
				communityDOM = dBuilder.parse(communityXML);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
		}
	
		communityDOM.getDocumentElement().normalize();
		System.out.println("root of xml file: " + communityDOM.getDocumentElement().getNodeName());
		
		NodeList postsParent = communityDOM.getChildNodes();
		NodeList posts = postsParent.item(0).getChildNodes();
		
		//int rowCounter = 0;
		int totalQuestions = 0;
		int viewCutoff = 10;
		int questionsBelowViewCutoff = 0;
		
		for (int i = 0; i < posts.getLength(); i++) {
			
			Node post = posts.item(i);
			
			// if the node is an element, it is a post
			if (post.getNodeType() == 1) {
				
				totalQuestions++;
				String postType = post.getAttributes().getNamedItem("PostTypeId").getNodeValue();
				
				String postViews = "0";
				if (post.getAttributes().getNamedItem("ViewCount") != null) {
					postViews = post.getAttributes().getNamedItem("ViewCount").getNodeValue();
				}
			
				// if the post is has type 1 it is a question
				if (Integer.parseInt(postType) == 1) {
					if (Integer.parseInt(postViews) < viewCutoff) {
						questionsBelowViewCutoff++;
					}
				}
			}
			
			/*
			if (posts.item(i).getNodeType() == 1) {
				rowCounter++;
				System.out.println(rowCounter + "th row is type is " + posts.item(i).getNodeType());
				System.out.println(rowCounter + "th row is name is " + posts.item(i).getNodeName());
				System.out.println(rowCounter + "th row is attributes is " + posts.item(i).getAttributes());
				System.out.println(rowCounter + "th row is id " + posts.item(i).getAttributes().getNamedItem("Id"));
				System.out.println("***");
			}
			//*/
		}
		
		System.out.println(questionsBelowViewCutoff + " questions did not have enough views");
		System.out.println("there are " + totalQuestions + " total questions");
	}
}
