package org.anc.propbank;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.xces.graf.api.GrafException;
import org.xces.graf.api.IGraph;
import org.xml.sax.SAXException;

import org.anc.propbank.PropbankParser;

/** this class tests the PropbankParser */
public class PropbankParserDriver {
	public static final Constants K = new Constants();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, GrafException
	{
		//The propbank file to be processed (example in test files folder)
		File testDir = new File(K.TEST_DATA_PATH);		
		File testFile = new File(testDir, K.TEST_FILE + ".prop");
		//The folder containing the corresponding PTB files
		//String PennTreeBankRepresentationPath = "C:/Users/SandraMiller/Documents/PropBankNew1/test-files/MASC-3.0.0/data/written";
//		String PennTreeBankRepresentationPath = "test-files";
		//initialize a PropbankTokenizer with the path for the PTB files
		PropbankParser pp = new PropbankParser(K.TEST_DATA_PATH);
		//process the files and create a graph representation of them
		IGraph graph = pp.process(testFile);
		
		
		/* // dot rendering stuff
		   File dotFile = new File("C:/Users/Bobby/Desktop/" + "TestViz.dot");
		   
		   DotRenderer dot = new DotRenderer(dotFile);
		   
		   try {
			dot.render(graph);
		   } catch (RenderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		   }
		*/
	}
}
