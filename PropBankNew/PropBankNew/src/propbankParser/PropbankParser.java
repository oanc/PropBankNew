package propbankParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;


import org.xml.sax.SAXException;

import ptbNavigator.PTBNavigator;

public class PropbankParser 
{
	public PropbankParser(String PTBPath)
	{
		PTBDirectory = PTBPath;
	}
	
	/** This method processes a PropBank file line by line
	 *  @input input file should be a PropBank .prop file
	 *  @output right now, void, root node of the annotation?
	 */
	public IGraph process(File infile) throws IOException, FileNotFoundException, SAXException
	{
		/** Creates a buffered reader to process a PropBank file
		 *	@throws FileNotFoundException
		 */
		BufferedReader in = new BufferedReader(new FileReader(infile));
		
		/** String to keep track of the next line in the input file */
		String currLine = in.readLine();
		
		
		
		/*  read each line from the file and pass it to parseLine()
		 *  method to extract the relevant information
		 */
		do
		{
			//TODO: remove print
			System.out.println(currLine);
			
			//pass currLine to parseLine to be analyzed
			processLine(currLine);
			
			
			//set currLine to the next line in the file
			currLine = in.readLine();
		}
		//while there are lines to be read in the buffered reader,
		//keep going ***is this while too far separated?
		while(currLine != null);
		
		//once every line has been processed, return the IGraph
		return graph;

	} //end of process method
	
	/* LINE HELPER FUNCTION */
	
	protected void processLine(String line) throws SAXException, IOException
	{
		/** this node is the root for the entire sentence, or Predicate Argument Structure */
		INode pas = makeNode("PAStructure");
		
		/** the annotation for the Predicate Argument Structure */
		IAnnotation a = pas.getAnnotation();
		
		/* Break the line up into its various parts */
		ArrayList<String> features = new ArrayList<String>(Arrays.asList(line.split(" ")));
		
		/* Store the parts temporarily */
		
			/* the first part holds the target file necessary for navigating 
			 *  to the node-anchors of this annotation
			 */
			String tokPath= features.get(0);
			String fileName = tokPath.substring(tokPath.lastIndexOf('/'));//parse out the file name
			targetFile = fileName.substring(0, fileName.lastIndexOf('.'));//parse out the bare file name
			
			
			/* the second part holds the index of the sentence of this 
			 * annotation, for later processing with PTBHelper
			 */
			sentenceIndex = Integer.parseInt(features.get(1));
			
			/* the fourth element of the prop annotation is the annotator's
			 * ID, add to the PAStructure's annotation
			 */
			a.addFeature("annotator", features.get(3));
			
			/* the lemma of the word that anchors the annotation,
			 * add to the PAStructure's annotation
			 */
			a.addFeature("lemma", features.get(4));
			
			/* the sixth element of the annotation is the frameset, which
			 * indexes the sense of the word that anchors the annotation,
			 * add to the PAStructure's annotation
			 */
			a.addFeature("frameset", features.get(5));
			
			/* initialize the PTB Navigator */
			PTBNav = new PTBNavigator(PTBDirectory+targetFile);

			/* the remaining elements are the arguments for this PAStructure
			 * which are incrementally added as edges to the pas node
			 */
			for( int i = 7; i < features.size(); i++)
			{
				/** temporarily store the argument
				 */
				String arg = features.get(i);
				
				//process the argument, return the resulting node
				INode node = processArgument(arg);
				
				/* link this node to the PAStructure */
				graph.addEdge(pas,node);
				
			} //end of for loop for argument processing
	
		
	}
	
	/* ARGUMENT PROCESSING */
	/** This method takes in the string argument from a PropBank line and 
	 * returns a node with a link to the corresponding PTB node
	 */
	protected INode processArgument(String argument)
	{
		//separate the label from its positional indices
		String[] arg = argument.split("-", 2);
		String pos = arg[0]; //positional indices
		String label = arg[1]; //label
		
		//make a new node to represent this argument
		INode node = makeNode(label);
		
		//array to hold referents
		ArrayList<INode> referents;
		
		//if there's a "*", the first node traces to the following nodes
		if(pos.contains("*"))
		{
			//get the nodes
			referents = processStars(pos);
			
			//link them to the main node
			for(INode n : referents)
			{
				graph.addEdge(node, n);
			}
			
			//add "trace" to the edges of nodes following the first
			for(int i = 1; i < node.outDegree(); i++)
			{
				node.getOutEdge(i).addAnnotation("trace");
			}
		}
		//if there's a ",", the label applies to multiple concatenated nodes
		else if (pos.contains(","))
		{
			//get the nodes
			referents = processCommas(pos);
			
			//link them to the main node
			for(INode n : referents)
			{
				graph.addEdge(node, n);
			}
			
			//add "concatenated" to the edges of nodes following the first
			for(int i = 0; i < node.outDegree(); i++)
			{
				node.getOutEdge(i).addAnnotation("concatenated");
			}
		}
		else
		{
			//the usual case, there's only one node
			graph.addEdge(node, PTBHelper(pos));
		}
		
		
		return node;
		
	}
	
