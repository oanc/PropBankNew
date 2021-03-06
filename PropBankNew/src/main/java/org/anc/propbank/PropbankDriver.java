package org.anc.propbank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.anc.io.UTF8Reader;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.DotRenderer;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.GrafRenderer;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;


public class PropbankDriver {

	public static final Constants K = new Constants();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, GrafException
	{
		//Initialize path to propbank file
		File testDir = new File(K.PROP_DATA_PATH);		
		File testFile = new File(testDir, K.INPUT_FILE + ".prop");
		
		//Initialize New_PropbankParser/
		System.out.println("=====================================");
		System.out.println("============PROPBANK INFO============");		
		System.out.println("=====================================");
		PropbankParser parser = new PropbankParser();
		
		// Process the .prop file and return the corresponding graph
		IGraph newGraph = parser.process(testFile);
		
		// Render the new -pb.xml file
		File newFile = new File(K.OUTPUT_DATA_PATH + "/" + K.INPUT_FILE + "-pb.xml");
		GrafRenderer grafRenderer = new GrafRenderer(newFile);
		grafRenderer.render(newGraph);
		
		// Render the new -pb.dot file
		UTF8Reader reader1 = new UTF8Reader(new File("TXTFILES/" + K.INPUT_FILE + ".txt"));
		String contents1 = reader1.readString();
		reader1.close();
		newGraph.setContent(contents1);
		File dotFile = new File(K.OUTPUT_DATA_PATH + "/" + K.INPUT_FILE + "-pb.dot");
		DotRenderer dotRenderer = new DotRenderer(dotFile);
		dotRenderer.render(newGraph);
	
		
		//Render the original file, -ptb.dot, for checking purposes
		File headerFile = new File(K.MASC_RESOURCE_HEADER);
		ResourceHeader header = new ResourceHeader(headerFile);
		GrafParser graphParse = new GrafParser(header);
		IGraph graph = graphParse.parse(K.PTB_DATA_PATH + "/" + K.INPUT_FILE + "-ptb.xml");
		
		// Render dot file with text
		UTF8Reader reader = new UTF8Reader(new File("TXTFILES/" + K.INPUT_FILE + ".txt"));
		String contents = reader.readString();
		reader.close();
		graph.setContent(contents);
		File originalDotFile = new File(K.OUTPUT_DATA_PATH + "/" + K.INPUT_FILE + "-withText-ptb.dot");
		DotRenderer dotRenderer1 = new DotRenderer(originalDotFile);
		dotRenderer1.render(graph);
		
		// For testing and clarification purposes, initialize a New_PTB_Navigator and demonstrate the navigation function
		PTBNavigator navigator = new PTBNavigator(K.PTB_DATA_PATH + "/" + K.INPUT_FILE);
		System.out.println("=====================================");
		System.out.println("=============PENNTREEBANK============");		
		System.out.println("=====================================");
		navigator.printTerminalDetails();

//		System.out.println("=========== NAVIGATE DETAILS=============");
//		System.out.println("NavigateTerminals (4,7): " + navigator.navigateTerminals(4,7).getAnnotation().features().toString());
//		System.out.println("Navigate (4,7,1): " + navigator.navigate(4,7,1).getAnnotation().features().toString());
//		System.out.println("Navigate (4,7,2): " + navigator.navigate(4,7,2).getAnnotation().features().toString());
	}
}
