package JKernelMachines.fr.lip6.type;

import java.io.Serializable;

/**
 * Simple class of training sample that contains the generic <T> of sample and the associated label.
 * @author dpicard
 *
 * @param <T>
 */
public class TrainingSample<T> implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 874733576041102410L;
	
	
	public T sample;
	public int label;
	
	public TrainingSample(T t, int l)
	{
		sample = t;
		label = l;
	}
}
