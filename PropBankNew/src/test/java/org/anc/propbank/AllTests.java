package org.anc.propbank;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.xces.graf.api.GrafException;
import org.xces.graf.api.INode;
import org.xml.sax.SAXException;



public class AllTests {
	public static final Constants K = new Constants();
	
	///TESTS FOR FILE 110CYL069
//	public void testSetSentences() throws SAXException, IOException, GrafException{
//		New_FixedPTBNavigator navigator = new New_FixedPTBNavigator(K.PTB_DATA_PATH + "/" + K.INPUT_FILE);
//		assertTrue(navigator.getSentences().size() == 42);
//	}
//	
//	
//	@Test
//	public void testDepthFirstSearch() throws SAXException, IOException, GrafException{
//		New_FixedPTBNavigator navigator = new New_FixedPTBNavigator(K.PTB_DATA_PATH + "/" + K.INPUT_FILE);
//		HashMap<Integer, ArrayList<INode>> sentenceTerminals = navigator.getSentenceTerminalNodes();
//		assertTrue(sentenceTerminals.get(11).size() == 23);
//		assertTrue(sentenceTerminals.get(0).size() == 4);
//		assertTrue(sentenceTerminals.get(12).size() == 36);
//		//assertTrue(sentenceTerminals.get(22).size() == 27);
//	}
	
	
	//TESTS FOR FILE RINDNERBONNIE
	@Test
	public void testDepthFirstSearch() throws SAXException, IOException, GrafException{
		PTBNavigator navigator = new PTBNavigator(K.PTB_DATA_PATH + "/" + K.INPUT_FILE);
		assertTrue(navigator.getSentenceTerminalNodes().get(0).size() == 5);
	}
	
	
	
	
	
}
