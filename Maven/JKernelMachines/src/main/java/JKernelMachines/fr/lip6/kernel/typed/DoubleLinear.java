package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Linear Kernel on double[].
 * @author dpicard
 *
 */
public class DoubleLinear extends Kernel<double[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8274638352733867140L;

	@Override
	public double valueOf(double[] t1, double[] t2) {
		double sum = 0.;
		int lim = Math.min(t1.length, t2.length);
		for(int i = lim-1 ; i >= 0 ; i--)
			if(t2[i] != 0 && t1[i] != 0)
				sum += t2[i]*t1[i];
		return sum;
	}

	@Override
	public double valueOf(double[] t1) {
		return valueOf(t1, t1);
	}


}
