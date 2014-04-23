package JDescriptors.fr.lip6.test;

import java.io.FileOutputStream;
import java.io.PrintStream;

import JDescriptors.fr.lip6.texture.GaborFilterFactory;


public class TestGaborFilter {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception 
	{
		int w = 17, h = 17;
		float[] g = GaborFilterFactory.getGaborFilter(w, h, 3.0, 3*Math.PI/4);
		
		
		PrintStream out = new PrintStream(new FileOutputStream("gabor.m"));
		out.println("clear all;");
		out.println("close all;");
		out.print("g = [ ");
		int i = 0;
		for(int x =-(w-1)/2; x <=(w-1)/2; x++)
		{
			for(int y =-(h-1)/2; y <=(h-1)/2; y++)
			{
				out.print(g[i++]+" ");
			}
			out.println();
		}
		out.println("];");

	}

}
