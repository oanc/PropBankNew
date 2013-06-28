package org.anc.propbank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

import org.xces.graf.api.*;
import org.xces.graf.impl.CharacterAnchor;

/** This class augments an INode with two fields
 * to represent the anchors to the regions of text
 * in some source file that the INode operates over.
 * 
 * Thus, it stores:
 * 	INode sentence - the original INode being augmented
 *  long start - the starting index of the region of text that the INode links to
 *  long end - the ending index of the region of text that the INode links to
 * 
 * It provides a single parameter constructor that takes in an INode to be augmented
 * and automatically calculates that node's start and end parameters
 * 
 * @author Bobby
 */
public class AugmentedSentenceNode 
{

/*--------------------CONSTRUCTORS */
	/** default constructor */
	public AugmentedSentenceNode()
	{
		sentence = null;
		start = 0;
		end = 0;
	}
	
	/** This constructor takes in a single
	 * INode, presumably at the root of a 
	 * sentence, and stores it into a new 
	 * AugmentedSentenceNode which provides 
	 * fields for the start and end anchors 
	 * to the general region that the sentence
	 * operates over
	 * 
	 * @param sent INode the INode at the root of a sentence
	 */
	public AugmentedSentenceNode(INode sent)
	{
		sentence = sent;
		findStart();
		findEnd();
	}
	
	public AugmentedSentenceNode(INode sent, String sourceText)
	{
		sentence = sent;
		source = sourceText;
		findStart();
		findEnd();
		sortTokens();
	}
	
/*---------------------PRIVATE MUTATOR/INITIALIZER FUNCTIONS */
	/** finds the start anchor for this sentence node 
	 * and updates the start field of this class accordingly
	 */
	private void findStart()
	{
		/** an integer index local to {@link #findStart()}
		 * that is updated through a depth first search
		 */
		long tempStart = Long.MAX_VALUE;
		
		
		/** a stack local to {@link #findStart()} 
		 * that allows for a depth first search
		 */
		Stack<INode> startStack = new Stack<INode>();
		
		/* initialize the stack by adding the sentence node root */
		startStack.push(sentence);
		
		/* the following loop uses depth first search to find 
		 * the node with the smallest anchor
		 * TODO: is there a difference b/w empty() and isEmpty()?
		 */
		while(!startStack.empty()) 
		{
			//pop the node at the top of the stack
			INode currNode = startStack.pop();
			
			//add its children to the stack
			for(IEdge e : currNode.outEdges())
			{
				//**** TOKEN CHECK HERE -- CHANGE THIS.****//
				
				if(!(e.getTo().getAnnotation().getLabel().contains("Trace"))) 
				{
					startStack.add(e.getTo());
				}
			}
			
			//if its out degree is zero, then we should be able to find an anchor
			if((currNode.outDegree() == 0)
					&&(currNode.annotated())
					)
			{
				//**** TOKEN CHECK HERE -- CHANGE THIS. ****//
				
				if(currNode.getAnnotation().getLabel().contentEquals("tok")) 
				{
					//get the region that this node operates over
					IRegion region = currNode.getLinks().get(0).getRegions().get(0);
					
					//**** TRACE NODES WILL NOT HAVE TEXT IN A REGION****//
					
					//extract the text from the source file using the region
					String text = getTokenText(region);
					
					//get the start anchor
					CharacterAnchor chTemp = (CharacterAnchor) region.getStart();
					
					//get the offset of the anchor
					long temp = chTemp.getOffset();
					
					//if we haven't encountered the index of this node before
					if((indexPopulation.indexOf(temp) < 0)
						//&& (text.split("[\\p{Punct}]").length > 0) //and it's not punctuation
						//TODO: check for traces here?
					   )
					{
						
						// for later correction, add this index to a population of indices
						indexPopulation.add(temp);
					
						//add the node to the tokens list 
						tokens.add(currNode);
						
						//and add the node to a list indexed by its start
						indexedTokens.put(temp, currNode);
					
						//if the node changed the start anchor, update the startNode field to keep track of it
						// TODO: for diagnosing purposes, also should be removed if the code is working
						if( Math.min(tempStart, temp) == temp)
						{					
							//update the start index
							tempStart = temp;
							//update the start token
							startTok = currNode;
						}
					}
				}
			}
		}
		
		//change our start field to the new start
		start = tempStart;
		
		//correct for any overlap between sentences
		correctStartIndex();
		
	}
	
