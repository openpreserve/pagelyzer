package JKernelMachines.fr.lip6.kernel.typed;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Generalized  linear kernel on double[]. Provided a proper inner product matrix M, this kernel returns :<br />
 * k(x, y) = x'*M*y
 * @author dpicard
 *
 */
public class GeneralizedDoubleLinear extends Kernel<double[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6618610116348247480L;
	
	double[][] M;
	int size;
	
	public GeneralizedDoubleLinear(double[][] innerProduct)
	{
		if(innerProduct.length != innerProduct[0].length)
		{
			M = null;
			size = 0;
		}
		else
		{
			M = innerProduct;
			size = M.length;
		}
		
		
	}
	
	@Override
	public double valueOf(double[] t1, double[] t2) {
		
		if(t1.length != size && t2.length != size)
			return 0;
		
		double sum = 0;
		for(int i = 0 ; i < M.length; i++)
		{
			double xtM = 0;
			for(int j = 0 ; j < M[0].length; j++)
			{
				xtM += t1[j]*M[j][i];
			}
			sum += xtM*t2[i];
		}
		
		return sum;
	}

	@Override
	public double valueOf(double[] t1) {
		
		return valueOf(t1, t1);
	}

}
