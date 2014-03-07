package JKernelMachines.fr.lip6.kernel.extra.bag;

import java.io.Serializable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;


/**
 * Default kernel on bags : sum all kernel values involving an element from B1 and an element from B2 between specified bounds.
 * the bound can be made different for left and right list, which doesn't lead to a kernel anymore.
 * However, this can be a usefull option to debug or view some precise elements of the sum.
 * @author dpicard
 *
 * @param <S>
 * @param <T> type of element in the bag
 */
public class SubListKernel2<S,T extends List<S>> extends Kernel<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1591055803491554966L;
	
	private int fr1 = 0, fr2 = 0;
	private int to1 = 0, to2 = 0;
	private Kernel<S> kernel;
	private double eps = 10e-6;
	
	


	/**
	 * @param kernel
	 * @param fr1 beginning bound for left list (inclusive)
	 * @param to1 end bound for left list (exclusive)
	 * @param fr2 beginning bound for right list (inclusive)
	 * @param to2 end bound for right list (exclusive)
	 */
	public SubListKernel2(Kernel<S> kernel, int fr1, int to1, int fr2, int to2) {
		this.kernel = kernel;
		this.fr1 = fr1;
		this.to1 = to1;
		this.fr2 = fr2;
		this.to2 = to2;
	}

	@Override
	public double valueOf(T t1, T t2) {
		double sum = 0;
		if(to1 > t1.size())
			to1 = t1.size();
		if(to2 > t2.size())
			to2 = t2.size();
		
		for(int i = fr1; i < to1; i++)
		for(int j = fr2; j < to2; j++)
		{
			double d = kernel.valueOf(t1.get(i), t2.get(j));
			if(d > eps)
				sum += d;
		}
		
		return sum/((double)((to1-fr1)*(to2-fr2)));
	}

	@Override
	public double valueOf(T t1) {
		return valueOf(t1, t1);
	}



	
	
}

	

