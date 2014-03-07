package JKernelMachines.fr.lip6.kernel;

import java.util.List;

import JKernelMachines.fr.lip6.type.TrainingSample;

public class SimpleCacheKernel<T> extends Kernel<T> {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2417905029129394427L;
	
	final private Kernel<T> kernel;
	final private double matrix[][];
	
	public SimpleCacheKernel(Kernel<T> k, List<TrainingSample<T>> l) {
		kernel = k;
		matrix = k.getKernelMatrix(l);
	}
	
	
	@Override
	public double valueOf(T t1, T t2) {
		return kernel.valueOf(t1, t2);
	}

	@Override
	public double valueOf(T t1) {
		return kernel.valueOf(t1);
	}


	@Override
	public double[][] getKernelMatrix(List<TrainingSample<T>> e) {
		return matrix;
	}
	
	

}
