package JDescriptors.fr.lip6.color.model;

import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.util.ArrayList;

import javax.media.jai.*;

import JDescriptors.fr.lip6.color.ColorQuantizer;


public class IHSColorQuantizer implements ColorQuantizer 
{

	private ColorModel colorModel;
	
	int[][] colors;
	float iOff = 0;
	float hOff = 0;
	float sOff = 0;
	int I = 1, H = 1, S = 1;
	
	
	public IHSColorQuantizer(int I, int H, int S)
	{
		//compute color table
//		colors = new int[H*S*V][3];

		this.I = I;
		this.H = H;
		this.S = S;
		
		iOff = 256.0f/I;
		hOff = 256.0f/H;
		sOff = 256.0f/S;
		

		//compute color bins 

		
		ArrayList<int[]> list = new ArrayList<int[]>();
		for(int i = 0; i < I; i++)
		{
				
			for(int h = 0; h < H; h++)
			{

				for(int s = 0; s < S; s++)
				{
					int[] color = new int[3];
					
					color[0] =(int) (i*iOff + iOff/2);
					color[1] =(int) (h*hOff + hOff/2);
					color[2] =(int) (s*sOff + sOff/2);

					list.add(color);
//					System.out.println("colors "+i+","+h+','+s+" : "+Arrays.toString(color));
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
	 * @param ihs
	 * @return
	 */
	public int getBin(int[] ihs)
	{
		int index = -1;
		double min = Double.MAX_VALUE;
		
		for(int i = 0 ; i < colors.length; i++)
		{
			int[] c = colors[i];
			double d = (c[0]-ihs[0])*(c[0]-ihs[0]) + (c[1]-ihs[1])*(c[1]-ihs[1]) + (c[2]-ihs[2])*(c[2]-ihs[2]);
//			//hue
//			if(c[1] > ihs[1])
//				d +=  Math.min(  (c[1]-ihs[1])*(c[1]-ihs[1]) , (255-c[1]+ihs[1])*(255-c[1]+ihs[1]) );
//			else
//				d +=  Math.min(  (c[1]-ihs[1])*(c[1]-ihs[1]) , (255+c[1]-ihs[1])*(255+c[1]-ihs[1]) );
			if(d < min)
			{
				min = d;
				index = i;
			}
		}

//		System.out.println("ihs : "+Arrays.toString(ihs) +" binColor : "+Arrays.toString(colors[index]));
		
		return index;
		
//		int i = ihs[0] / iOff;
//		int h = ihs[1] / hOff;
//		int s = ihs[2] / sOff;
//		
//		return i*(H*S)+h*S+s;
		
		
	}

	public int getBin(float[] fcol) {
		int i = (int) (fcol[0]*255);
		int h = (int) (fcol[1]*255/2/Math.PI);
		int s = (int) (fcol[2]*255);
		
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
		int [] c = colors[bin];
		
		float[] f = new float[c.length];
		
		f[0] = c[0] / 255.0f;
		f[1] = c[1] / 255.0f * (2*(float)Math.PI);
		f[2] = c[2] / 255.0f;
		
//		float[] f = colorModel.getNormalizedComponents(c, 0, null, 0);
//		System.out.println(Arrays.toString(f));
		return f;
	}
	
	public float[][] getColors()
	{
		float[][] f = new float[colors.length][];
		for(int i = 0 ; i < f.length; i++)
			f[i] = getColorFromBin(i);
		
		return f;
	}

	
}