	/** Helper for the {@link #processArgument(String)} procedure
	 * splits up the string given, finds the corresponding PTBNodes
	 * and returns them as an array of INodes
	 * 
	 * @param pos - String indicating the position in a PennTreeBank representation
	 * @return
	 */
	protected ArrayList<INode> processStars(String pos)
	{
		//Array List to store INodes
		ArrayList<INode> nodes = new ArrayList<INode>();
		
		String pattern = "\\*";
		//get the positions of the nodes
		String[] nodePos = pos.split(pattern);

		//add the nodes
		for(String node: nodePos)
		{
			INode n = PTBHelper(node);
			nodes.add(n);
		}
		
		
		return nodes;
	}
	
	/** Helper for the {@link #processArgument(String)} procedure
	 * splits up the string given, finds the corresponding PTBNodes
	 * and returns them as an array of INodes
	 * 
	 * @param pos - String indicating the position in a PennTreeBank representation
	 * @return
	 */
	protected ArrayList<INode> processCommas(String pos)
	{
		//Array List to store INodes
		ArrayList<INode> nodes = new ArrayList<INode>();
		
		String pattern = ",";
		//get the nodes' positional indices
		String[] nodePos = pos.split(pattern);
		
		for(String node: nodePos)
		{
			INode n = PTBHelper(node);
			nodes.add(n);
		}
		
		return nodes;
	}
	
	/** Helper for the {@link #processArgument(String)}, {@link #processCommas(String)},
	 * and {@link #processStars(String)} methods, splits up the string given, finds the
	 * corresponding PTB node and returns it
	 * 
	 * @param pos - String indicating the position in a PennTreeBank representation
	 * @return
	 */
	protected INode PTBHelper(String pos)
	{
		//get the positions of the node
		String[] positions = pos.split(":");
		
		//navigate to the position, and return the resulting node
		return PTBNav.navigate(sentenceIndex, Integer.parseInt(positions[0]), 
									   Integer.parseInt(positions[1]));
	}
	
	
	/* IGRAPH HELPER FUNCTION */
	/** This function makes an INode to anchor an IAnnotation which will be
	 * created using the label given as input. Returns an INode with the 
	 * desired IAnnotation attached.
	 * @param label String - the label for the annotation to be created*/
	protected INode makeNode(String label)
	{
		INode node = Factory.newNode(id.generate("a")); //create a new node with a unique ID
		IAnnotation a = node.addAnnotation(label); //add the input label as its annotation
		propBankSet.addAnnotation(a); //add the annotation to the propBankSet
		graph.addNode(node); //add the node to the graph
		return node;
	}
	
	
	/* LOCAL VARIABLES */
	/** Create a new graph to hold all our new annotations */
	protected IGraph graph = Factory.newGraph();
	
	/** This allows us to generate new ids for each Propbank annotation we create */
	protected IDGenerator id = new IDGenerator();
	
	/** This set allows us to define the annotations we create as of type Propbank */
	protected IAnnotationSet propBankSet = Factory.newAnnotationSet(AnnotationSets.PROPBANK.NAME, AnnotationSets.PROPBANK.TYPE);
	
	/** Instantiate a PTBNavigator */
	protected PTBNavigator PTBNav;
	
	/** keeps track of the path to the directory where the PTB files are */
	String PTBDirectory;
	
	/** keeps track of the target file path of a PropBank annotation */
	String targetFile;
	
	/** stores the sentence location of a particular PropBank annotation */
	int sentenceIndex;
	
	/** stores the name of the annotator for a particular PropBank annotation*/
	String annotatorID;
	
	/** stores the lemma of the word anchoring the annotation */
	String lemma;
	
	/** stores the particular sense of the word anchoring the annotation */
	String frameset;

}
