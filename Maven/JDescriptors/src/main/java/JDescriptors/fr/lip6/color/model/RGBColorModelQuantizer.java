package JDescriptors.fr.lip6.color.model;

import java.awt.image.IndexColorModel;

public class RGBColorModelQuantizer {
	
	
	public static IndexColorModel getColorModel(int rBins, int gBins, int bBins)
	{
		
		byte[] rc = new byte[rBins*gBins*bBins];
		byte[] gc = new byte[rBins*gBins*bBins];
		byte[] bc = new byte[rBins*gBins*bBins];
		
		int rOff = 255/rBins;
		int gOff = 255/gBins;
		int bOff = 255/bBins;
		
		for(int r = 0 ; r < rBins; r++)
		for(int g = 0 ; g < gBins; g++)
		for(int b = 0 ; b < bBins; b++)
		{
			
			rc[r*rBins+g*gBins+b] = (byte)(r*rOff+rOff/2);
			gc[r*rBins+g*gBins+b] = (byte)(g*gOff+gOff/2);
			bc[r*rBins+g*gBins+b] = (byte)(b*bOff+bOff/2);
			
		}
		
		IndexColorModel icm = new IndexColorModel(8, rBins*gBins*bBins, rc, gc, bc);
		return icm;
	}

}
