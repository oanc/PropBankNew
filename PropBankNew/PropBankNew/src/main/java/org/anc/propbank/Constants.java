package org.anc.propbank;

import java.io.FileNotFoundException;


public class Constants extends org.anc.constants.Constants 
{
	@Default("/var/corpora/MASC-3.0.0")
	public final String MASC_ROOT = null;
	
	@Default("/var/corpora/MASC-3.0.0/resource-header.xml")
	public final String MASC_RESOURCE_HEADER = null;
	
	@Default("test-files")
	public final String TEST_DATA_PATH = null;
	
	@Default("110CYL068")
	public final String TEST_FILE = null;
	
	public Constants()
	{
		super.init();
	}
	
	public static void main(String[] args)
	{
		try {
			new Constants().save();
			System.out.println("Constants saved.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
