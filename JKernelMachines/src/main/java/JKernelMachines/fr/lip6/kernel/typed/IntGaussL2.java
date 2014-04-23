package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on int[] that uses a L2 distance.
 * @author dpicard
 *
 */
public class IntGaussL2 extends Kernel<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4415295327411182596L;
	
	
	private double gamma = 0.1;
	
	@Override
	public double valueOf(int[] t1, int[] t2) {
		double sum = 0.;
		for(int i = 0 ; i < Math.min(t1.length, t2.length); i++)
			sum += (t1[i]-t2[i])*(t1[i] - t2[i]);
		return Math.exp(-gamma * sum);
	}

	@Override
	public double valueOf(int[] t1) {
		return 1.0;
	}


	/**
	 * @return the sigma
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * @param gamma inverse of std dev parameter
	 */
	public void setGamma(double gamma) {
		this.gamma = gamma;
	}

}
