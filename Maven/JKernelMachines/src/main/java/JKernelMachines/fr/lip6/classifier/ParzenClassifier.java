package JKernelMachines.fr.lip6.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Classification tool using a Parzen window
 * @author dpicard
 *
 * @param <T> type of input space
 */
public class ParzenClassifier<T> implements Classifier<T>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5147554432765939157L;
	
	
	Kernel<T> kernel;
	ArrayList<TrainingSample<T>> ts;
	
	public ParzenClassifier(Kernel<T> kernel)
	{
		this.kernel = kernel;
	}

	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(java.lang.Object, int)
	 */
	public void train(TrainingSample<T> t) {

		if(ts == null)
		{
			ts = new ArrayList<TrainingSample<T>>();
			
		}
		ts.add(t);

		
	}

	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(T[], int[])
	 */
	public void train(List<TrainingSample<T>> t) {

		ts = new ArrayList<TrainingSample<T>>(t);
	}

	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#valueOf(java.lang.Object)
	 */
	public double valueOf(T e) {

		double sum = 0.;
		for(int i = 0 ; i < ts.size(); i++)
		{
			TrainingSample<T> t = ts.get(i);
			sum += t.label * kernel.valueOf(t.sample, e);
		}
		
		sum /= ts.size();
		
		return sum;
	}
	
}
