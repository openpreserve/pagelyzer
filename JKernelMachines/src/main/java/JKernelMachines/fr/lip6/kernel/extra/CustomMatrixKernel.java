package JKernelMachines.fr.lip6.kernel.extra;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * kernel with a provided custom matrix
 * @author dpicard
 *
 */
public class CustomMatrixKernel extends Kernel<Integer> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5379932592270965091L;
	private double matrix[][];
	
	public CustomMatrixKernel(double matrix[][])
	{
		this.matrix = matrix;
	}
	
	@Override
	public double valueOf(Integer t1, Integer t2) {
		if(t1 > matrix.length || t2 > matrix.length)
			return 0;
		return matrix[t1][t2];
	}

	@Override
	public double valueOf(Integer t1) {
		if(t1 > matrix.length)
			return 0.;
		return matrix[t1][t1];
	}

}
