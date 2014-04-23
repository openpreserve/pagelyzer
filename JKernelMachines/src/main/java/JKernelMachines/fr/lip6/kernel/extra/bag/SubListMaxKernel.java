package JKernelMachines.fr.lip6.kernel.extra.bag;

import java.io.Serializable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * max value of kernel between to bags
 * @author dpicard
 *
 * @param <S>
 * @param <T>
 */
public class SubListMaxKernel<S,T extends List<S>> extends Kernel<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591055803491554966L;
	
	private int from = 0;
	private int to = 0;
	private Kernel<S> kernel;
	
	

	

	/**
	 * @param from
	 * @param to
	 * @param kernel
	 */
	public SubListMaxKernel(int from, int to, Kernel<S> kernel) {
		this.from = from;
		this.to = to;
		this.kernel = kernel;
	}

	@Override
	public double valueOf(T t1, T t2) {
		double max = 0;
		if(to > t1.size())
			to = t1.size();
		if(to > t2.size())
			to = t2.size();
		
		for(int i = from; i < to; i++)
		for(int j = from; j < to; j++)
		{
			double v = kernel.valueOf(t1.get(i), t2.get(j));
			if(v > max)
				max = v;
		}
		
		return max;
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}



	
	
}

	

