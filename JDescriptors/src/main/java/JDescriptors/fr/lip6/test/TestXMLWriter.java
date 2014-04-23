package JDescriptors.fr.lip6.test;

import java.util.ArrayList;

import JDescriptors.fr.lip6.color.ColorVQDescriptorCreator;
import JDescriptors.fr.lip6.color.ColorVQFloatDescriptor;
import JDescriptors.fr.lip6.color.model.IHSColorQuantizer;
import JDescriptors.fr.lip6.detector.HoneycombDetector;
import JDescriptors.fr.lip6.io.XMLWriter;


public class TestXMLWriter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 2)
		{
			System.out.println("usage : TestXMLWriter <image> <output>");
			return;
		}
		
		ColorVQDescriptorCreator c = ColorVQDescriptorCreator.getInstance();
		
		//honeycomb patches
		HoneycombDetector detector = new HoneycombDetector(12, 12);
		c.setDetector(detector);
		
		int x = 8, y = 6, z = 3;
//		RGBColorQuantizer q = new RGBColorQuantizer(x, y, z); 
		IHSColorQuantizer q = new IHSColorQuantizer(x, y, z);
		c.setQuantizer(q);
		
		c.setNormalize(false);
		
		ArrayList<ColorVQFloatDescriptor> d = c.createDescriptors(args[0]);

		XMLWriter.writeXMLFile(args[1], d, true);
		
	}

}
