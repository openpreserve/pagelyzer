package JDescriptors.fr.lip6.detector;

import java.awt.image.RenderedImage;
import java.util.ArrayList;

import JDescriptors.fr.lip6.Descriptor;


public class HoneycombDetector implements Detector {

	
	private int spacing;
	private int radius;
	
	
	
	
	/**
	 * @param spacing
	 * @param scaling
	 */
	public HoneycombDetector(int spacing, int scaling) {
		this.spacing = spacing;
		this.radius = scaling;
	}

	public <T extends Descriptor> ArrayList<T> getDescriptors(Class<T> c, RenderedImage image) {
		
		int width = image.getWidth();
		int height = image.getHeight();
		
		//System.out.println("width = " + width + " ; height = " + height);
		int widthd = width - radius;
		int wtimes = widthd / spacing +1;
		//System.out.println("spacing = " + spacing + " ; radius = " + radius + " ; wtimes = " + wtimes + " ; widthd = " + widthd);
		if (widthd%spacing < radius)
			wtimes -= radius/spacing +1;
		int widthr = widthd  - ((wtimes-1)*spacing+radius);
		
		//System.out.println("width = " + width + " ; spacing = " + spacing + " ; wtimes = " + wtimes + " ; widthr = " + widthr);
		
		int heightd = height - radius;
		int htimes = heightd / spacing +1;
		//System.out.println("height = " + height + " ; heightd = " + heightd + " ; htimes = " + htimes);
		if (heightd%spacing < radius)
			htimes -= radius/spacing +1;
		int heightr = heightd  - ((htimes-1)*spacing+radius);
		
		//System.out.println("heightd = " + heightd + " ; htimes = " + htimes + " ; heightr = " + heightr);
		int hsize = htimes;
		//System.out.println("honeycomb hsize = " + hsize);
		ArrayList<T> list = new ArrayList<T>();
		
		try
		{
			//great loop
			for(int y = 0 ; y < hsize; y++)
			{
				int yOffset = heightr/2+radius;
				// case even
				int xOffset = widthr/2+radius;
				// case odd : shift right by spacing/2
				int wsize = wtimes;
				if(y%2 == 1)
				{
					xOffset += spacing/2;
					wsize = wtimes -1;
				}
				
				for(int x = 0 ; x < wsize; x++)
				{
					T d = c.newInstance();
					
					d.setXmin(xOffset+x*spacing-radius);
					d.setXmax(xOffset+x*spacing+radius);
					
					d.setYmin(yOffset+y*spacing-radius);
					d.setYmax(yOffset+y*spacing+radius);
					
					d.setShape(Descriptor.SQUARE);
					
					//System.out.println("Xmin = " + (xOffset+x*spacing-radius) + " ; Xmax = " + ((xOffset+x*spacing+radius)) + " ; Ymin = " + (yOffset+y*spacing-radius) + " ; Ymax = " + (yOffset+y*spacing+radius));
					list.add(d);
				}
			}
		
		}
		catch(InstantiationException ie)
		{
			ie.printStackTrace();
		}
		catch(IllegalAccessException iae)
		{
			iae.printStackTrace();
		}
		return list;
	}

}
