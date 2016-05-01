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

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import graph.StackExchangeTopicGraph;
import graph.UserNode;

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
     * Posts.xml, User.xml, Tags.xml, Comments.xml
     * @param graph the StackExchangeTopicGraph to populate
     * @param directoryWithXMLFiles the directory with all of a Stack Exchange
     * topic's data in the form of XML files found at the data dump.
     */
	public static void populateStackExchangeTopicGraph(StackExchangeTopicGraph graph, 
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
		
		// add relevant metadata to the graph
		// needs to be done first because QuestionNode constructor depends on
		// referencing correct Tag objects
		loadTagsIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Tags.xml");
		
		// add relevant vertices to the graph
		// order is important here because a an answer/comment must already have a parent
		// and any vertex must already have a user
		// to use same method for questions and answers (they are both in Posts.xml)
		// could instead sort Posts.xml data ascending by post type
		loadUsersIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Users.xml");
		loadQuestionsIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Posts.xml");
		loadAnswersIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Posts.xml");
		loadCommentsIntoGraph(graph, dBuilder, directoryWithXMLFiles + "Comments.xml");
		
		// add all the edges to the graph
		graph.addAllEdges();
	}
	
	public static Document getXMLFileDOM(DocumentBuilder dBuilder,
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
		
		return xmlFileDOM;
	}
	
	private static void loadUsersIntoGraph(StackExchangeTopicGraph graph,
								          DocumentBuilder dBuilder,
								          String userXMLFilePath) {
		
		Document xmlFileDOM = getXMLFileDOM(dBuilder, userXMLFilePath);
		
		NodeList usersParent = xmlFileDOM.getChildNodes();
		NodeList users = usersParent.item(0).getChildNodes();
		
		for (int i = 0; i < users.getLength(); i++) {
			
			Node user = users.item(i);
			
			// if node is type 1 it is a user, so add it to the graph
			if (user.getNodeType() == 1) {

				graph.addVertex(graph.getUniqueVertexIDCounter(), 
								user, StackExchangeTopicGraph.USER);
			}
		}
		
	}
	
	private static void loadQuestionsIntoGraph(StackExchangeTopicGraph graph, 
			  					         	  DocumentBuilder dBuilder,
			  					         	  String postsXMLFilePath) {
		
		Document xmlFileDOM = getXMLFileDOM(dBuilder, postsXMLFilePath);
		
		NodeList postsParent = xmlFileDOM.getChildNodes();
		NodeList posts = postsParent.item(0).getChildNodes();
		
		for (int i = 0; i < posts.getLength(); i++) {
			
			Node post = posts.item(i);
			
			// if node is type 1 it is a post, so add it to the graph
			if (post.getNodeType() == 1) {
				
				String postType = post.getAttributes().getNamedItem("PostTypeId").getNodeValue();
			
				// if the post is type 1 it is a question
				if (Integer.parseInt(postType) == 1) {
					
					// if the question's user has been deleted,
					// create a dummy user and add it to the graph
					// and set the question's user
					if (post.getAttributes().getNamedItem("OwnerUserId") == null) {
					
						UserNode user = graph.createDummyUser(graph.getUniqueVertexIDCounter());
						graph.addVertex(user);
						
						Attr OwnerUserId = xmlFileDOM.createAttribute("OwnerUserId");
						OwnerUserId.setValue(Integer.toString(user.getUserID()));
						
						post.getAttributes().setNamedItem(OwnerUserId);
					}
					
					// only then add the question with the dummy user as author
					graph.addVertex(graph.getUniqueVertexIDCounter(), 
							post, StackExchangeTopicGraph.QUESTION);
				}
				else if (post.getNodeType() != 2) {
					// disallow if not a question or answer
					graph.getDisallowedPosts().put(Integer.parseInt(
							post.getAttributes().getNamedItem("Id").getNodeValue()),
							true);
				}
			}
		}
	}
	

	private static void loadAnswersIntoGraph(StackExchangeTopicGraph graph, 
											DocumentBuilder dBuilder,
											String postsXMLFilePath) {

		Document xmlFileDOM = getXMLFileDOM(dBuilder, postsXMLFilePath);
		
		NodeList postsParent = xmlFileDOM.getChildNodes();
		NodeList posts = postsParent.item(0).getChildNodes();

		for (int i = 0; i < posts.getLength(); i++) {

			Node post = posts.item(i);

			// if node is type 1 it is a post, so add it to the graph
			if (post.getNodeType() == 1) {

				String postType = post.getAttributes().getNamedItem("PostTypeId").getNodeValue();

				// if the post is type 2 it is an answer
				if (Integer.parseInt(postType) == 2) {

					// if the question's user has been deleted,
					// create a dummy user and add it to the graph
					// and set the answer's user
					if (post.getAttributes().getNamedItem("OwnerUserId") == null) {
					
						UserNode user = graph.createDummyUser(graph.getUniqueVertexIDCounter());
						graph.addVertex(user);
						
						Attr OwnerUserId = xmlFileDOM.createAttribute("OwnerUserId");
						OwnerUserId.setValue(Integer.toString(user.getUserID()));
						
						post.getAttributes().setNamedItem(OwnerUserId);
					}
					
					// only then add the answer with the dummy user as author
					graph.addVertex(graph.getUniqueVertexIDCounter(), 
							post, StackExchangeTopicGraph.ANSWER);
				}
			}
		}
	}
	
	private static void loadCommentsIntoGraph(StackExchangeTopicGraph graph, 
											 DocumentBuilder dBuilder,
											 String commentsXMLFilePath) {
		
		Document xmlFileDOM = getXMLFileDOM(dBuilder, commentsXMLFilePath);
		
		NodeList commentsParent = xmlFileDOM.getChildNodes();
		NodeList comments = commentsParent.item(0).getChildNodes();
		
		for (int i = 0; i < comments.getLength(); i++) {
			
			Node comment = comments.item(i);
			
			// if node is type 1 it is a comment, so add it to the graph
			if (comment.getNodeType() == 1) {
				
				// if the comment's user has been deleted,
				// create a dummy user and add it to the graph
				// and set the comments's user
				if (comment.getAttributes().getNamedItem("UserId") == null) {
				
					UserNode user = graph.createDummyUser(graph.getUniqueVertexIDCounter());
					graph.addVertex(user);
					
					Attr UserId = xmlFileDOM.createAttribute("UserId");
					UserId.setValue(Integer.toString(user.getUserID()));
					
					comment.getAttributes().setNamedItem(UserId);
				}
				
				// only then add the comment with the dummy user as author
				graph.addVertex(graph.getUniqueVertexIDCounter(), 
								comment, StackExchangeTopicGraph.COMMENT);
			}
		}
	}
	
	private static void loadTagsIntoGraph(StackExchangeTopicGraph graph, 
										 DocumentBuilder dBuilder,
										 String tagsXMLFilePath) {
		
		Document xmlFileDOM = getXMLFileDOM(dBuilder, tagsXMLFilePath);
		
		NodeList tagsParent = xmlFileDOM.getChildNodes();
		NodeList tags = tagsParent.item(0).getChildNodes();
		
		for (int i = 0; i < tags.getLength(); i++) {
			
			Node tag = tags.item(i);
			
			// if node is type 1 it is a tag, so add it to the graph
			if (tag.getNodeType() == 1) {
				
				graph.addTagToGraph(graph.createTagFromDOMNode(tag));
			}
		}
	}
}
