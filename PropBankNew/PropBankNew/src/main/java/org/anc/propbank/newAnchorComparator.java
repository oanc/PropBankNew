package org.anc.propbank;

import java.util.Comparator;
import java.util.Stack;

import org.xces.graf.api.IEdge;
import org.xces.graf.api.INode;
import org.xces.graf.impl.CharacterAnchor;

public class newAnchorComparator implements Comparator<INode>
{

	public int compare(INode o1, INode o2) 
	{

			CharacterAnchor reg1 = (CharacterAnchor) o1.getLinks().get(0).getRegions().get(0).getAnchor(0);
		    CharacterAnchor reg2 = (CharacterAnchor) o2.getLinks().get(0).getRegions().get(0).getAnchor(0);
		
		return reg1.compareTo(reg2);
	}
}