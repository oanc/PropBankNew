package org.anc.propbank;


import java.io.*;
import java.util.*;

import org.xces.graf.api.*;
import org.xces.graf.impl.CharacterAnchor;
import org.xces.graf.io.*;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class PTBNavigator {
	
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
	 public PTBNavigator(String fileName) throws SAXException, IOException, GrafException{
		 
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
			 Collections.sort(terminals, new newAnchorComparator());
			 this.fixTraces(terminals);
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
				 	CharacterAnchor reg1 = (CharacterAnchor) terminalNode.getLinks().get(0).getRegions().get(0).getAnchor(0);
				 	System.out.println(k + ". TRACE NODE:" + terminalNode.getAnnotation().features().toString() + reg1);
				 	}
				 else{
					 CharacterAnchor reg1 = (CharacterAnchor) terminalNode.getLinks().get(0).getRegions().get(0).getAnchor(0);
				 	System.out.println(k + ". " + terminalNode.getAnnotation().features().toString() + reg1);
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
		//Initialize a stack for the depth first search and an arraylist to store the 
		//nodes in the order in which they are visited
		Stack<INode> stack = new Stack<INode>();
		ArrayList<INode> DFSoutput = new ArrayList<INode>();
		
		//Push the sentence node to begin DFS
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
		
		//Clear all visited flags
		for (INode graphNode: this.graph.nodes()){
			graphNode.clear();
		}
		
		// Iterate through the arraylist DFSOutput and return all terminal nodes(i.e. either outDegree()==0 or trace node)
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
	 * Take a list of terminal nodes, move each trace node directly ahead of the terminal node with a corresponding anchor value.
	 * @param terminals
	 * @return
	 */
	private void fixTraces(ArrayList<INode> terminals){
		ArrayList<INode> terminalNodes = new ArrayList<INode>();
		terminalNodes.addAll(terminals);
		for (int i = 0; i < (terminalNodes.size() - 1); i ++){
			CharacterAnchor reg1 = (CharacterAnchor) terminalNodes.get(i).getLinks().get(0).getRegions().get(0).getAnchor(0);
			CharacterAnchor reg2 = (CharacterAnchor) terminalNodes.get(i + 1).getLinks().get(0).getRegions().get(0).getAnchor(0);
			if (reg1.equals(reg2)){
				if (terminalNodes.get(i + 1).getAnnotation().getLabel().equals("Trace")){
					int index = terminals.indexOf(terminalNodes.get(i+1));
					terminals.remove(terminalNodes.get(i+1));
					terminals.add(index-1, terminalNodes.get(i + 1));
				}
			}
		}
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

}
