package JKernelMachines.fr.lip6.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.kernel.SimpleCacheKernel;
import JKernelMachines.fr.lip6.kernel.adaptative.ThreadedSumKernel;
import JKernelMachines.fr.lip6.type.TrainingSample;

public class GradMKL<T> implements Classifier<T> {

	ArrayList<TrainingSample<T>> listOfExamples;
	ArrayList<Double> listOfExampleWeights;
	ArrayList<Kernel<T>> listOfKernels;
	ArrayList<Double> listOfKernelWeights;
	
	SMOSVM<T> svm;
	
	double stopGap = 1e-7;
	double eps_regul = 1e-3;
	double num_cleaning = 1e-7;
	double p_norm = 2;
	double C = 1e5;
	
	
	public GradMKL()
	{
		listOfKernels = new ArrayList<Kernel<T>>();
		listOfKernelWeights = new ArrayList<Double>();
		listOfExamples = new ArrayList<TrainingSample<T>>();
		listOfExampleWeights = new ArrayList<Double>();
	}
	
	public void addKernel(Kernel<T> k)
	{
		listOfKernels.add(k);
		listOfKernelWeights.add(1.0);
	}
	

	public void train(TrainingSample<T> t) {
		// TODO Auto-generated method stub

	}


	public void train(List<TrainingSample<T>> l) {

		long tim = System.currentTimeMillis();
		eprintln(2, "training on "+listOfKernels.size()+" kernels and "+l.size()+" examples");
		
		//1. init kernels
		ArrayList<SimpleCacheKernel<T>> kernels = new ArrayList<SimpleCacheKernel<T>>();
		ArrayList<Double> weights = new ArrayList<Double>();
		
		//normalize to cst trace and init weights to 1/N
		for(int i = 0 ; i < listOfKernels.size(); i++)
		{
			SimpleCacheKernel<T> sck = new SimpleCacheKernel<T>(listOfKernels.get(i), l);
			sck.setName(listOfKernels.get(i).toString());
			double[][] matrix = sck.getKernelMatrix(l);
			//compute trace
			double trace = 0.;
			for(int x = 0 ; x < matrix.length; x++)
			{
				trace += matrix[x][x];
			}
			//divide by trace
			for(int x = 0 ; x < matrix.length; x++)
			for(int y = x ; y < matrix.length; y++)
			{
				matrix[x][y] *= matrix.length/(double)trace;
				matrix[y][x] = matrix[x][y];
			}
			kernels.add(sck);
			weights.add(Math.pow(1/(double)listOfKernels.size(), 1/(double)p_norm));
			eprintln(3, "kernel : "+sck+" weight : "+weights.get(i));
		}
		
		
		//1 train first svm
		ThreadedSumKernel<T> tsk = new ThreadedSumKernel<T>();
		for(int i = 0 ; i < kernels.size(); i++)
			tsk.addKernel(kernels.get(i), weights.get(i));
		svm = new SMOSVM<T>(tsk);
		svm.setC(C);
		svm.setVerbosityLevel(VERBOSITY_LEVEL);
		svm.train(l);
		
		//2. big loop
		double gap = 0;
		do
		{
			eprintln(3, "weights : "+weights);
			//compute sum kernel
			tsk = new ThreadedSumKernel<T>();
			for(int i = 0 ; i < kernels.size(); i++)
				tsk.addKernel(kernels.get(i), weights.get(i));
		
			//train svm
			svm.setKernel(tsk);
			svm.retrain();
			
			//compute sum of example weights and gradient direction
			double suma = computeSumAlpha();
			double [] grad = computeGradBeta(kernels, l);
			
			//perform one step
			double objEvol = performMKLStep(suma, grad, kernels, weights, l);
			
			if(objEvol < 0)
			{
				eprintln(1, "Error, performMKLStep return wrong value");
				System.exit(0);;
			}
			gap = 1 - objEvol;
			
			//compute norm
			double norm = 0;
			for(int i = 0 ; i < weights.size(); i++)
				norm += Math.pow(weights.get(i), p_norm);
			norm = Math.pow(norm, -1/(double)p_norm);
			
			eprintln(1, "objective_gap : "+gap+" norm : "+norm);
			
		}
		while(gap >= stopGap);
		
		//3. save weights
		listOfKernelWeights.clear();
		listOfKernelWeights.addAll(weights);
		
		//4. retrain svm 
		//compute sum kernel
		tsk = new ThreadedSumKernel<T>();
		for(int i = 0 ; i < kernels.size(); i++)
			tsk.addKernel(listOfKernels.get(i), listOfKernelWeights.get(i));
		//train svm
		svm.setKernel(tsk);
		svm.retrain();
		
		//5. save examples wxaeights
		listOfExamples.addAll(l);
		listOfExampleWeights.clear();
		for(double d : svm.getAlphas())
			listOfExampleWeights.add(d);

		eprintln(1, "MKL trained in "+(System.currentTimeMillis()-tim)+" milis.");
	}
	
