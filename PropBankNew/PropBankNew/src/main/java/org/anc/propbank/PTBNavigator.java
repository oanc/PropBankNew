package org.anc.propbank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import org.anc.io.UTF8Reader;
import org.xml.sax.SAXException;
import org.xces.graf.api.*;
import org.xces.graf.impl.CharacterAnchor;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.dom.ResourceHeader;

/**
 * Application entry point
 * 
 */
public class PTBNavigator
{
   /** System dependant settings */
   public static final Constants K = new Constants();

   /* CONSTRUCTORS */

   /**
    * This Constructor takes in a single string 'root' for the path to the
    * desired file
    * 
    * It will throw a SAXException if it cannot successfully construct an
    * internal GraphParser, and an IOException if it cannot find a file at the
    * given path
    * 
    * Note: do not include the extension. e.g. if the file were "117CWL008.mrg"
    * give the string "path/117CWL008", not "path/117CWL008.mrg"
    * 
    * @param root
    *           String the path to the desired file w/o the extension
    * @throws SAXException
    *            , IOException
    * @throws GrafException
    */
   public PTBNavigator(String root) throws SAXException, IOException,
         GrafException
   {
      //set the root directory to the given directory
      this.root = root;

      File headerFile = new File(K.MASC_RESOURCE_HEADER);
      ResourceHeader header = new ResourceHeader(headerFile);
      graphParse = new GrafParser(header);
      
      //initialize the UTF8 reader to the text file associated with the given path
      setSource();

      //initialize the main graph over which we will navigate
      setGraph();
   }

/* PRIVATE MUTATOR/INITIALIZER FUNCTIONS */
   /**
    * initializes the graph representation for the file given as input to
    * {@link #PTBNavigator(String)} throws SAXException, IOException
    * 
    * @throws GrafException
    */
   private void setGraph() throws SAXException, IOException, GrafException
   {
      /*
       * initialize the graphParser NOTE: Throws SAXException
       */
      File resourceHeaderFile = new File(K.MASC_RESOURCE_HEADER);
      ResourceHeader resourceHeader = new ResourceHeader(resourceHeaderFile);

      graphParse = new GrafParser(resourceHeader);

      /*
       * use the graph parser to create the PTBGraph
       */
      IGraph graph = graphParse.parse(root + PTB_EXTENSION);

      /*
       * now that we have a graph, call the setSentences method to initialize
       * our sentences
       */
      setSentences(graph);

      /*
       * now that we have a graph, call the setAugmentedSentences method to
       * initialize our sentences TODO: replace/merge this with setSentences()
       */
      setAugmentedSentences(graph);
   }

   /**
    * initializes a reader for the source text of the file given as input to
    * {@link #PTBNavigator(String)}
    * 
    * @throws IOException
    */
   private void setSource() throws IOException
   {
      /*
       * try to initialize a UTF8 reader using the given file extension if this
       * doesn't work, print out the stack trace for now TODO: try-catch block
       * with more helpful error message?
       */
      source = new UTF8Reader(root + SOURCE_EXTENSION);

      /*
       * grab the source file as a single string TODO: it may be more efficient
       * to do this only when testing, since the string will be quite large
       */
      sourceText = source.readString();
   }

   /**
    * initializes the sentences list by retrieving all the sentence nodes from
    * the PTB representation of the file given as input to
    * {@link #PTBNavigator(String)}
    */
   private void setSentences(IGraph graph)
   {
      //get the root of the ptbGraph
      INode rootNode = graph.getRoot();

      //add all of the child nodes, presumably our 
      //sentences to the sentences list
      for (IEdge e : rootNode.getOutEdges())
      {
         INode sentence = e.getTo();
         sentences.add(sentence);
      }

      //now that we have all our sentences, sort them
      sortSentences();
   }

   /**
    * sorts the sentences list
    */
   private void sortSentences()
   {
      //duplicate the sentence list for comparison
      //TODO: remove this once sorting is working
      unOrganizedSentences.addAll(sentences);

      //sort the sentences by their linked regions
      Collections.sort(sentences, new AnchorComparator());

      //now that our sentences are sorted, we can initialize
      // the current sentence parameters
      setSentence(0);
   }

