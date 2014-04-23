package Scape;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;



public class XMLDescriptors {

	//static Document documentDelta;
	//static Document documentViXML1;
	//static Document documentViXML2;

	static Document documentDelta;
	static Document documentViXML1;
	static Document documentViXML2;
	
	public static void run(String fichierXml1, String fichierXml2/*, String fichierDelta*/, ArrayList<Double> desc, boolean isFortrain) {
		// TODO Auto-generated method stub
		//Element rootDelta;
		Element rootViXML1;
		Element rootViXML2;

		//double[] tableJaccardIndex  = new double[2];//Links & Images 

		//On crée une instance de SAXBuilder
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			//documentDelta  = builder.parse(fichierDelta);
			if(isFortrain)
			{
				documentViXML1 = builder.parse(new File(fichierXml1)); // for test
				documentViXML2 = builder.parse(new File(fichierXml2));
			}
			else // coming from pagelyzer
			{	
				documentViXML1 = builder.parse(new InputSource(new StringReader(fichierXml1)));
				documentViXML2 = builder.parse(new InputSource(new StringReader(fichierXml2)));
			}
			//Le parsing est terminé ;)
		}
		catch(Exception e){
			e.printStackTrace();
		}

		//On initialise un nouvel élément racine avec l'élément racine du document.
		rootViXML1 = documentViXML1.getDocumentElement();
		rootViXML2 = documentViXML2.getDocumentElement();
		//desc.add(JaccardIndexLinks(rootViXML1, rootViXML2, false));
		//desc.add(JaccardIndexImages(rootViXML1, rootViXML2, false));
		NodeList nodeLstsource = rootViXML1.getElementsByTagName("Block");
	    NodeList nodeLstversion = rootViXML2.getElementsByTagName("Block");
		desc.add(BlockBasedContent(0,nodeLstsource, nodeLstversion,"Adr"));// links
		desc.add(BlockBasedContent(0,nodeLstsource, nodeLstversion,"Name"));// links
		desc.add(BlockBasedContent(1,nodeLstsource, nodeLstversion,"Src"));// images
		desc.add(BlockBasedContent(1,nodeLstsource, nodeLstversion,"Name"));// images
		desc.add(BlockBasedContent(2,nodeLstsource, nodeLstversion,"Txt"));// Text
	
	
	}
	
	private static double BlockBasedContent(int type, NodeList nodeLstsource, NodeList nodeLstversion, String atr)
	{ 
	    
	    if(nodeLstsource.getLength() != nodeLstversion.getLength())
	    	return 0; // structural change  ////TODO discuss with MARC
	    
	    double resultoverblocks = 0;

	    HashMap<String, Double> temptext1;
	    HashMap<String, Double> temptext2;
	    for(int i = 0; i< nodeLstsource.getLength();i++ )
	    {
	    	if(type == 0) // Links
			{
	    		resultoverblocks+=JaccardIndexLinks((Element)nodeLstsource.item(i), (Element)nodeLstversion.item(i), false,atr);
			}
	    	else if(type == 1) // Images
	    		resultoverblocks+=JaccardIndexImages((Element)nodeLstsource.item(i), (Element)nodeLstversion.item(i), false,atr);
	    	else // Txt
	    	{
	    		// get first document text
	    		Element el = (Element) nodeLstsource.item(i);
	    		if(el.getAttribute("ID")!="") // not all blocks I need leaf blocks
	    		{
		    		NodeList txtl = el.getElementsByTagName("Txts");
		    		
		    		
		    		
		    		if(txtl!=null&&txtl.item(0)!=null)
		    			temptext1 = CosineSimilarity.getFeaturesFromString(((Element)txtl.item(0)).getAttribute(atr));
		    		else return 0;
		    		
		    		// second document text 
		    		el = (Element) nodeLstversion.item(i);
		    		txtl = el.getElementsByTagName("Txts");
		    		
		    		if(txtl!=null&&txtl.item(0)!=null)
		    			temptext2 = CosineSimilarity.getFeaturesFromString(((Element)txtl.item(0)).getAttribute(atr));
		    		else return 0;
		    		//System.out.println(temptext1);
		    		//System.out.println(temptext2);
		    		//System.out.println(CosineSimilarity.calculateCosineSimilarity(temptext1, temptext2));
		    		if(temptext1.size()!=0 &&temptext2.size()!=0)
		    			resultoverblocks+= CosineSimilarity.calculateCosineSimilarity(temptext1, temptext2);
	    		}
	    	}
	    }
		System.out.println(resultoverblocks);
		return resultoverblocks/nodeLstsource.getLength();
		
		
	}
	
	/**
	 * @param args
	 */
	public static void run_old(String filePath, String repM) {
		// TODO Auto-generated method stub
		//Element rootDelta;
		Element rootViXML1;
		Element rootViXML2;
		File src = new File(filePath);
		if(!src.exists() || !src.isDirectory())
		{
			System.out.println(filePath+" : No such directory !");
			return;
		}
		String[] files2 = src.list();
		int nbCouples = files2.length;
		/*int[] tableSameStructure = new int[nbCouples];
		int[] tableContainsDelete = new int[nbCouples];
		int[] tableContainsInsert = new int[nbCouples];
		int[] tableContainsNoDeleteNorInsert = new int[nbCouples];
		int[] tableContainsUpdate = new int[nbCouples];
		int[] tableNbDeletedBlocks = new int[nbCouples];
		int[] tableNbInsertedBlocks = new int[nbCouples];
		int[] tableNbUpdatedBlocks = new int[nbCouples];
		int[] tableSourceContainsLinks = new int[nbCouples];
		int[] tableVersionContainsLinks = new int[nbCouples];
		int[] tableSourceContainsImages = new int[nbCouples];
		int[] tableVersionContainsImages = new int[nbCouples];*/
		double[] tableJaccardIndexLinks = new double[nbCouples];
		double[] tableJaccardIndexImages = new double[nbCouples];
		/*double[] tableJaccardIndexIDLinks = new double[nbCouples];
		double[] tableJaccardIndexIDImages = new double[nbCouples];
		double[] tableMaxRatioDelete = new double[nbCouples];
		double[] tableMinRatioDelete = new double[nbCouples];
		double[] tableMaxRatioUpdate = new double[nbCouples];
		double[] tableMinRatioUpdate = new double[nbCouples];
		double[] tableMaxRatioInsert = new double[nbCouples];
		double[] tableMinRatioInsert = new double[nbCouples];
		int[] nbBlocksSourceUpperBounded = new int[nbCouples];
		int[] nbBlocksVersionUpperBounded = new int[nbCouples];
		int[] tableNbDeleteAllTree = new int[nbCouples];
		int[] tableNbInsertAllTree = new int[nbCouples];
		int[] tableNbUpdateAllTree = new int[nbCouples];*/



		for (String f2 : files2) {
			int coupleIndex = Integer.valueOf(f2);
			String deltaFileName = null;// = "DeltaVI-XML_07-05-11_15-16-26.xml";
			boolean first = true;
			String viXML1FileName = null;
			String viXML2FileName = null;
			for(String f : new File(filePath+f2).list()) {
				File fi = new File(filePath+f2+"/"+f);
//				System.out.println("f = " + f);
				if ("delta".equals(fi.getName())) {
					deltaFileName = "delta/" +fi.list()[0];
//					System.out.println(deltaFileName);
				}
				else if (first){
					viXML1FileName = f;
					first = false;
				}
				else if ((f.compareTo(viXML1FileName) < 0)) {
					viXML2FileName = viXML1FileName;
					viXML1FileName = f;
				}
				else 
					viXML2FileName = f;
			}

			//On crée une instance de SAXBuilder
			//SAXBuilder sxb = new SAXBuilder();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			try
			{
				DocumentBuilder builder = factory.newDocumentBuilder();
				documentDelta = builder.parse(new File(filePath+f2+"/" + deltaFileName));
				//System.out.println(filePath+f2+"/" + viXML1FileName);
				documentViXML1 = builder.parse(new File(filePath+f2+"/" + viXML1FileName));
				documentViXML2 = builder.parse(new File(filePath+f2+"/" + viXML2FileName));
				//On crée un nouveau document JDOM avec en argument le fichier XML
				//Le parsing est terminé ;)
				//documentDelta = sxb.build(new File(filePath + deltaFileName));
				//documentViXML1 = sxb.build(new File(filePath + viXML1FileName));
				//documentViXML2 = sxb.build(new File(filePath + viXML2FileName));
			}
			catch(Exception e){}

			//On initialise un nouvel élément racine avec l'élément racine du document.
			//rootDelta = documentDelta.getDocumentElement();
			rootViXML1 = documentViXML1.getDocumentElement();
			rootViXML2 = documentViXML2.getDocumentElement();


//			System.out.println("same structure : " + sameStructure(rootViXML1, rootViXML2));
//			System.out.println("delete : " + containsDelete(rootDelta));
//			System.out.println("insert : " + containsInsert(rootDelta));
//			System.out.println("update : " + containsUpdate(rootDelta));
//			System.out.println("total nb deleted blocks : " + nbDelete(rootDelta));
//			System.out.println("total nb inserted blocks : " + nbInsert(rootDelta));
//			System.out.println("total nb updated blocks : " + nbUpdate(rootDelta));
//			System.out.println("not feature : nb node blocks viXML1 : " + nbNodeBlocks(rootViXML1));
//			System.out.println("not feature : nb node blocks viXML2 : " + nbNodeBlocks(rootViXML2));
//
//			System.out.println("noDeleteNorInsert : " + noInsertionNorDeletion(rootDelta));
//			System.out.println("has links vixml1 : " + containsLinks(rootViXML1));
//			System.out.println("has links vixml2 : " + containsLinks(rootViXML2));
//			System.out.println("has images vixml1 : " + containsImages(rootViXML1));
//			System.out.println("has images vixml2 : " + containsImages(rootViXML2));
//			System.out.println("jaccard index links : " + JaccardIndexLinks(rootViXML1, rootViXML2, false));
//			System.out.println("jaccard index images : " + JaccardIndexImages(rootViXML1, rootViXML2, false));
//			System.out.println("jaccard index ID links : " + JaccardIndexLinks(rootViXML1, rootViXML2, false));
//			System.out.println("jaccard index ID images : " + JaccardIndexImages(rootViXML1, rootViXML2, false));
//			System.out.println("maxRatioDelete : " + maxRatioDelete(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("minRatioDelete : " + minRatioDelete(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("maxRatioUpdate : " + maxRatioUpdate(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("minRatioUpdate : " + minRatioUpdate(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("maxRatioInsert : " + maxRatioInsert(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("minRatioInsert : " + minRatioInsert(rootDelta, rootViXML1, rootViXML2));
//			System.out.println("total nbblocks : " + nbBlocks(rootViXML1));
//			System.out.println("containsstring : " +  containsStringAdvertisement(rootDelta, "Block", "Insert", "link"));
//			System.out.println("nb links that are not advertisements " + nbChooseAdvertisement(rootViXML1, "link", false));
//			System.out.println("nb links that can be advertisements " + nbChooseAdvertisement(rootViXML1, "link", true));



/*

			tableSameStructure[coupleIndex-1] = sameStructure(rootViXML1, rootViXML2)?1:0;
			tableContainsDelete[coupleIndex-1] = containsDelete(rootDelta)?1:0;
			tableContainsInsert[coupleIndex-1] = containsInsert(rootDelta)?1:0;
			tableContainsNoDeleteNorInsert[coupleIndex-1] = noInsertionNorDeletion(rootDelta)?1:0;
			tableContainsUpdate[coupleIndex-1] = containsUpdate(rootDelta)?1:0;
			tableNbDeletedBlocks[coupleIndex-1] = nbDelete(rootDelta);
			tableNbInsertedBlocks[coupleIndex-1] = nbInsert(rootDelta);
			tableNbUpdatedBlocks[coupleIndex-1] = nbUpdate(rootDelta);

			tableNbDeleteAllTree[coupleIndex-1] = nbDeleteAllTree(rootDelta);
			tableNbInsertAllTree[coupleIndex-1] = nbInsertAllTree(rootDelta);
			tableNbUpdateAllTree[coupleIndex-1] = nbUpdateAllTree(rootDelta);

			tableSourceContainsLinks[coupleIndex-1] = containsLinks(rootViXML1)?1:0;
			tableVersionContainsLinks[coupleIndex-1] =  containsLinks(rootViXML2)?1:0;
			tableSourceContainsImages[coupleIndex-1] =  containsImages(rootViXML1)?1:0;
			tableVersionContainsImages[coupleIndex-1] = containsImages(rootViXML1)?1:0;*/
			tableJaccardIndexLinks[coupleIndex-1] = JaccardIndexLinks(rootViXML1, rootViXML2, false,"Adr");
			tableJaccardIndexImages[coupleIndex-1] = JaccardIndexImages(rootViXML1, rootViXML2, false,"Img");
			/*tableJaccardIndexIDLinks[coupleIndex-1] = JaccardIndexIDLinks(rootViXML1, rootViXML2, false);
			tableJaccardIndexIDImages[coupleIndex-1] = JaccardIndexIDImages(rootViXML1, rootViXML2, false);
			tableMaxRatioDelete[coupleIndex-1] = maxRatioDelete(rootDelta, rootViXML1, rootViXML2);
			tableMinRatioDelete[coupleIndex-1] = minRatioDelete(rootDelta, rootViXML1, rootViXML2);
			tableMaxRatioUpdate[coupleIndex-1] = maxRatioInsert(rootDelta, rootViXML1, rootViXML2);
			tableMinRatioUpdate[coupleIndex-1] = minRatioUpdate(rootDelta, rootViXML1, rootViXML2);
			tableMaxRatioInsert[coupleIndex-1] = maxRatioInsert(rootDelta, rootViXML1, rootViXML2);
			tableMinRatioInsert[coupleIndex-1] = minRatioInsert(rootDelta, rootViXML1, rootViXML2);

			nbBlocksSourceUpperBounded[coupleIndex-1] = nbBlocksUpperBounded(rootViXML1);
			nbBlocksVersionUpperBounded[coupleIndex-1] = nbBlocksUpperBounded(rootViXML2);*/

		}

/*
//		printTable(tableSameStructure, "same structure");
		writeTable(tableSameStructure, repM);
//		printTable(tableContainsUpdate, "contains update");
		writeTable(tableContainsUpdate, repM);
//		printTable(tableContainsDelete, "contains delete");
		writeTable(tableContainsDelete, repM);
//		printTable(tableContainsInsert, "contains insert");
		writeTable(tableContainsInsert, repM);


//		printTable(tableNbUpdateAllTree, "nb update in all tree");
		writeTable(tableNbUpdateAllTree, repM);
//		printTable(tableNbDeleteAllTree, "nb Delete in all tree");
		writeTable(tableNbDeleteAllTree, repM);
//		printTable(tableNbInsertAllTree, "nb insert in all tree");
		writeTable(tableNbInsertAllTree, repM);
//		printTable(tableContainsNoDeleteNorInsert, "contains no delete nor insert");
		writeTable(tableContainsNoDeleteNorInsert, repM);
//		printTable(tableNbDeletedBlocks, "nb deleted blocks");
		writeTable(tableNbDeletedBlocks, repM);
//		printTable(tableNbInsertedBlocks, "nb inserted blocks");
		writeTable(tableNbInsertedBlocks, repM);
//		printTable(tableNbUpdatedBlocks, "nb updates blocks");
		writeTable(tableNbUpdatedBlocks, repM);

//		printTable(tableSourceContainsLinks, "Source contains links");
		writeTable(tableSourceContainsLinks, repM);
//		printTable(tableVersionContainsLinks, "Version contains links");
		writeTable(tableVersionContainsLinks, repM);
//		printTable(tableSourceContainsImages, "Source contains images");
		writeTable(tableSourceContainsImages, repM);
//		printTable(tableVersionContainsImages, "Version contains images");
		writeTable(tableVersionContainsImages, repM);
		*/
//		printTable(tableJaccardIndexLinks, "jaccard index of links between source and version");
		writeTable(tableJaccardIndexLinks, repM);
//		printTable(tableJaccardIndexImages, "jaccard index of images between source and version");
		writeTable(tableJaccardIndexImages, repM);/*
//		printTable(tableJaccardIndexIDLinks, "jaccard index of ID links between source and version");
		writeTable(tableJaccardIndexIDLinks, repM);
//		printTable(tableJaccardIndexIDImages, "jaccard index of ID images between source and version");
		writeTable(tableJaccardIndexIDImages, repM);
		
//		printTable(tableMaxRatioDelete, "max ratio delete");
		writeTable(tableMaxRatioDelete, repM);
//		printTable(tableMinRatioDelete, "min ratio delete");
		writeTable(tableMinRatioDelete, repM);
//		printTable(tableMaxRatioUpdate, "max ratio update");
		writeTable(tableMaxRatioUpdate, repM);
//		printTable(tableMinRatioUpdate, "min ratio update");
		writeTable(tableMinRatioUpdate, repM);
//		printTable(tableMaxRatioInsert, "max ratio insert");
		writeTable(tableMaxRatioInsert, repM);
//		printTable(tableMinRatioInsert, "min ratio insert");
		writeTable(tableMinRatioInsert, repM);
		
//		printTable(nbBlocksSourceUpperBounded, "nb blocks in source");
		writeTable(nbBlocksSourceUpperBounded, repM);
//		printTable(nbBlocksVersionUpperBounded, "nb blocks in version");
		writeTable(nbBlocksVersionUpperBounded, repM);*/
	}

	public static void printTable(int[] table, String s){
		System.out.print(s + " :");
		for (int i = 0 ; i < table.length ; ++i){
			System.out.print(" " + table[i]);
		}
		System.out.println();
	}

	public static void writeTable(int[] table, String s){
		FileWriter writer = null;
		String texte = "";
		for (int i = 0 ; i < table.length -1; ++i){
			texte += table[i] + " ";
		}
		texte += table[table.length-1] + "\n";
		try{
		     writer = new FileWriter(s, true);
		     writer.write(texte,0,texte.length());
		}catch(IOException ex){
		    ex.printStackTrace();
		}finally{
		  if(writer != null){
		     try {
				writer.close();
			} catch (IOException e) {e.printStackTrace();}
		  }
		}
	}
	
	public static void printTable(double[] table, String s){
		System.out.print(s + " :");
		for (int i = 0 ; i < table.length ; ++i){
			System.out.print(" " + table[i]);
		}
		System.out.println();
	}

	public static void writeTable(double[] table, String s){
		FileWriter writer = null;
		String texte = "";
		for (int i = 0 ; i < table.length -1; ++i){
			texte += table[i] + " ";
		}
		texte += table[table.length-1] + "\n";
		try{
		     writer = new FileWriter(s, true);
		     writer.write(texte,0,texte.length());
		}catch(IOException ex){
		    ex.printStackTrace();
		}finally{
		  if(writer != null){
		     try {
				writer.close();
			} catch (IOException e) {e.printStackTrace();}
		  }
		}
	}
	
	public static int nbBlocksUpperBounded(Element e) {
		return nbBlocksUpperBounded(e, 10);
	}

	public static int nbBlocksUpperBounded(Element e, int nbBlockMax) {
		int a = nbBlocks(e);
		return (a>=nbBlockMax)?nbBlockMax:a;
	}

	public static boolean sameStructure(Element e1, Element e2){
		NodeList l1 = e1.getElementsByTagName("Block");
		NodeList l2 = e2.getElementsByTagName("Block");
		final int length = l1.getLength();
		if (length != l2.getLength()) {
			return false;
		}
		for (int i = 0 ; i < length ; ++i){
			if (!((Element)l1.item(i)).getAttribute("Ref").equals(((Element)l2.item(i)).getAttribute("Ref"))) {
				return false;
			}
		}
		return true;
	}

	public static int nbBlockString(Element e, String s) {
		NodeList l = e.getChildNodes();
		final int length = l.getLength();
		int result = 0;
		for (int i = 0 ; i < length ; ++i) {
			if (s.equals(l.item(i).getNodeName()))
				++result;
		}
		return result;
	}

	public static int nbNodeBlocks(Element eXML) {
		NodeList nl = eXML.getElementsByTagName("Block");
		final int length = nl.getLength();
		int result = 0;
		for (int i = 0 ; i < length ; ++i) {
			NodeList l = nl.item(i).getChildNodes();
			final int length2 = l.getLength();
			boolean addbool = true;
			for (int j = 0 ; j < length2 ; ++j) {
				if ("Block".equals(l.item(j).getNodeName())) {
					addbool = false;
					break;
				}
			}
			if (addbool)
				++result;
		}
		return result;
	}

	public static int nbString(Element e, String s) {
		return e.getElementsByTagName(s).getLength();
	}

	// ok
	public static int nbBlocks(Element e){
		return nbString(e, "Block");
	}

	// ok
	public static int nbInsertAllTree(Element e){
		return nbString(e, "Insert");
	}

	// ok
	public static int nbDeleteAllTree(Element e){
		return nbString(e, "Delete");
	}

	// ok
	public static int nbUpdateAllTree(Element e){
		return nbString(e, "Update");
	}

	// ok
	public static int nbDelete(Element e) {
		return nbBlockString(e, "Delete");
	}

	// ok
	public static int nbInsert(Element e) {
		return nbBlockString(e, "Insert");
	}

	// ok
	public static int nbUpdate(Element e) {
		return nbBlockString(e, "Update");
	}	

	// ok
	public static double ratioString(Element eDelta, Element eXML, String s) {
		return ((double) nbBlockString(eDelta, s))/nbBlocks(eXML);
	}


	// ok : ratio de blocks mis a jour
	public static double ratioUpdate(Element eDelta, Element eXML) {
		return ratioString(eDelta, eXML, "Update");
	}

	public static double ratioInsert(Element eDelta, Element eXML) {
		return ratioString(eDelta, eXML, "Insert");
	}

	public static double ratioDelete(Element eDelta, Element eXML) {
		return ratioString(eDelta, eXML, "Delete");
	}

	// ok
	public static double ratioNodeString(Element eDelta, Element eXML, String s) {
		return ((double) nbBlockString(eDelta, s))/nbNodeBlocks(eXML);
	}
	// ok : ratio de blocks mis a jour
	public static double ratioNodeUpdate(Element eDelta, Element eXML) {
		return ratioNodeString(eDelta, eXML, "Update");
	}

	public static double ratioNodeInsert(Element eDelta, Element eXML) {
		return ratioNodeString(eDelta, eXML, "Insert");
	}

	public static double ratioNodeDelete(Element eDelta, Element eXML) {
		return ratioNodeString(eDelta, eXML, "Delete");
	}


	public static double ratioNodeString(Element eDelta, Element eXML1, Element eXML2, String s, boolean min) {
		if (min)
			return Math.min(ratioNodeString(eDelta, eXML1, s), ratioString(eDelta, eXML2, s));
		return Math.max(ratioNodeString(eDelta, eXML1, s), ratioString(eDelta, eXML2, s));
	}

	public static double minRatioNodeUpdate(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Update", true);
	}

	public static double maxRatioNodeUpdate(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Update", false);
	}

	public static double minRatioNodeInsert(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Insert", true);
	}

	public static double maxRatioNodeInsert(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Insert", false);
	}

	public static double minRatioNodeDelete(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Delete", true);
	}

	public static double maxRatioNodeDelete(Element eDelta, Element eXML1, Element eXML2) {
		return ratioNodeString(eDelta, eXML1, eXML2, "Delete", false);
	}


	public static double ratioString(Element eDelta, Element eXML1, Element eXML2, String s, boolean min) {
		if (min)
			return Math.min(ratioString(eDelta, eXML1, s), ratioString(eDelta, eXML2, s));
		return Math.max(ratioString(eDelta, eXML1, s), ratioString(eDelta, eXML2, s));
	}

	public static double minRatioUpdate(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Update", true);
	}

	public static double maxRatioUpdate(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Update", false);
	}

	public static double minRatioInsert(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Insert", true);
	}

	public static double maxRatioInsert(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Insert", false);
	}

	public static double minRatioDelete(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Delete", true);
	}

	public static double maxRatioDelete(Element eDelta, Element eXML1, Element eXML2) {
		return ratioString(eDelta, eXML1, eXML2, "Delete", false);
	}

	public static int containsStringAdvertisement(Element e, String s, String s2, String s3) {
		final NodeList nl = e.getElementsByTagName(s);
		final int length1 = nl.getLength();
		Element eAux = null;
		NamedNodeMap nnm = null;
		int result = 0;
		for (int i = 0 ; i < length1 ; ++i) {
			final NodeList nl2 = nl.item(i).getChildNodes();
			final int length2 = nl2.getLength();
			for (int j = 0 ; j < length2 ; ++j) {
				if (s2.equals((eAux = (Element) nl2.item(j)).getNodeName())) {
					final NodeList nl3 = eAux.getChildNodes();
					final int length3 = nl3.getLength();
					for (int k = 0 ; k < length3 ; ++k) {
						if (s3.equals((nl3.item(k).getNodeName()))) {
							boolean pub = false;
							nnm = nl3.item(k).getAttributes();
							final int length4 = nnm.getLength();
							for (int l = 0 ; l < length4 ; ++l){
								if ((nnm.item(l).toString().contains("publicit")) || (nnm.item(l).toString().contains("advertise"))){
									pub = true;
									break;
								}
							}
							if (!pub) {
								++result;							
							}
						}
					}
				}
			}
		}
		return result;
	}


	public static boolean containsString(Element e, String s) {
		return (e.getElementsByTagName(s).getLength() != 0);
	}

	public static boolean containsDelete(Element e) {
		return containsString(e,"Delete");
	}

	public static boolean containsInsert(Element e) {
		return containsString(e,"Insert");
	}

	public static boolean containsUpdate(Element e) {
		return containsString(e,"Update");
	}

	public static boolean noInsertionNorDeletion(Element e) {
		return !containsInsert(e) && !containsDelete(e);
	}

	public static boolean containsLinks(Element e){
		return containsString(e, "link");
	}

	public static boolean containsImages(Element e){
		return containsString(e, "img");
	}

	public static Hashtable<String, Integer> getLinksOrImage(Element e, boolean split, String tag, String attribute){
		Hashtable<String, Integer> result = new Hashtable<String, Integer>();
		NodeList l = e.getElementsByTagName(tag);
		final int length = l.getLength();
		for (int i = 0 ; i < length ; ++i){
			String address = ((Element)l.item(i)).getAttribute(attribute);
			if (split) {
				String[] s = address.split("://");
				address = s[s.length-1];
			}
			result.put(address, 0);
		}
		return result;
	}

	public static Hashtable<String, Integer> getLinks(Element e, boolean split){
		return getLinksOrImage(e, split, "links", "Adr");
	}

	public static Hashtable<String, Integer> getImages(Element e, boolean split){
		return getLinksOrImage(e, split, "img", "Src");
	}

	public static double JaccardIndex(Element e1, Element e2, boolean split, String tag, String attribute){
		Hashtable<String, Integer> hash1 = getLinksOrImage(e1, split, tag, attribute);
		Hashtable<String, Integer> hash2 = getLinksOrImage(e2, split, tag, attribute);
		int nbCommon = 0; 
		for (String s : hash1.keySet()) {
			if (hash2.containsKey(s)) {
				++nbCommon;
			}
		}
		int a = (hash1.size()+hash2.size()-nbCommon);
		if (a == 0) {
			return 0;
		}
		return ((double)nbCommon)/a;
	}

	public static double JaccardIndexLinks(Element e1, Element e2, boolean split,String attr){
		return JaccardIndex(e1, e2, split, "link", attr);
	}
	
	public static double JaccardIndexImages(Element e1, Element e2, boolean split,String attr){
		return JaccardIndex(e1, e2, split, "img", attr);
	}

	public static double JaccardIndexIDLinks(Element e1, Element e2, boolean split){
		return JaccardIndex(e1, e2, split, "link", "ID");
	}

	public static double JaccardIndexIDImages(Element e1, Element e2, boolean split){
		return JaccardIndex(e1, e2, split, "img", "ID");
	}

	public static int nbChooseAdvertisement(Element eXML, String s, boolean advertisement){
		int result = 0;
		NodeList nl = eXML.getElementsByTagName(s);
		final int length = nl.getLength();
		if (advertisement) {
			return length;
		}
		for (int i = 0 ; i < length ; ++i) {
			boolean pub = false;
			NamedNodeMap nnm = nl.item(i).getAttributes();
			final int length2 = nnm.getLength();
			for (int l = 0 ; l < length2 ; ++l){
				if ((nnm.item(l).toString().contains("publicit")) || (nnm.item(l).toString().contains("advertise"))){
					pub = true;
					break;
				}
			}
			if (!pub){
				++result;
			}
		}
		return result;
	}

}
