package JDescriptors.fr.lip6.color.model;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.ArrayList;

import JDescriptors.fr.lip6.color.ColorQuantizer;


public class RGBColorQuantizer implements ColorQuantizer 
{

	private ColorModel colorModel;
	
	int[][] colors;
	
	public RGBColorQuantizer(int H, int S, int V)
	{
		//compute color table
//		colors = new int[H*S*V][3];
		
		int hOff = 255/H;
		int sOff = 255/S;
		int vOff = 255/V;
		

		//compute color bins 

		
		ArrayList<int[]> list = new ArrayList<int[]>();
		for(int v = 0; v < V; v++)
		{
				
			for(int h = 0; h < H; h++)
			{

				for(int s = 0; s < S; s++)
				{
					int[] color = new int[3];
					
					color[0] = h*hOff+hOff/2;
					color[1] = s*sOff+sOff/2;
					color[2] = v*vOff+vOff/2;

					list.add(color);
//					System.out.println("colors "+v+","+h+','+s+" : "+Arrays.toString(color));
				}
//				System.out.println("S.");
			}
//			System.out.println("V.");
		}
		
		colors = new int[list.size()][];
		colors = list.toArray(colors);
		
		//colorSpace
		colorModel = new ComponentColorModel( ColorSpace.getInstance(ColorSpace.CS_sRGB), new int[] {8, 8, 8}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE );
	}

	/**
	 * @return the colorModel
	 */
	public ColorModel getColorModel() {
		return colorModel;
	}

	/**
	 * @param colorModel the colorModel to set
	 */
	public void setColorModel(ColorModel colorModel) {
		this.colorModel = colorModel;
	}
	
	/**
	 * compute the bin in corresponding to the lab color given in param
	 * @param rgb
	 * @return
	 */
	public int getBin(int[] rgb)
	{
		int index = -1;
		double min = Double.MAX_VALUE;
		
		for(int i = 0 ; i < colors.length; i++)
		{
			int[] c = colors[i];
			double d = (c[0]-rgb[0])*(c[0]-rgb[0])+(c[1]-rgb[1])*(c[1]-rgb[1])+(c[2]-rgb[2])*(c[2]-rgb[2]);
			if(d < min)
			{
				min = d;
				index = i;
			}
		}
		
		return index;
	}
	
	public int getBinNumber()
	{
		return colors.length;
	}
	
	public float[] getColorFromBin(int bin)
	{
		float[] f = new float[colors[bin].length];
		for(int i = 0 ; i < f.length; i++)
			f[i] = colors[bin][i]/255.0f;
		return f;
	}


	public int getBin(float[] fcol) {
		
		int[] rgb = new int[3];
		rgb[0] = (int) fcol[0]*255;
		rgb[1] = (int) fcol[2]*255;
		rgb[2] = (int) fcol[2]*255;
		
		return getBin(rgb);
	}
	
}
