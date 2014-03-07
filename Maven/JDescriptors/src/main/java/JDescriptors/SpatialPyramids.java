package JDescriptors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Coding;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Norm;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Pooling;
import JDescriptors.fr.lip6.io.DescriptorReader;




public class SpatialPyramids {
	/**
	 * 
	 * @param dscIn in descriptors
	 * @param centers the centroid of dictionary
	 * @param sigma the sigma of dictionary
	 * @param knn the number of k in knn classifier
	 * @param scales
	 * @param l1_vectors
	 * @return the histogram 
	 * @throws Exception
	 */
	public static ArrayList<double[]> run(ArrayList<Descriptor> dscIn,double[][] centers,double[] sigma,int knn,String scales,boolean l1_vectors) throws Exception
	{	
		//parsing scales
		ArrayList<int[]> listOfScales = new ArrayList<int[]>();
		for(String s : scales.trim().split("/+"))
		{
			String[] lc = s.split("x");
			if(lc.length != 2)
				continue;

			int[] linecols = new int[2];
			linecols[0] = Integer.parseInt(lc[0]);
			linecols[1] = Integer.parseInt(lc[1]);
			listOfScales.add(linecols);
		}
		ArrayList<double[]> bag = new ArrayList<double[]>();
		//each scales
		for(int[] s : listOfScales)
		{
			SpatialPyramidFactory.lines = s[0];
			SpatialPyramidFactory.cols = s[1];
			SpatialPyramidFactory.knn = knn;
			SpatialPyramidFactory.coding = Coding.HARD;
			SpatialPyramidFactory.pooling = Pooling.SUM;
			SpatialPyramidFactory.norm = Norm.NONE;
			SpatialPyramidFactory.l1_norm = l1_vectors;
			ArrayList<double[]> l = SpatialPyramidFactory.createBagOfWindows(dscIn, centers, sigma);
			bag.addAll(l);
		}
		return bag;
	}
}
