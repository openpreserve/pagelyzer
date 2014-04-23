package JKernelMachines.fr.lip6.kernel.extra;

import java.util.ArrayList;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * performs a product of several minor kernels
 * @author dpicard
 *
 * @param <T>
 */
public class ProductKernel<T> extends Kernel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6273022923321895693L;
	
	ArrayList<Kernel<T>> kernels;
	ArrayList<Double> weights;
	
	public ProductKernel ()
	{
		super();
		
		kernels = new ArrayList<Kernel<T>>();
		weights = new ArrayList<Double>();
	}
	
	@Override
	public double valueOf(T t1, T t2) {
		
		double prod = 1.0;
		
		for(int i = 0 ; i < kernels.size(); i++)
			prod *= Math.pow(kernels.get(i).valueOf(t1, t2), weights.get(i));
		
		return prod;
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}
	
	/**
	 * adds a kernel to to product
	 * @param k
	 */
	public void addKernel(Kernel<T> k)
	{
		kernels.add(k);
		weights.add(1.0);
	}
	
	public void addKernel(Kernel<T> k, double w)
	{
		kernels.add(k);
		weights.add(w);
	}
	
	/**
	 * removes a kernel from the product
	 * @param k
	 */
	public void removeKernel(Kernel<T> k)
	{
		kernels.remove(k);
	}

}
