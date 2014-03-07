package JKernelMachines.fr.lip6.classifier;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.kernel.SimpleCacheKernel;
import JKernelMachines.fr.lip6.kernel.adaptative.ThreadedSumKernel;
import JKernelMachines.fr.lip6.threading.ThreadedMatrixOperator;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Implementation of the SimpleMKL solver by A. Rakotomamonjy
 * Java conversion of the original matlab code.
 * 
 * @author dpicard
 *
 * @param <T>
 */
public class SimpleMKL<T> implements Classifier<T> {
	
	private ArrayList<Kernel<T>> kernels;
	private ArrayList<Double> kernelWeights;
	
	private int maxIteration = 2000;
	private double C = 1.e5;
	private double numPrec = 1.e-8, epsKTT = 0.1, epsDG = 0.01, epsGS = 1.e-10, eps = 1.e-12;
	private boolean checkDualGap = true, checkKTT = false;
	
	private SMOSVM<T> svm;
	
	private DecimalFormat format = new DecimalFormat("#0.0000");

	public SimpleMKL()
	{
		kernels = new ArrayList<Kernel<T>>();
		kernelWeights = new ArrayList<Double>();
	}
	
	/**
	 * adds a kernel to the MKL problem
	 * @param k
	 */
	public void addKernel(Kernel<T> k)
	{
		if(!kernels.contains(k))
		{
			kernels.add(k);
			kernelWeights.add(1.0);
		}
	}
	

	public void train(TrainingSample<T> t) {
		// TODO Auto-generated method stub
		
	}


	public void train(List<TrainingSample<T>> l) {
		
		//caching matrices
		ArrayList<SimpleCacheKernel<T>> km = new ArrayList<SimpleCacheKernel<T>>();
		ArrayList<Double> dm = new ArrayList<Double>();
		double dm0 = 1./kernels.size();
		for(int i = 0 ; i < kernels.size(); i++)
		{
			Kernel<T> k = kernels.get(i);
			SimpleCacheKernel<T> csk = new SimpleCacheKernel<T>(k, l);
			csk.setName(k.toString());
			//normalize to constant trace
			double[][] matrix = csk.getKernelMatrix(l);
			double trace = 0;
			for(int j = 0 ; j < matrix.length; j++)
				trace += matrix[j][j];
			trace /= l.size();
			for(int m = 0; m < matrix.length; m++)
			for(int n = m; n < matrix.length; n++)
			{
				matrix[m][n] /= trace;
				matrix[n][m] = matrix[m][n];
			}
			km.add(csk);
			dm.add(dm0);
		}
		
		//------------------------------
		//			INIT
		//------------------------------
		//creating kernel
		ThreadedSumKernel<T> tsk = buildKernel(km, dm);
		retrainSVM(tsk, l);
		double oldObj = svmObj(km, dm, l);
		ArrayList<Double> grad = gradSVM(km, dm, l);
		eprintln(1, "iter \t | \t obj \t\t | \t dualgap \t | \t KKT");
		eprintln(1, "init \t | \t "+format.format(oldObj)+" \t | \t "+Double.NaN+" \t\t | \t "+Double.NaN);
		
		//------------------------------
		//			START
		//------------------------------
		boolean loop = true;
		int iteration = 1;
		while(loop && iteration < maxIteration)
		{
			
			//------------------------------
			//		UPDATE WEIGHTS
			//------------------------------
			double newObj = mklUpdate(grad, km, dm, l);
			
			//------------------------------
			//		numerical cleaning
			//------------------------------
			double sum = 0;
			for(int i = 0 ; i < dm.size(); i++)
			{
				double d = dm.get(i);
				if(d < numPrec)
					d = 0.;
				sum += d;
				dm.set(i, d);
			}
			for(int i = 0 ; i < dm.size(); i++)
			{
				double d = dm.get(i);
				dm.set(i, d/sum);
			}
			//verbosity
			eprintln(3, "loop : dm after cleaning = "+dm);
			
			//------------------------------
			//	approximate KTT condition
			//------------------------------
			grad = gradSVM(km, dm, l);
			eprintln(3, "loop : grad = "+grad);
			
			//searching min and max grad for non nul dm
			double gMin = Double.POSITIVE_INFINITY, gMax = Double.NEGATIVE_INFINITY;
			for(int i = 0 ; i < dm.size(); i++)
			{
				double d = dm.get(i);
				if(d > numPrec)
				{
					double g = grad.get(i);
					if(g <= gMin)
						gMin = g;
					if(g >= gMax)
						gMax = g;
				}
			}
			double kttConstraint = Math.abs((gMin-gMax)/gMin);
			eprintln(3, "Condition check : KTT gmin = "+gMin+" gmax = "+gMax);
			//searching grad min over zeros dm
			double gZeroMin = Double.POSITIVE_INFINITY;
			for(int i = 0 ; i < km.size(); i++)
			{
				if(dm.get(i) < numPrec)
				{
					double g = grad.get(i);
					if(g < gZeroMin)
						gZeroMin = g;
				}
			}
			boolean kttZero = (gZeroMin > gMax)?true:false;
			
			//-------------------------------
			//		Duality gap
			//-------------------------------
			
			//searching -grad max
			double max = Double.MIN_VALUE;
			for(int i = 0 ; i < dm.size(); i++)
			{
				double g = -grad.get(i);
				if(g > max)
					max = g;
			}
			//computing sum alpha
			sum = 0.;
			double alp[] = svm.getAlphas();
			for(int i = 0 ; i < alp.length; i++)
				sum += Math.abs(alp[i]);
			double dualGap = (newObj + max - sum)/newObj;
			
			
			
			//--------------------------------
			//		Verbosity
			//--------------------------------
			
			eprintln(1, "iter \t | \t obj \t\t | \t dualgap \t | \t KKT");
			eprintln(1, iteration+" \t | \t "+format.format(newObj)+" \t | \t "+format.format(dualGap)+" \t | \t "+format.format(kttConstraint));
			
			
			
			//------------------------------
			//	STOP CRITERIA
			//------------------------------
			boolean stop = false;
			//ktt
			if(kttConstraint < epsKTT && kttZero && checkKTT)
			{
				eprintln(1, "KTT conditions met, possible stoping");
				stop = true;
			}
			//dualgap
			if(dualGap < epsDG && checkDualGap)
			{
				eprintln(1, "DualGap reached, possible stoping");
				stop = true;
			}
			//stagnant gradient
			if(Math.abs(oldObj - newObj) < numPrec)
			{
				eprintln(1, "No improvement during iteration, stoping (old : "+oldObj+" new : "+newObj+")");
				stop = true;
			}
			if(stop)
				loop = false;
			
			//storing newObj
			oldObj = newObj;
			
			iteration++;
		}
		
		kernelWeights = dm;
		//creating kernel
		tsk = buildKernel(km, dm);
		retrainSVM(tsk, l);
		
	}
	
