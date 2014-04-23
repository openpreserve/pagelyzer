package JKernelMachines.fr.lip6.kernel;

import java.util.List;

import JKernelMachines.fr.lip6.threading.ThreadedMatrixOperator;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Simple multithreaded implementation over a given Kernel. The multithreading comes only when
 * computing the Gram matrix.<br />
 * Number of Threads is function of available processors.
 * @author dpicard
 *
 * @param <T>
 */
public class ThreadedKernel<T> extends Kernel<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2193768216118832033L;
	
	
	protected final Kernel<T> k;

	/**
	 * MultiThread the given kernel
	 * @param kernel
	 */
	public ThreadedKernel(Kernel<T> kernel)
	{
		this.k = kernel;
	}
	

	@Override
	public double valueOf(T t1, T t2) {
		return k.valueOf(t1, t2);
	}

	@Override
	public double valueOf(T t1) {
		return k.valueOf(t1);
	}
	
	
	@Override
	public double[][] getKernelMatrix(final List<TrainingSample<T>> l) {
		
		final List<TrainingSample<T>> e = l;
		double[][] matrix = new double[e.size()][e.size()];
				
		ThreadedMatrixOperator factory = new ThreadedMatrixOperator()
		{
			@Override
			public void doLine(int index, double[] line) {
				
				T xi = l.get(index).sample;
				
				for(int i = line.length-1 ; i >= 0 ; i--)
				{
					line[i] = k.valueOf(xi, l.get(i).sample);
				}
			};
		};

		/* do the actuel computing of the matrix */
		matrix = factory.getMatrix(matrix);
		
		return matrix;
	}



}
