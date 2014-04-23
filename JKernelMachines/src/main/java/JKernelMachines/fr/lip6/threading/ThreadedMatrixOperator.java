package JKernelMachines.fr.lip6.threading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class ThreadedMatrixOperator {
	
	/**
	 * get the parallelized matrix
	 * @param matrix
	 * @return
	 */
	public double[][] getMatrix(final double[][] matrix)
	{
		try
		{
			//max cpu
			int nbcpu = Runtime.getRuntime().availableProcessors();

			//one job per line of the matrix
			ThreadPoolExecutor threadPool = new ThreadPoolExecutor(nbcpu, nbcpu, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(matrix.length+2));
			for(int i = matrix.length-1 ; i >= 0 ; i--)
			{
				final int index = i;
				final double line[] = matrix[i];
				Runnable r = new Runnable(){
					public void run() {
						doLine(index, line);
					}
				};
				
				threadPool.execute(r);
			}

			threadPool.shutdown();
			threadPool.awaitTermination(Integer.MAX_VALUE, TimeUnit.DAYS);
			return matrix;
		} catch (InterruptedException e) {

			System.err.println("MatrixWorkerFactory : getMatrix impossible");
			e.printStackTrace();
			return null;
		}
	}
	
	public abstract void doLine(int index, double[] line);
	
}