	/**
	 * computing the objective function value
	 * @param km
	 * @param dm
	 * @param l
	 * @return
	 */
	private double svmObj(List<SimpleCacheKernel<T>> km,
			List<Double> dm, final List<TrainingSample<T>> l) {

		eprint(3, "[");
		//creating kernel
		ThreadedSumKernel<T> k = buildKernel(km, dm);
		SimpleCacheKernel<T> csk = new SimpleCacheKernel<T>(k, l);
		final double kmatrix[][] = csk.getKernelMatrix(l);

		
		eprint(3, "-");
		//updating svm
		retrainSVM(csk, l);
		final double alp[] = svm.getAlphas();
		eprint(3, "-");
		
		//verbosity
		eprintln(4, "svmObj : alphas = "+Arrays.toString(alp));
		eprintln(4, "svmObj : b="+svm.getB());
		
//		//computing obj
//		double obj1 = 0;
//		for(int x = 0 ; x < l.size(); x++)
//		{
//			double alx = alp[x]*l.get(x).label;
//			if(Math.abs(alx) > numPrec)
//				for(int y = x ; y < l.size(); y++)
//				{
//					if(alp[y] > numPrec && kmatrix[x][y] > numPrec)
//					{
//						double o = -alx*alp[y]*l.get(y).label*kmatrix[x][y];
//						if(x == y)
//							o *= 0.5;
//						obj1 += o;
//					}
//				}
//		}
		
		//parallelized
		final double[] resLine = new double[kmatrix.length];
		ThreadedMatrixOperator objFactory = new ThreadedMatrixOperator()
		{
			@Override
			public void doLine(int index, double[] line) {
				if(alp[index] < numPrec)
					return;
				double al1 = -0.5 * alp[index] * l.get(index).label;
				for(int j = line.length-1 ; j != 0 ; j--)
				{
					resLine[index] += al1 * alp[j] * l.get(j).label * kmatrix[index][j];
				}
				
			}	
		};
		
		objFactory.getMatrix(kmatrix);
		double obj1 = 0;
		for(double d : resLine)
			obj1 += d;
		
		double obj2 = 0;
		for(int i = 0; i < l.size(); i++)
			if(alp[i] > numPrec)
				obj2 += alp[i];
		
		double obj = obj1+obj2;
		
		eprint(3, "]");
		
		if(obj < 0)
		{
			eprintln(1, "A fatal error occured, please report to picard@ensea.fr");
			eprintln(1, "error obj : "+obj+" obj1:"+obj1+" obj2:"+obj2);
			eprintln(1, "alp : "+Arrays.toString(alp));
			eprintln(1, "kmatrix < numPrec");
			double max = 0;
			for(int i = 0 ; i < kmatrix.length; i++)
			for(int j = 0 ; j < kmatrix.length; j++)
			{
//				if(kmatrix[i][j] < numPrec)
//					eprintln(3, i+":"+j+" kmatrix:"+kmatrix[i][j]);
				if(kmatrix[i][j] > max)
					max = kmatrix[i][j];
			}
			eprintln(1, "kMatrix max : "+max);
			
			//computing obj
			obj1 = 0;
			for(int x = 0 ; x < l.size(); x++)
			for(int y = 0 ; y < l.size(); y++)
			{
				if( alp[x] > numPrec && alp[y] > numPrec && kmatrix[x][y] > numPrec)
				{
					double o = -0.5*alp[x]*alp[y]*l.get(x).label*l.get(y).label*kmatrix[x][y];
					obj1 += o;
					if(Math.abs(o) > numPrec)
						eprintln(1, x+":"+y+" o:"+o+" ax:"+alp[x]+" ay:"+alp[y]+" kmatrix:"+kmatrix[x][y]+" yx:"+l.get(x).label+" yy:"+l.get(y).label+" obj1:"+obj1);
				}
			}
			System.exit(0);
			return Double.POSITIVE_INFINITY;
		}
		
		return obj;
	}

