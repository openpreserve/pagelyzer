package JKernelMachines.fr.lip6.kernel.typed.index;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Kernel on double[] that computes the L2 distance of a specified component j:<br />
 * k(x, y) = (x[j]-y[j])*(x[j]-y[j])
 * @author dpicard
 *
 */
public class IndexDoubleGaussL2 extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 102467593724674738L;
	
	
	private double gamma = 0.1;
	private int ind = 0;
	
	public IndexDoubleGaussL2(int feature)
	{
		ind = feature;
	}
	
	@Override
	public double valueOf(double[] t1, double[] t2) {
		if(t1[ind] == 0. && t2[ind] == 0.)
			return 1.;
		return Math.exp(-gamma * (t1[ind] - t2[ind])*(t1[ind] - t2[ind]));
	}

	@Override
	public double valueOf(double[] t1) {
		
		return 1.0;
	}

	public void setGamma(double g)
	{
		gamma = g;
	}
	
	public void setIndex(int i)
	{
		this.ind = i;
	}

	public double getGamma() {
		return gamma;
	}

}
