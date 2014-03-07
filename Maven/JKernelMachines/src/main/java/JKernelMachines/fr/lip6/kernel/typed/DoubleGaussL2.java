package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Gaussian Kernel on double[] that uses a L2 distance.
 * @author dpicard
 *
 */
public class DoubleGaussL2 extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8139656729530005699L;
	
	
	private double gamma = 0.1;
	
	public DoubleGaussL2(double g) {
		gamma = g;
	}

	public DoubleGaussL2() {
	}

	@Override
	public double valueOf(double[] t1, double[] t2) {
		double sum = 0.;
		int lim = Math.min(t1.length, t2.length);
		for(int i = lim-1 ; i >= 0 ; i--)
		{
			double d = (t1[i]-t2[i]);
			sum += d*d;
		}
		if(Double.isNaN(sum))
		{
			System.err.println(this+" : Warning sum NaN");
			return 0.0;
		}
		return Math.exp(-gamma * sum);
	}

	@Override
	public double valueOf(double[] t1) {
		return valueOf(t1, t1);
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
