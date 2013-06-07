package drivers;

import ptbNavigator.PTBNavigator;

public class PTBNavigatorDriver 
{
	public static void main(String args[])
	{
		/** This is the root directory for the files on @author Bobby 's laptop */
		String testRoot = "C:/Users/SandraMiller/PropBankNew1/test-files";
		/** This is the generic path to the MASC written data
		 * TODO: Note: this will be relevant to versions of this
		 * program outside @author Bobby 's laptop
		 */
		String writtenRoot = "/MASC-3.0.0/data/written";
		
		String writtenRoot2 = "/masc-graf-1.0/data/written";
		/** This is the generic, extension-less, name of the test
		 * file
		 * TODO: GO HERE TO CHANGE THE TARGET FILE
		 * Some suitable entries:
		 * 110CYL068, 110CYL069, 110CYL070, 110CYL071, 110CYL072, 110CYL200, 
		 * 113CWL017, 113CWL018, 
		 * 114CUL057, 114CUL058, 114CUL059, 114CUL060
		 */
		String testFile = "110CYL068";
		
		/** this just composes the path to the test file */
		String path = testRoot + writtenRoot + "/" + testFile;
		
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
			// pn.navigate(0, 0, 0);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
}
