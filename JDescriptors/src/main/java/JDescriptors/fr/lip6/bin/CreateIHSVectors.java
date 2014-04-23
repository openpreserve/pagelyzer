package JDescriptors.fr.lip6.bin;

import java.io.File;
import java.util.ArrayList;

import JDescriptors.fr.lip6.color.ColorVQDescriptorCreator;
import JDescriptors.fr.lip6.color.ColorVQFloatDescriptor;
import JDescriptors.fr.lip6.color.model.IHSColorQuantizer;
import JDescriptors.fr.lip6.detector.HoneycombDetector;
import JDescriptors.fr.lip6.io.XMLWriter;


public class CreateIHSVectors {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		if(args.length < 7)
		{
			System.out.println("usage : CreateIHSvectors <src> <dst> <I> <H> <S> <spacing> <radius>");
			return ;
		}
		
		String srcDir = args[0];
		String dstDir = args[1];
		int I = Integer.parseInt(args[2]);
		int H = Integer.parseInt(args[3]);
		int S = Integer.parseInt(args[4]);
		int s = Integer.parseInt(args[5]);
		int r = Integer.parseInt(args[6]);
		int maxHeight = (args.length >= 8) ? Integer.parseInt(args[7]) : -1;
		boolean onlyTop = (Integer.parseInt(args[8]) == 1);
		
		ColorVQDescriptorCreator c = ColorVQDescriptorCreator.getInstance();
		//System.out.println(" s = " + s + " ; r = " + s + " ; cut = " + cut);
		
		//Descriptor creator
		HoneycombDetector detector = new HoneycombDetector(s, r);
		c.setDetector(detector); 
		IHSColorQuantizer q = new IHSColorQuantizer(I, H, S);
		c.setQuantizer(q);
		c.setNormalize(false);
		
		File src = new File(srcDir);
		if(!src.exists() || !src.isDirectory())
		{
			System.out.println(srcDir+" : No such directory !");
			return;
		}
		
		String[] files2 = src.list();
		if(files2 == null)
		{
			System.out.println("No files in "+srcDir);
			return;
		}
		//(new File(dstDir)).mkdir();
		for (String f2 : files2) {
		System.out.println("f2 = " + dstDir+f2);

		(new File(dstDir+f2)).mkdir();
		for(String f : new File(srcDir+"\\"+f2).list())
		{
			if (!(f.endsWith(".png"))){
				continue;
			}
			try
			{
				//if (!(new File(dstDir+f2+"\\"+f.substring(0, f.indexOf('.'))+".xgz").exists()) && !f.equals("280.png") && !f.equals("281.png") ){
					//System.out.println(f);
				System.out.println("lecture de " + srcDir+"/"+f2+"/"+f);
				if (!(new File(dstDir+f2+"\\"+f.substring(0, f.indexOf('.'))+".xgz").exists())) {
				ArrayList<ColorVQFloatDescriptor> list = c.createDescriptors(srcDir+"/"+f2+"/"+f, maxHeight, onlyTop);
				XMLWriter.writeXMLFile(dstDir+"/"+f2+"/"+f.substring(0, f.indexOf('.')), list, true);
				System.out.println(f+" descriptor written : taille = " + list.size());
				}
			}
			catch(Exception ioe)
			{
				System.err.println("no descriptors for "+f);
				ioe.printStackTrace();
			}
		}
		}

	}

}