	/**
	 * computing the gradient of the objective function
	 * @param km
	 * @param dm
	 * @param l
	 * @return
	 */
	private ArrayList<Double> gradSVM(ArrayList<SimpleCacheKernel<T>> km, ArrayList<Double> dm, final List<TrainingSample<T>> l) {
		//creating kernel
		ThreadedSumKernel<T> tsk = buildKernel(km, dm);		
		
		//updating svm
		retrainSVM(tsk, l);
		final double alp[] = svm.getAlphas();
		
		//computing grad
		ArrayList<Double> grad = new ArrayList<Double>();
		for(int i = 0 ; i < km.size(); i++)
		{
//			double g = 0;
			Kernel<T> k = km.get(i);
			final double kmatrix[][] = k.getKernelMatrix(l);
			
//			for(int x = 0 ; x < l.size(); x++)
//			for(int y = 0 ; y < l.size(); y++)
//			{
//				g += -0.5* alp[x]*alp[y]*l.get(x).label*l.get(y).label*kmatrix[x][y];
//			}
			
			//parallelized
			final double[] resLine = new double[kmatrix.length];
			ThreadedMatrixOperator gradFactory = new ThreadedMatrixOperator()
			{
				@Override
				public void doLine(int index, double[] line) {
					if(alp[index] < numPrec)
						return;
					double al1 = -0.5 * alp[index] * l.get(index).label;
					for(int j = line.length-1 ; j != 0 ; j--)
					{
						resLine[index] += al1 * alp[j] * l.get(j).label * kmatrix[index][j];
					}
					
				}	
			};
			
			gradFactory.getMatrix(kmatrix);
			double g = 0;
			for(double d : resLine)
				g += d;
			grad.add(i, g);
		}
		
		return grad;
	}

