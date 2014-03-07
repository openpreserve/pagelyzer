package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on int[] that uses a Chi2 distance.
 * @author dpicard
 *
 */
public class IntGaussChi2 extends Kernel<int[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7324980870969389374L;
	
	
	private double gamma = 0.1;
	private double eps = 0;
	
	@Override
	public double valueOf(int[] t1, int[] t2) {
		double sum = 0.;
		double tmp = 0.;
		for (int i = 0; i < Math.min(t1.length, t2.length); i++)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
				sum += (t1[i] - t2[i])*(t1[i] - t2[i]) / tmp; //chi2
		
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