	/** finds the end anchor for this sentence node
	 * and updates the end field of this class accordingly
	 * TODO: we should be able to do this and {@link #findStart()} 
	 * in one depth first search, but for now they're separated
	 */
	private void findEnd()
	{
		/** an integer index local to {@link #findEnd()}
		 * that is updated through a depth first search
		 */
		long tempEnd = Long.MIN_VALUE;
		
		/** a stack local to {@link #findEnd()} 
		 * that allows for a depth first search
		 */
		Stack<INode> endStack = new Stack<INode>();
		
		/* initialize the stack by adding the sentence node root */
		endStack.push(sentence);
		
		/* the following loop uses depth first search to find 
		 * the node with the largest anchor
		 * TODO: is there a difference b/w empty() and isEmpty()?
		 */
		while(!endStack.empty())
		{
			//pop the node at the top of the stack
			INode currNode = endStack.pop();
			
			//add its children to the stack
			for(IEdge e : currNode.outEdges())
			{
				endStack.push(e.getTo());
			}
			
			/* If its out degree is zero and its annotated, it may be a token node
			 */
			if((currNode.outDegree() == 0)&&(currNode.annotated()))
			{
				
				//**** TOKEN CHECK HERE -- CHANGE THIS. ****//
				
				if(currNode.getAnnotation().getLabel().contentEquals("tok"))
				{
					CharacterAnchor chTemp = (CharacterAnchor) currNode.getLinks().get(0).getRegions().get(0).getAnchor(1);
					long temp = chTemp.getOffset();
					
					//if we haven't encountered the index of this node before
					if(indexPopulation.indexOf(temp) < 0)
					{
						// for later correction, add this index to a population of indices
						indexPopulation.add(temp);
						
						//add the node to a list indexed by its end
						indexedTokens.put(temp, currNode);
						
						//if the node changed the start anchor, update the startNode field to keep track of it
						// TODO: for diagnosing purposes, also should be removed if the code is working
						if(Math.max(tempEnd, temp) == temp)
						{
							//update the end anchor 
							tempEnd = temp;
					
							//update the end token
							endTok = currNode;
						}
					}
				}
			}
		}
		
		//change our end field to the new end
		end = tempEnd;
		
		//correct for any overlap between sentences
		correctEndIndex();
	}
	
	/** sorts the tokens by their anchors */
	private void sortTokens()
	{
		
		//fill the indices array with the keys of all the tokens
		indices.addAll(indexedTokens.keySet());
		//sort those keys
		Collections.sort(indices);
		
		//TODO: get the tokens by their key values
		for(long i: indices)
		{
			sortedTokens.add(indexedTokens.get(i));
		}
		
	}
	
/*---------------------ACCESSOR FUNCTIONS */
	/** returns the long value of the start field for this
	 * AugmentedSentenceNode
	 */
	public long getStart()
	{
		return start;
	}
	
	/** returns the long value of the end field for this
	 * AugmentedSentenceNode
	 */
	public long getEnd()
	{
		return end;
	}
	
	/** returns the INode that is the root of this
	 * AugmentedSentenceNode
	 */
	public INode getNode()
	{
		return sentence;
	}
	
/*-------------------------TESTING/DIAGNOSIS FUNCTIONS */
	/** retrieves the full list of token nodes that this 
	 * sentence node links to (as a result of the depth 
	 * first search conducted by {@link #getStart()}
	 * 
	 */
	public ArrayList<INode> getTokens()
	{
		return sortedTokens;
	}
	
	/** retrieves the node containing the token whose anchor
	 * represents this sentence's starting point
	 * 
	 * @return startToken INode
	 */
	public INode getStartNode()
	{
		return startTok;
	}
	
	/** retrieves the node containing the token whose anchor
	 * represents this sentence's end point 
	 * 
	 * @return endToken INode
	 */
	public INode getEndNode()
	{
		return endTok;
	}

	/** retrieves the token at the given index in the sentence */
	public INode getToken(int index)
	{		
		//get the INode corresponding to the index-th key into the indexedTokens hash map
		return indexedTokens.get(indices.get(index));
	}
	
	/** retrieves the text from the source given two CharacterAnchor positions*/
	public String getTokenText(CharacterAnchor start, CharacterAnchor end)
	{
		return source.substring(start.getOffset().intValue(),
								end.getOffset().intValue());
	}
	
