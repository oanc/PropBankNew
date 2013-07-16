package org.anc.propbank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.Properties;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.DotRenderer;
import org.xces.graf.io.GrafRenderer;
import org.xml.sax.SAXException;


public class New_PropbankDriver {

	public static final Constants K = new Constants();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, GrafException
	{
		//Define constants based on user input
		String ptbPath = args[0];
		String propPath = args[1];
		String inputFile = args[2];
		String outputPath = args[3];
		
		//Replace constants data in the properties file based on that user input
		Properties prop = new Properties();
		prop.setProperty("PTB_DATA_PATH", ptbPath);
		prop.setProperty("PROP_DATA_PATH", propPath);
		prop.setProperty("INPUT_FILE", inputFile);
		prop.setProperty("OUTPUT_DATA_PATH", outputPath);
		prop.store(new FileOutputStream("conf/sandramiller/org.anc.propbank.Constants.properties"), null);
		
		
		//Initialize path to propbank file
		File testDir = new File(K.PROP_DATA_PATH);		
		File testFile = new File(testDir, K.INPUT_FILE + ".prop");
		
		//Initialize New_PropbankParser
		System.out.println("=====================================");
		System.out.println("============PROPBANK INFO============");
		System.out.println("=====================================");
		New_PropbankParser parser = new New_PropbankParser();
		
		// Process the .prop file and return the corresponding graph
		IGraph newGraph = parser.process(testFile);
		
		// Render the new -pb.xml file
		File newFile = new File(K.OUTPUT_DATA_PATH + "/" + K.INPUT_FILE + "-pb.xml");
		GrafRenderer grafRenderer = new GrafRenderer(newFile);
		grafRenderer.render(newGraph);
		
		// Render the new -pb.dot file
		File dotFile = new File(K.OUTPUT_DATA_PATH + "/" + K.INPUT_FILE + "-pb.dot");
		DotRenderer dotRenderer = new DotRenderer(dotFile);
		dotRenderer.render(newGraph);

		// For testing and clarification purposes, initialize a New_PTB_Navigator and demonstrate the navigation function
		New_PTBNavigator navigator = new New_PTBNavigator(K.PTB_DATA_PATH + "/" + K.INPUT_FILE);
		System.out.println("=========== NAVIGATE DETAILS=============");
		System.out.println("NavigateTerminals (4,7): " + navigator.navigateTerminals(4,7).getAnnotation().features().toString());
		System.out.println("Navigate (4,7,1): " + navigator.navigate(4,7,1).getAnnotation().features().toString());
		System.out.println("Navigate (4,7,2): " + navigator.navigate(4,7,2).getAnnotation().features().toString());
	}
}
