package JKernelMachines.fr.lip6.classifier.transductive;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import JKernelMachines.fr.lip6.classifier.DoubleSGDQN;
import JKernelMachines.fr.lip6.type.TrainingSample;

public class S3VMLightSGDQN implements TransductiveClassifier<double[]> {

	
	int numplus = 0;
	
	ArrayList<TrainingSample<double[]>> train;
	ArrayList<TrainingSample<double[]>> test;
	
	DoubleSGDQN svm;
	double C = 1.0;
	double E = 10;
	
	public S3VMLightSGDQN()
	{
	}
	
	public void train(List<TrainingSample<double[]>> trainList,
			List<TrainingSample<double[]>> testList) {
	
		train = new ArrayList<TrainingSample<double[]>>();
		train.addAll(trainList);
		
		test = new ArrayList<TrainingSample<double[]>>();
		//copy test samples
		for(TrainingSample<double[]> tm : testList)
		{
			TrainingSample<double[]> t = new TrainingSample<double[]>(tm.sample, 0);
			test.add(t);
		}
		
		train();

	}

	private void train()
	{
		eprintln(2, "training on "+train.size()+" train data and "+test.size()+" test data");
		
		//first training
		eprint(3, "first training ");
		svm = new DoubleSGDQN();
		DoubleSGDQN.VERBOSE = false;
		svm.train(train);
		eprintln(3, " done.");
		
		//affect numplus highest output to plus class
		eprintln(3, "affecting 1 to the "+numplus+" highest output");
		SortedSet<TrainingSample<double[]>> sorted = new TreeSet<TrainingSample<double[]>>(new Comparator<TrainingSample<double[]>>(){

			public int compare(TrainingSample<double[]> o1, TrainingSample<double[]> o2) {
				int ret = (new Double(svm.valueOf(o2.sample))).compareTo(svm.valueOf(o1.sample));
				if(ret == 0)
					ret = -1;
				return ret;
			}
			
		});
		sorted.addAll(test);
		eprintln(4, "sorted size : "+sorted.size()+" test size : "+test.size());
		int n = 0;
		for(TrainingSample<double[]> t : sorted)
		{
			if(n < numplus)
				t.label = 1;
			else
				t.label = -1;
			n++;
		}
		
		double Cminus = 1e-5;
		double Cplus = 1e-5 * numplus/(test.size() - numplus);
		
		while(Cminus < C || Cplus < C)
		{
			//solve full problem
			ArrayList<TrainingSample<double[]>> full = new ArrayList<TrainingSample<double[]>>();
			full.addAll(train);
			full.addAll(test);
			
			eprint(3, "full training ");
			svm = new DoubleSGDQN();
			svm.setC((Cminus+Cplus)/2.);
			svm.train(full);
			eprintln(3, "done.");
			
			boolean changed = false;
			
			do
			{
				changed = false;
				//0. computing error
				final Map<TrainingSample<double[]>, Double> errorCache = new HashMap<TrainingSample<double[]>, Double>();
				for(TrainingSample<double[]> t : test)
				{
					double err1 = 1. - t.label * svm.valueOf(t.sample);
					errorCache.put(t, err1);
				}
				eprintln(3, "Error cache done.");
				
				// 1 . sort by descending error
				sorted = new TreeSet<TrainingSample<double[]>>(new Comparator<TrainingSample<double[]>>(){

					public int compare(TrainingSample<double[]> o1,
							TrainingSample<double[]> o2) {
						int ret = errorCache.get(o2).compareTo(errorCache.get(o1));
						if(ret == 0)
							ret = -1;
						return ret;
					}
				});
				sorted.addAll(test);
				List<TrainingSample<double[]>> sortedList = new ArrayList<TrainingSample<double[]>>();
				sortedList.addAll(sorted);
				
				
				eprintln(3, "sorting done, checking couple");
				
				// 2 . test all couple by decreasing error order
//				for(TrainingSample<T> i1 : sorted)
				for(int i = 0 ; i < sortedList.size(); i++)
				{
					TrainingSample<double[]> i1 = sortedList.get(i);
//					for(TrainingSample<T> i2 : sorted)
					for(int j = i+1; j < sortedList.size(); j++)
					{
						TrainingSample<double[]> i2 = sortedList.get(j);
						if(examine(i1, i2, errorCache))
						{
							eprintln(3, "couple found !");
							changed = true;
							break;
						}
					}
					if(changed)
						break;
				}

				if(changed)
				{
					eprintln(3, "re-training");
					svm = new DoubleSGDQN();
					svm.setC((Cminus+Cplus)/2.);
					svm.train(full);
				}
			}
			while(changed);

			eprintln(3, "increasing C+ : "+Cplus+" and C- : "+Cminus);
			Cminus = Math.min(2*Cminus, C);
			Cplus = Math.min(2 * Cplus, C);
		}
		
		eprintln(2, "training done");
	}
	

	//check if the pair of example fulfill the swapping conditions
	private boolean examine(TrainingSample<double[]> i1, TrainingSample<double[]> i2, Map<TrainingSample<double[]>, Double> errorCache)
	{
		if(i1.label * i2.label > 0)
			return false;
		
		if(!errorCache.containsKey(i1))
			return false;
		double err1 = errorCache.get(i1);	
		if(err1 <= 0)
			return false;
		
		if(!errorCache.containsKey(i2))
			return false;
		double err2 = errorCache.get(i2);
		if(err2 <= 0)
			return false;
		
		eprintln(4, "y1 : "+i1.label+" err1 : "+err1+" y2 : "+i2.label+" err2 : "+err2);
		if(err1 + err2 <= 2)
			return false;
		
		//found a good couple
		int tmplabel = i1.label;
		i1.label = i2.label;
		i2.label = tmplabel;
		
		return true;
	}
	
	
	public double valueOf(double[] t) {
		return svm.valueOf(t);
	}


	public int getNumplus() {
		return numplus;
	}

	public void setNumplus(int numplus) {
		this.numplus = numplus;
	}

	private int VERBOSITY_LEVEL = 0;
	
	/**
	 * set how verbose SimpleMKL shall be. <br />
	 * Everything is printed to stderr. <br />
	 * none : 0 (default), few  : 1, more : 2, all : 3
	 * @param l
	 */
	public void setVerbosityLevel(int l)
	{
		VERBOSITY_LEVEL = l;
	}
	
	public void eprint(int level, String s)
	{
		if(VERBOSITY_LEVEL >= level)
			System.err.print(s);
	}
	
	public void eprintln(int level, String s)
	{
		if(VERBOSITY_LEVEL >= level)
			System.err.println(s);
	}

	public double getC() {
		return C;
	}

	public void setC(double c) {
		C = c;
	}

	public double getE() {
		return E;
	}

	public void setE(double e) {
		E = e;
	}

	
}
