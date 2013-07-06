package org.anc.propbank;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;

import org.xces.graf.api.GrafException;
import org.xces.graf.io.GrafRenderer;
import org.xml.sax.SAXException;


public class New_PropbankParserDriver {

	public static final Constants K = new Constants();
	
	public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, GrafException
	{
		//Initialize path to propbank file
		File testDir = new File(K.TEST_DATA_PATH);		
		File testFile = new File(testDir, K.TEST_FILE + ".prop");
		
		
		System.out.println("=====================================");
		System.out.println("============PROPBANK INFO============");
		System.out.println("=====================================");
		New_PropbankParser parser = new New_PropbankParser(K.TEST_DATA_PATH);
		parser.process(testFile);
		
		//System.out.println("=====================================");
		//System.out.println("============PTB GRAPH INFO===========");
		//System.out.println("=====================================");
		New_PTBNavigator navigator = new New_PTBNavigator(K.TEST_DATA_PATH + "/" + K.TEST_FILE);
		///navigator.printPTBGraph();

	}
}