   /**
    * initializes the augmented sentences list by retrieving all the sentence
    * nodes from the PTB representation of the file given as input to
    * {@link #PTBNavigator(String)} transforming them into
    * AugmentedSentenceNodes, and sorting them by their start fields
    */
   private void setAugmentedSentences(IGraph graph)
   {
      //get the root of the PTB Graph
      INode rootNode = graph.getRoot();

      /*
       * add all of the child nodes, which at this level should be the sum total
       * of independent entities (sentences) from the source
       */
      for (IEdge e : rootNode.getOutEdges())
      {
         /**
          * Each sentence node is converted into an AugmentedSentenceNode which,
          * upon initialization, sets its start and end anchors which will be
          * used to sort the sentences
          */
         AugmentedSentenceNode sentence = new AugmentedSentenceNode(e.getTo(),
               sourceText);

         //add the new augmented sentence node to the list of augmented sentences
         augmentedSentences.add(sentence);
      }

      //now that we have collected all our sentences and augmented them, sort them!
      sortAugmentedSentences();
   }

   /**
    * Sorts the sentences retrieved from the PTB representation by their start
    * anchors (which were included when they were converted into
    * AugmentedSentenceNodes)
    */
   private void sortAugmentedSentences()
   {
      //sort the augmented sentences
      Collections.sort(augmentedSentences, new AugmentedSentenceComparator());
   }

/* MAIN UTILITY FUNCTIONS */

   /**
    * This procedure looks in the directory for the address indicated by the
    * input, root, and initializes the source text and PTBGraph for the
    * PTBNavigator. When processing files from multiple different texts, use
    * this function to switch from one text to the next.
    * 
    * @throws GrafException
    */
   public void _setRoot(String root) throws IOException, SAXException,
         GrafException
   {
      //set the root directory to the given directory
      this.root = root;

      //get the source text at the root (throws IOException)
      setSource();

      //construct a graph from the source text (throws SAXException)
      setGraph();

   }

   /**
    * This signals the PTBNavigator to operate at the sentence whose number is
    * given as input
    * 
    * @param index
    *           the "index"th sentence in the PTB representation of the source
    */
   public void setSentence(int index)
   {
      //update our sentence index to the given index
      sentIndex = index;

      //find the sentence node at that position and store that in the sentence variable
      sentence = sentences.get(index);
   }

   /**
    * This signals the PTBNavigator to travel n number of tokens in its given
    * sentence
    */
   public void setToken(int index)
   {
      //update the token anchor index to the given index
      tokIndex = index;

      //find the token node at that position and store that in the token field
      token = augmentedSentences.get(sentIndex).getTokens().get(index);
   }

   //TODO: IMPLEMENT FURTHER NAVIGATOR METHODS HERE

   /**
    * This method presumes that the sentence has already been specified and
    * navigates within the sentence using the first input, tokenAcross, to move
    * that number of leaves along the sentence's tree, and then it moves "up"
    * the tree using the second input, parentUP.
    * 
    * @return PTBNodeResult
    * 
    */
   public INode navigate(int tokenAcross, int parentUp)
   {
      //move horizontally
      setToken(tokenAcross);

      //move vertically and return node
      return trace(parentUp);
   }

   /**
    * This is the main method to navigate within a PTB text. The order of
    * parameters is sentence, indicating the index of the sentence within the
    * text; tokenAcross, indicating the number of leaves to travel along a tree
    * representation of the sentence; and parentUp, the number of nodes "up"
    * from the leaf indicated by tokenAcross.
    * 
    * @param sentence
    *           - index of the sentence within the PTB representation
    * @param tokenAcross
    *           - the horizontal position of the desired node
    * @param parentUp
    *           - the vertical position of the desired node
    * @return PTBNodeResult
    */
   public INode navigate(int sentence, int tokenAcross, int parentUp)
   {
      //move to the sentence
      setSentence(sentence);

      //move horizontally
      setToken(tokenAcross);

      //move vertically and return node
      return trace(parentUp);
   }

/* DISPLAY FUNCTIONS */

