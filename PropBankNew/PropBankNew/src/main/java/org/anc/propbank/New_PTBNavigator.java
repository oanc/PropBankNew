package org.anc.propbank;


import java.io.*;
import java.util.*;
import org.xces.graf.api.*;
import org.xces.graf.io.*;
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
		 
		 /// Initialize all fields
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
		 ///Iterate through sentences and initialize the hashmap sentenceTerminalNodes, each mapping
		 /// will map an integer (the sentence index) to an arrayList of that sentence's terminal nodes
		 /// in sorted order -- using this map we can navigate to a given terminal ID in a given sentence
		 for (int i = 0; i < this.sentences.size(); i++){
			 this.propbankLocationMap.get(this.sentences.get(i)).put("TerminalNode", "NO");
			 ArrayList<INode> terminals = this.setTerminalIDs(this.sentences.get(i));
			 this.sentenceTerminalNodes.put(i, new ArrayList<INode>());
			 this.sentenceTerminalNodes.get(i).addAll(terminals);
			 this.terminalNodes.removeAll(terminals);
	 	}
		 System.out.println("PROPBANK LOCATION MAP:" + this.propbankLocationMap.toString());
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
	 	
	 	Collections.sort(this.terminalNodes, new AnchorComparator());
	 	ArrayList<INode> traces = new ArrayList<INode>();
	 	for (INode terminalNode: this.terminalNodes){
	 		if (terminalNode.getAnnotation().getLabel().equals("Trace")){
	 			traces.add(terminalNode);
	 		}
	 	}
	 	for (INode traceNode: traces){
	 			int index;
	    		INode neighborNode = this.findNeighboringNodes(traceNode);
	    		if (this.terminalNodes.contains(neighborNode)){
	    			index = this.terminalNodes.indexOf(neighborNode);
	    		}
	    		else{
	    			this.terminalNodes.add(neighborNode);
	    			Collections.sort(this.terminalNodes, new AnchorComparator());
	    			index = this.terminalNodes.indexOf(neighborNode);
	    			this.terminalNodes.remove(neighborNode);
	    		}
	    		this.terminalNodes.remove(traceNode);
	    		this.terminalNodes.add(index, traceNode);
	 	}
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
					 System.out.println("NEIGHBORING:" + this.findNeighboringNodes(terminalNode).getAnnotation().features().toString());
				 }
				 else{
				 System.out.println(terminalNode.getAnnotation().features().toString());
			 }
		 }
		 }
	 }
	 
	 /**
	  * Navigate to a given terminal ID in a given sentence using the hashmap sentenceTerminalNodes.
	  * @param sentenceIndex
	  * @param terminalID
	  * @return
	  */
	 public INode navigateTerminals(Integer sentenceIndex, Integer terminalID){
		 return this.sentenceTerminalNodes.get(sentenceIndex).get(terminalID);
	 }	 

	 /**
	  * Navigate to a node using its sentenceIndex, terminalID and depth.
	  * @param sentenceIndex
	  * @param terminalID
	  * @param depth
	  * @return
	  */
	 public INode navigate(Integer sentenceIndex, Integer terminalID, Integer depth){
		 INode returnNode = this.navigateTerminals(sentenceIndex, terminalID);
		 int i = 0;
		 while (i < depth){
			 returnNode = returnNode.getParent();
			 i++;
		 }
		 return returnNode;
	 }
	 
	/**
	 * Return the terminal node neighboring a given trace node, to then be used for sorting. 
	 * @param traceNode
	 * @return
	 */
	private INode findNeighboringNodes(INode traceNode){
		Stack<INode> stack = new Stack<INode>();
		ArrayList<INode> DFSoutput = new ArrayList<INode>();
		stack.push(this.rootNode);
		this.rootNode.visit();
		DFSoutput.add(this.rootNode);
		while(!stack.isEmpty()){
			INode node = stack.peek();
			ArrayList<INode> unvisitedChildren = new ArrayList<INode>();
			for(IEdge edge: node.getOutEdges()){
				if (edge.getTo().visited() == false){
					unvisitedChildren.add(edge.getTo());
				}
			}
			if (!unvisitedChildren.isEmpty()){
				INode child = unvisitedChildren.get(0);
				stack.push(child);
				child.visit();
				DFSoutput.add(child);
			}
			else{
				stack.pop();
			}
		}	
		for (INode graphNode: this.graph.nodes()){
			graphNode.clear();
		}
		ArrayList<INode> terminals = new ArrayList<INode>();
		for (INode outputNode: DFSoutput){
			if (outputNode.outDegree() == 0){
				terminals.add(outputNode);
			}
		}
		Integer index = terminals.indexOf(traceNode);
		return terminals.get(index-1);
	}
	
	
	//-----RENDERING FUNCTIONS-----//
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

	 
}
