package JKernelMachines.fr.lip6.kernel;

import java.util.List;

import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Generic kernel with a matrix based cache policy.<br />
 * The Gram matrix associated with an list of TrainingSample is
 * cached for future requests.
 * @author dpicard
 *
 * @param <T>
 */
public class CMKernel<T> extends Kernel<T> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3074562537295803520L;
	
	
	/**
	 * Default constructor
	 * @param k the kernel to cache
	 */
	public CMKernel(Kernel<T> k)
	{
		this.kernel = k;
		this.hash = -1;
	}
	
	
	double[][] matrix;
	int hash;
	Kernel<T> kernel;
	
	@Override
	public double[][] getKernelMatrix(List<TrainingSample<T>> e) {
			
		if(e.hashCode() == this.hash && matrix != null)
		{
			return matrix;
		}
		else
		{
			hash = e.hashCode();
			matrix = kernel.getKernelMatrix(e);
			return matrix;
		}
	}

	@Override
	public double valueOf(T t1, T t2) {
		
		return kernel.valueOf(t1, t2);
	}

	@Override
	public double valueOf(T t1) {
		return kernel.valueOf(t1);
	}

	
	
}
