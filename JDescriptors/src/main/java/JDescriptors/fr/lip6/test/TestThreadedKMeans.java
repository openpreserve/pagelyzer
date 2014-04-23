package JDescriptors.fr.lip6.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.clustering.ThreadedKMeans;
import JDescriptors.fr.lip6.io.DescriptorReader;


public class TestThreadedKMeans {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		final Random ran = new Random(System.currentTimeMillis());

		String directory = args[0];
		

		// the list of descriptors
		final ArrayList<File> listOfDescriptorFiles = new ArrayList<File>();
		File[] f = (new File(directory)).listFiles();
		if (f == null)
			return;
		for (int i = 0; i < f.length; i++)
			listOfDescriptorFiles.add(f[i]);

		// training instances

		int numberOfTrainingInstances = 10000;
		int nbDescParImage = 1000;
		
		float[][] instances = new float[numberOfTrainingInstances][];
		int i = 0 ; 
		for(File instance : listOfDescriptorFiles)
		{
			if(i >= numberOfTrainingInstances)
				break;

			ArrayList<double[]> list2 = new ArrayList<double[]>();
			try{
				ArrayList<Descriptor> list = DescriptorReader.readFile(instance.getAbsolutePath());
				
				for(Descriptor d : list)
				{
					if(d.getD() instanceof double[])
						list2.add((double[])d.getD());
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			double[][] instanceDescriptors = new double[1][];
			instanceDescriptors = list2.toArray(instanceDescriptors);
			
			if(instanceDescriptors == null)
				continue;
			
			for (int j = 0; j < nbDescParImage; j++) // number of points taken in this image
			{
				int r = ran.nextInt(instanceDescriptors.length);
				double[] desc = instanceDescriptors[r];

				if(desc == null)
					continue;
				
				float[] newDesc = new float[desc.length];
				for (int k = 0; k < newDesc.length; k++)
					newDesc[k] = (float)desc[k];

				instances[i] = newDesc;
				i++;
			}
			if (i % 1000 == 0)
			{
				System.out.println(i + " points added.");
				Runtime run = Runtime.getRuntime();
				System.out.println(" free : "+(run.freeMemory()/1000000)+" total : "+(run.totalMemory()/1000000));
			}

		}

		Runtime.getRuntime().gc();
		System.out.println(" free : "+(Runtime.getRuntime().freeMemory()/1000000)+" total : "+(Runtime.getRuntime().totalMemory()/1000000));
		
		// clustering
		ThreadedKMeans km = new ThreadedKMeans(instances, 50, 500,2);
		

		System.out.println("getting clusters...");
		double[][] centers = km.getCenters();
		double[] sigma = km.getMeanDistance();
		int[] pop = km.getPopulationInCluster();
		
		for(i = 0 ; i < centers.length; i++)
		{
			System.out.println("Cluster "+i+"("+pop[i]+")[+"+sigma[i]+"] : "+Arrays.toString(centers[i]));
		}
	}

}
