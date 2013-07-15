package org.anc.propbank;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.anc.util.IDGenerator;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IAnnotation;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.impl.Factory;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class New_PropbankParser {
	
	private String PTBDirectory;
	private String targetFile;
	private IGraph graph;
	private IDGenerator id;
	public static final Constants K = new Constants();
	
	/**
	 * Constructor for class New_PropbankParser
	 * @param path
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws GrafException 
	 */
	public New_PropbankParser(String path) throws IOException, SAXException, GrafException{
		this.PTBDirectory = path;
		this.id = new IDGenerator();
		File headerFile = new File(K.MASC_RESOURCE_HEADER);
		ResourceHeader header = new ResourceHeader(headerFile);
		GrafParser graphParse = new GrafParser(header);
		this.graph = graphParse.parse(K.TEST_DATA_PATH + "/" + K.TEST_FILE + "-ptb.xml");
	}
	
	/**
	 * Process the inputted propbank file.
	 * @param infile
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws SAXException
	 * @throws GrafException
	 */
	public IGraph process(File infile) throws IOException, FileNotFoundException, SAXException, GrafException{
		
		// Isolate the file name without extension
		this.targetFile = infile.getName().substring(0, infile.getName().lastIndexOf('.'));
		
		System.out.println("targetFile = " + this.targetFile);
		
		//Initialize a BufferedReader to process the propbank file
		BufferedReader reader = new BufferedReader(new FileReader(infile));
		String currLine = reader.readLine();
		
		while(currLine != null){
			System.out.println(currLine);
			processLine(currLine);
			currLine = reader.readLine();
		}
		return this.graph;
	}
	
	
	/**
	 * Create a new instance of INode with given input string as its label.
	 * @param label
	 * @return
	 */
	private INode makeNode(String label){
		
		INode node = Factory.newNode(id.generate("pb-n"));
		IAnnotation annotation = node.addAnnotation(id.generate("pb-a"), label);
		this.graph.addNode(node);
		return node;
	}
	
	
	/**
	 * Process an individual line of a propbank file.
	 * @param line
	 * @throws GrafException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private void processLine(String line) throws SAXException, IOException, GrafException{
		//INode propbankRoot = makeNode("PropBank");
		//IAnnotation annotation = propbankRoot.getAnnotation();
		
		ArrayList<String> features = new ArrayList<String>(Arrays.asList(line.split(" ")));
		
		String sentenceIndex = features.get(0);
		
		HashMap<String, ArrayList<String>> argumentInfo = new HashMap<String, ArrayList<String>>();
		for (int i = 6; i < features.size(); i++){
	
			processArgument(features.get(i), argumentInfo);
		
		}
		
		System.out.println("Argument Info Map: " + argumentInfo);
		
		///----- ADD NEW NODES AND EDGES TO GRAPH ------//
		
		New_PTBNavigator navigator = new New_PTBNavigator(K.TEST_DATA_PATH + "/" + K.TEST_FILE);
		
		INode propbankNode = this.makeNode("PropBank");
		for (String argument: argumentInfo.keySet()){
			INode argNode = this.makeNode(argument);
			this.graph.addEdge(propbankNode, argNode);
			for (String pos : argumentInfo.get(argument)){
				String[] splitPos = pos.split(":", 2);
				INode targetNode = navigator.navigate(Integer.parseInt(sentenceIndex), Integer.parseInt(splitPos[0]), Integer.parseInt(splitPos[1]));
				this.graph.addEdge(argNode, targetNode);
			}
		}
	}
	
	
	/**
	 * Process the string argument and add information to the line's argumentInfo hashmap
	 * @param argument
	 * @param argumentInfo
	 */
	private void processArgument(String argument, HashMap<String, ArrayList<String>> argumentInfo){
		//Split up and store position and label of the given argument
		String[] arg = argument.split("-", 2);
		String pos = arg[0];
		String label = arg[1];
		
		argumentInfo.put(label, new ArrayList<String>());

		System.out.println(label + ": " + pos);
		
		if (pos.contains("*")){
			String[] nodes = pos.split("\\*");
			for (String node: nodes){
				argumentInfo.get(label).add(node);
			}
		}
		
		else{
			if (pos.contains(",")){
				String[] nodes = pos.split(",");
				for (String node: nodes){
					argumentInfo.get(label).add(node);
				}	
			}
			else{
				argumentInfo.get(label).add(pos);
			}
		}
	}
	
	
	
	
}