   /**
    * Print out the sentences that the PTBNavigator has discovered in the given
    * input file
    */
   /** Print out the list of the sentences retrieved from the given file */
   public void printSentences()
   {
      //Signal that we are displaying the sentences
      System.out.println("============SENTENCES FOUND============");

      //local sentence index for printing purposes
      int localSentIndex = 0;

      //for each sentence
      for (INode n : sentences)
      {
         /* ---- PRINT OUT THE ORDER IN WHICH WE DISCOVERED IT ---- */
         System.out.print(" "); //indentation sets off SENTENCE ORDER DISPLAY
         System.out.print("[" + localSentIndex++ + "]"); //display the order in which this sentence is found

         /* ---- DISPLAY ITS TYPE ---- */
         System.out.print("\t"); //indentation sets off PTB-TYPE DISPLAY
         System.out.print("TYPE: ");
         System.out.print(n.getAnnotation().getLabel()); //get the label

         /*
          * ---- DISPLAY A LIST OF OUT EDGES ---- System.out.print("\t");
          * //indentation sets off list of out edges
          * System.out.print(n.getOutEdges()); //not sure if this'll be useful
          */

         /* ---- DISPLAY A LIST OF THE FOLLOWING NODES ---- */
         System.out.print(" \t"); //indentation to set off CHILD NODES DISPLAY
         System.out.print("CHILD: [");
         for (IEdge e : n.getOutEdges()) //loops through this node's immediate children
         {
            System.out.print(e.getTo().getAnnotation().getLabel()); //print out this node's label
            System.out.print(" ");
         }
         System.out.print("]"); //end CHILD NODES DISPLAY

         /* ---- END THIS SENTENCE'S ENTRY ---- */
         System.out.print("\n"); //enters a newline for the next sentence
      }

      //Mark the end of this print sequence
      System.out.println("=======================================");
   }

   /**
    * Print out the current sentence that the PTBNavigator is using
    */

   /** Print out the current PTB sentence being accessed by the PTBNavigator */
   public void printCurrentSentence()
   {
      AugmentedSentenceNode curr = augmentedSentences.get(sentIndex);
      System.out.println("===================CURRENT SENTENCE=========\n"
            + "NUMBER: " + sentIndex + "  " + "TYPE: "
            + sentence.getAnnotation().getLabel() + "\nTEXT: "
            + sourceText.substring( //extract the target text from the source file
                  (int) curr.getStart(), //using the start anchor field
                  (int) curr.getEnd()) //and the end anchor field of the AugmentedSentenceNode
      );
      //TODO: implement more detailed information
   }

   /**
    * Print out the original sentences for comparison TODO: this should not be
    * necessary after the sort function is working
    */
   public void printUnOrganizedSentences()
   {
      //Signal that we are displaying the sentences
      System.out.println("============ORIGINAL SENTENCES============");

      //local sentence index for printing purposes
      int localSentIndex = 0;

      //for each sentence
      for (INode n : unOrganizedSentences)
      {
         /* ---- PRINT OUT THE ORDER IN WHICH WE DISCOVERED IT ---- */
         System.out.print(" "); //indentation sets off SENTENCE ORDER DISPLAY
         System.out.print("[" + localSentIndex++ + "]"); //display the order in which this sentence is found

         /* ---- DISPLAY ITS TYPE ---- */
         System.out.print("\t"); //indentation sets off PTB-TYPE DISPLAY
         System.out.print("TYPE: ");
         System.out.print(n.getAnnotation().getLabel()); //get the label

         /*
          * ---- DISPLAY A LIST OF OUT EDGES ---- System.out.print("\t");
          * //indentation sets off list of out edges
          * System.out.print(n.getOutEdges()); //not sure if this'll be useful
          */

         /* ---- DISPLAY A LIST OF THE FOLLOWING NODES ---- */
         System.out.print(" \t"); //indentation to set off CHILD NODES DISPLAY
         System.out.print("CHILD: [");
         for (IEdge e : n.getOutEdges()) //loops through this node's immediate children
         {
            System.out.print(e.getTo().getAnnotation().getLabel()); //print out this node's label
            System.out.print(" ");
         }
         System.out.print("]"); //end CHILD NODES DISPLAY

         /* ---- END THIS SENTENCE'S ENTRY ---- */
         System.out.print("\n"); //enters a newline for the next sentence
      }

      //Mark the end of this print sequence
      System.out.println("==========================================");
   }

