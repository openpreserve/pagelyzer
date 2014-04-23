package JDescriptors.fr.lip6.color.model;

import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.ArrayList;

import javax.media.jai.IHSColorSpace;

import JDescriptors.fr.lip6.color.ColorQuantizer;


public class HSVLinearColorQuantizer implements ColorQuantizer 
{

	private ColorModel colorModel;
	
	int[][] colors;
	
	public HSVLinearColorQuantizer(int H, int S, int V)
	{
		//compute color table
//		colors = new int[H*S*V][3];
//		
//		int hOff = 255/H;
//		int sOff = 255/S;
//		int vOff = 255/V;
//		
//		//compute color bins
//		for(int h = 0; h < H; h++)
//		{
//			for(int s = 0; s < S; s++)
//			{
//				for(int v = 0; v < V; v++)
//				{
//					colors[h*(S*V)+s*V+v][0] = h*hOff+hOff/2;
//					colors[h*(S*V)+s*V+v][1] = s*sOff+sOff/2;
//					colors[h*(S*V)+s*V+v][2] = v*vOff+vOff/2;
//					
////					System.out.println("colors "+l+","+ai+','+bi+" : "+Arrays.toString(colors[l*(S*V)+ai*V+bi]));
//				}
//			}
//		}
		
		//compute color bins with linear increase in h and s

		int vOff = 255/V;
		ArrayList<int[]> list = new ArrayList<int[]>();
		for(int v = 0; v < V; v++)
		{
			int Sv = v/(V/S) +1;
			int Hv = v/(V/H) +1;

			int hOff = 255/Hv;
			int sOff = 255/Sv;
			for(int h = 0; h < Hv; h++)
			{

				for(int s = 0; s < Sv; s++)
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
		colorModel = new ComponentColorModel( IHSColorSpace.getInstance(), new int[] {8, 8, 8}, false, false, ColorModel.OPAQUE, DataBuffer.TYPE_BYTE );
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
	 * @param hsv
	 * @return
	 */
	public int getBin(int[] hsv)
	{
		int index = -1;
		double min = Double.MAX_VALUE;
		
		for(int i = 0 ; i < colors.length; i++)
		{
			int[] c = colors[i];
			double d = (c[0]-hsv[0])*(c[0]-hsv[0])+(c[1]-hsv[1])*(c[1]-hsv[1])+(c[2]-hsv[2])*(c[2]-hsv[2]);
			if(d < min)
			{
				min = d;
				index = i;
			}
		}
		
		return index;
	}
	


	public int getBin(float[] fcol) {
		int i = (int) (fcol[0] * 255.0f);
		int h = (int) (fcol[1] / 2 / Math.PI * 255.0f);
		int s = (int) (fcol[2] * 255.0f);
		
		int index = -1;
		double min = Double.MAX_VALUE;
		
		for(int j = 0 ; j < colors.length; j++)
		{
			int[] c = colors[j];
			double d = (c[0]-i)*(c[0]-i)+(c[1]-h)*(c[1]-h)+(c[2]-s)*(c[2]-s);
			if(d < min)
			{
				min = d;
				index = j;
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
			f[i] = colors[bin][i];
		return f;
	}
}
