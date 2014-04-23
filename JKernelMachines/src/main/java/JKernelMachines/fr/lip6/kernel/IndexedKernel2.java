package JKernelMachines.fr.lip6.kernel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

@SuppressWarnings("static-access")
public class IndexedKernel2 {

	public static void run(String input,String output,String distmean,String gamma,boolean bComptuteMeanDist,boolean norm_L2,String m) {

		//ArrayList<String> files = new ArrayList<String> ();
		ArrayList<ArrayList<double[]>> bows = new ArrayList<ArrayList<double[]>> ();
		//ArrayList<String> bownames = new ArrayList<String> ();

		// printing options
		System.out.println("IndexKernel options: ");
		System.out.println("input: " + input);
		System.out.println("output: " + output);
		System.out.println("meandist: " + distmean);
		System.out.println("gamma: " + gamma);
		System.out.println();

		// parsing input directory containing bows
		File[] f4 = (new File(input)).listFiles();
		if (f4 == null)
			return;
		double[] distancesL1 = new double[f4.length];
		double[] distancesL2 = new double[f4.length];
		for (int i = 0; i < f4.length; i++){
			String filename4 = f4[i].getName();
			//System.out.println(filename);
			File[] f2 = f4[i].listFiles();
			if (f2 == null)
				return;
			bows.clear();
			long source_length = f2[0].listFiles().length;
			for (int k = 0 ; k < f2.length ; ++k){
				String filename2 = f2[k].getName();
				System.out.println(filename2);
				File[] f3 = f2[k].listFiles();
				//System.out.println(f3.length + " f3.length");
				for (int k2 = 0 ; k2 < f3.length ; ++k2){
					String filename3 = f3[k2].getName();
					try{
						//					System.out.println("input+filename2 " + input+"/" +filename4 + "/" +filename2 + "/" + filename3);
						ObjectInputStream in = new ObjectInputStream(new FileInputStream(input+"/"+filename4 + "/" +filename2 + "/" + filename3));
						ArrayList<double[]> value = (ArrayList<double[]>)in.readObject();
						for (double[] d : value) {
							double sum_L2 = 0;
							double sum_L1 = 0;
							for (int j = 0 ; j < d.length ; ++j) {
								System.out.print("d[j] = "  + d[j]);
								sum_L2 += d[j] * d[j];
								sum_L1 += Math.abs(d[j]);
							}
							//System.out.println("sum_L2 = " + sum_L2);
							//System.out.println("sum_L1 = " + sum_L1);
							for (int j = 0 ; j < d.length ; ++j) {
								if (norm_L2){

									if (sum_L2 > 0){
										System.out.println("norm = " + Math.sqrt(sum_L2));
										d[j] /= Math.sqrt(sum_L2);
									}
								}
								else {
									if (sum_L1 > 0){
										d[j] /= sum_L1;
									}
								}
							}
							//System.out.println("");
						}
						//System.out.println("");
						//System.out.println("value = " + value);
						bows.add(value);
						//bownames.add(filename.split("_")[1]);
						in.close();
					}
					catch(ClassNotFoundException e){
						e.printStackTrace();
					}
					catch(IOException e){
						e.printStackTrace();
					}			
				}
			}
			double distance_L2_kept = 0;
			double distance_L1_kept = 0;
			System.out.println("bows.size() = " + bows.size());
			System.out.println("source_length = " + source_length);
			for (int l1 = 0 ; l1 < bows.size() ; ++l1){
				double[] bow1 = bows.get(l1).get(0);

				double distance_L2_min = Double.MAX_VALUE;
				double distance_L1_min = Double.MAX_VALUE;
				for (int l2 = (l1 < source_length ? (int) source_length : 0) ; l2 < (l1 < source_length ? bows.size() : source_length) ; ++l2){
					double[] bow2 = bows.get(l2).get(0);
					double distance_L2 = 0;
					double distance_L1 = 0;
					// System.out.println(bow1.length);
					// System.out.println(bow2.length);
					for (int l = 0 ; l < bow1.length ; ++l) {
						distance_L2 += (bow1[l] - bow2[l])*(bow1[l] - bow2[l]); // L2
						//System.out.println("dista, = " + distance_L1);
						if ((bow1[l] + bow2[l]) != 0)
							distance_L1 += ((bow1[l] - bow2[l])*(bow1[l] - bow2[l]))/(bow1[l] + bow2[l]); // chi 2
					}
					if (norm_L2){
						System.out.println("distance_L2 " + distance_L2 + " ; l1 = " + l1 + " ; l2 = " + l2);
					}
					else {
						System.out.println("distance_L1 " + distance_L1 + " ; l1 = " + l1 + " ; l2 = " + l2);
					}
					distance_L2_min = Math.min(distance_L2_min, distance_L2);
					distance_L1_min = Math.min(distance_L1_min, distance_L1);
				}
				distance_L2_kept += distance_L2_min;
				distance_L1_kept += distance_L1_min;
			}
			distancesL2[Integer.valueOf(f4[i].getName())-1] = bows.isEmpty() ? Double.NaN : (distance_L2_kept /bows.size());
			distancesL1[Integer.valueOf(f4[i].getName())-1] = bows.isEmpty() ? Double.NaN : (distance_L1_kept /bows.size());
			if(Double.isInfinite(distancesL1[Integer.valueOf(f4[i].getName())-1]))System.err.println("distance_L1_kept: "+distance_L1_kept+"\t"+bows.size());
			if (bows.isEmpty())
				System.out.println(f4[i].getName() + " is empty");
			//System.out.println("distance = " + distance);
		}
		System.out.print((input.contains("color") ? "color " : "sift ") + "ad hoc bloc " + (input.contains("100") ? "100 " : "200 ") + (norm_L2 ? "L2 : " : "L1 : "));

		String texte = "";
		for (int i = 0 ; i < distancesL2.length - 1 ; ++i){
			if(norm_L2){
				System.out.print(distancesL2[i] + " ");
				texte += distancesL2[i] + " ";
			}else{
				System.out.print(distancesL1[i] + " ");
				texte += distancesL1[i] + " ";
			}
		}
		if(norm_L2){
			System.out.print(distancesL2[distancesL2.length-1]);
			texte += distancesL2[distancesL2.length-1] + "\n";
		}else{
			System.out.print(distancesL1[distancesL2.length-1]);
			texte += distancesL1[distancesL2.length-1] + "\n";
		}

		FileWriter writer = null;
		try{
			writer = new FileWriter(m, true);
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
		System.out.println();
		System.out.println();

		return;
		/*
		for (int i = 0; i < f.length; i++){
			String filename = f[i].getName();
			System.out.println(filename);
			files.add(filename);

			try{
				ObjectInputStream in = new ObjectInputStream(new FileInputStream(input+filename));
				ArrayList<double[]> value = (ArrayList<double[]>)in.readObject();
				for (double[] d : value) {
					double sum = 0;
					for (int j = 0 ; j < d.length ; ++j) {
							sum += d[j] * d[j];
					}
					for (int j = 0 ; j < d.length ; ++j) {
						d[j] /= Math.sqrt(sum);
					}
					//System.out.println("");
				}
				//System.out.println("");
				bows.add(value);
				bownames.add(filename.split("_")[1]);
				in.close();
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}
			catch(IOException e){
				e.printStackTrace();
			}
		}
		int count1 = -1;
		for (ArrayList<double[]> value : bows) {
			++count1;
			if (count1 == bows.size()/2) {
				break;
			}
			int count2 = -1;
			double d2 = Integer.MAX_VALUE;
			double d = 0.0;
			String s = null;
			for (ArrayList<double[]> value2 : bows) {
				++count2;
				if (count2 < bows.size()/2) {
					continue;
				}
				if ((bownames.get(count1).equals(bownames.get(count2)))) {
					for (int i = 0 ; i < value.get(0).length ; i++) {
						d += Math.pow(value.get(0)[i] - value2.get(0)[i], 2.0);
					}
				}
				else {
					double d3 = 0.0;
					for (int i = 0 ; i < value.get(0).length ; i++) {
						d3 += Math.pow(value.get(0)[i] - value2.get(0)[i], 2.0);
					}
					if (d3 < d2) {
						d2 = d3;
						s = bownames.get(count2);
					}
				}
			}
			System.out.println("block : " + bownames.get(count1) + " avec lui-meme: " + d);
			System.out.println("block : " + bownames.get(count1) + " avec " + s + ": " + d2);
			System.out.println((d < d2) ? ("ok : " + bownames.get(count1) + " bien plus proche de lui-meme") :  ("pas ok : " + bownames.get(count1) + " pas plus proche de lui-meme"));
			System.out.println("");
		}

		System.out.println(files.size() + " - " + bows.size() + " bow files loaded.");

		IndexedCacheKernel<String,ArrayList<double[]> > icachekernel = IndexedCacheKernelFactory.createIndexCacheKernel(files, bows, bComptuteMeanDist);
		try
		{
			String ouputfile = output + "IndexKernel_" + IndexedCacheKernelFactory.gamma + ".obj";
			File outFile = new File(ouputfile);
			ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream(outFile));
			objOut.writeObject(icachekernel);
			System.out.println("output kernel: " + ouputfile + " written");
		}
		catch(Exception e){
			e.printStackTrace();
		}*/
	}

}