package JKernelMachines.fr.lip6.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import JKernelMachines.fr.lip6.kernel.typed.DoubleLinear;
import JKernelMachines.fr.lip6.type.TrainingSample;

public class DoublePegasosSVM implements Classifier<double[]>, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5289136605543751554L;
	
	private DoubleLinear kernel = new DoubleLinear();
	private List<TrainingSample<double[]>> tList;
	private double[] w;
	private double b = 0;
	
	
	int T = 100000;
	int k = 10;
	double lambda = 1e-3;
	double t0 = 1.e2;
	boolean bias = true;
	
	double C = 1;
	boolean hasC = false;
	

	
	
	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(java.util.ArrayList)
	 */
	public void train(List<TrainingSample<double[]>> l) {
		
		Random ran = new Random(System.currentTimeMillis());
		
		//hard limit for k
		if(k > l.size())
			k = l.size();
		
		tList = l;
		int taille = tList.get(0).sample.length; 
		//reset w
		w = new double[taille];
		for(int m = 0 ; m < taille; m++)
			w[m] = 0.;
		
		//reset bias
		b = 0;
		
		//check if C
		if(hasC)
			lambda = 1.0 / (C * tList.size());
		
		eprintln(1, "begin training");
		long time = System.currentTimeMillis();
		for(int i = 0; i< T; i++)
		{
			//sub sample selection
			ArrayList<Integer> subSampleIndices = new ArrayList<Integer>();
			for( ;subSampleIndices.size()< k;)
			{
				int e = ran.nextInt(tList.size());
				if(!subSampleIndices.contains(e))
				{
					subSampleIndices.add(e);
				}
			}
			//remove y(<w,x>-b) >= 1
			for(Iterator<Integer> iter = subSampleIndices.iterator(); iter.hasNext(); )
			{
				Integer index = iter.next();
				double[] d = tList.get(index).sample;
				int y = tList.get(index).label;
				if((kernel.valueOf(w, d) - b)*y > 1)
					iter.remove();
			}
			
			//choosing step
			double eta = 1/(double)(lambda*(i+t0));
			
			//calculate half step coefficient;
			double[] w_halfstep = new double[w.length]; 
			for(int m = 0 ; m < taille; m++)
			{
				w_halfstep[m] = (1-eta*lambda)*w[m];
			}
			double dir = 0;
			for(Iterator<Integer> iter =  subSampleIndices.iterator(); iter.hasNext(); )
			{
				Integer index = iter.next();
				TrainingSample<double[]> t = tList.get(index);
				for(int m = 0 ; m < taille; m++)
				{
					if(t.sample[m] != 0)
					{
						dir = t.label*t.sample[m];
						w_halfstep[m] += eta/(double)(k)*dir;
					}
				}

			}
			
			//b
			double b_new = 0;
			if(bias)
				for(int index : subSampleIndices)
				{
					b_new += tList.get(index).label;

				}
			
			//final step
			
			double norm = Math.sqrt(kernel.valueOf(w_halfstep, w_halfstep));
			double min = 1/Math.sqrt(lambda)/norm;
			if(min > 1)
				min = 1;
			
			double[] w_fullstep = w_halfstep.clone();
			for(int m = 0 ; m < taille; m++)
				w_fullstep[m] = w_halfstep[m] * min;
			

			
			w = w_fullstep;
			if(bias)
				b = min*( (1-eta*lambda)*b - eta/(double)k*b_new);
			else
				b = 0;

			eprintln(4, "w : "+Arrays.toString(w)+" b : "+b);
			if(T>20 && i%(T/20) == 0)
				eprint(2, ".");
			
		}
		eprintln(2, "");
	
		
		eprintln(1, "done in "+(System.currentTimeMillis()-time)+" ms");
		eprintln(3, "w : "+Arrays.toString(w)+" b : "+b);
	}

	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(fr.lip6.type.TrainingSample)
	 */
	public void train(TrainingSample<double[]> t) {
		if(tList == null)
			tList = new ArrayList<TrainingSample<double[]>>();
		
		tList.add(t);
		
		train(tList);
		
	}

	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#valueOf(java.lang.Object)
	 */

	public double valueOf(double[] e) {
		return kernel.valueOf(w, e)-b;
	}
	



	/**
	 * @return the t
	 */
	public int getT() {
		return T;
	}


	/**
	 * @param t the t to set
	 */
	public void setT(int t) {
		T = t;
	}


	/**
	 * @return the k
	 */
	public int getK() {
		return k;
	}


	/**
	 * @param k the k to set
	 */
	public void setK(int k) {
		this.k = k;
	}


	/**
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}


	/**
	 * @param lambda the lambda to set
	 */
	public void setLambda(double lambda) {
		this.lambda = lambda;
	}

	/**
	 * @return the hyperplane coordinates
	 */
	public double[] getW() {
		return w;
	}

	/**
	 * Setting the hyperplane coordinates
	 * @param w the w to set
	 */
	public void setW(double[] w) {
		this.w = w;
	}

	/**
	 * the bias b of (w*x -b)
	 * @return the bias
	 */
	public double getB() {
		return b;
	}

	/**
	 * Setting the bias term
	 * @param b the b to set
	 */
	public void setB(double b) {
		this.b = b;
	}



	public boolean isBias() {
		return bias;
	}

	public void setBias(boolean bias) {
		this.bias = bias;
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

	public double getT0() {
		return t0;
	}

	public void setT0(double t0) {
		this.t0 = t0;
	}

	/**
	 * Set C hyperparameter
	 * @param c
	 */
	public void setC(double c)
	{
		hasC = true;
		C = c;
	}
}
