package org.anc.propbank;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xces.graf.io.DotRenderer;
import org.xces.graf.io.RenderException;
import org.xml.sax.SAXException;

import org.anc.propbank.PropbankParser;

/** this class tests the PropbankParser */
public class PropbankParserDriver {
	public static final Constants K = new Constants();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, GrafException
	{
		//Initialize path to propbank file
		File testDir = new File(K.TEST_DATA_PATH);		
		File testFile = new File(testDir, K.TEST_FILE + ".prop");
		
		//initialize a PropbankTokenizer with the path for the corresponding PTB files
		PropbankParser pp = new PropbankParser(K.TEST_DATA_PATH);
		
		//process the files and create a graph representation of them
		IGraph graph = pp.process(testFile);
	
		//dot rendering stuff
		  // File dotFile = new File("DotFiles/" + "TestViz.dot");
		   
		  /** DotRenderer dot = new DotRenderer(dotFile);
		   
		   try {
			dot.render(graph);
		   } catch (RenderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
		*/
	}
}
