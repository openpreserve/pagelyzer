package JKernelMachines.fr.lip6.density;

import java.io.Serializable;
import java.util.ArrayList;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Parzen window for estimating the probability density function of a random variable.
 * @author dpicard
 *
 * @param <T> Elements of input space
 */
public class ParzenDensity<T> implements DensityFunction<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5414922333951533146L;
	
	
	private Kernel<T> kernel;
	ArrayList<T> set;
	
	public ParzenDensity(Kernel<T> kernel)
	{
		this.kernel = kernel;
	}

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#train(java.lang.Object)
	 */
	public void train(T e) {
		if(set == null)
		{
			set = new ArrayList<T>();
		}

		set.add(e);
				
	}

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#train(T[])
	 */
	public void train(ArrayList<T> e) {
		if(set == null)
		{
			set = new ArrayList<T>();
		}
		
		for(T t : e)
			set.add(t);
	}

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#valueOf(java.lang.Object)
	 */
	public double valueOf(T e) {

		double sum = 0.;
		for(int i = 0 ; i < set.size(); i++)
			sum += kernel.valueOf(set.get(i), e);
		
		sum /= set.size();
		
		return sum;
	}

}
