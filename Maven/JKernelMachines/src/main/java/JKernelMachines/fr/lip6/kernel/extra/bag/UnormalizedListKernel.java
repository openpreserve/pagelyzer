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
public class UnormalizedListKernel<S,T extends List<S>> extends Kernel<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591055803491554966L;
	
	private Kernel<S> kernel;
	

	/**
	 * @param from starting index of the bags
	 * @param to end index of the bags
	 * @param kernel minor kernel
	 */
	public UnormalizedListKernel(Kernel<S> kernel) {
		this.kernel = kernel;
	}

	@Override
	public double valueOf(T t1, T t2) {
		double sum = 0;
		
		for(int i = 0; i < t1.size(); i++)
		for(int j = 0; j < t2.size(); j++)
		{
			double d = kernel.valueOf(t1.get(i), t2.get(j));
//			if(d > eps)
				sum += d;
		}
		
//		System.out.println("<t1, t2> = "+sum);
//		if(sum > eps)
//			System.out.println("<t1, T2> = "+sum);
		return sum;
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}



	
	
}

	

