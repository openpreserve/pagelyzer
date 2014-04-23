package JKernelMachines.fr.lip6.classifier.transductive;

import java.util.List;

import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Interface for transductive classifiers.
 * @author dpicard
 *
 * @param <T>
 */
public interface TransductiveClassifier<T> {
	
	/**
	 * Train the classifier on trainList, with the help of testList in a transductive way.
	 * @param trainList
	 * @param testList
	 */
	public void train(List<TrainingSample<T>> trainList, List<TrainingSample<T>> testList);
	
	/**
	 * prediction output for t;
	 * @param t
	 * @return
	 */
	public double valueOf(T t);

}
