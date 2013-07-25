package org.anc.propbank;


import java.io.*;
import java.util.*;

import org.xces.graf.api.*;
import org.xces.graf.impl.CharacterAnchor;
import org.xces.graf.io.*;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class FixedPTBNavigator {
	
	//---FIELDS---//
	 public static final Constants K = new Constants();
	 private String fileName;
	 private GrafParser graphParse;
	 private IGraph graph;
	 private ArrayList<INode> sentences;
	 private INode rootNode;
	 private HashMap<Integer, ArrayList<INode>> sentenceTerminalNodes;
	 
	 /**
	  * Constructor for class New_PTBNavigator
	  * @param root
	  * @throws SAXException
	  * @throws IOException
	  * @throws GrafException
	  */
	 public FixedPTBNavigator(String fileName) throws SAXException, IOException, GrafException{
		 
		 /// Initialize all fields
		 this.sentenceTerminalNodes = new HashMap<Integer, ArrayList<INode>>();
		 this.fileName = fileName;
		 File headerFile = new File(K.MASC_RESOURCE_HEADER);
		 ResourceHeader header = new ResourceHeader(headerFile);
		 this.graphParse = new GrafParser(header);
		 this.graph = graphParse.parse(fileName + "-ptb.xml");
		 this.rootNode = this.graph.getRoot();
		 this.sentences = new ArrayList<INode>();
		 this.setSentences();	 

		 ///Iterate through sentences and initialize the hashmap sentenceTerminalNodes, each mapping
		 /// will map an integer (the sentence index) to an arrayList of that sentence's terminal nodes
		 /// in sorted order -- using this map we can navigate to a given terminal ID in a given sentence
		 for (int i = 0; i < this.sentences.size(); i++){
			 this.sentenceTerminalNodes.put(i, new ArrayList<INode>());
			 ArrayList<INode> terminals = this.depthFirstSearch(this.sentences.get(i));
			 ArrayList<INode> traceNodes = this.isolateTraces(terminals); 
			 Collections.sort(terminals, new AnchorComparator());
			 //////ONLY WORKING ON SENTENCES WITH 1 TYPE OF TRACE NODE (i.e. all 0-degree or all 1-degree) -- FIX THIS!!//
			 this.sortTerminals(this.sentences.get(i), terminals, traceNodes);
			 this.sentenceTerminalNodes.get(i).addAll(terminals);
	 	}
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
	  * Print the details of each sentence's terminal nodes to the console.
	  */
	 public void printTerminalDetails(){
		 for (int i = 0; i < this.sentenceTerminalNodes.keySet().size(); i++){
			 int key = i;
			 System.out.println("Sentence " + key + ": ");
			 for (int k = 0; k < this.sentenceTerminalNodes.get(key).size(); k++){
				 INode terminalNode = this.sentenceTerminalNodes.get(key).get(k);
				 if (terminalNode.getAnnotation().getLabel().equals("Trace")){
				 		System.out.println(k + ". TRACE NODE:" + terminalNode.getAnnotation().features().toString());
				 		//System.out.println(k + ". NEIGHBORING:" + this.findNeighboringNodes(this.sentences.get(key), terminalNode).getAnnotation().features().toString());
				 }
				 else{
				 	System.out.println(k + ". " + terminalNode.getAnnotation().features().toString());
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
	  * Depth first search a sentence tree to find all terminal nodes, including trace nodes that may have an out degree of 1. 
	  * @param sentence
	  * @return
	  */
	private ArrayList<INode>  depthFirstSearch(INode sentence){
		Stack<INode> stack = new Stack<INode>();
		ArrayList<INode> DFSoutput = new ArrayList<INode>();
		stack.push(sentence);
		sentence.visit();
		DFSoutput.add(sentence);
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
		for (int i = 0; i < DFSoutput.size(); i++){
			if (DFSoutput.get(i).annotated()){
				if ((DFSoutput.get(i).outDegree() == 0) || (DFSoutput.get(i).getAnnotation().getLabel().equals("Trace"))){
						terminals.add(DFSoutput.get(i));
			}
			}
		}
		return terminals;
	}

	
	/**
	 * Take a list of terminal nodes, remove all of the trace nodes and return an ArrayList containing those trace
	 * nodes -- these will then be inserted in the appropriate place. 
	 * @param terminals
	 * @return
	 */
	private ArrayList<INode> isolateTraces(ArrayList<INode> terminals){
		ArrayList<INode> terminalNodes = new ArrayList<INode>();
		ArrayList<INode> traces = new ArrayList<INode>();
		terminalNodes.addAll(terminals);
		for (INode terminal: terminalNodes){
			if (terminal.getAnnotation().getLabel().equals("Trace")){
				terminals.remove(terminal);
				traces.add(terminal);
			}
		}
		return traces;
	}
	
	
//-- ACCESSOR FUNCTIONS FOR TESTING -- //
	
	public HashMap<Integer, ArrayList<INode>> getSentenceTerminalNodes(){
		return this.sentenceTerminalNodes;
	}
	
	public ArrayList<INode> getSentences(){
		return this.sentences;
	}
	
	public IGraph getGraph(){
		return this.graph;
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
 * Use depth first search to return the terminal node neighboring a given trace node (OF DEGREE 0), to then be used for sorting. 
 * @param traceNode
 * @return
 */
private INode findNeighboringNodes(INode sentence, INode traceNode){
	Stack<INode> stack = new Stack<INode>();
	ArrayList<INode> DFSoutput = new ArrayList<INode>();
	stack.push(sentence);
	sentence.visit();
	DFSoutput.add(sentence);
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
	
	///Trace node has out degree 0
	if (traceNode.outDegree() == 0){
		ArrayList<INode> terminals = new ArrayList<INode>();
		for (INode outputNode: DFSoutput){
			if (outputNode.outDegree() == 0){
				terminals.add(outputNode);
			}
		}
		Integer index = terminals.indexOf(traceNode);
		if (index != 0){
			return terminals.get(index-1);
		}
		else{
			return terminals.get(index);
		}
	}
	
	// Else trace node has out degree 1, *PRO*-1, *-1, *T*-1, etc. 
	else{
		ArrayList<INode> terminals = new ArrayList<INode>();
		for (INode outputNode: DFSoutput){
			if (outputNode.annotated()){
				if ((outputNode.outDegree() == 0) || outputNode.getAnnotation().getLabel().equals("Trace")){
					terminals.add(outputNode);
				}
			}
		}
			Integer index = terminals.indexOf(traceNode);
			if (index != 0){
			return terminals.get(index-1);
			}
			else{
				return terminals.get(index);
			}
		
	}
	
}

/**
 * Sort the terminal nodes of a sentence, taking in the terminal (but not trace) nodes and trace nodes.
 * @param sentence
 * @param terminals
 * @param traceNodes
 */
private void sortTerminals(INode sentence, ArrayList<INode> terminals, ArrayList<INode> traceNodes){
	ArrayList<INode> terminalList = new ArrayList<INode>();
	for (INode traceNode: traceNodes){
		terminalList.addAll(terminals);
		int index;
		INode neighborNode = this.findNeighboringNodes(sentence, traceNode);
		if (neighborNode != traceNode){
			if (terminals.contains(neighborNode)){
				index = terminals.indexOf(neighborNode);
			}
			else{
				terminalList.add(neighborNode);
				Collections.sort(terminalList, new AnchorComparator());
				index = terminalList.indexOf(neighborNode);
			}
			terminals.add(index, traceNode);
		}
		else{
			terminals.add(0, traceNode);
	}
		terminalList.clear();
}
}





}
