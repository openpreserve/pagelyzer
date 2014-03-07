package JKernelMachines.fr.lip6.kernel.typed.index;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Kernel on double[] that performs the product of a specified component j:<br />
 * k(x,y) = x[j]*y[j]
 * @author dpicard
 *
 */
public class IndexDoubleLinear extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 457509780005148420L;
	
	
	private int ind = 0;
	
	public IndexDoubleLinear(int feature)
	{
		ind = feature;
	}
	
	@Override
	public double valueOf(double[] t1, double[] t2) {
		if(t1[ind] == 0. || t2[ind] == 0.)
			return 0.;
		return t2[ind]*t1[ind];
	}

	@Override
	public double valueOf(double[] t1) {

		if(t1[ind] == 0.)
			return 0.;
		return t1[ind]*t1[ind];
	}

	
	public void setIndex(int i)
	{
		this.ind = i;
	}

}
