package JKernelMachines.fr.lip6.kernel;

import java.util.HashMap;
import java.util.Map;

import JKernelMachines.fr.lip6.threading.ThreadedMatrixOperator;


public class IndexedCacheKernel<S,T> extends Kernel<S> {

	
	private static final long serialVersionUID = 1706425748759430692L;
	
	private double[][] matrix;
	private HashMap<S, Integer> map;
	final private Kernel<T> kernel;
	
	public IndexedCacheKernel(Kernel<T> k, final Map<S, T> signatures)
	{
		this.kernel = k;
		
		matrix = new double[signatures.size()][signatures.size()];
	
		//adding index
		map = new HashMap<S,Integer>(signatures.size());
		int index = 0;
		for(S s : signatures.keySet())
		{
			map.put(s, index);
			index++;
		}
		
		//computing matrix				
		ThreadedMatrixOperator factory = new ThreadedMatrixOperator()
		{
			@Override
			public void doLine(int index, double[] line) {
				//reverse search through mapping S <-> index
				S s1 = null;
				for(S s : map.keySet())
					if( map.get(s) == index )
					{
						s1 = s;
						break;
					}
				//mapped signature
				T t1 = signatures.get(s1);
				
				//all mapping S <-> T
				for(S s2 : map.keySet())
				{
					//get index of s2
					int j = map.get(s2);
					//get signature of s2
					T t2 = signatures.get(s2);
					//add value of kernel
					line[j] = kernel.valueOf(t1, t2);
				}
			};
		};


		/* do the actuel computing of the matrix */
		matrix = factory.getMatrix(matrix);
		
	}
	
	@Override
	public double valueOf(S t1, S t2) {
		//return 0 if doesn't know of
		if(!map.containsKey(t1) || !map.containsKey(t2))
		{
			System.err.println("<"+t1+","+t2+"> not in matrix !!!");
			return 0;
		}
		int id1 = map.get(t1);
		int id2 = map.get(t2);
		
		return matrix[id1][id2];
	}

	@Override
	public double valueOf(S t1) {
		//return 0 if doesn't know of
		if(!map.containsKey(t1))
		{
			System.err.println("<"+t1+","+t1+"> not in matrix !!!");
			return 0;
		}
		
		int id = map.get(t1);
		return matrix[id][id];
	}

}
