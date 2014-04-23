package eu.scape_project;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;





import JDescriptors.CreateIHSVectors;
import JDescriptors.SpatialPyramids;
import JDescriptors.fr.lip6.Descriptor;
import JKernelMachines.fr.lip6.classifier.SMOSVM;
import JKernelMachines.fr.lip6.kernel.IndexedKernel;
import Scape.FileConfig;
import Scape.XMLDescriptors;

public class MarcAlizer {
	
	protected boolean isInialize=false;
	protected FileConfig configFile=null;
	protected SMOSVM<double[]> svm=null;
	private ArrayList<double[][]> dicoCenters=null;
	private ArrayList<double[]> dicoSigma=null;
	
	/**
	 * Initialize the algorithm
	 * @param param the file with the paramter of our algorithm.
	 */
	
	public void init(File param){
		
		init(param,param.getParent());
	}
	
	public void init(File param, String path){
		isInialize=true;

		try {
			configFile = FileConfig.deserializeXMLToObject(param);
		} catch (FileNotFoundException e1) {e1.printStackTrace();}
		
		/*
		 * Download SVM
		 */
		try {
			File f = new File( path + configFile.getBinSVM());
			if(f.exists()){				
				FileInputStream fis = new FileInputStream(f);
				ObjectInputStream ois = new ObjectInputStream(fis);
				svm = (SMOSVM<double[]>)ois.readObject();
				ois.close();
			}
		}
		catch (FileNotFoundException e) {e.printStackTrace();} 
		catch (IOException e) {e.printStackTrace();} 
		catch (ClassNotFoundException e) {e.printStackTrace();}
		
		/*
		 * Download Dico
		 */
		dicoCenters = new ArrayList<double[][]>();
		dicoSigma   = new ArrayList<double[]>();
		for(String codebook : configFile.getDicoHsv()){
			//visual codebook
			ObjectInputStream oin;
			try {
				oin = new ObjectInputStream(new FileInputStream(path +codebook));
				dicoCenters.add((double[][]) oin.readObject());
				dicoSigma.add((double[]) oin.readObject());
				oin.close();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	/**
	 * 
	 * @param imageIn1
	 * @param imageIn2
	 */
	public void create_features_visual(BufferedImage image1,BufferedImage image2,ArrayList<Double> pairDesc){
		if(!isInialize){
			System.err.println("we must initialize algorithm");
			System.exit(-1);
		}
		
		ArrayList<ArrayList<double[]> > histo1 = computeHisto(image1);
		ArrayList<ArrayList<double[]> > histo2 = computeHisto(image2);
		/* parameter of distance*/
		String distmean = "TRUE";
		String gamma = "1.0";
		boolean bComptuteMeanDist = false;
		/* create the visual couple feature descriptors */
		for(int i=0 ; i<histo1.size() ; i++){
				IndexedKernel.run(histo1.get(i), histo2.get(i),pairDesc, distmean, gamma, bComptuteMeanDist,true);
				IndexedKernel.run(histo1.get(i), histo2.get(i),pairDesc, distmean, gamma, bComptuteMeanDist,false);
		}
	}
	
	public double run(ArrayList<Double> pairDesc){
		/* convert ArrayList in array of double*/
		double[] pairDescTest = new double[pairDesc.size()];
		int i=0;
		for(Double a : pairDesc)
			pairDescTest[i++]=a.doubleValue();

		/* run SVM*/
		double res = svm.valueOf(pairDescTest);
		if(res > 1)
			res = 1;
		else if(res < -1)
			res = -1;
		System.out.println("Distance between the two web-pages:: "+res);
		return res;		
	}
	
	/**
	 * 
	 * @param imageIn1
	 * @param imageIn2
	 */
	public double run(BufferedImage image1,BufferedImage image2){
		/* create the visual couple feature descriptors */
		ArrayList<Double> pairDesc = new ArrayList<Double> ();
		create_features_visual(image1,image2,pairDesc);
		return run(pairDesc);
	}
	/**
	 * 
	 * @param fichierXml1
	 * @param fichierXml2
	 * @return
	 */
	public double run(String fichierXml1, String fichierXml2){
		/* create the visual couple feature descriptors */
		ArrayList<Double> pairDesc = new ArrayList<Double> ();
		XMLDescriptors.run(fichierXml1, fichierXml2, pairDesc,false);
		return run(pairDesc);
	}
	/**
	 * 
	 * @param fichierXml1
	 * @param fichierXml2
	 * @param image1
	 * @param image2
	 * @return
	 */
	public double run(String fichierXml1, String fichierXml2,BufferedImage image1,BufferedImage image2){
		/* create the visual couple feature descriptors */
		ArrayList<Double> pairDesc = new ArrayList<Double> ();
		create_features_visual(image1,image2,pairDesc);
		XMLDescriptors.run(fichierXml1, fichierXml2, pairDesc,false);
		return run(pairDesc);
	}
	/**
	 * compute the descriptors of a image
	 * @param image the image to descript
	 * @return the descriptors of this image: One histogram for each descriptor.
	 */
	public ArrayList<ArrayList<double[]> > computeHisto(BufferedImage image){
		
		final int heightMax = 1000; 
		/* extract the visible part of web site */
		image = image.getSubimage(0, 0, image.getWidth(),min(image.getHeight(),heightMax));
		
		/* compute the HSV descriptor */		
		ArrayList<Descriptor> colorDesc=new ArrayList<Descriptor>();
		CreateIHSVectors.run(image,  colorDesc,  8,  3,  6,  12,  6, heightMax, true);
		
		ArrayList<ArrayList<double[]> > histo = new ArrayList<ArrayList<double[]> >();
		//parameters of clustering
		int knn = 10;
		String scales = "1x1";
		boolean l1_vectors = false;					
		/* BoW*/
		for(int i=0 ; i<dicoSigma.size() ; i++){
			try {
				histo.add(SpatialPyramids.run(colorDesc, dicoCenters.get(i), dicoSigma.get(i), knn, scales, l1_vectors));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return histo;
	}
	
	/**
	 *
	 * @param args le path du fichier de configuration du marcalizer
	 *//*
	public static void main(String[] args) {		
		ScapeTest sc= new ScapeTest();
		sc.init(new File(args[0]));
		try {
			sc.run(ImageIO.read(new File(args[1])),ImageIO.read(new File(args[2])));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}*/
	
	/*public static void main(String[] args) {		
		ScapeTest sc= new ScapeTest();
		sc.init(new File(args[0]));
		try {
			sc.run(
			ImageIO.read(new File("/home/lechervya/code/MarcAlizer/testIM/4/ref/screenshot-127/screenshot-127.png")),
			ImageIO.read(new File ("/home/lechervya/code/MarcAlizer/testIM/4/ref/screenshot-127/screenshot-127_2.png"))
			);
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}*/
	
	public static void main(String[] args) {		
		MarcAlizer sc= new MarcAlizer();
		//sc.init(new File(args[0]), args[1]);
		try {
			// Just to test marcalizer without pagelyzer. 
			
			URL url1=new URL("http://im1a11.internetmemory.org/shots/2/cas/screenshot-57.png");
			//url1.openConnection().connect();
			URL url2=new URL("http://im1a11.internetmemory.org/shots/2/ref/screenshot-57.png");
			//url2.openConnection().connect();
			
			String file1 = "/home/pehlivanz/SCAPE_ZP/Roc/dataset_doceng_2012/xml/2/VIPSDoc_08-22-11_03-54-55.xml";
			String file2  = "/home/pehlivanz/SCAPE_ZP/Roc/dataset_doceng_2012/xml/2/VIPSDoc_08-22-11_03-54-31.xml";
             
			sc.run(file1,file2);
			//sc.run(ImageIO.read(url1),ImageIO.read(url2));
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	}
/*
	public static void main(String[] args) {
		final String nameListeFile="/home/lechervya/code/MarcAlizer/train/images/labels.txt";
		ScapeTest sc= new ScapeTest();
		sc.init(new File(args[0]));
		try {
			FileReader fr=null;
			BufferedReader r=null;
			BufferedWriter w=new BufferedWriter(new FileWriter("/home/lechervya/code/MarcAlizer/exemple/work/res.txt"));
			try {
				fr = new FileReader(nameListeFile);
				r = new BufferedReader(fr);
			} 
			catch (FileNotFoundException e) {e.printStackTrace();} 
			String parent=(new File(nameListeFile)).getParentFile().getAbsolutePath()+"/";
			try {
				while(r.ready()){
					String []l=r.readLine().split("\t");
					double score=sc.run(ImageIO.read(new File(parent+l[1])), ImageIO.read(new File(parent+l[2])));
					w.write(score+"\t"+l[1]+"\t"+l[2]+"\n");
				}
				w.close();
				r.close();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}		
	}*/

	
	/**
	 * This function compute the minimum between a and b
	 * @param a the first param
	 * @param b the second param
	 * @return min(a,b)
	 */
	private static int min(int a, int b) {
		return a<b?a:b;
	}
}
