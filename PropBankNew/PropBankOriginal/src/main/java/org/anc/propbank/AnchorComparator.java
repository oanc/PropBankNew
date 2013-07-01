package org.anc.propbank;

import java.util.Comparator;
import java.util.Stack;

import org.xces.graf.api.IEdge;
import org.xces.graf.api.INode;
import org.xces.graf.impl.CharacterAnchor;

public class AnchorComparator implements Comparator<INode>
{

	public int compare(INode o1, INode o2) 
	{
		Integer reg1 = findStartAnchor(o1);
		Integer reg2 = findStartAnchor(o2);
		
		return reg1.compareTo(reg2);
	}
	
	protected int findStartAnchor(INode node)
	{
		/* to be able to find the start node,
		 *  we'll use the Math.min function and
		 *  initialize our start value to what is 
		 *  essentially positive infinity */
		int start = Integer.MAX_VALUE;
		
		/** this stack allows us to explore child nodes
		 * using depth-first search
		 */
		Stack<INode> nStack = new Stack<INode>();
		
		/* Add the given node onto the stack */
		nStack.add(node);
		
		/* this while loop handles the 
		 * depth-first search
		 */
		while(!(nStack.empty()))
		{
			/* grab the next child node from the stack */
			INode n = nStack.pop();
			
			/* add any of its children to the stack */
			for(IEdge e: n.outEdges())
			{
				if(!e.getTo().visited())
				{
					nStack.add(e.getTo());
					//e.visit();
				}
			}
			
			/* if it's degree is zero, then we're at a 
			 * leaf, if the leaf is a token, then we 
			 * can find the anchor, otherwise ignore
			 * (e.g. a "TRACE" node can also be a leaf 
			 * node, but only indirectly references any 
			 * anchors)
			 */
			if(n.outDegree() == 0)
			{
				if(n.annotated()) // CHECKS IF THE INODE CONTAINS ONE OR MORE ANNOTATIONS SETS
				{
					if(n.getAnnotation().getLabel().contentEquals("tok") // CHECKS IF THE INODE IS A TOKEN -- TRACE NODES WILL RETURN FALSE!
						&& 
						(n.getAnnotation().getFeatureStructure().get("cat") == null))
					{
						/* grab the start anchor for this node */
						CharacterAnchor temp = (CharacterAnchor) n.getLinks().get(0).getRegions().get(0).getAnchor(0);
						
						/* temporarily store it for ease in comparison */
						int tempInt = temp.getOffset().intValue();
						
						/* if the anchor that we just discovered is less than what we have,
						 * then we need to update our start value accordingly
						 */
						start = Math.min(start, tempInt);			
					}
				}
			}
		}
		
		return start;
	}

}