   /**
    * Print out the augmented sentences which provides detailed information
    * about their anchors in the source text and displays the actual text they
    * annotate
    */
   public void printSentencesDetailed()
   {
      //Signal that we are printing out augmented sentences
      System.out.println("========SENTENCES+ANCHORS+ORIGINALTEXT========");

      //local ordering index TODO: put this in AugmentedSentenceNode class?
      int localIndex = 0;

      //---DISPLAY THE SOURCE FILE NAME---
      System.out.println("FILE: " + root.substring(root.lastIndexOf('/') + 1));

      /* this for each loop handles the detailed information for each sentence */
      for (AugmentedSentenceNode a : augmentedSentences)
      {
         //---DISPLAY THE ORDER ENCOUNTERED---
         System.out.print("["); //opening [
         System.out.print(localIndex++); //sentence ordering, incremented simultaneously
         System.out.print("]");

         //---DISPLAY THE PTB TAG---
         System.out.print(" --- ");
         System.out.print(a.getNode().getAnnotation().getLabel());
         System.out.print(": ");

         //---DISPLAY THE ANCHORS---
         System.out.print("(" + a.getStart() + "," + a.getEnd() + ")");

         //---DISPLAY THE SOURCE TEXT---
         System.out.print("\n\t"); //indentation for source text
         System.out.print(sourceText.substring( //extract the target text from the source file
               (int) a.getStart(), //using the start anchor field
               (int) a.getEnd())); //and the end anchor field of the AugmentedSentenceNode

         //---MAKE A NEW LINE FOR THE NEXT SENTENCE---
         System.out.println("\n");
      }

      //Indicate the end of the detailed sentence information display
      System.out.println("=============================================");
   }

   /** Prints out the current token being accessed and its anchor values */
   public void printCurrentToken()
   {
      System.out.println("===================CURRENT TOKEN============");
      System.out.println("Index: " + tokIndex);
      System.out.println("Token: " + currentTokenText());
      System.out.println("Trace: ");
      printTrace(token);
      System.out.println("============================================");
   }

   /**
    * returns the string corresponding to the text that anchors the token that
    * the PTBNavigator is set at
    * 
    * @return currTokText
    */
   protected String currentTokenText()
   {
      //get the token's anchors
      CharacterAnchor tokS = (CharacterAnchor) token.getLinks().get(0)
            .getRegions().get(0).getAnchor(0);
      CharacterAnchor tokE = (CharacterAnchor) token.getLinks().get(0)
            .getRegions().get(0).getAnchor(1);

      return sourceText.substring(tokS.getOffset().intValue(), tokE.getOffset()
            .intValue());
   }

   /* DIAGNOSIS FUNCTIONS */

