package JKernelMachines.fr.lip6.kernel.typed.index;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Kernel on double[] that computes the L2 distance of a specified component j:<br />
 * k(x, y) = (x[j]-y[j])*(x[j]-y[j])
 * @author dpicard
 *
 */
public class IndexDoubleGaussChi2 extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 102467593724674738L;
	
	
	private double gamma = 1.0;
	private int ind = 0;
	private double eps = 10e-6;
	
	public IndexDoubleGaussChi2(int feature)
	{
		ind = feature;
	}
	
	@Override
	public double valueOf(double[] t1, double[] t2) {
		if(t1[ind] == 0. && t2[ind] == 0.)
			return 1.;
		
		double tmp = 0;
		double min = 0;
		
		//assume X and Y > 0
		if( (tmp = t1[ind]+t2[ind]) > eps)
		{
			min = t1[ind]-t2[ind];
			min = (min*min) / tmp; //chi2
		}
	
		return Math.exp(-gamma * min);
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
