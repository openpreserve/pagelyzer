package JKernelMachines.fr.lip6.kernel.extra;

import JKernelMachines.fr.lip6.kernel.Kernel;

public class PowerKernel<T> extends Kernel<T> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4913325882464601407L;
	
	
	private Kernel<T> kernel;
	private double e = 1.0;
	/**
	 * @param kernel
	 * @param e
	 */
	public PowerKernel(Kernel<T> kernel, double e) {
		this.kernel = kernel;
		this.e = e;
	}
	@Override
	public double valueOf(T t1, T t2) {
		return Math.pow(kernel.valueOf(t1, t2), e);
	}
	@Override
	public double valueOf(T t1) {
		return Math.pow(kernel.valueOf(t1), e);
	}
	
	


}
