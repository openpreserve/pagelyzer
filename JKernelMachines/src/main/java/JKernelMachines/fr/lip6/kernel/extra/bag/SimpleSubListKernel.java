package JKernelMachines.fr.lip6.kernel.extra.bag;

import java.io.Serializable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Kernel on bags of same length.<br/> 
 * Let B1 and B2 be bags of elements b1[i] and b2[i], let k(b1[i],b2[i]) be the minor kernel, then K(B1, B2) = sum_{i=n}^{N} k(b1[i],b2[i])<br/>
 * With n and N being the bounds of the sum.
 * @author dpicard
 *
 * @param <S>
 * @param <T>
 */
public class SimpleSubListKernel<S,T extends List<S>> extends Kernel<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591055803491554966L;
	
	private int from = 0;
	private int to = 0;
	private Kernel<S> kernel;
	
	

	

	/**
	 * @param from starting point of the sum
	 * @param to end poitn of the sum
	 * @param kernel minor kernel
	 */
	public SimpleSubListKernel(int from, int to, Kernel<S> kernel) {
		this.from = from;
		this.to = to;
		this.kernel = kernel;
	}

	@Override
	public double valueOf(T t1, T t2) {
		double sum = 0;
		if(to > t1.size())
			to = t1.size();
		if(to > t2.size())
			to = t2.size();
		
		for(int i = from; i < to; i++)
			sum += kernel.valueOf(t1.get(i), t2.get(i));
		
		return sum/((double)(to-from));
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}



	
	
}

	

