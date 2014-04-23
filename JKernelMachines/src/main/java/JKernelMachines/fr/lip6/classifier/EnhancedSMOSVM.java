package JKernelMachines.fr.lip6.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Classifieur SVM utilisant l'algorithme SMO de J. Platt avec l'implementation de weka.<br />
 * N'importe quel type <T> peut être traité, pourvu qu'un noyau Kernel<T> Soit
 * fournit.
 * 
 * @author dpicard
 * 
 * @param <T>
 *            Type de donnée de l'espace d'entrée.
 */
public class EnhancedSMOSVM<T> implements Classifier<T>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3409002064617582128L;
	
	// SV, y et alpha associés
	private double[] alphay;
	private double[] alpha;
	private ArrayList<TrainingSample<T>> ts;
	int size;

	// le noyau
	private Kernel<T> kernel;
	private double[][] kcache;


	// paramètres du SVM
	private double C = 1.0e5, b, eps = 1e-12, tolerance = 1e-10;

	// Logger
	static private Logger logger = Logger.getLogger(EnhancedSMOSVM.class
			.toString());


	/** The thresholds. */
	protected double m_bLow, m_bUp;

	/** The indices for m_bLow and m_bUp */
	protected int m_iLow, m_iUp;

	/** The training data. */
	protected ArrayList<T> m_data;

	/** Weight vector for linear machine. */
	protected double[] m_weights;

	/**
	 * Variables to hold weight vector in sparse form. (To reduce storage
	 * requirements.)
	 */
	protected double[] m_sparseWeights;
	protected int[] m_sparseIndices;

	/** The transformed class values. */
	protected double[] m_class;

	/** The current set of errors for all non-bound examples. */
	protected double[] m_errors;

	/* The five different sets used by the algorithm. */
	/** {i: 0 < m_alpha[i] < C} */
	protected TreeSet<Integer> m_I0;
	/** {i: m_class[i] = 1, m_alpha[i] = 0} */
	protected TreeSet<Integer> m_I1;
	/** {i: m_class[i] = -1, m_alpha[i] =C} */
	protected TreeSet<Integer> m_I2;
	/** {i: m_class[i] = 1, m_alpha[i] = C} */
	protected TreeSet<Integer> m_I3;
	/** {i: m_class[i] = -1, m_alpha[i] = 0} */
	protected TreeSet<Integer> m_I4;

	/** The set of support vectors */
	protected TreeSet<Integer> m_supportVectors; // {i: 0 < m_alpha[i]}

	// /** Stores logistic regression model for probability estimate */
	// protected Logistic m_logistic = null;

	/** Stores the weight of the training instances */
	protected double m_sumOfWeights = 0;

	/** Precision constant for updating sets */
	protected static double m_Del = 1000 * Double.MIN_VALUE;

	/**
	 * Constructeur passant le noyau servant à calculer la similarité entre les
	 * éléments de l'espace d'entrée.
	 * 
	 * @param k
	 *            le noyau templatisé en <T>
	 */
	public EnhancedSMOSVM(Kernel<T> k) {
		kernel = k;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lip6.classifier.Classifier#train(java.lang.Object, int)
	 */
	public void train(TrainingSample<T> t) {

		if(ts == null)
		{
			ts = new ArrayList<TrainingSample<T>>();
			
		}
		ts.add(t);

		
		double[] a_tmp = Arrays.copyOf(alpha, alpha.length+1);
		a_tmp[alpha.length] = 0.;
		alpha = a_tmp.clone();
		
		size = ts.size();

		train();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lip6.classifier.Classifier#train(T[], int[])
	 */
	public void train(List<TrainingSample<T>> t) {

		ts = new ArrayList<TrainingSample<T>>(t);
		
		alpha = new double[ts.size()];
		Arrays.fill(alpha, 0.0);
		
		size = ts.size();
		
		train();
	}

	/**
	 * entraînement du classifieur
	 */
	private void train() {

		// Initialize some variables
		m_bUp = -1;
		m_bLow = 1;
		b = 0;
		alpha = null;
		m_data = null;
		m_weights = null;
		m_errors = null;
		m_I0 = null;
		m_I1 = null;
		m_I2 = null;
		m_I3 = null;
		m_I4 = null;
		m_sparseWeights = null;
		m_sparseIndices = null;

		// // Store the sum of weights
		// m_sumOfWeights = insts.sumOfWeights();

		// Set class values
		// m_class = new double[insts.numInstances()];
		m_class = new double[size];
		m_iUp = -1;
		m_iLow = -1;

		// copie les classe en +1 et -1
		// for (int i = 0; i < m_class.length; i++) {
		// if ((int) insts.instance(i).classValue() == cl1) {
		// m_class[i] = -1;
		// m_iLow = i;
		// } else if ((int) insts.instance(i).classValue() == cl2) {
		// m_class[i] = 1;
		// m_iUp = i;
		// } else {
		// throw new Exception("This should never happen!");
		// }
		// }
		for (int i = 0; i < m_class.length; i++) {
			if ((int) ts.get(i).label == -1) {
				m_class[i] = -1;
				m_iLow = i;
			} else if ((int) ts.get(i).label == 1) {
				m_class[i] = 1;
				m_iUp = i;
			} else {
				logger.severe("this should never happen !!!");
				return;
			}
		}

		// Check whether one or both classes are missing
		if ((m_iUp == -1) || (m_iLow == -1)) {
			if (m_iUp != -1) {
				b = -1;
			} else if (m_iLow != -1) {
				b = 1;
			} else {
				m_class = null;
				return;
			}
			// check du kernel ???
			// if (m_KernelIsLinear) {
			// m_sparseWeights = new double[0];
			// m_sparseIndices = new int[0];
			// m_class = null;
			// } else {
			// m_supportVectors = new SMOset(0);
			// m_alpha = new double[0];
			// m_class = new double[0];
			// }

			m_supportVectors = new TreeSet<Integer>();
			alpha = new double[0];
			m_class = new double[0];

			// // Fit sigmoid if requested
			// if (fitLogistic) {
			// fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
			// }
			return;
		}

		// Set the reference to the data
		m_data = new ArrayList<T>();
		for(TrainingSample<T> t : ts)
			m_data.add(t.sample);

		// // If machine is linear, reserve space for weights
		// if (m_KernelIsLinear) {
		// m_weights = new double[m_data.numAttributes()];
		// } else {
		// m_weights = null;
		// }

		// Initialize alpha array to zero
		// m_alpha = new double[m_data.numInstances()];
		alpha = new double[size];

		// Initialize sets
		// m_supportVectors = new SMOset(m_data.numInstances());
		// m_I0 = new SMOset(m_data.numInstances());
		// m_I1 = new SMOset(m_data.numInstances());
		// m_I2 = new SMOset(m_data.numInstances());
		// m_I3 = new SMOset(m_data.numInstances());
		// m_I4 = new SMOset(m_data.numInstances());
		m_supportVectors = new TreeSet<Integer>();
		m_I0 = new TreeSet<Integer>();
		m_I1 = new TreeSet<Integer>();
		m_I2 = new TreeSet<Integer>();
		m_I3 = new TreeSet<Integer>();
		m_I4 = new TreeSet<Integer>();

		// Clean out some instance variables
		m_sparseWeights = null;
		m_sparseIndices = null;

		// init kernel
		// m_kernel.buildKernel(m_data);
		//cache de noyau
		logger.info("Building kernel cache");
		kcache = kernel.getKernelMatrix(ts);
		logger.info("Kernel cache built");
		
		
		// Initialize error cache
		// m_errors = new double[m_data.numInstances()];
		m_errors = new double[size];
		m_errors[m_iLow] = 1;
		m_errors[m_iUp] = -1;

		// Build up I1 and I4
		for (int i = 0; i < m_class.length; i++) {
			if (m_class[i] == 1) {
				// m_I1.insert(i);
				m_I1.add(i);
			} else {
				// m_I4.insert(i);
				m_I4.add(i);
			}
		}

		// Loop to find all the support vectors
		int numChanged = 0;
		boolean examineAll = true;
		int ite = 0;
		while ((numChanged > 0) || examineAll) {
			numChanged = 0;
			if (examineAll) {
				for (int i = 0; i < alpha.length; i++) {
					if (examineExample(i)) {
						numChanged++;
					}
				}
			} else {

				// This code implements Modification 1 from Keerthi et al.'s
				// paper
				for (int i = 0; i < alpha.length; i++) {
					if ((alpha[i] > 0)
					// && (m_alpha[i] < m_C * m_data.instance(i).weight())) {
							&& (alpha[i] < C * 1)) {
						if (examineExample(i)) {
							numChanged++;
						}

						// Is optimality on unbound vectors obtained?
						// if (m_bUp > m_bLow - 2 * m_tol) {
						if (m_bUp > m_bLow - 2 * tolerance) {
							numChanged = 0;
							break;
						}
					}
				}

				// This is the code for Modification 2 from Keerthi et al.'s
				// paper
				/*
				 * boolean innerLoopSuccess = true; numChanged = 0; while
				 * ((m_bUp < m_bLow - 2 * m_tol) && (innerLoopSuccess == true)) {
				 * innerLoopSuccess = takeStep(m_iUp, m_iLow, m_errors[m_iLow]); }
				 */
			}

			if (examineAll) {
				examineAll = false;
			} else if (numChanged == 0) {
				examineAll = true;
			}

			ite++;
			
			if(ite%100 == 0)
				logger.info("iteration : "+ite);
		}

		// Set threshold
		b = (m_bLow + m_bUp) / 2.0;

		// // Save memory
		// m_kernel.clean();

		m_errors = null;
		m_I0 = m_I1 = m_I2 = m_I3 = m_I4 = null;

		// // If machine is linear, delete training data
		// // and store weight vector in sparse format
		// if (m_KernelIsLinear) {
		//
		// // We don't need to store the set of support vectors
		// m_supportVectors = null;
		//
		// // We don't need to store the class values either
		// m_class = null;
		//
		// // Clean out training data
		// if (!m_checksTurnedOff) {
		// m_data = new Instances(m_data, 0);
		// } else {
		// m_data = null;
		// }
		//
		// // Convert weight vector
		// double[] sparseWeights = new double[m_weights.length];
		// int[] sparseIndices = new int[m_weights.length];
		// int counter = 0;
		// for (int i = 0; i < m_weights.length; i++) {
		// if (m_weights[i] != 0.0) {
		// sparseWeights[counter] = m_weights[i];
		// sparseIndices[counter] = i;
		// counter++;
		// }
		// }
		// m_sparseWeights = new double[counter];
		// m_sparseIndices = new int[counter];
		// System.arraycopy(sparseWeights, 0, m_sparseWeights, 0, counter);
		// System.arraycopy(sparseIndices, 0, m_sparseIndices, 0, counter);
		//
		// // Clean out weight vector
		// m_weights = null;
		//
		// // We don't need the alphas in the linear case
		// m_alpha = null;
		// }
		//
		// // Fit sigmoid if requested
		// if (fitLogistic) {
		// fitLogistic(insts, cl1, cl2, numFolds, new Random(randomSeed));
		// }
		// ----------------------------------------------------

		
		alphay = new double[alpha.length];
		for (int i = 0; i < alpha.length; i++)
			alphay[i] = alpha[i] * ts.get(i).label; // alphai * yi

		logger.info("Training done in " + ite + " iterations.");

	}

	/**
	 * Examines instance.
	 * 
	 * @param i2
	 *            index of instance to examine
	 * @return true if examination was successfull
	 * @throws Exception
	 *             if something goes wrong
	 */
	protected boolean examineExample(int i2) {

		double y2, F2;
		int i1 = -1;

		y2 = m_class[i2];
		if (m_I0.contains(i2)) {
			F2 = m_errors[i2];
		} else {
			// F2 = SVMOutput(i2, m_data.instance(i2)) + m_b - y2;
			F2 = valueOf(m_data.get(i2)) + b - y2;
			m_errors[i2] = F2;

			// Update thresholds
			if ((m_I1.contains(i2) || m_I2.contains(i2)) && (F2 < m_bUp)) {
				m_bUp = F2;
				m_iUp = i2;
			} else if ((m_I3.contains(i2) || m_I4.contains(i2))
					&& (F2 > m_bLow)) {
				m_bLow = F2;
				m_iLow = i2;
			}
		}

		// Check optimality using current bLow and bUp and, if
		// violated, find an index i1 to do joint optimization
		// with i2...
		boolean optimal = true;
		if (m_I0.contains(i2) || m_I1.contains(i2) || m_I2.contains(i2)) {
			// if (m_bLow - F2 > 2 * m_tol) {
			if (m_bLow - F2 > 2 * tolerance) {
				optimal = false;
				i1 = m_iLow;
			}
		}
		if (m_I0.contains(i2) || m_I3.contains(i2) || m_I4.contains(i2)) {
			// if (F2 - m_bUp > 2 * m_tol) {
			if (F2 - m_bUp > 2 * tolerance) {
				optimal = false;
				i1 = m_iUp;
			}
		}
		if (optimal) {
			return false;
		}

		// For i2 unbound choose the better i1...
		if (m_I0.contains(i2)) {
			if (m_bLow - F2 > F2 - m_bUp) {
				i1 = m_iLow;
			} else {
				i1 = m_iUp;
			}
		}
		if (i1 == -1) {
			logger.severe("This should never happen!");
			return false;
		}
		return takeStep(i1, i2, F2);
	}

	/**
	 * Method solving for the Lagrange multipliers for two instances.
	 * 
	 * @param i1
	 *            index of the first instance
	 * @param i2
	 *            index of the second instance
	 * @param F2
	 * @return true if multipliers could be found
	 * @throws Exception
	 *             if something goes wrong
	 */
	protected boolean takeStep(int i1, int i2, double F2) {

		double alph1, alph2, y1, y2, F1, s, L, H, k11, k12, k22, eta, a1, a2, f1, f2, v1, v2, Lobj, Hobj;
		// double C1 = m_C * m_data.instance(i1).weight();
		// double C2 = m_C * m_data.instance(i2).weight();
		double C1 = C;
		double C2 = C;

		// Don't do anything if the two instances are the same
		if (i1 == i2) {
			return false;
		}

		// Initialize variables
		alph1 = alpha[i1];
		alph2 = alpha[i2];
		y1 = m_class[i1];
		y2 = m_class[i2];
		F1 = m_errors[i1];
		s = y1 * y2;

		// Find the constraints on a2
		if (y1 != y2) {
			L = Math.max(0, alph2 - alph1);
			H = Math.min(C2, C1 + alph2 - alph1);
		} else {
			L = Math.max(0, alph1 + alph2 - C1);
			H = Math.min(C2, alph1 + alph2);
		}
		if (L >= H) {
			return false;
		}

		// Compute second derivative of objective function
		// k11 = m_kernel.eval(i1, i1, m_data.instance(i1));
		// k12 = m_kernel.eval(i1, i2, m_data.instance(i1));
		// k22 = m_kernel.eval(i2, i2, m_data.instance(i2));
		k11 = kcache[i1][i1];
		k12 = kcache[i1][i2];
		k22 = kcache[i2][i2];
		eta = 2 * k12 - k11 - k22;

		// Check if second derivative is negative
		if (eta < 0) {

			// Compute unconstrained maximum
			a2 = alph2 - y2 * (F1 - F2) / eta;

			// Compute constrained maximum
			if (a2 < L) {
				a2 = L;
			} else if (a2 > H) {
				a2 = H;
			}
		} else {

			// Look at endpoints of diagonal
			// f1 = SVMOutput(i1, m_data.instance(i1));
			// f2 = SVMOutput(i2, m_data.instance(i2));
			f1 = valueOf(m_data.get(i1));
			f2 = valueOf(m_data.get(i2));
			v1 = f1 + b - y1 * alph1 * k11 - y2 * alph2 * k12;
			v2 = f2 + b - y1 * alph1 * k12 - y2 * alph2 * k22;
			double gamma = alph1 + s * alph2;
			Lobj = (gamma - s * L) + L - 0.5 * k11 * (gamma - s * L)
					* (gamma - s * L) - 0.5 * k22 * L * L - s * k12
					* (gamma - s * L) * L - y1 * (gamma - s * L) * v1 - y2 * L
					* v2;
			Hobj = (gamma - s * H) + H - 0.5 * k11 * (gamma - s * H)
					* (gamma - s * H) - 0.5 * k22 * H * H - s * k12
					* (gamma - s * H) * H - y1 * (gamma - s * H) * v1 - y2 * H
					* v2;
			// if (Lobj > Hobj + m_eps) {
			if (Lobj > Hobj + eps) {
				a2 = L;
				// } else if (Lobj < Hobj - m_eps) {
			} else if (Lobj < Hobj - eps) {
				a2 = H;
			} else {
				a2 = alph2;
			}
		}
		// if (Math.abs(a2 - alph2) < m_eps * (a2 + alph2 + m_eps)) {
		if (Math.abs(a2 - alph2) < eps * (a2 + alph2 + eps)) {
			return false;
		}

		// To prevent precision problems
		if (a2 > C2 - m_Del * C2) {
			a2 = C2;
		} else if (a2 <= m_Del * C2) {
			a2 = 0;
		}

		// Recompute a1
		a1 = alph1 + s * (alph2 - a2);

		// To prevent precision problems
		if (a1 > C1 - m_Del * C1) {
			a1 = C1;
		} else if (a1 <= m_Del * C1) {
			a1 = 0;
		}

		// Update sets
		if (a1 > 0) {
//			m_supportVectors.insert(i1);
			m_supportVectors.add(i1);
		} else {
//			m_supportVectors.delete(i1);
			m_supportVectors.remove(i1);
		}
		if ((a1 > 0) && (a1 < C1)) {
//			m_I0.insert(i1);
			m_I0.add(i1);
		} else {
//			m_I0.delete(i1);
			m_I0.remove(i1);
		}
		if ((y1 == 1) && (a1 == 0)) {
//			m_I1.insert(i1);
			m_I1.add(i1);
		} else {
//			m_I1.delete(i1);
			m_I1.remove(i1);
		}
		if ((y1 == -1) && (a1 == C1)) {
//			m_I2.insert(i1);
			m_I2.add(i1);
		} else {
//			m_I2.delete(i1);
			m_I2.remove(i1);
		}
		if ((y1 == 1) && (a1 == C1)) {
//			m_I3.insert(i1);
			m_I3.add(i1);
		} else {
//			m_I3.delete(i1);
			m_I3.remove(i1);
		}
		if ((y1 == -1) && (a1 == 0)) {
//			m_I4.insert(i1);
			m_I4.add(i1);
		} else {
//			m_I4.delete(i1);
			m_I4.remove(i1);
		}
		if (a2 > 0) {
//			m_supportVectors.insert(i2);
			m_supportVectors.add(i2);
		} else {
//			m_supportVectors.delete(i2);
			m_supportVectors.remove(i2);
		}
		if ((a2 > 0) && (a2 < C2)) {
//			m_I0.insert(i2);
			m_I0.add(i2);
		} else {
//			m_I0.delete(i2);
			m_I0.remove(i2);
		}
		if ((y2 == 1) && (a2 == 0)) {
//			m_I1.insert(i2);
			m_I1.add(i2);
		} else {
//			m_I1.delete(i2);
			m_I1.remove(i2);
		}
		if ((y2 == -1) && (a2 == C2)) {
//			m_I2.insert(i2);
			m_I2.add(i2);
		} else {
//			m_I2.delete(i2);
			m_I2.remove(i2);
		}
		if ((y2 == 1) && (a2 == C2)) {
//			m_I3.insert(i2);
			m_I3.add(i2);
		} else {
//			m_I3.delete(i2);
			m_I3.remove(i2);
		}
		if ((y2 == -1) && (a2 == 0)) {
//			m_I4.insert(i2);
			m_I4.add(i2);
		} else {
//			m_I4.delete(i2);
			m_I4.remove(i2);
		}

//		// Update weight vector to reflect change a1 and a2, if linear SVM
//		if (m_KernelIsLinear) {
//			Instance inst1 = m_data.instance(i1);
//			for (int p1 = 0; p1 < inst1.numValues(); p1++) {
//				if (inst1.index(p1) != m_data.classIndex()) {
//					m_weights[inst1.index(p1)] += y1 * (a1 - alph1)
//							* inst1.valueSparse(p1);
//				}
//			}
//			Instance inst2 = m_data.instance(i2);
//			for (int p2 = 0; p2 < inst2.numValues(); p2++) {
//				if (inst2.index(p2) != m_data.classIndex()) {
//					m_weights[inst2.index(p2)] += y2 * (a2 - alph2)
//							* inst2.valueSparse(p2);
//				}
//			}
//		}

		// Update error cache using new Lagrange multipliers
//		for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
//			if ((j != i1) && (j != i2)) {
//				m_errors[j] += y1 * (a1 - alph1)
//						* m_kernel.eval(i1, j, m_data.instance(i1)) + y2
//						* (a2 - alph2)
//						* m_kernel.eval(i2, j, m_data.instance(i2));
//			}
//		}
	for (Iterator<Integer> iter = m_I0.iterator(); iter.hasNext();) {
		int j = iter.next();
		if ((j != i1) && (j != i2)) {
			m_errors[j] += y1 * (a1 - alph1)
					* kcache[i1][j] + y2
					* (a2 - alph2)
					* kcache[i2][j];
		}
	}

		// Update error cache for i1 and i2
		m_errors[i1] += y1 * (a1 - alph1) * k11 + y2 * (a2 - alph2) * k12;
		m_errors[i2] += y1 * (a1 - alph1) * k12 + y2 * (a2 - alph2) * k22;

		// Update array with Lagrange multipliers
		alpha[i1] = a1;
		alpha[i2] = a2;

		// Update thresholds
		m_bLow = -Double.MAX_VALUE;
		m_bUp = Double.MAX_VALUE;
		m_iLow = -1;
		m_iUp = -1;
//		for (int j = m_I0.getNext(-1); j != -1; j = m_I0.getNext(j)) {
//			if (m_errors[j] < m_bUp) {
//				m_bUp = m_errors[j];
//				m_iUp = j;
//			}
//			if (m_errors[j] > m_bLow) {
//				m_bLow = m_errors[j];
//				m_iLow = j;
//			}
//		}
		for (Iterator<Integer> iter = m_I0.iterator(); iter.hasNext();) {
			int j = iter.next();
			if (m_errors[j] < m_bUp) {
				m_bUp = m_errors[j];
				m_iUp = j;
			}
			if (m_errors[j] > m_bLow) {
				m_bLow = m_errors[j];
				m_iLow = j;
			}
		}
		if (!m_I0.contains(i1)) {
			if (m_I3.contains(i1) || m_I4.contains(i1)) {
				if (m_errors[i1] > m_bLow) {
					m_bLow = m_errors[i1];
					m_iLow = i1;
				}
			} else {
				if (m_errors[i1] < m_bUp) {
					m_bUp = m_errors[i1];
					m_iUp = i1;
				}
			}
		}
		if (!m_I0.contains(i2)) {
			if (m_I3.contains(i2) || m_I4.contains(i2)) {
				if (m_errors[i2] > m_bLow) {
					m_bLow = m_errors[i2];
					m_iLow = i2;
				}
			} else {
				if (m_errors[i2] < m_bUp) {
					m_bUp = m_errors[i2];
					m_iUp = i2;
				}
			}
		}
		if ((m_iLow == -1) || (m_iUp == -1)) {
			logger.severe("This should never happen!");
			return false;
		}

		// Made some progress.
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.lip6.classifier.Classifier#valueOf(java.lang.Object)
	 */
	public double valueOf(T e) {

		double sum = 0;
		for (int i = 0; i < size; i++)
		{
			TrainingSample<T> t = ts.get(i);
			sum += alpha[i] * t.label * kernel.valueOf(t.sample, e);
		}

		return sum-b;
	}

	/**
	 * 
	 */
	public double[] getAlphas() {
		return alpha;
	}

}