	/** retrieves the text from the source given an IRegion */
	public String getTokenText(IRegion region)
	{
		CharacterAnchor start = (CharacterAnchor)region.getAnchor(0);
		CharacterAnchor end = (CharacterAnchor)region.getAnchor(1);
		
	
		return source.substring(start.getOffset().intValue(),
				end.getOffset().intValue());
	
	}
	
/*---------------------------------CORRECTION FUNCTIONS */
	/** This procedure ensures that the start index of this
	 * sentence does not overlap with another sentence. It 
	 * accomplishes this using statistical methods. In other 
	 * words, if the computed start index is farther from the 
	 * mean, on average, than every other start anchor, then 
	 * the word it corresponds to likely comes from another 
	 * sentence.
	 */
	private void correctStartIndex()
	{
		/* find the second to least index */
			//remove the start index, add back in later
			indexPopulation.remove(indexPopulation.indexOf(start));
			
			//find the next possible start index
			secondIndex = Long.MAX_VALUE;
			for(long i: indexPopulation)
			{
				secondIndex = (long) Math.min(secondIndex, i);
			}
			
			secondNode = indexedTokens.get(secondIndex);
	
			//add the start index back into the indexPopulation
			indexPopulation.add(start);
			
		
		/* calculate the mean of all of the indices */
			//get the sum of all the indices
			double sum = 0;
			for (double x : indexPopulation)
			{
				sum += x;
			}
			//get the mean of all the indices
			indexMean = (sum / indexPopulation.size());
			
		/* calculate the standard deviation of the indices */
			//get the sum of all the differences of each index from the mean
			double ssd = 0;
			for (double x : indexPopulation)
			{
				ssd += Math.pow(
						(x - indexMean),
						2);
			}
			indexStdDev = Math.sqrt(ssd/(indexPopulation.size()- 1));
			
		/* compare the two potential boundary indices */
			//calculate the z-score of each
			double zStart = Math.abs((start - indexMean)/indexStdDev);
			double zSecond = Math.abs((secondIndex - indexMean)/indexStdDev);
			
		
			//TODO: comment this part better
			//compare it to the next zScore
				if((zStart - zSecond) > 2)
				{
					System.out.println("["+start+"] "+ zStart +
									 "  ["+secondIndex+"] "+ zSecond +
									 "  Mean = "+indexMean+
									 "  StdDev = "+indexStdDev);
					start = (long) secondIndex;
					startTok = secondNode;
				}
			
		/* clear the global helper variables */
		indexPopulation.clear();
		indexMean = 0;
		indexStdDev = 0;
		indexedTokens.clear();
		 
	}
	/** This procedure ensures that the end index of this
	 * sentence does not overlap with another sentence. It 
	 * accomplishes this using statistical methods. In other 
	 * words, if the computed start index is farther from the 
	 * mean, on average, than every other start anchor, then 
	 * the word it corresponds to likely comes from another 
	 * sentence.
	 */
	private void correctEndIndex()
	{
		/* find the second to least index */
			//remove the start index, add back in later
			indexPopulation.remove(indexPopulation.indexOf(end));
			
			//find the next possible start index
			secondIndex = Long.MIN_VALUE;
			for(long i: indexPopulation)
			{
				secondIndex = (long) Math.max(secondIndex, i);
			}
			
			//get the corresponding INode
			secondNode = indexedTokens.get(secondIndex);
	
			//add the start index back into the indexPopulation
			indexPopulation.add(end);
			
		
		/* calculate the mean of all of the indices */
			//get the sum of all the indices
			double sum = 0;
			for (double x : indexPopulation)
			{
				sum += x;
			}
			//get the mean of all the indices
			indexMean = (sum / indexPopulation.size());
			
		/* calculate the standard deviation of the indices */
			//get the sum of all the differences of each index from the mean
			double ssd = 0;
			for (double x : indexPopulation)
			{
				ssd += Math.pow(
						(x - indexMean),
						2);
			}
			indexStdDev = Math.sqrt(ssd/(indexPopulation.size()- 1));
			
		/* compare the two potential boundary indices */
			//calculate the z-score of each
			double zEnd = Math.abs((end - indexMean)/indexStdDev);
			double zSecond = Math.abs((secondIndex - indexMean)/indexStdDev);
			
		
				//compare it to the next zScore
				if((zEnd - zSecond) > 2)
				{
					System.out.println("["+end+"] "+ zEnd +
									 "  ["+secondIndex+"] "+ zSecond +
									 "  Mean = "+indexMean+
									 "  StdDev = "+indexStdDev);
					end = (long) secondIndex;
					endTok = secondNode;
				}
		 
	}
	
/*---------------------------------LOCAL VARIABLES */
	/** Holds the INode for the root of this sentence */
	private INode sentence;
	
	/** Holds the start anchor linked to this sentence */
	private long start;
	
	/** Holds the end anchor linked to this sentence */
	private long end;
	
	/** Holds the source text for this sentence*/
	private String source;
	
/*---------------------------------TEST/DIAGNOSIS VARIABLES */
	/** Holds the token nodes accessed when finding the start anchor */
	private ArrayList<INode> tokens = new ArrayList<INode>();
	
	/** Holds the token nodes accessed when finding the start anchor, sorted by their anchors*/
	private ArrayList<INode> sortedTokens = new ArrayList<INode>();
	
	/** Holds the token nodes with their ids changed to their start anchors*/
	private HashMap<Long, INode> indexedTokens = new HashMap<Long, INode>();
	
	/** Holds an array of the indices encountered */
	private ArrayList<Long> indices = new ArrayList<Long>();

	/** Holds the token node that has the start anchor */
	private INode startTok;
	
	/** Holds the token node that has the end anchor */
	private INode endTok;
	
/*---------------------------------CORRECTION VARIABLES */
	/** Holds a population of indices as they are discovered in either
	 * the {@link #findStart()} or {@link #findEnd()} functions
	 */
		private ArrayList<Long> indexPopulation = new ArrayList<Long>();
		
	/** Holds the mean of the indices found by either the 
	 * {@link #findStart()} or {@link #findEnd()} functions
	 */
		private double indexMean;
		
	/** Holds the calculated standard devation of all the indices
	 * discovered by {@link #findStart()} and {@link #findEnd()}
	 */
		private double indexStdDev;
	
	/** Holds the second-to-last start or end index found 
	 * by {@link #findStart()} and {@link #findEnd()}
	 */
		private long secondIndex;
	
	/** Holds the second-to-last start or end node found
	 * by {@link #findStart()} and {@link #findEnd()}
	 */
		private INode secondNode;
}
