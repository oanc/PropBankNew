package org.anc.propbank;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IEdge;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.DotRenderer;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.GrafRenderer;
import org.xces.graf.io.RenderException;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class New_PTBNavigator {
	 public static final Constants K = new Constants();
	 
	 private String fileName;
	 private GrafParser graphParse;
	 private IGraph graph;
	 private ArrayList<INode> sentences;
	 private INode rootNode;
	 private ArrayList<INode> terminalNodes;
	 //PropbanklocationMap will store each node as a key, and as its mapping,
	 //a hashmap with keys, "terminalID" if its a terminal ID and "propbankLocations"
	 private HashMap<INode, HashMap<String, String>> propbankLocationMap;
	 private HashMap<Integer, ArrayList<INode>> sentenceTerminalNodes;
	 
	 
	 
	 /**
	  * Constructor for class New_PTBNavigator
	  * @param root
	  * @throws SAXException
	  * @throws IOException
	  * @throws GrafException
	  */
	 public New_PTBNavigator(String fileName) throws SAXException, IOException, GrafException{
		 
		 this.sentenceTerminalNodes = new HashMap<Integer, ArrayList<INode>>();
		 this.terminalNodes = new ArrayList<INode>();
		 this.fileName = fileName;
		 File headerFile = new File(K.MASC_RESOURCE_HEADER);
		 ResourceHeader header = new ResourceHeader(headerFile);
		 this.propbankLocationMap = new HashMap<INode, HashMap<String, String>>();
		 this.graphParse = new GrafParser(header);
		 this.graph = graphParse.parse(fileName + "-ptb.xml");
		 for (INode node: this.graph.nodes()){
			 this.propbankLocationMap.put(node, new HashMap<String, String>());
		 }
		 this.rootNode = this.graph.getRoot();
		 this.sentences = new ArrayList<INode>();
		 this.setSentences();	 
		 for (int i = 0; i < this.sentences.size(); i++){
			 this.propbankLocationMap.get(this.sentences.get(i)).put("TerminalNode", "NO");
			 ArrayList<INode> terminals = this.setTerminalIDs(this.sentences.get(i));
			 this.sentenceTerminalNodes.put(i, new ArrayList<INode>());
			 this.sentenceTerminalNodes.get(i).addAll(terminals);
			 this.terminalNodes.removeAll(terminals);
	 	}
	 }
	 
	 /**
	  * Print the nodes of this.graph
	  * @throws RenderException
	  */
	 public void printPTBGraph() throws RenderException{
		 System.out.println(this.graph.toString());
		 GrafRenderer renderer = new GrafRenderer(System.out);
		 renderer.render(this.graph);
	 }
	 
	 /**
	  * Initialize the arraylist of INodes,  sentences, and sort them using an instance of AnchorComparator
	  */
	 private void setSentences(){
		 for (IEdge edge : this.rootNode.getOutEdges()){
			 this.sentences.add(edge.getTo());
		 }
		 Collections.sort(this.sentences, new AnchorComparator());
	 }
	 
	 
	 /**
	  * Create a dot file of this.graph to use with GraphViz to visualize
	 * @throws IOException 
	 * @throws RenderException 
	  */
	 public void makeDotFile() throws IOException, RenderException{
		 String[] fileRoot = this.fileName.split("/");
		 String root = fileRoot[1];
		 PrintWriter writer = new PrintWriter(new FileWriter("DotFiles/" + root + ".dot"));
		 DotRenderer renderer = new DotRenderer(writer);
		 renderer.render(this.graph);
	 }

		 
	 /**
	  * Update propbankLocationMap by adding new key, TerminalNode to each
	  * mapping's value map -- indicate whether or not its a terminal node 
	  * and if it is, add it to our arraylist, terminalNodes.
	  * @param sentence
	  */
	 private ArrayList<INode> setTerminalIDs(INode sentence){
	 	ArrayList<INode> currentChildren = new ArrayList<INode>();
	 	for (IEdge edge: sentence.getOutEdges()){
	 		currentChildren.add(edge.getTo());
	 	}
	 	Collections.sort(currentChildren, new AnchorComparator());
	 	for (INode childNode: currentChildren){
	 		if (childNode.outDegree() != 0){
	 			this.propbankLocationMap.get(childNode).put("TerminalNode", "NO");
	 			this.setTerminalIDs(childNode);
	 		}
	 		else {
	 			this.propbankLocationMap.get(childNode).put("TerminalNode", "YES");
	 			if (!this.terminalNodes.contains(childNode)){
	 			this.terminalNodes.add(childNode);
	 			}
	 		}
	 	}
	 	
	 	//////USE A DIFFERENT COMPARISON CLASS TO ORDER THESE!! RIGHT NOW WITHOUT COMPARATOR, TERMINAL NODES ARE SHOWING UP IN
	 	//// CORRECT ORDER EXCEPT FOR TRACE NODES WHICH SHOW UP RANDOMLY
	 	Collections.sort(this.terminalNodes, new AnchorComparator());
	 	return this.terminalNodes;
	 }
		 
	 
	 /**
	  * Print the details of each sentence's terminal nodes to the console.
	  */
	 public void printTerminalDetails(){
		 for (Integer key: this.sentenceTerminalNodes.keySet()){
			 System.out.println("Sentence " + key + ": ");
			 for (INode terminalNode: this.sentenceTerminalNodes.get(key)){
				 if (terminalNode.getAnnotation().getLabel().equals("Trace")){
					 System.out.println("TRACE NODE:" + terminalNode.getAnnotation().features().toString());
				 }
				 else{
				 System.out.println(terminalNode.getAnnotation().features().toString());
			 }
		 }
		 }
	 }
	 
	 
	 public INode navigate(Integer sentenceIndex, Integer terminalID){
		 return this.sentenceTerminalNodes.get(sentenceIndex).get(terminalID);
	 }	 
}
	 
