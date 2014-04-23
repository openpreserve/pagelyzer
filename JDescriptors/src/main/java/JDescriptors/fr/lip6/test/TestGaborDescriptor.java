package JDescriptors.fr.lip6.test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import JDescriptors.fr.lip6.detector.Detector;
import JDescriptors.fr.lip6.detector.HoneycombDetector;
import JDescriptors.fr.lip6.io.XMLWriter;
import JDescriptors.fr.lip6.texture.GaborDescriptor;
import JDescriptors.fr.lip6.texture.GaborDescriptorCreator;


public class TestGaborDescriptor {

	
	public static void main(String args[])
	{
		String im = args[0];
		String descOut = args[1];
		
		
		GaborDescriptorCreator creator = new GaborDescriptorCreator();
		
		Detector detector = new HoneycombDetector(6, 9);
		creator.setDetector(detector);
		
		long tim = System.currentTimeMillis();
		ArrayList<GaborDescriptor> listOfDesc = creator.createDescriptors(im);
		System.out.println("time : "+(System.currentTimeMillis()-tim));
		
//		System.out.println(XMLWriter.writeXMLString(listOfDesc));
		try {
			XMLWriter.writeXMLFile(descOut, listOfDesc, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		double[] mean = creator.getMeanGabor();
		for(int i = 0; i < mean.length; i++)
		{
			mean[i] /= creator.getNbProcessedDescriptors(); 
		}
		double std[] = creator.getStdGabor();
		for(int i = 0 ; i < std.length; i++)
		{
			std[i] = Math.sqrt(std[i]/creator.getNbProcessedDescriptors() - mean[i]*mean[i]);
		}
	
		System.out.println("nbDescs : "+creator.getNbProcessedDescriptors());
		System.out.println(" mean : "+Arrays.toString(mean));
		System.out.println(" std : "+Arrays.toString(std));
	}
}