	/**
	 * performs an update of the weights in the mkl
	 * @param km
	 * @param dm
	 * @param l
	 * @return value of objective fonction
	 */
	private double mklUpdate(List<Double> gradOld, List<SimpleCacheKernel<T>> km, List<Double> dmOld, List<TrainingSample<T>> l)
	{
		//save
		ArrayList<Double> dm = new ArrayList<Double>();
		dm.addAll(dmOld);
		ArrayList<Double> grad = new ArrayList<Double>();
		grad.addAll(gradOld);
		
		//init obj
		double costMin = svmObj(km, dm, l);
		double costOld = costMin;
		
		//norme du gradient
		double normGrad = 0;
		for(int i = 0 ; i < grad.size(); i++)
			normGrad += grad.get(i)*grad.get(i);
		normGrad = Math.sqrt(normGrad);
		for(int i = 0 ; i < grad.size(); i++)
		{
			double g = grad.get(i)/normGrad;
			grad.set(i, g);
		}
		
		//finding max dm
		double max = Double.MIN_VALUE;
		int indMax = 0;
		for(int i = 0 ; i < dm.size(); i++)
		{
			double d = dm.get(i);
			if(d > max)
			{
				max = d;
				indMax = i;
			}
		}
		double gradMax = grad.get(indMax);

		//reduced gradient
		ArrayList<Double> desc = new ArrayList<Double>();
		double sum = 0;
		for(int i = 0; i < dm.size(); i++)
		{
			grad.set(i, grad.get(i)- gradMax);
			double d = - grad.get(i);
			if(!(dm.get(i) > 0 || grad.get(i) < 0))
				d = 0;	
			sum += -d;
			desc.add(i, d);
		}
		desc.set(indMax, sum); // NB : grad.get(indMax) == 0
		//verbosity
		eprintln(3, "mklupdate : grad = "+grad);
		eprintln(3, "mklupdate : desc = "+desc);
		
		//optimal stepsize
		double stepMax = Double.POSITIVE_INFINITY;
		for(int i = 0 ; i < desc.size(); i++)
		{
			double d = desc.get(i);
			if(d < 0)
			{
				double min = - dm .get(i)/d;
				if(min < stepMax)
					stepMax = min;
			}	
		}
		if(Double.isInfinite(stepMax) || stepMax == 0)
			return costMin;
		if(stepMax > 0.1)
			stepMax = 0.1;
		
		//small loop
		double costMax = 0;
		while(costMax < costMin)
		{
			ArrayList<Double> dmNew = new ArrayList<Double>();
			for(int i = 0 ; i < dm.size(); i++)
			{
				dmNew.add(i, dm.get(i) + desc.get(i)*stepMax);
			}
			
			//verbosity
			eprintln(3, "* descent : dm = "+dmNew);
			
			costMax = svmObj(km, dmNew, l);
			
			if(costMax < costMin)
			{
				costMin = costMax;
				dm = dmNew;
				
				//numerical cleaning
				//empty
				
				//keep direction in admisible cone
				for(int i = 0; i < desc.size(); i++)
				{
					double d = 0;
					if((dm.get(i) > numPrec || desc.get(i) > 0))
							d = desc.get(i);
					desc.set(i, d);
				}
				sum = 0;
				for(int i = 0 ; i < indMax; i++)
					sum += desc.get(i);
				for(int i = indMax+1; i < desc.size(); i++)
					sum += desc.get(i);
				desc.set(indMax, -sum);
				
				//nex stepMap
				stepMax = Double.POSITIVE_INFINITY;
				for(int i = 0 ; i < desc.size(); i++)
				{
					double d = desc.get(i);
					if(d < 0)
					{
						double Dm = dm.get(i);
						if(Dm < numPrec)
							Dm = 0.;
						double min = - Dm/d;
						if(min < stepMax)
							stepMax = min;
					}
				}
				if(Double.isInfinite(stepMax))
					stepMax = 0.;
				else
					costMax = 0;
			}
			
			//verbosity
			eprint(2, "*");
			eprintln(3, " descent : costMin : "+costMin+" costOld : "+costOld+" stepMax : "+stepMax);
		}

		//verbosity
		eprintln(3, "mklupdate : dm after descent = "+dm);
		
		//-------------------------------------
		//		Golden Search
		//-------------------------------------
		double stepMin = 0;
		int indMin = 0;
		double gold = (1. + Math.sqrt(5))/2.;
		
		ArrayList<Double> cost = new ArrayList<Double>(4);
		cost.add(0, costMin);
		cost.add(1, 0.);
		cost.add(2, 0.);
		cost.add(3, costMax);
		
		ArrayList<Double> step = new ArrayList<Double>(4);
		step.add(0, 0.);
		step.add(1, 0.);
		step.add(2, 0.);
		step.add(3, stepMax);
		
		double deltaMax = stepMax;
		while(stepMax-stepMin > epsGS*deltaMax && stepMax > eps)
		{
			double stepMedR = stepMin +(stepMax-stepMin)/gold;
			double stepMedL = stepMin +(stepMedR-stepMin)/gold;
			
			//setting cost array
			cost.set(0, costMin);
			cost.set(3, costMax);
			//setting step array
			step.set(0, stepMin);
			step.set(3, stepMax);
			
			//cost medr
			ArrayList<Double> dMedR = new ArrayList<Double>();
			for(int i = 0 ; i < dm.size(); i++)
			{
				double d = dm.get(i);
				dMedR.add(i, d + desc.get(i)*stepMedR);
			}
			double costMedR = svmObj(km, dMedR, l);
			
			
			//cost medl
			ArrayList<Double> dMedL = new ArrayList<Double>();
			for(int i = 0 ; i < dm.size(); i++)
			{
				double d= dm.get(i);
				dMedL.add(i, d + desc.get(i)*stepMedL);
			}
			double costMedL = svmObj(km, dMedL, l);
			
			
			cost.set(1, costMedL);
			step.set(1, stepMedL);
			cost.set(2, costMedR);
			step.set(2, stepMedR);
			
			//search min cost
			double min = Double.POSITIVE_INFINITY;
			indMin = -1;
			for(int i = 0 ; i < 4; i++)
			{
				if(cost.get(i) < min)
				{
					indMin = i;
					min = cost.get(i);
				}
			}
			
			eprintln(3, "golden search : cost = ["+costMin+" "+costMedL+" "+costMedR+" "+costMax+"]");
			eprintln(3, "golden search : step = ["+stepMin+" "+stepMedL+" "+stepMedR+" "+stepMax+"]");
			eprintln(3, "golden search : costOpt="+cost.get(indMin)+" costOld="+costOld);
			
			//update search
			switch(indMin)
			{
				case 0:
					stepMax = stepMedL;
					costMax = costMedL;
					break;
				case 1:
					stepMax = stepMedR;
					costMax = costMedR;
					break;
				case 2:
					stepMin = stepMedL;
					costMin = costMedL;
					break;
				case 3:
					stepMin = stepMedR;
					costMin = costMedR;
					break;	
				default:
					eprintln(1, "Error in golden search.");
					return costMin;
			}
			
			//verbosity
			eprint(2, ".");
			
		}
		//verbosity
		eprintln(2, "");
		
		//final update
		double costNew = cost.get(indMin);
		double stepNew = step.get(indMin);
		
		dmOld.clear();
		dmOld.addAll(dm);
		
		if(costNew < costOld)
		{
			for(int i = 0 ; i < dmOld.size(); i++)
			{
				double d = dm.get(i);
				dmOld.set(i, d + desc.get(i)*stepNew);
			}
		}
		
		//creating kernel
		ThreadedSumKernel<T> tsk = buildKernel(km, dm);		
		//updating svm
		retrainSVM(tsk, l);
		
		//verbosity
		eprint(3, "mklupdate : dm = "+dmOld);
		
		return costNew;
	}
	
