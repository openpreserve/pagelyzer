package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on float[] that uses a Chi2 distance.
 * @author dpicard
 *
 */
public class FloatGaussChi2 extends Kernel<float[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1626829154456556731L;
	
	
	private double gamma = 0.1;
	private double eps = 10e-12;
	
	@Override
	public double valueOf(float[] t1, float[] t2) {
		double sum = 0.;
		double tmp = 0.;
		double min = 0.;
		for (int i = 0; i < Math.min(t1.length, t2.length); i++)
			//assume X and Y > 0
			if( (tmp = t1[i]+t2[i]) > eps)
			{
				min = t1[i]-t2[i];
				sum += (min*min) / tmp; //chi2
			}
		
		return Math.exp(-gamma * sum);
	}

	@Override
	public double valueOf(float[] t1) {
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
