package org.anc.propbank;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.anc.constants.*;

public class Constants extends org.anc.constants.Constants 
{
	@Default("MASC-3.0.0")
	public final String MASC_ROOT = null;
	
	@Default("MASC-3.0.0/MASC_3.0.0-resource-header.xml")
	public final String MASC_RESOURCE_HEADER = null;
	
	@Default("new-test-files")
	public final String PROP_DATA_PATH = null;
	
	@Default("TXTFILES")
	public final String TXT_DATA_PATH = null;
	
	@Default("new-test-files")
	public final String PTB_DATA_PATH = null;
	
	@Default("output-files")
	public final String OUTPUT_DATA_PATH = null;
	
	@Default("113CWL017")
	public final String INPUT_FILE = null;
	
	public Constants()
	{
		super.init();
	}
	
	public static void main(String[] args)
	{
		try {
			new Constants().save();
			System.out.println("Constants saved.");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