	private ThreadedSumKernel<T> buildKernel(List<SimpleCacheKernel<T>> km, List<Double> dm)
	{
		long startTime = System.currentTimeMillis();
		ThreadedSumKernel<T> tsk = new ThreadedSumKernel<T>();
		for(int i = 0 ; i < km.size(); i++)
			if(dm.get(i) > numPrec)
				tsk.addKernel(km.get(i), dm.get(i));
		long stopTime = System.currentTimeMillis() - startTime;
		eprintln(3, "building kernel : time="+stopTime);
		return tsk;
	}
	
	/**
	 * update svm classifier (update alphas)
	 * @param k
	 * @param l
	 */
	private void retrainSVM(Kernel<T> k, List<TrainingSample<T>> l)
	{
		if(svm == null)
		{
			svm = new SMOSVM<T>(k);
			svm.setVerbosityLevel(VERBOSITY_LEVEL-1);
			svm.setC(C);
			svm.train(l);
		}
		svm.setKernel(k);
		svm.retrain();
	}
	


	public double valueOf(T e) {
		return svm.valueOf(e);
	}

	/**
	 * return weights of the svm
	 * @return
	 */
	public double[] getTrainingWeights() {
		return svm.getAlphas();
	}
	
	public double[] getKernelWeights()
	{
		double dm[] = new double[kernelWeights.size()];
		for(int i = 0 ; i < dm.length; i++)
			dm[i] = kernelWeights.get(i);
		return dm;
	}

	/**
	 * associative table of kernels and their corresponfding weights
	 * @return
	 */
	public Hashtable<Kernel<T>, Double> getWeights() {
		Hashtable<Kernel<T>, Double> hash = new Hashtable<Kernel<T>, Double>();
		for(int i = 0 ; i < kernels.size(); i++)
		{
			hash.put(kernels.get(i), kernelWeights.get(i));
		}
		return hash;
	}
	
	public double getC() {
		return C;
	}

	public void setC(double c) {
		C = c;
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

	public double getDualGap() {
		return epsDG;
	}

	public void setDualGap(double epsDG) {
		this.epsDG = epsDG;
	}

}
