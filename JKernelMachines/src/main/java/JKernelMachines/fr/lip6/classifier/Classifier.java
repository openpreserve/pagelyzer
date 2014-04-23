package JKernelMachines.fr.lip6.classifier;

import java.util.List;

import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Classifier interface that provides training and evaluation methods.
 * @author dpicard
 *
 * @param <T>
 */
public interface Classifier<T> {

	/**
	 * Add a single example to the current training set and train the classifier
	 * @param t the training sample
	 */
	public void train(TrainingSample<T> t);
	
	/**
	 * Replace the current training and train the classifier
	 * @param l list of training samples
	 */
	public void train(List<TrainingSample<T>> l);
	
	/**
	 * Computes the category of the provided example
	 * @param e example
	 * @return >0. if e belongs to the category, <0. if not.
	 */
	public double valueOf(T e);
	
}
