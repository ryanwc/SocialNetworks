/** Utility class to add vertices and edges to a graph
 * 
 * @author ryanwilliamconnor (all methods except for loadGraph)
 * @author UCSD MOOC development team (loadGraph method)
 */
package util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import graph.StackExchangeTopicGraph;

public class GraphLoader {
	
    /**
     * Loads graph with data from a file.
     * The file should consist of lines with 2 integers each, corresponding
     * to a "from" vertex and a "to" vertex.
     */ 
    public static void loadGraph(graph.Graph g, String filename) {
        Set<Integer> seen = new HashSet<Integer>();
        Scanner sc;
        try {
            sc = new Scanner(new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // Iterate over the lines in the file, adding new
        // vertices as they are found and connecting them with edges.
        while (sc.hasNextInt()) {
            int v1 = sc.nextInt();
            int v2 = sc.nextInt();
            if (!seen.contains(v1)) {
                g.addVertex(v1, 1);
                seen.add(v1);
            }
            if (!seen.contains(v2)) {
                g.addVertex(v2, 1);
                seen.add(v2);
            }
            g.addEdge(v1, v2);
        }
        
        sc.close();
    }
    
    /** Populates a StackExchangeTopicGraph with data from a Stack Exchange topic.
     * 
     * Uses the following XML files from a specified topic data dump bundle:
     * Posts.xml, User.xml, Tags.xml, Comments.xml, Badges.xml
     * @param graph the StackExchangeTopicGraph to populate
     * @param directoryWithXMLFiles the directory with all of a Stack Exchange
     * topic's data in the form of XML files found at the data dump.
     */
	public void populateStackExchangeTopicGraph(StackExchangeTopicGraph graph, 
												String directoryWithXMLFiles) {
		
		// get instance of the factory that can give us a document builder
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = null;
		
		// try to create a new document builder
		try {
			dBuilder = dbFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		}
		
		// load relevant data into the graph
		loadUserDataIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Users.xml");
		loadPostDataIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Posts.xml");
		loadCommentDataIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Comments.xml");
		loadTagDataIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Tags.xml");
	}
	
	public NodeList getNodeListFromXMLFile(DocumentBuilder dBuilder,
										   String xmlFilePath) {
		
		// create a Java file from the XML file
		File xmlFile = new File(xmlFilePath);
		Document xmlFileDOM = null;
		
		// try to parse the XML file into a DOM using the document builder
		try {
				xmlFileDOM = dBuilder.parse(xmlFile);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
		}
	
		xmlFileDOM.getDocumentElement().normalize();
		
		NodeList DOMNodeListParent = xmlFileDOM.getChildNodes();
		NodeList DOMNodeList = DOMNodeListParent.item(0).getChildNodes();
		
		return DOMNodeList;
	}
	
	public void loadUserDataIntoGraph(StackExchangeTopicGraph graph,
									  DocumentBuilder dBuilder,
									  String userXMLFilePath) {
		
		NodeList users = getNodeListFromXMLFile(dBuilder, userXMLFilePath);
		
		for (int i = 0; i < users.getLength(); i++) {
			
			Node user = users.item(i);
			
			// if node is type 1 it is a user, so add it to the graph
			if (user.getNodeType() == 1) {
				// add user
			}
		}
		
	}
	
	public void loadPostDataIntoGraph(StackExchangeTopicGraph graph, 
			  						  DocumentBuilder dBuilder,
			  						  String postsXMLFilePath) {
		
		NodeList posts = getNodeListFromXMLFile(dBuilder, postsXMLFilePath);
		
		for (int i = 0; i < posts.getLength(); i++) {
			
			Node post = posts.item(i);
			
			// if node is type 1 it is a post, so add it to the graph
			if (post.getNodeType() == 1) {
				
				String postType = post.getAttributes().getNamedItem("PostTypeId").getNodeValue();
			
				// if the post is type 1 it is a question
				// if the post is type 2 it is an answer
				// these are the only elements in Posts.xml we care about
				if (Integer.parseInt(postType) == 1) {
					// add question
				}
				else if (Integer.parseInt(postType) == 2) {
					// add answer
				}
			}
		}
	}
	
	public void loadCommentDataIntoGraph(StackExchangeTopicGraph graph, 
			  							 DocumentBuilder dBuilder,
			  							 String commentsXMLFilePath) {
		
		NodeList comments = getNodeListFromXMLFile(dBuilder, commentsXMLFilePath);
		
		for (int i = 0; i < comments.getLength(); i++) {
			
			Node comment = comments.item(i);
			
			// if node is type 1 it is a comment, so add it to the graph
			if (comment.getNodeType() == 1) {
				// add comment
			}
		}
	}
	
	public void loadTagDataIntoGraph(StackExchangeTopicGraph graph, 
			  						 DocumentBuilder dBuilder,
			  						 String tagsXMLFilePath) {
		
		NodeList tags = getNodeListFromXMLFile(dBuilder, tagsXMLFilePath);
		
		for (int i = 0; i < tags.getLength(); i++) {
			
			Node tag = tags.item(i);
			
			// if node is type 1 it is a tag, so add it to the graph
			if (tag.getNodeType() == 1) {
				// add tag
			}
		}
	}
}
