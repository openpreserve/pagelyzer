package JKernelMachines.fr.lip6.kernel.adaptative;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.threading.ThreadedMatrixOperator;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Major kernel computed as a weighted sum of minor kernels : 
 * K = w_i * k_i<br />
 * Computation of the kernel matrix is done by running a thread on sub matrices.
 * The number of threads is choosen as function of the number of available cpus.
 * @author dpicard
 *
 * @param <T>
 */
public class ThreadedProductKernel<T> extends Kernel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7780445301175174296L;
	
	
	private Hashtable<Kernel<T>, Double> kernels;
	protected int numThread = 0;
	
	public ThreadedProductKernel()
	{
		kernels = new Hashtable<Kernel<T>, Double>();
	}

	/**
	 * Sets the weights to h. Beware! It does not make a copy of h!
	 * @param h
	 */
	public ThreadedProductKernel(Hashtable<Kernel<T>, Double> h)
	{
//		kernels = h;
		kernels = new Hashtable<Kernel<T>, Double>();
		kernels.putAll(h);
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
		double sum = 1.;
		for(Kernel<T> k : kernels.keySet())
		{
			double w = kernels.get(k);
			if(w != 0)
				sum *= Math.pow(k.valueOf(t1, t2), kernels.get(k));
		}
		
		return sum;
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}
	
	/**
	 * get the list of kernels and associated weights.
	 * @return hashtable containing kernels as keys and weights as values.
	 */
	public Hashtable<Kernel<T>, Double> getWeights()
	{
		return kernels;
	}
	
	@Override
	public double[][] getKernelMatrix(List<TrainingSample<T>> list)
	{
		final List<TrainingSample<T>> l = list;
		//init matrix with ones
		double matrix[][] = new double[l.size()][l.size()];
		for(double[] lines : matrix)
			Arrays.fill(lines, 1.);
		

		for(final Kernel<T> k : kernels.keySet())
		{
			final double w = kernels.get(k);
			
			//check w
			if(w == 0)
				continue;
			

			final double[][] m = k.getKernelMatrix(l);
			// specific factory
			ThreadedMatrixOperator tmo = new ThreadedMatrixOperator(){
				
				@Override
				public void doLine(int index, double[] line) {
					
					for(int i = line.length-1 ; i >= 0 ; i--)
					{
						line[i] *= Math.pow(m[index][i], w);
					}
				};
				
			};
			
			tmo.getMatrix(matrix);
		}
		return matrix;
	}
}
