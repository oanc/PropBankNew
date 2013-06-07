package drivers;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


import org.xces.graf.api.IGraph;
import org.xml.sax.SAXException;

import propbankParser.PropbankParser;



/** this class tests the PropbankParser */
public class PropbankParserDriver {

	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException
	{
		//The propbank file to be processed (example in test files folder)
		File testFile = new File("C:/Users/SandraMiller/Documents/PropBankNew1/test-files/110CYL068.prop");
		//The folder containing the corresponding PTB files
		String PennTreeBankRepresentationPath = "C:/Users/SandraMiller/Documents/PropBankNew1/test-files/MASC-3.0.0/data/written";
		
		//initialize a PropbankTokenizer with the path for the PTB files
		PropbankParser pp = new PropbankParser(PennTreeBankRepresentationPath);
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