   /**
    * Prints out information about the sentence at the given index including: 1.
    * the order in which the token occurs in the sentence(the "NUM" column) 2.
    * whether the sentence was marked as the start or end of the sentence (the
    * "S/E" column) 3. the text string corresponding to the token (the
    * "TOKEN-VALUE" column) 4. the anchor values for the token within the text
    * (the "ANCHORS" column)
    * 
    * Following this table, the procedure looks at the start and end tokens and
    * traces their parentage
    * 
    * Typically, if there are issues, there will either be multiple tokens
    * labeled "#S#" or "#E#" and their anchors will be drastically different
    * from other tokens in the sentence. These are usually caused by "traces",
    * in which a word refers to another word earlier in the sentence (e.g. the
    * demonstrative "these" in the previous sentence, referring to the issues in
    * the sentence preceding it)
    */
   public void diagnose(int sentenceIndex)
   {
      /* use sentenceIndex to index into the augmentedSentences list */
      AugmentedSentenceNode patient = augmentedSentences.get(sentenceIndex);

      /* print out information about the sentence in question */
      //OPENING BORDER
      System.out.println("\n\n=====DIAGNOSING SENTENCE[" + sentenceIndex
            + "]...=====");

      //ANCHOR INFORMATION
      System.out.print("STARTING INDEX:\t");
      System.out.print(patient.getStart());
      System.out.println();

      System.out.print("ENDING INDEX:\t");
      System.out.print(patient.getEnd());
      System.out.println();

      //FULL LIST OF TOKENS
      int tokIndex = 0; //index to keep track of token number
      //HEADER
      System.out.println("\n|-NUM-|-S/E-|----TOKEN-VALUE----|---ANCHORS---");
      for (INode tok : patient.getTokens())
      {
         //get the token's anchors
         CharacterAnchor tokS = (CharacterAnchor) tok.getLinks().get(0)
               .getRegions().get(0).getAnchor(0);
         CharacterAnchor tokE = (CharacterAnchor) tok.getLinks().get(0)
               .getRegions().get(0).getAnchor(1);

         //print out the token's index
         System.out.printf("| %-3d ", tokIndex++);

         /*
          * If the token marks a start or end index, i.e. is at the beginning or
          * end of the sentence, record that, otherwise, print the equivalent
          * number of spaces
          */
         if (tokS.getOffset() == patient.getStart())
         {
            System.out.print("| #S# ");
         }
         else if (tokE.getOffset() == patient.getEnd())
         {
            System.out.print("| #E# ");
         }
         else
         {
            System.out.print("|     ");
         }

         //print out the token's string value				
         System.out.printf("|   %-15s", sourceText.substring(tokS.getOffset()
               .intValue(), (Integer) tokE.getOffset().intValue()));

         //display the start and end anchors
         System.out.print("\t|  [" + tokS.getOffset() + "," + tokE.getOffset()
               + "]");
         System.out.print("\n");
      }

      //---START TOKEN DIAGNOSIS---
      // get the node corresponding to the token that last updated the start value
      INode startToken = patient.getStartNode();
      // get the startToken's end anchor
      CharacterAnchor stEndAnchor = (CharacterAnchor) startToken.getLinks()
            .get(0).getRegions().get(0).getAnchor(1);
      // use the end anchor to get the integer offset
      int stEnd = stEndAnchor.getOffset().intValue();

      System.out.println();
      System.out.println("---START-TOKEN---");
      System.out.println("INDEX: " + patient.getStart());
      System.out.println("LABEL: " + startToken.getAnnotation().getLabel());
      System.out.println("TOKEN: "
            + sourceText.substring((int) patient.getStart(), stEnd));
      System.out.println("TRACE: ");
      printTrace(startToken);
      System.out.println("-----------------");
      //-------------------------------

      //---END TOKEN DIAGNOSIS---
      // get the node corresponding to the token that last updated the start value
      INode endToken = patient.getEndNode();
      // get the startToken's end anchor
      CharacterAnchor etStartAnchor = (CharacterAnchor) endToken.getLinks()
            .get(0).getRegions().get(0).getAnchor(0);
      // use the end anchor to get the integer offset
      int etStart = etStartAnchor.getOffset().intValue();

      System.out.println();
      System.out.println("---End-TOKEN---");
      System.out.println("INDEX: " + patient.getEnd());
      System.out.println("LABEL: " + endToken.getAnnotation().getLabel());
      System.out.println("TOKEN: "
            + sourceText.substring(etStart, (int) patient.getEnd()));
      System.out.println("TRACE: ");
      printTrace(endToken);
      System.out.println("-----------------");
      //---------------------------

      /* Signal the end of the diagnosis */
      System.out.println("\n=========END DIAGNOSIS=========");

   }

   /**
    * Prints out a trace of the navigation indicated by the given indices,
    * assumes that the sentence has already been set
    * 
    * @param tokenAcross
    *           - # of leaves to move horizontally
    * @param parentUp
    *           - # of nodes to move up vertically
    */
   public void navigateTrace(int tokenAcross, int parentUp)
   {
      //move across the leaves to the given token
      setToken(tokenAcross);

      System.out.println("=======NAVIGATE TRACE=======");
      System.out.println("Move [" + sentIndex + "](" + tokenAcross + ","
            + parentUp + ")");
      printTraceIndexed(this.token, parentUp);

   }

