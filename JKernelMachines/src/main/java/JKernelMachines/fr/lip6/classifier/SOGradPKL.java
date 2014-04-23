package JKernelMachines.fr.lip6.classifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.kernel.SimpleCacheKernel;
import JKernelMachines.fr.lip6.kernel.adaptative.ThreadedProductKernel;
import JKernelMachines.fr.lip6.threading.ThreadedMatrixOperator;
import JKernelMachines.fr.lip6.type.TrainingSample;

public class SOGradPKL<T> implements Classifier<T> {

	List<TrainingSample<T>> listOfExamples;
	List<Double> listOfExampleWeights;
	List<Kernel<T>> listOfKernels;
	List<Double> listOfKernelWeights;
	
	SMOSVM<T> svm;
	
	double stopGap = 1e-5;
	double num_cleaning = 1e-8;
	double p_norm = 1;
	
	double C = 1e0;
	boolean traceNorm = false;
	
	double d_lambda;
	double [][] lambda_matrix = null;
	double oldObjective;
	
	boolean cache = true;

	public SOGradPKL()
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
		if(listOfExamples == null)
			listOfExamples = new ArrayList<TrainingSample<T>>();
		if(!listOfExamples.contains(t))
			listOfExamples.add(t);
		train(listOfExamples);
	}


	public void train(List<TrainingSample<T>> l) {

		long tim = System.currentTimeMillis();
		eprintln(2, "training on "+listOfKernels.size()+" kernels and "+l.size()+" examples");
		
		//1. init kernels
		ArrayList<Kernel<T>> kernels = new ArrayList<Kernel<T>>();
		ArrayList<Double> weights = new ArrayList<Double>();
		
		//normalize to cst trace and init weights to 1/N
		for(int i = 0 ; i < listOfKernels.size(); i++)
		{
			if(cache)
			{
				eprintln(3, "+ cache is set, computing cache");
				SimpleCacheKernel<T> sck = new SimpleCacheKernel<T>(listOfKernels.get(i), l);
				sck.setName(listOfKernels.get(i).toString());
				double[][] matrix = sck.getKernelMatrix(l);
				if(traceNorm)
				{
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
				}
				kernels.add(sck);
			}
			else
			{
				eprintln(3, "+ cache is not set, skipping cache");
				kernels.add(listOfKernels.get(i));
			}
			weights.add(Math.pow(1/(double)listOfKernels.size(), 1/(double)p_norm));
			eprintln(3, "+ kernel : "+kernels.get(i)+" weight : "+weights.get(i));
		}
		
		
		//1 train first svm
		ThreadedProductKernel<T> tpk = new ThreadedProductKernel<T>();
		for(int i = 0 ; i < kernels.size(); i++)
			tpk.addKernel(kernels.get(i), weights.get(i));
		svm = new SMOSVM<T>(tpk);
		svm.setC(C);
		svm.setVerbosityLevel(VERBOSITY_LEVEL-1);
		eprintln(3, "+ training svm");
		svm.train(l);
		double[] a = svm.getAlphas();
		//update lambda matrix
		updateLambdaMatrix(a, tpk, l);
		//compute old value of objective function
		oldObjective = computeObj(a, tpk, l);
		
		eprintln(3, "+ initial weights : "+weights);
		
		//2. big loop
		double gap = 0;
		do
		{						
			//perform one step
			double objEvol = performPKLStep(kernels, weights, l);
			
			if(objEvol < 0)
			{
				eprintln(1, "Error, performPKLStep return wrong value");
				System.exit(0);;
			}
			gap = 1 - objEvol;
			
			eprintln(1, "+ objective_gap : "+(float)gap);
			eprintln(1, "+");
			
		}
		while(gap >= stopGap);
		
		//3. save weights
		listOfKernelWeights.clear();
		listOfKernelWeights.addAll(weights);
		
		//4. retrain svm 
		//compute sum kernel
		tpk = new ThreadedProductKernel<T>();
		for(int i = 0 ; i < kernels.size(); i++)
			tpk.addKernel(listOfKernels.get(i), listOfKernelWeights.get(i));
		//train svm
		svm.setKernel(tpk);
		eprintln(3, "+ retraining svm");
		svm.retrain();
		
		//5. save examples weights
		listOfExamples.addAll(l);
		listOfExampleWeights.clear();
		for(double d : svm.getAlphas())
			listOfExampleWeights.add(d);

		eprintln(1, "PKL trained in "+(System.currentTimeMillis()-tim)+" milis.");
	}
	
	/**
	 * perform one approximate second order gradient descent step
	 * @param kernels
	 * @param weights
	 * @param l
	 * @return
	 */
	private double performPKLStep(ArrayList<Kernel<T>> kernels, ArrayList<Double> weights, List<TrainingSample<T>> l)
	{

		//store new as old for the loop
		double objective = oldObjective;
		
		eprintln(3, "+++ old weights : "+weights);
		eprintln(3, "+++ oldObjective : "+oldObjective+" sumAlpha : "+computeSumAlpha());
		
		
		//compute grad
		double [] grad = gradBeta(kernels, weights, l);
		double [] sgrad = secondGradBeta(kernels, weights, l);
		
		
		double newBeta[] = new double[grad.length];

		//update weights in this direction until objective decreases
		d_lambda = 1.0; //reset learning rate
		do
		{
			for(int i = 0 ; i < grad.length; i++)
			{
				//second order update
				if(sgrad[i] != 0.)
					newBeta[i] = weights.get(i) * (1 - d_lambda*grad[i]/sgrad[i]);
				if(newBeta[i] < num_cleaning)
					newBeta[i] = 0;

			}

			//normalize
			double norm = 0;
			if(p_norm == 1) // L1-norm
			{
				for(int i = 0 ; i < newBeta.length; i++)
					norm += Math.abs(newBeta[i]);
			}
			else //Lp-norm
			{
				for(int i = 0 ; i < newBeta.length; i++)
					norm += Math.pow(newBeta[i], p_norm);
				norm = Math.pow(norm, -1/(double)p_norm);
			}
			if(norm < 0)
			{
				eprintln(1, "Error normalization, norm < 0");
				return -1;
			}
			eprintln(3, "+++ norm : "+norm);
			//normalize
			for(int i = 0 ; i < newBeta.length; i++)
				newBeta[i] /= (double)(norm);
			


			//compute new objective function
			ThreadedProductKernel<T> pk = new ThreadedProductKernel<T>();
			for( int i = 0 ; i < kernels.size(); i++)
				pk.addKernel(kernels.get(i), newBeta[i]);
			//train svm
			svm.setKernel(pk);
			eprintln(3, "+ retraining svm");
			svm.retrain();
			double[] a = svm.getAlphas();
			//update lambda
			updateLambdaMatrix(a, pk, l);
			//new objective
			objective = computeObj(a, pk, l);

			if(objective < oldObjective + num_cleaning) // did the objective at least stay similar
			{
				//store new weights
				for(int i = 0 ; i < weights.size(); i++)
					weights.set(i, newBeta[i]);
				eprintln(3, "+++ new weights : "+weights);
			}
			else //if not, reduce learning rate exponentially
			{
				if(d_lambda > num_cleaning)
					d_lambda /= 8.;
				else
				{
					d_lambda = 0.;
					eprint(3, "+++ d_lambda is zero, stopping.");
					eprintln(2, "");
					break;
				}
				eprint(2, "+");
				eprintln(3, "++ new objective ("+(float)objective+") did not decrease ("+(float)oldObjective+"), reducing step : "+d_lambda);
			}
			
		}
		while(oldObjective + num_cleaning < objective);
		
		eprintln(2, "+ objective : "+(float)objective+"\t+\t sumAlpha : "+(float)computeSumAlpha());

		double gap = objective/oldObjective;
		
		//store objective as oldObjective
		oldObjective = objective;

		//return objective evolution
		return gap;
	}
			
	
	/** calcul du gradient en chaque beta */
	private double [] gradBeta(ArrayList<Kernel<T>> kernels, ArrayList<Double> weights, List<TrainingSample<T>> l)
	{
		double grad[] = new double[kernels.size()];

		//doing <L, Dn>
		for(int i = 0 ; i < kernels.size(); i++)
		{
			double matrix[][] = kernels.get(i).getKernelMatrix(l);
			for(int x = 0 ; x < matrix.length; x++)
			{
				for(int y = x ; y < matrix.length; y++)
				{
					if(matrix[x][y] == 0)
						continue;
					grad[i] += - Math.log(matrix[x][y]) * lambda_matrix[x][y];
				}
			}
		}
		
		eprintln(4, "++++++ gradDir : "+Arrays.toString(grad));
		
		return grad;
	}
	
		
	
	/** calcul du gradient second en chaque beta */
	private double [] secondGradBeta(ArrayList<Kernel<T>> kernels, ArrayList<Double> weights, List<TrainingSample<T>> l)
	{
		double grad[] = new double[kernels.size()];
		
		//doing <L, Dn.^2>
		for(int i = 0 ; i < kernels.size(); i++)
		{
			double matrix[][] = kernels.get(i).getKernelMatrix(l);
			for(int x = 0 ; x < matrix.length; x++)
			{
				for(int y = x ; y < matrix.length; y++)
				{
					if(matrix[x][y] == 0)
						continue;
					double d = Math.log(matrix[x][y]);
					grad[i] += d * d * lambda_matrix[x][y];
				}
			}
		}
		
		for(int i = 0 ; i < grad.length; i++)
			if(grad[i] < num_cleaning)
				grad[i] = 0.0;
		
		eprintln(4, "++++++ secondGradDir : "+Arrays.toString(grad));
		
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
	
	/** compute obj */
	private double computeObj(double[] a, Kernel<T> kernel, List<TrainingSample<T>> l)
	{
		double obj = 0;
		
		//sum of alpha
		for(double aa : a)
			obj += aa;
		
		
		for(int x = 0 ; x < lambda_matrix.length; x++)
		{
			for(int y = x ; y < lambda_matrix.length; y++)
			{
				if(lambda_matrix[x][y] == 0)
					continue;
				if(x != y)
					obj += 2*lambda_matrix[x][y];
				else
					obj += lambda_matrix[x][y];
			}
		}
		return obj;
	}
	
	/** compute the lambda matrix */
	private void updateLambdaMatrix(final double[] a, Kernel<T> kernel, final List<TrainingSample<T>> l)
	{
		final double [][] matrix = kernel.getKernelMatrix(l);
		lambda_matrix = new double[matrix.length][matrix.length];
		
//		for(int x = 0 ; x < matrix.length; x++)
//		{
//			int l1 = l.get(x).label;
//			for(int y = x ; y < matrix.length; y++)
//			{
//				if(matrix[x][y] == 0)
//					continue;
//				int l2 = l.get(y).label;
//				lambda_matrix[x][y] = -0.5 * l1 * l2 * a[x] * a[y] * matrix[x][y];
//				lambda_matrix[y][x] = lambda_matrix[x][y];
//			}
//		}
//		
		eprintln(3, "+ update lambda");
		ThreadedMatrixOperator factory = new ThreadedMatrixOperator()
		{
			@Override
			public void doLine(int index, double[] line) {
				int l1 = l.get(index).label;
				double al1 = -0.5 * a[index]*l1;
				for(int j = line.length-1 ; j != 0 ; j--)
				{
					int l2 = l.get(j).label;
					line[j] = al1 * l2 * a[j] * matrix[index][j];
				}
			}

			
		};
		
		lambda_matrix = factory.getMatrix(lambda_matrix);
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
	
	public boolean isCache() {
		return cache;
	}

	public void setCache(boolean cache) {
		this.cache = cache;
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
	
	public void setTraceNorm(boolean traceNorm) {
		this.traceNorm = traceNorm;
	}

	public double getNum_cleaning() {
		return num_cleaning;
	}

	public void setNum_cleaning(double num_cleaning) {
		this.num_cleaning = num_cleaning;
	}

	public List<Double> getExampleWeights() {
		return listOfExampleWeights;
	}
	
	public List<Double> getKernelWeights()
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
