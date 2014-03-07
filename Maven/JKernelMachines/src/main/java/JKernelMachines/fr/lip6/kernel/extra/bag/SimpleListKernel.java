package JKernelMachines.fr.lip6.kernel.extra.bag;

import java.io.Serializable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;


/**
 * Default kernel on bags : sum all kernel values involving an element from B1 and an element from B2 between specified bounds.
 * @author dpicard
 *
 * @param <S>
 * @param <T> type of element in the bag
 */
public class SimpleListKernel<S,T extends List<S>> extends Kernel<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591055803491554966L;
	
	private Kernel<S> kernel;
	private double eps = 10e-6;
	
	

	

	/**
	 * @param from starting index of the bags
	 * @param to end index of the bags
	 * @param kernel minor kernel
	 */
	public SimpleListKernel(Kernel<S> kernel) {
		this.kernel = kernel;
	}

	@Override
	public double valueOf(T t1, T t2) {
		double sum = 0;
		
		for(int i = 0 ; i < t1.size() && i < t2.size(); i++)
		{
			S s1 = t1.get(i);
			S s2 = t2.get(i);
			double d = kernel.valueOf(s1, s2);
			if(d > eps)
				sum += d;
		}
		
		return sum/((double)Math.min(t1.size(), t2.size()));
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}



	
	
}

	

