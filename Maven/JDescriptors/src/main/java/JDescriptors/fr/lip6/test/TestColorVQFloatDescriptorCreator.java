package JDescriptors.fr.lip6.test;

import java.util.ArrayList;

import JDescriptors.fr.lip6.color.ColorVQDescriptorCreator;
import JDescriptors.fr.lip6.color.ColorVQFloatDescriptor;
import JDescriptors.fr.lip6.color.model.IHSColorQuantizer;
import JDescriptors.fr.lip6.detector.HoneycombDetector;
import JDescriptors.fr.lip6.io.XMLWriter;


public class TestColorVQFloatDescriptorCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		ColorVQDescriptorCreator c = ColorVQDescriptorCreator.getInstance();
		
		//honeycomb patches
		HoneycombDetector detector = new HoneycombDetector(12, 12);
		c.setDetector(detector);
		
		int x = 10, y = 6, z = 4;
//		RGBColorQuantizer q =new RGBColorQuantizer(x, y, z); 
		IHSColorQuantizer q = new IHSColorQuantizer(x, y, z);
		c.setQuantizer(q);
		
		c.setNormalize(true);
		
		ArrayList<ColorVQFloatDescriptor> d = c.createDescriptors(args[0]);
		
//		for(ColorVQFloatDescriptor f : d)
			System.out.println(XMLWriter.writeXMLString(d));
		
		System.out.println("size : "+d.size());

	
	}

}