   /**
    * Prints out a trace of the navigation indicated by the given indices,
    * assumes that the sentence has already been set
    * 
    * @param sentence
    *           - # sentence in the text
    * @param tokenAcross
    *           - # of leaves to move horizontally
    * @param parentUp
    *           - # of nodes to move up vertically
    */
   public void navigateTrace(int sentence, int tokenAcross, int parentUp)
   {
      //move down to the desired sentence
      setSentence(sentence);
      //move across the leaves to the given token
      setToken(tokenAcross);

      System.out.println("=======NAVIGATE TRACE=======");
      System.out.println("Move [" + sentence + "](" + tokenAcross + ","
            + parentUp + ")");
      printTraceIndexed(this.token, parentUp);

   }

   /**
    * this method takes in an INode and traces its parentage
    * 
    * @param target
    *           INode the base token to trace upward on a graph
    */
   public void printTrace(INode target)
   {
      /* LOCAL VARIABLES */
      int index = 0; //keeps track of how far up we've gone

      //initialize our temporary variable
      INode temp = target;

      //while we still have parents to explore
      while (temp.getParent() != null)
      {
         System.out.println("\t" + index++ + ": "
               + temp.getAnnotation().getLabel());
         temp = temp.getParent();
      }
   }

   /**
    * this method takes in an INode and traces its parentage
    * 
    * @param target
    *           INode the base token to trace upward on a graph
    */
   public void printTraceIndexed(INode target, int endIndex)
   {
      /* LOCAL VARIABLES */
      int index = 0; //keeps track of how far up we've gone

      //initialize our temporary variable
      INode temp = target;

      //while we still have parents to explore
      while ((temp.getParent() != null) && (index <= endIndex))
      {
         System.out.println("\t" + index++ + ": "
               + temp.getAnnotation().getLabel());
         temp = temp.getParent();
      }
   }

   /**
    * this method takes in an index and travels that many levels up from the
    * start token *
    * 
    * @param index
    *           int how far up to trace from the token given
    */
   public INode trace(int index)
   {
      /* LOCAL VARIABLES */
      int currindex = 0; //keeps track of how far up we've gone

      //initialize our temporary variable
      INode temp = token;

      //while we still have parents to explore
      while (currindex != index)
      {
         //go up one level
         temp = temp.getParent();
         //update the current index
         currindex++;
      }

      return temp;
   }

/* LOCAL VARIABLES */

   /* Variables involved in file I/O */
   /** This string holds the path to the desired PTB file to be processed */
   protected String root;

   /** the extension for the Penn Tree Bank representation */
   static final String PTB_EXTENSION = "-ptb.xml";

   /** the extension for the source text file */
   static final String SOURCE_EXTENSION = ".txt";

   /* Variables involved in constructing the source files */
   /** a UTF8Reader for the source file */
   protected UTF8Reader source;

   /** a string to hold the source text */
   protected String sourceText;

   /** a graph parser to handle the PTB representation */
   protected GrafParser graphParse;

   /**
    * the graph that will serve as the main construct over which we are
    * navigating
    */
   //protected IGraph _ptbGraph;

   /* Variables involved in sentence organization */
   /** holds the root node for the current sentence being navigated to */
   protected INode sentence;

   /** holds the index for the current sentence being navigated to */
   protected int sentIndex;

   /** empty collection for all the sentences in the PTB representation */
   protected ArrayList<INode> sentences = new ArrayList<INode>();

   /**
    * empty collection for the original order of the sentences to be preserved
    * after the sentences are sorted by the AnchorComparator. TODO: hopefully
    * this won't be necessary in the future, but for now, the sentences from the
    * PTB representation need to be organized locally
    */
   protected ArrayList<INode> unOrganizedSentences = new ArrayList<INode>();

   /**
    * empty collection for all the sentences in the PTB representation after
    * being converted to AugmentedSentenceNodes
    */
   protected ArrayList<AugmentedSentenceNode> augmentedSentences = new ArrayList<AugmentedSentenceNode>();

   /* Variables involved in token navigating */
   /** holds the token being accessed in the current sentence being navigated to */
   protected INode token;
   /** stores the index for the current token anchoring the navigation */
   protected int tokIndex;
}
