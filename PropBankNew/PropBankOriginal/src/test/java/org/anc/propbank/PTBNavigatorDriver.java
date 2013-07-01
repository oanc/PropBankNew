package org.anc.propbank;

import java.io.File;
import java.io.FileNotFoundException;

import org.anc.propbank.PTBNavigator;
import org.xces.graf.io.dom.ResourceHeader;

public class PTBNavigatorDriver 
{
	public static final Constants K = new Constants();
	
	public static void main(String args[]) 
	{

		/** Compose path to test file */
		String path = K.TEST_DATA_PATH + "/" + K.TEST_FILE;
		
		/** try to initialize a PTBNavigator using the path
		 * constructed above, the PTBNavigator can throw 
		 * SAXExceptions if for some reason there is a 
		 * problem with the GraphParser, or IOExceptions 
		 * if the path we fed in for the test file is invalid
		 */
		try
		{
			//initialize a new PTBNavigator
			PTBNavigator pn = new PTBNavigator(path);
			
			
			//TODO: choose test print function here
			pn.printSentencesDetailed();
			
			//TODO: diagnoses a problem sentence, if it exists
			pn.diagnose(26);
			
			//TODO: this is a model of how to navigate
			pn.navigateTrace(0, 0); //two parameter navigates take the last sentence used
			pn.navigateTrace(1, 0, 0); //but three paramater navigates allow the sentence to be specified first
			pn.navigateTrace(2, 0, 0); //the sentence that is set in navigates is a destructive function
			pn.navigateTrace(0, 0); //so the next two parameter navigate will be to the 1st sentence, not the 0th
			
			//TODO: the plain navigate function returns the INode
			 //pn.navigate(0, 0, 0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
