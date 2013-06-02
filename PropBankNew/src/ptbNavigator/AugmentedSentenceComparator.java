package ptbNavigator;

import java.util.Comparator;

public class AugmentedSentenceComparator implements Comparator<AugmentedSentenceNode>
{

	public int compare(AugmentedSentenceNode o1, AugmentedSentenceNode o2) 
	{
		Long start1 = o1.getStart();
		Long start2 = o2.getStart();
		
		//Long end1 = o1.getEnd();
		//Long end2 = o2.getEnd();
		
		return start1.compareTo(start2);
	}

}
