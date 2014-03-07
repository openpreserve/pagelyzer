package JDescriptors.fr.lip6.bin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Arrays;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.io.DescriptorReader;
import JDescriptors.fr.lip6.io.XMLWriter;
import JDescriptors.fr.lip6.texture.GaborDescriptor;


public class GaborNormalizer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		if(args.length<1)
		{
			System.out.println("usage : GaborNormalizer <descriptorDirectory> [statFiles directory]");
			return;
		}
		
		String inDir = args[0];
		String statDir;
		if(args.length >= 2)
		{
			statDir = args[1];
		}
		else
		{
			System.out.println("Assuming statDir is .");
			statDir = ".";
		}
		
		//collecting stats
		System.out.println("collecting stats");
		
		int nbDescs = 0;
		double meanDesc[] = null;
		double meanSquareDesc[] = null;
		
		String[] statFiles = (new File(statDir)).list(new FilenameFilter(){

			
			public boolean accept(File dir, String name) {
				if(name.startsWith("stat") && name.endsWith(".obj"))
					return true;
				return false;
			}});
		
		for(String s : statFiles)
		{
			System.out.println("trying to add "+s);
			try
			{
				ObjectInputStream objIn = new ObjectInputStream(new FileInputStream(s));
				int localNbDescs = objIn.readInt();
				if(localNbDescs <= 0)
				{
					System.out.println("no descriptor in stat file, skipping.");
					continue;
				}
				double localMean[] = (double[]) objIn.readObject();
				double localSquareMean[] = (double[]) objIn.readObject();

				System.out.println("stats : ");
				System.out.println("local nb descriptors : "+localNbDescs);
				System.out.println("local mean : "+Arrays.toString(localMean));
				System.out.println("local squareMean : "+Arrays.toString(localSquareMean));
				if(meanDesc == null && meanSquareDesc == null)
				{
					meanDesc = localMean;
					meanSquareDesc = localSquareMean;
					for(int i = 0; i < meanDesc.length; i++)
						meanDesc[i] *= localNbDescs;

					for(int i = 0; i < meanSquareDesc.length; i++)
						meanSquareDesc[i] *= localNbDescs;
					nbDescs = localNbDescs;
				}
				else
				{
					if(meanDesc.length != localMean.length || meanSquareDesc.length != localSquareMean.length)
					{
						System.out.println("size not equal, skipping.");
						continue;
					}
					
					for(int i = 0; i < meanDesc.length; i++)
						meanDesc[i] += localMean[i]*localNbDescs;

					for(int i = 0; i < meanSquareDesc.length; i++)
						meanSquareDesc[i] += localSquareMean[i]*localNbDescs;
					
					nbDescs += localNbDescs;
				}

				System.out.println("total nb descriptors : "+nbDescs);
				System.out.println("total mean : "+Arrays.toString(meanDesc));
				System.out.println("total squareMean : "+Arrays.toString(meanSquareDesc));
				System.out.println("added.");
			}
			catch(IOException ioe)
			{
				ioe.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
		
		//formatting
		for(int i = 0 ; i < meanDesc.length; i++)
		{
			meanDesc[i] /= nbDescs;
		}
		for(int i = 0 ; i < meanSquareDesc.length; i++)
		{
			meanSquareDesc[i] = Math.sqrt(meanSquareDesc[i] / nbDescs - meanDesc[i]*meanDesc[i]);
		}
		
		System.out.println("stats : ");
		System.out.println("total nb descriptors : "+nbDescs);
		System.out.println("mean : "+Arrays.toString(meanDesc));
		System.out.println("squareMean : "+Arrays.toString(meanSquareDesc));
		
		if(true)
			System.exit(0);
		//doing descs
		String[] descFiles = (new File(inDir)).list(new FilenameFilter(){

			
			public boolean accept(File dir, String name) {
				if(name.endsWith(".gab.xgz"))
					return true;
				return false;
			}});
		for(String s : descFiles)
		{
			try {
				System.out.println("normalizing "+s);
				ArrayList<Descriptor> list = DescriptorReader.readFile(inDir+"/"+s);
				
				for(Descriptor d : list)
				{
					GaborDescriptor gd = (GaborDescriptor)d;
					double D[] = gd.getD();
					
					for(int i = 0 ; i < D.length; i++)
					{
						D[i] -= meanDesc[i];
						D[i] /= meanSquareDesc[i];
					}
					
//					System.out.println("D : "+Arrays.toString(D));
					
//					//split in 8 list
//					ArrayList<ArrayList<Double>> listOfScales = new ArrayList<ArrayList<Double>>();
//					for(int i= 0 ; i < 8; i++)
//					{
//						ArrayList<Double> l = new ArrayList<Double>();
//						for(int j = i*16; j < (i+1)*16; j++)
//							l.add(D[j]);
//						listOfScales.add(l);
////						System.out.println(l);
//					}
//					
//					
//					//search max orient
//					double sum[] = new double[8];
//					for(int i = 0 ; i < 8; i++)
//					for(int j = 0 ; j < 8; j++)
//					{
//						sum[i] += D[(i+j*8)*2];
//					}
////					System.out.println("sum : "+Arrays.toString(sum));
//					double max = Double.NEGATIVE_INFINITY;
//					int index = 0;
//					for(int i = 0; i < sum.length; i++)
//					{
//						if(sum[i] > max)
//						{
//							max = sum[i];
//							index = i;
//						}
//					}
////					System.out.println("max : "+max+" index : "+index);
//					for(ArrayList<Double> l : listOfScales)
//					{
//						Collections.rotate(l, -index*2);
////						System.out.println(l);
//					}
////					sum = new double[8];
////					for(int i = 0 ; i < 8; i++)
////					for(int j = 0 ; j < 8; j++)
////					{
////						sum[i] += D[(i+j*8)*2];
////					}
////					System.out.println("sum : "+Arrays.toString(sum));
//					
//					for(int i= 0 ; i < 8; i++)
//					{
//						ArrayList<Double> l = listOfScales.get(i);
//						for(int j = 0; j < 16; j++)
//							D[i*16+j] = l.get(j);
//						
//						
//					}
//					System.out.println("D : "+Arrays.toString(D));
//					System.out.println("next");
				}
				
				File oldDesc = new File(inDir+"/"+s);
				oldDesc.delete();
				
				String newName = inDir+"/"+s.replaceAll(".xgz", "");
				XMLWriter.writeXMLFile(newName, list, true);
				
				System.out.println(s+" done.");
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// TODO Auto-generated method stub

	}

}
