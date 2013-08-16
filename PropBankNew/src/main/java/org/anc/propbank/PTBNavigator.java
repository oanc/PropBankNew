package org.anc.propbank;


import java.io.*;
import java.util.*;

import org.xces.graf.api.*;
import org.xces.graf.io.*;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class PTBNavigator {
	 public static final Constants K = new Constants();
	 
	 private String fileName;
	 private GrafParser graphParse;
	 private IGraph graph;
	 private ArrayList<INode> sentences;
	 private INode rootNode;
	 private ArrayList<INode> terminalNodes;
	 private HashMap<Integer, ArrayList<INode>> sentenceTerminalNodes;
	 
	 
	 
	 /**
	  * Constructor for class New_PTBNavigator
	  * @param root
	  * @throws SAXException
	  * @throws IOException
	  * @throws GrafException
	  */
	 public PTBNavigator(String fileName) throws SAXException, IOException, GrafException{
		 
		 /// Initialize all fields
		 this.sentenceTerminalNodes = new HashMap<Integer, ArrayList<INode>>();
		 this.terminalNodes = new ArrayList<INode>();
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
			 ArrayList<INode> terminals = this.depthFirstSearch(this.sentences.get(i));
			 //this.sortNodes(this.sentences.get(i), terminals);
			 this.sentenceTerminalNodes.put(i, new ArrayList<INode>());
			 this.sentenceTerminalNodes.get(i).addAll(terminals);
			 this.terminalNodes.removeAll(terminals);
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
		 for (Integer key: this.sentenceTerminalNodes.keySet()){
			 System.out.println("Sentence " + key + ": ");
			 for (INode terminalNode: this.sentenceTerminalNodes.get(key)){
				 if (terminalNode.getAnnotation().getLabel().equals("Trace")){
					 System.out.println("HEYTRACE NODE:" + terminalNode.getAnnotation().features().toString() + terminalNode.outDegree());
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
	 * Sort the terminal nodes of a sentence.
	 * @param sentence
	 * @param nodeList
	 */
	private void sortNodes(INode sentence, ArrayList<INode> nodeList){
		ArrayList<INode> outDegreeZeroNodes = new ArrayList<INode>();
		for (INode node: nodeList){
			if(!node.getAnnotation().getLabel().equals("Trace")){
				outDegreeZeroNodes.add(node);
			}
		}
		Collections.sort(outDegreeZeroNodes, new AnchorComparator());
		
		nodeList.removeAll(outDegreeZeroNodes);
		
		for (INode traceNode: nodeList){
				int index;
	    		INode neighborNode = this.findNeighboringNodes(sentence, traceNode);
	    		if (neighborNode != traceNode){
	    			if (outDegreeZeroNodes.contains(neighborNode)){
	    				index = outDegreeZeroNodes.indexOf(neighborNode);
	    			}
	    			else{
	    				outDegreeZeroNodes.add(neighborNode);
	    				Collections.sort(outDegreeZeroNodes, new AnchorComparator());
	    				index = outDegreeZeroNodes.indexOf(neighborNode);
	    				outDegreeZeroNodes.remove(neighborNode);
	    			}
	       		outDegreeZeroNodes.remove(traceNode);
	       		outDegreeZeroNodes.add(index, traceNode);
			}
	    		else{
	    			outDegreeZeroNodes.remove(traceNode);
	    			outDegreeZeroNodes.add(0, traceNode);
	    		}
		}
		nodeList.removeAll(outDegreeZeroNodes);
		nodeList.addAll(outDegreeZeroNodes);
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
}
