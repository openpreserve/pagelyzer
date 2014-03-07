package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on double[] that uses a Chi1 distance.
 * @author dpicard
 *
 */
public class DoubleGaussChi1 extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1011192915083821659L;
	
	
	private double gamma = 0.1;
	private double eps = 10e-8;
	
	@Override
	public double valueOf(double[] t1, double[] t2) {
		double sum = 0.;
		double tmp = 0.;
		
		int lim = Math.min(t1.length, t2.length);
		for (int i = lim-1; i >= 0 ; i--)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
				sum += Math.abs(t1[i] - t2[i]) / tmp; //chi1
		
		return Math.exp(-gamma * sum);
	}

	@Override
	public double valueOf(double[] t1) {
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
