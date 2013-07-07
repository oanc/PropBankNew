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
import org.xml.sax.SAXException;

public class New_PropbankParser {
	
	private String PTBDirectory;
	private String targetFile;
	private IGraph graph;
	private IDGenerator id;
	
	/**
	 * Constructor for class New_PropbankParser
	 * @param path
	 */
	public New_PropbankParser(String path){
		this.PTBDirectory = path;
		this.id = new IDGenerator();
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
	 */
	private void processLine(String line){
		//INode propbankRoot = makeNode("PropBank");
		//IAnnotation annotation = propbankRoot.getAnnotation();
		
		ArrayList<String> features = new ArrayList<String>(Arrays.asList(line.split(" ")));
		
		String sentenceIndex = features.get(0);
		
		HashMap<String, ArrayList<String>> argumentInfo = new HashMap<String, ArrayList<String>>();
		for (int i = 6; i < features.size(); i++){
	
			processArgument(features.get(i), argumentInfo);
		
		}
		
		System.out.println("Argument Info Map: " + argumentInfo);
		///STUFF TO ADD NODES TO GRAPH
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