	private double performMKLStep(double suma, double[] grad, ArrayList<SimpleCacheKernel<T>> kernels, ArrayList<Double> weights, List<TrainingSample<T>> l)
	{
		eprint(2, ".");
		//compute objective function
		double oldObjective = +suma;
		for(int i = 0 ; i < grad.length; i++)
		{
			oldObjective -= weights.get(i)*grad[i]; 
		}
		eprintln(3, "oldObjective : "+oldObjective+" sumAlpha : "+suma);
		
		//compute optimal step
		double newBeta[] = new double[grad.length];
		
		for(int i = 0 ; i < grad.length; i++)
		{
			if(grad[i] >= 0 && weights.get(i) >= 0)
			{
				newBeta[i] = grad[i] * weights.get(i)*weights.get(i) / p_norm;
				newBeta[i] = Math.pow(newBeta[i], 1 / ((double) 1 + p_norm));
			}
			else
				newBeta[i] = 0;
		}
		
		//normalize
		double norm = 0;
		for(int i = 0 ; i < newBeta.length; i++)
			norm += Math.pow(newBeta[i], p_norm);
		norm = Math.pow(norm, -1/(double)p_norm);
		if(norm < 0)
		{
			eprintln(1, "Error normalization, norm < 0");
			return -1;
		}
		for(int i = 0 ; i < newBeta.length; i++)
			newBeta[i] *= norm;
		
		//regularize and renormalize
		double R = 0;
		for (int i = 0 ; i < kernels.size(); i++)
			R += Math.pow(weights.get(i)-newBeta[i], 2);
		R = Math.sqrt(R/(double)p_norm) * eps_regul;
		if(R < 0)
		{
			eprintln(1, "Error regularization, R < 0");
			return -1;
		}
		norm = 0;
		for(int i = 0 ; i < kernels.size(); i++)
		{
			newBeta[i] += R;
			if(newBeta[i] < num_cleaning)
				newBeta[i] = 0;
			norm += Math.pow(newBeta[i], p_norm);
		}
		norm = Math.pow(norm, -1/(double)p_norm);
		if(norm < 0)
		{
			eprintln(1, "Error normalization, norm < 0");
			return -1;
		}
		for(int i = 0 ; i < newBeta.length; i++)
			newBeta[i] *= norm;
		
		//store new weights
		for(int i = 0 ; i < weights.size(); i++)
			weights.set(i, newBeta[i]);
		
		//compute objective function
		double objective = +suma;
		for(int i = 0 ; i < grad.length; i++)
		{
			objective -= weights.get(i)*grad[i]; 
		}
		eprintln(3, "objective : "+objective+" sumAlpha : "+suma);
		
		//return objective evolution
		return objective/oldObjective;
	}
			
	
	/** calcul du gradient en chaque beta */
	private double [] computeGradBeta(ArrayList<SimpleCacheKernel<T>> kernels, List<TrainingSample<T>> l)
	{
		double grad[] = new double[kernels.size()];
		
		for(int i = 0 ; i < kernels.size(); i++)
		{
			double matrix[][] = kernels.get(i).getKernelMatrix(l);
			double a[] = svm.getAlphas();
			
			for(int x = 0 ; x < matrix.length; x++)
			{
				int l1 = l.get(x).label;
				for(int y = 0 ; y < matrix.length; y++)
				{
					int l2 = l.get(y).label;
					grad[i] += 0.5 * l1 * l2 * a[x] * a[y] * matrix[x][y];
				}
			}
		}
		
		eprint(3, "gradDir : "+Arrays.toString(grad));
		
		return grad;
	}
	
	/** compute the sum of examples weights */
	private double computeSumAlpha()
	{
		double sum = 0;
		double[] a = svm.getAlphas();
		for(double d : a)
			sum += Math.abs(d);
		return sum;
	}


	public double valueOf(T e) {
		
		return svm.valueOf(e);
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

	public void setMKLNorm(double p)
	{
		p_norm = p;
	}
	
	public void setStopGap(double w)
	{
		stopGap = w;
	}
	
	public ArrayList<Double> getExampleWeights() {
		return listOfExampleWeights;
	}
	
	public ArrayList<Double> getKernelWeights()
	{
		return listOfKernelWeights;
	}
	
	public Hashtable<Kernel<T>, Double> getWeights()
	{
		Hashtable<Kernel<T>, Double> map = new Hashtable<Kernel<T>, Double>();
		for(int i = 0 ; i < listOfKernels.size(); i++)
			map.put(listOfKernels.get(i), listOfKernelWeights.get(i));
		return map;
	}

}
