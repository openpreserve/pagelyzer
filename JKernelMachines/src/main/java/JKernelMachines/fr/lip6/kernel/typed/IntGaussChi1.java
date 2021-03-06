package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on int[] that uses a Chi1 distance.
 * @author dpicard
 *
 */
public class IntGaussChi1 extends Kernel<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3434476346814281311L;
	
	
	private double gamma = 0.1;
	private double eps = 0;
	
	@Override
	public double valueOf(int[] t1, int[] t2) {
		double sum = 0.;
		double tmp = 0.;
		for (int i = 0; i < Math.min(t1.length, t2.length); i++)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
				sum += Math.abs(t1[i] - t2[i]) / tmp; //chi1
		
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
