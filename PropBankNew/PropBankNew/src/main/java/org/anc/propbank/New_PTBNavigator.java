package org.anc.propbank;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import org.xces.graf.api.GrafException;
import org.xces.graf.api.IEdge;
import org.xces.graf.api.IGraph;
import org.xces.graf.api.INode;
import org.xces.graf.io.GrafParser;
import org.xces.graf.io.GrafRenderer;
import org.xces.graf.io.RenderException;
import org.xces.graf.io.dom.ResourceHeader;
import org.xml.sax.SAXException;

public class New_PTBNavigator {
	 public static final Constants K = new Constants();
	 
	 private String root;
	 private GrafParser graphParse;
	 private IGraph graph;
	 private ArrayList<INode> sentences;
	 
	 public New_PTBNavigator(String root) throws SAXException, IOException, GrafException{
			 
		 this.root = root;
		 File headerFile = new File(K.MASC_RESOURCE_HEADER);
		 ResourceHeader header = new ResourceHeader(headerFile);
		 this.graphParse = new GrafParser(header);
		 this.graph = graphParse.parse(root + "-ptb.xml");
		 this.sentences = new ArrayList<INode>();
		 this.setSentences();
		 System.out.println(this.sentences);
	 }
	 
	 public void printPTBGraph() throws RenderException{
		 System.out.println(this.graph.toString());
		 GrafRenderer renderer = new GrafRenderer(System.out);
		 renderer.render(this.graph);
	 }
	 
	 public void setSentences(){
		 INode rootNode = this.graph.getRoot();
		 for (IEdge edge : rootNode.getOutEdges()){
			 this.sentences.add(edge.getTo());
		 }
		 Collections.sort(sentences, new AnchorComparator());
	 }
	 
	 
	 
	 
}
	 
