package JKernelMachines.fr.lip6.kernel.adaptative;

import java.util.Hashtable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Major kernel computed as a weighted sum of minor kernels : 
 * K = w_i * k_i
 * @author dpicard
 *
 * @param <T>
 */
public class WeightedSumKernel<T> extends Kernel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4590492743843223113L;
	
	
	private Hashtable<Kernel<T>, Double> kernels;
	
	public WeightedSumKernel()
	{
		kernels = new Hashtable<Kernel<T>, Double>();
	}
	
	/**
	 * Sets the weights to h. Beware! It does not make a copy of h!
	 * @param h
	 */
	public WeightedSumKernel(Hashtable<Kernel<T>, Double> h)
	{
		kernels = h;
	}
	
	/**
	 * adds a kernel to the sum with weight 1.0
	 * @param k
	 */
	public void addKernel(Kernel<T> k)
	{
		kernels.put(k, 1.0);
	}
	
	/**
	 * adds a kernel to the sum with weight d
	 * @param k
	 * @param d
	 */
	public void addKernel(Kernel<T> k , double d)
	{
		kernels.put(k, d);
	}
	
	/**
	 * removes kernel k from the sum
	 * @param k
	 */
	public void removeKernel(Kernel<T> k)
	{
		kernels.remove(k);
	}
	
	/**
	 * gets the weights of kernel k
	 * @param k
	 * @return the weight associated with k
	 */
	public double getWeight(Kernel<T> k)
	{
		Double d = kernels.get(k);
		if(d == null)
			return 0.;
		return d.doubleValue();
	}
	
	/**
	 * Sets the weight of kernel k
	 * @param k
	 * @param d
	 */
	public void setWeight(Kernel<T> k, Double d)
	{
		kernels.put(k, d);
	}
	
	@Override
	public double valueOf(T t1, T t2) {
		double sum = 0.;
		for(Kernel<T> k : kernels.keySet())
			sum += kernels.get(k)*k.valueOf(t1, t2);
		
		return sum;
	}

	@Override
	public double valueOf(T t1) {
		double sum = 0.;
		for(Kernel<T> k : kernels.keySet())
			sum += kernels.get(k)*k.valueOf(t1);
		
		return sum;
	}
	
	/**
	 * get the list of kernels and associated weights.
	 * @return hashtable containing kernels as keys and weights as values.
	 */
	public Hashtable<Kernel<T>, Double> getWeights()
	{
		return kernels;
	}

	/* (non-Javadoc)
	 * @see fr.lip6.kernel.Kernel#getKernelMatrix(java.util.ArrayList)
	 */
	@Override
	public double[][] getKernelMatrix(List<TrainingSample<T>> e) {
		double matrix[][] = new double[e.size()][e.size()];
		
		for(Kernel<T> k : kernels.keySet())
		{
			double[][] m = k.getKernelMatrix(e);
			double w = kernels.get(k)/100;
			w = w*100;
			for(int i = 0 ; i < e.size() ; i++)
			for(int j = i ; j < e.size() ; j++)
			{
				matrix[i][j] += w*m[i][j];
				if(i != j)
					matrix[j][i] += w*m[j][i];
			}
		}
		
		return matrix;
	}
	
	

}
