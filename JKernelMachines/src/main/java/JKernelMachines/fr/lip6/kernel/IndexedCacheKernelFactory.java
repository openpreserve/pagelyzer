package JKernelMachines.fr.lip6.kernel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import JKernelMachines.fr.lip6.kernel.adaptative.ThreadedSumKernel;
import JKernelMachines.fr.lip6.kernel.extra.bag.SimpleSubListKernel;
import JKernelMachines.fr.lip6.kernel.typed.DoubleGaussChi2;

public class IndexedCacheKernelFactory {

	public static double gamma = 1.0;

	public static IndexedCacheKernel<String, ArrayList<double[]>> createIndexCacheKernel(ArrayList<String> files, ArrayList<ArrayList<double[]>> bow,  boolean bComptuteMeanDist){

		IndexedCacheKernel<String, ArrayList<double[]>> icachekernel = null;
		if (files.size() != bow.size()){
			System.err.println("Bow and files of different size !!!! STOPING ...");
			return null;
		}

		DoubleGaussChi2 gaussKernel = new DoubleGaussChi2(gamma);
		double distmean = 0.0;

		if (bComptuteMeanDist){
			int cpt = 0;
			for (int i=0; i < files.size(); i++ ){
				for (int j = i + 1; j < files.size(); j++ ){
					distmean += gaussKernel.distChi2(bow.get(i).get(0), bow.get(j).get(0));
					cpt++;
				}
			}
			distmean = distmean / ((double)cpt);
			if (distmean < Double.MIN_VALUE)
				System.err.println("distmean = " + distmean + " to small - pathological signatures !!!!!! STOPING ...");

			gamma = 1/distmean;
			gaussKernel.setGamma(gamma);

			System.out.println("distmean = " + distmean + " gamma = " + gamma);
		}

		ThreadedSumKernel<ArrayList<double[]>> tsk = new ThreadedSumKernel<ArrayList<double[]>>();

		SimpleSubListKernel<double[], ArrayList<double[]>> slk = new SimpleSubListKernel<double[], ArrayList<double[]>>(0, 1, gaussKernel);
		tsk.addKernel(slk, 1.0);

		Map<String , ArrayList<double[]> > map = new HashMap<String,ArrayList<double[]>>();
		for (int i = 0; i < files.size(); i++){
			map.put(files.get(i), bow.get(i));
		}
		icachekernel = new IndexedCacheKernel<String,ArrayList<double[]>>(tsk,map);

		return icachekernel;


	}

}