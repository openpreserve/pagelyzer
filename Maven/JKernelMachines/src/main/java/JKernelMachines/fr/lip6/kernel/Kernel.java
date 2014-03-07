package JKernelMachines.fr.lip6.kernel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Base class for kernels
 * 
 * @author dpicard
 * 
 * @param <T>
 *            Type of data from input space
 */
public abstract class Kernel<T> implements Serializable {
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1663774351688566794L;
	
	
	public String name = "k_default";
	
	/**
	 * compute the kernel similarity between two element of input space
	 * 
	 * @param t1
	 *            first element
	 * @param t2
	 *            second element
	 * @return the kernel value
	 */
	public abstract double valueOf(T t1, T t2);

	/**
	 * kernel similarity to zero
	 * 
	 * @param t1
	 *            the element to compute the similarity to zero
	 */
	public abstract double valueOf(T t1);
	
	/**
	 * kernel similarity normalized such that k(t1, t1) = 1
	 * @param t1 first element
	 * @param t2 second element
	 * @return normalized similarity
	 */
	public double normalizedValueOf(T t1, T t2)
	{
		return valueOf(t1, t2)/Math.sqrt(valueOf(t1, t1)*valueOf(t2,t2));
	}
	
	/**
	 * return the Gram Matrix of this kernel computed on given samples
	 * @param l
	 * @return double[][] containing similarities in the order of the e.
	 */
	public double[][] getKernelMatrix(List<TrainingSample<T>> l)
	{
		double[][] matrix = new double[l.size()][l.size()];
		for(int i = 0 ; i < l.size(); i++)
		{
			for(int j = i; j < l.size(); j++)
			{
				matrix[i][j] = valueOf(l.get(i).sample, l.get(j).sample);
				matrix[j][i] = matrix[i][j];
			}
//			if(i%(e.size()/10) == 0)
//				System.err.print(" "+i);
		}
//		System.err.println();
		
		return matrix;
	}
	
	/**
	 * return the Gram Matrix of this kernel computed on given samples
	 * @param e
	 * @return double[][] containing similarities in the order of the e.
	 */
	public double[][] getNormalizedKernelMatrix(ArrayList<TrainingSample<T>> e)
	{
		double[][] matrix = new double[e.size()][e.size()];
		for(int i = 0 ; i < e.size(); i++)
		{
			for(int j = i; j < e.size(); j++)
			{
				matrix[i][j] = normalizedValueOf(e.get(i).sample, e.get(j).sample);
				matrix[j][i] = matrix[i][j];
			}
//			if(i%(e.size()/10) == 0)
//				System.err.print(" "+i);
		}
//		System.err.println();
		
		return matrix;
	}	
	
	/**
	 * Set the name of this kernel
	 * @param n
	 */
	public void setName(String n)
	{
		name = n;
	}

	
	/**
	 * return the name of this kernel
	 */
	public String toString()
	{
		return name;
	}
}
