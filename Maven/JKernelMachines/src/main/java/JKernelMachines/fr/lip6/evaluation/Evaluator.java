package JKernelMachines.fr.lip6.evaluation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import JKernelMachines.fr.lip6.classifier.Classifier;
import JKernelMachines.fr.lip6.type.TrainingSample;

public class Evaluator<T> implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2713343666983051855L;
	
	Classifier<T> classifier;
	List<TrainingSample<T>> train;
	List<TrainingSample<T>> test;
	List<Evaluation<TrainingSample<T>>> tsResults;
	List<Evaluation<TrainingSample<T>>> esResults;
	
	
	public Evaluator(Classifier<T> c, List<TrainingSample<T>> trainList, List<TrainingSample<T>> testList)
	{
		classifier = c;
		train = trainList;
		test = testList;
		
		//instanciate Evaluation for loading class
		Evaluation<T> e = new Evaluation<T>(null, 0);
		e.compareTo(null);
	}
	
	public void evaluate()
	{
		train();
		evaluateTrainingSet();
		evaluateTestingSet();
	}
	
	
	private void train()
	{
		classifier.train(train);
	}
	
	/**
	 * Computes output values for each element of the training set
	 */
	private void evaluateTrainingSet()
	{
		if(tsResults == null)
			tsResults = new ArrayList<Evaluation<TrainingSample<T>>>();


		numThread = 0;
		
		final int nbcpu = Runtime.getRuntime().availableProcessors();
		final int step = (train.size()+1)/nbcpu +1;
		for(int t = 0 ; t < nbcpu ; t++)
		{
			final int ite = t;
			(new Thread(){
				public void run()
				{
					int start = ite*step;
					int stop = Math.min((ite+1)*step , train.size());
					for(int j = start; j < stop; j++)
					{
						TrainingSample<T> s = train.get(j);
						double r = classifier.valueOf(s.sample);
						Evaluation<TrainingSample<T>> e = new Evaluation<TrainingSample<T>>(s, r);
						synchronized(tsResults)
						{
							tsResults.add(e);
						}
					}
					synchronized(tsResults)
					{
						numThread++;
					}
				}
			}).start();
		}
		while(numThread < nbcpu)
		{
			Thread.yield();
		}
	}
	
	int numThread;
	/**
	 * Computes output values for each of the testing set
	 * @param testingSet
	 */
	private void evaluateTestingSet()
	{
		if(esResults == null)
			esResults = new ArrayList<Evaluation<TrainingSample<T>>>();

		numThread = 0;
		
		final int nbcpu = Runtime.getRuntime().availableProcessors();
		final int step = test.size()/nbcpu + 1;
		for(int t = 0 ; t < nbcpu ; t++)
		{
			final int ite = t;
			(new Thread(){
				public void run()
				{
					int start = ite*step;
					int stop = Math.min((ite+1)*step, test.size());
					for(int j = start; j < stop; j++)
					{
						TrainingSample<T> s = test.get(j);
						double r = classifier.valueOf(s.sample);
						Evaluation<TrainingSample<T>> e = new Evaluation<TrainingSample<T>>(s, r);
						synchronized(esResults)
						{
							esResults.add(e);
						}
					}
					synchronized(esResults)
					{
						numThread++;
					}
				}
			}).start();
		}
		while(numThread < nbcpu)
		{
			Thread.yield();
		}
	}
	
	/**
	 * Computes Mean Average Precision for the training set
	 * @return the MAP
	 */
	public double getTrainingMAP()
	{
		Collections.sort(tsResults);

		int top = 0;
		int i = 1;
		int rec = 0;
		double map = 0.;
		
		for(Evaluation<TrainingSample<T>> e : tsResults)
		{
			if(e.sample.label == 1)
			{
				top++;
				rec++;
				map += top/(double)i;
			}
			i++;
		}
		return map / rec;
	}
	
	/**
	 * computes the Mean average precision for the testing set
	 * @return the MAP
	 */
	public double getTestingMAP()
	{

		Collections.sort(esResults);

		int top = 0;
		int i = 1;
		int rec = 0;
		double map = 0.;
		
		for(Evaluation<TrainingSample<T>> e : esResults)
		{
			if(e.sample.label == 1)
			{
				top++;
				rec++;
				map += top/(double)i;
			}
			i++;
		}
		
		return map / rec;
	}
	
	/**
	 * Computes the precision curve for the training set
	 * @return
	 */
	public double[] getTrainingPrecision()
	{
		ArrayList<Double> precision = new ArrayList<Double>();
		
		Collections.sort(tsResults);
		int top = 0;
		int i = 1;
		for(Evaluation<TrainingSample<T>> e : tsResults)
		{
			if(e.sample.label == 1)
			{
				top++;
				precision.add(top/(double)i);
			}
			i++;
		}
		
		double[] d = new double[precision.size()];
		for(int j = 0 ; j < precision.size(); j++)
			d[j] = precision.get(j);
		
		return d;
	}
	
	/**
	 * Computes the precision curve for the testing set
	 * @return
	 */
	public double[] getTestingPrecision()
	{
		ArrayList<Double> precision = new ArrayList<Double>();
		
		Collections.sort(esResults);
		int top = 0;
		int i = 1;
		for(Evaluation<TrainingSample<T>> e : esResults)
		{
			if(e.sample.label == 1)
			{
				top++;
				precision.add(top/(double)i);
			}
			i++;
		}
		
		double[] d = new double[precision.size()];
		for(int j = 0 ; j < precision.size(); j++)
			d[j] = precision.get(j);
		
		return d;
	}
	
	/**
	 * returns a map of samples and their associated values for the testing set
	 * @return
	 */
	public HashMap<T, Double> getTestingValues()
	{
		HashMap<T, Double> map = new HashMap<T, Double>();
		for(Evaluation<TrainingSample<T>> e : esResults)
			map.put(e.sample.sample, e.value);
		
		return map;
	}
	
	
	/**
	 * Simple class containing a sample and its evaluation by the classifier
	 * @author dpicard
	 *
	 * @param <U>
	 */
	private class Evaluation<U> implements Comparable<Evaluation<U>>, Serializable
	{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 791024170617779718L;
		
		U sample;
		double value;
		
		public Evaluation(U s, double v)
		{
			sample = s;
			value = v;
		}
		

		public int compareTo(Evaluation<U> o) {
			if(o == null)
				return 0;
			return (int) Math.signum(o.value - value);
		}
	}

}
