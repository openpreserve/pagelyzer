package JDescriptors.fr.lip6.bin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.bof.VladFactory;
import JDescriptors.fr.lip6.io.DescriptorReader;



public class VladRepresentation {

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub

				if(args.length < 4)
				{
					System.out.println("VladRepresentation directory centers outDir .ext");
					return;
				}
				
				String dir = args[0];
				
				System.out.println("Reading visual dictionary");
				String centerFile = args[1];
				ObjectInputStream oin = new ObjectInputStream(new FileInputStream(centerFile));
				double[][] centers = (double[][]) oin.readObject();

				oin.close();
				
				System.out.println("Reading outdir and ext");
				String outDir = args[2];
				String ext = args[3];
					
				// check if it is a directory			
				File directory = new File(dir);
				if(!directory.isDirectory())
				{
					System.out.println("input file "+directory+" is not a directory.");
					System.exit(0);
				}
				File[] listFiles = directory.listFiles();
				ArrayList<File> list = new ArrayList<File>();
				for(File f : listFiles)
					list.add(f);
				Collections.shuffle(list);
				for(File f : list)
				{
					String destName = outDir+(f.getName().substring(0, f.getName().indexOf(".")))+ext;
					
					File outFile = new File(destName);
					
				
					if(outFile.exists())
					{
						System.out.println("not doing  : "+outFile+" ("+f+")");
						continue;
					}
					else
					{
						System.out.println("doing : "+outFile+" ("+f+")");
					}
					
					outFile.createNewFile();
								
					// read sift file for one image
					ArrayList<Descriptor> listOfWords = DescriptorReader.readFile(f.getAbsolutePath());
					
					int numClusters = centers.length;
					int descriptorSize = listOfWords.get(0).getDimension();
							                     
					double[][] vlad = new double[numClusters][descriptorSize];
					int nearestCenter;
					double[] diff = new double[descriptorSize];
					
					
					for(Descriptor vw : listOfWords) //pour tous les sifts
					{
						if(vw.getD() instanceof float[])
						{	
							float[] desc = (float[]) vw.getD();
							// find nearest neighbour center
							nearestCenter = VladFactory.nearestCluster(desc, centers);
							// for all the dimensions of the the sift
							for(int j = 0; j < vw.getDimension(); j++)
							{
								diff[j] = desc[j]-centers[nearestCenter][j]; // compute the difference between components 
							}	
							// assign the sum of difference to vlad   
							for(int j = 0; j < descriptorSize; j++) vlad[nearestCenter][j] += diff[j];
						}
						else if (vw.getD() instanceof double[])
						{
							double[] desc = (double[])vw.getD();
							// find nearest neighbour center
							nearestCenter = VladFactory.nearestCluster(desc, centers);
							// for all the dimensions of the the sift
							for(int j = 0; j < vw.getDimension(); j++)
							{
								diff[j] = desc[j]-centers[nearestCenter][j]; // compute the difference between components 
							}	
							// assign the sum of difference to vlad   
							for(int j = 0; j < descriptorSize; j++) 
								vlad[nearestCenter][j] += diff[j];
						}
						
					}
					
					// normalize the vlad vectors by L2 norm
					double[][] vladNorm = new double[numClusters][];
					double[] vladComponentforCluster = new double [descriptorSize];
					vladComponentforCluster = null;
					double sum ;
					
					for(int i = 0 ; i < numClusters; i++)
					{
						vladComponentforCluster = vlad[i];
						sum = 0.0;
						for(int j = 0 ; j  < vladComponentforCluster.length; j++)
							sum += vladComponentforCluster[j]*vladComponentforCluster[j];
						sum = Math.sqrt(sum);
						for(int j = 0 ; j  < vladComponentforCluster.length; j++)
							vladComponentforCluster[j] /= sum;
						vladNorm[i] = vladComponentforCluster;	
					}

					//System.out.println("vlad norm desc : "+Arrays.deepToString(vladNorm));
					//writing
					ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(outFile));
					oout.writeObject(vladNorm);
					oout.flush();
					oout.close();
		
				
				}		

	}

}
