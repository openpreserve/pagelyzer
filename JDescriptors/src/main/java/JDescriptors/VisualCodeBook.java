package JDescriptors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.clustering.ThreadedKMeans;
import JDescriptors.fr.lip6.io.DescriptorReader;


public class VisualCodeBook {

	public static void run(String directory,String output,int nbCluster,int nbPoints,int maxPoints,boolean l1norm) {	

		String stat = "stats.txt";
		
	    //printing options
//	    System.out.println("VisualCodeBook options : ");
//	    System.out.println("input : "+directory);
//	    System.out.println("output : "+output);
//	    System.out.println("nb clusters : "+nbCluster);
//	    System.out.println("nb points : "+nbPoints);
//	    System.out.println("max points : "+maxPoints);
//	    System.out.println("l1norm : "+l1norm);
//	    System.out.println();
	
	    //init random
		Random ran = new Random(System.currentTimeMillis());
		
	    // the list of descriptors
		final ArrayList<File> listOfDescriptorFiles = new ArrayList<File>();
		for(File rep: new File(directory).listFiles()){
			File[] f = rep.listFiles();
			if (f == null)
				return;
			for (int i = 0; i < f.length; i++)
				listOfDescriptorFiles.add(f[i]);
		}

		// training instances		
		float[][] instances;
		ArrayList<float[]> listOfInstances = new ArrayList<float[]>();
		
		int i = 0 ; 
		for(File instance : listOfDescriptorFiles)
		{
			if(i >= maxPoints)
				break;

			ArrayList<double[]> list2 = new ArrayList<double[]>();
			try{
				ArrayList<Descriptor> list = DescriptorReader.readFile(instance.getAbsolutePath());
				
				for(Descriptor d : list)
				{
					if(d.getD() instanceof double[])
					{
						double[] sde = (double[])d.getD();
						list2.add(sde);
					}
					else if(d.getD() instanceof float[])
					{
						float[] sde = (float[])d.getD();
						double[] ode = new double[sde.length];
						for(int n = 0 ; n < sde.length; n++)
						{
							ode[n] = sde[n];
						}
						list2.add(ode);
					}
					else if(d.getD() instanceof int[])
					{
						int[] sde = (int[])d.getD();
						double[] ode = new double[sde.length];
						for(int n = 0 ; n < sde.length; n++)
						{
							ode[n] = sde[n];
						}
						list2.add(ode);
					}
					else if(d.getD() instanceof char[])
					{
						char[] sde = (char[])d.getD();
						double[] ode = new double[sde.length];
						for(int n = 0 ; n < sde.length; n++)
						{
							ode[n] = sde[n];
						}
						list2.add(ode);
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			
			
			if(list2.isEmpty())
				continue;
			
			for (int j = 0; j < nbPoints; j++) // number of points taken in this image
			{

				if(list2.isEmpty())
					break;
				
				int r = ran.nextInt(list2.size());
				double[] desc = list2.remove(r);

				if(desc == null)
					continue;

				//l1 norm better for k-means option
				if(l1norm)
				{
					double sum = 0;
					for(int n = 0 ; n < desc.length; n++)
					{
						if(Double.isNaN(desc[n]))
							throw new ArithmeticException("desc is NaN");
						sum += desc[n];
					}
					if(sum == 0)
						sum = 1;

					float[] newDesc = new float[desc.length];
					for (int k = 0; k < newDesc.length; k++)
						newDesc[k] = (float) (desc[k]/sum);

					listOfInstances.add( newDesc);
					i++;
				}
				else 
				{
					float[] newDesc = new float[desc.length];
					for (int k = 0; k < newDesc.length; k++)
					{
						if(Double.isNaN(desc[k]))
							throw new ArithmeticException("desc is NaN");
						newDesc[k] = (float) (desc[k]);
					}
					listOfInstances.add( newDesc);
					i++;
				}
				
			}
			if (i % 5000 == 0)
			{
				System.out.println(i + " points added.");
				Runtime run = Runtime.getRuntime();
				System.out.println(" free : "+(run.freeMemory()/1000000)+" total : "+(run.totalMemory()/1000000));
			}

		}

		instances = new float[listOfInstances.size()][];
		listOfInstances.toArray(instances);
		
		Runtime.getRuntime().gc();
		System.out.println(" free : "+(Runtime.getRuntime().freeMemory()/1000000)+" total : "+(Runtime.getRuntime().totalMemory()/1000000));
		
		// clustering
		long time = System.currentTimeMillis();
		ThreadedKMeans km = new ThreadedKMeans(instances, nbCluster, 5000, Runtime.getRuntime().availableProcessors());
		

		System.out.println("getting clusters...");
		double[][] centers = km.getCenters();
		double[] sigma = km.getMeanDistance();
		int[] pop = km.getPopulationInCluster();
		
		System.out.println("done ("+(System.currentTimeMillis()-time)+").");
		System.out.println("Non empty clusters : "+centers.length);
		
		try
		{
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(output));
			objOut.writeObject(centers);
			System.out.println("centers written.");
			objOut.writeObject(sigma);
			System.out.println("distance writtent");
			objOut.writeObject(pop);
			System.out.println("population written.");
			objOut.close();
			System.out.println("binary write bone.");
		
			PrintStream out = new PrintStream(new FileOutputStream(stat));
			//printing options
			out.println("################################");
			out.println("# VisualCodeBook options : ");
			out.println("# input : "+directory);
			out.println("# output : "+output);
			out.println("# nb clusters : "+nbCluster);
			out.println("# nb points : "+nbPoints);
			out.println("# max points : "+maxPoints);
			out.println("# l1norm : "+l1norm);
			out.println("################################");
			out.println();
			
			for(i = 0 ; i < centers.length; i++)
			{
				out.println("Cluster "+i+"("+pop[i]+")[+"+sigma[i]+"] : "+Arrays.toString(centers[i]));
			}
			out.close();
			System.out.println("stat written.");
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		System.out.println("codebook done, exiting.");
	}

}
