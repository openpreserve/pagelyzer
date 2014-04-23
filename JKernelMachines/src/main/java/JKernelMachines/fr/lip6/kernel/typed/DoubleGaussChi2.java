package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on double[] that uses a Chi2 distance.
 * @author dpicard
 *
 */
public class DoubleGaussChi2 extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1626829154456556731L;
	
	
	private double gamma = 0.1;
	private double eps = 10e-7;
	
	public DoubleGaussChi2(double g) {
		gamma = g;
	}

	public DoubleGaussChi2() {
	}

	@Override
	public final double valueOf(double[] t1, double[] t2) {
		double sum = 0.;
		double tmp = 0.;
		double min = 0.;
		
		int lim = Math.min(t1.length, t2.length);
		
		for (int i = lim-1; i >= 0 ; i--)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
			{
				min = t1[i]-t2[i];
				sum += (min*min) / tmp; //chi2
			}
		
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
	
	public double distChi2(double[] t1, double[] t2) {
		double sum = 0.;
		double tmp = 0.;
		double min = 0.;
		for (int i = 0; i < Math.min(t1.length, t2.length); i++)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
			{
				min = t1[i]-t2[i];
				sum += (min*min)/tmp; //chi2
			}

		return sum;
	}

}
