package JKernelMachines.fr.lip6.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

@SuppressWarnings("static-access")
public class IndexedKernel {
	/**
	 * 
	 * @param bows1 descriptor of image 1
	 * @param bows2 descriptor of image 2
	 * @param pairDesc the descriptor of pair of images
	 * @param distmean
	 * @param gamma
	 * @param bComptuteMeanDist
	 * @param norm_L2
	 */
	public static void run(ArrayList<double[]> bows1, ArrayList<double[]> bows2 , ArrayList<Double> pairDesc,String distmean,String gamma,boolean bComptuteMeanDist,boolean norm_L2) {
		ArrayList<ArrayList<double[]>> bows = new ArrayList<ArrayList<double[]>> ();
		ArrayList<ArrayList<double[]>> bowsOut = new ArrayList<ArrayList<double[]>> ();
		bows.add(bows1);
		bows.add(bows2);
		// parsing input directory containing bows
		for(ArrayList<double[]> value:bows){
			for (double[] d : value) {
				double sum_L2 = 0;
				double sum_L1 = 0;
				for (int j = 0 ; j < d.length ; ++j) {
						sum_L2 += d[j] * d[j];
						sum_L1 += Math.abs(d[j]);
				}
				for (int j = 0 ; j < d.length ; ++j) {
					if (norm_L2){
						d[j] /= Math.sqrt(sum_L2);
					}
					else {
						d[j] /= sum_L1;
					}
				}
			}
			bowsOut.add(value);
		}			
		double[] bow1 = bowsOut.get(0).get(0);
		double[] bow2 = bowsOut.get(1).get(0);
		
		double distance = 0;

		if(norm_L2){
			for (int l = 0 ; l < bow1.length ; ++l) 
				distance += (bow1[l] - bow2[l])*(bow1[l] - bow2[l]); // L2
		}
		else{
			for (int l = 0 ; l < bow1.length ; ++l) 
				if ((bow1[l] + bow2[l]) != 0)
					distance += ((bow1[l] - bow2[l])*(bow1[l] - bow2[l]))/(bow1[l] + bow2[l]); // chi 2
		}
		
		System.out.println(distance + " ");
		
		pairDesc.add(distance);
	}
}