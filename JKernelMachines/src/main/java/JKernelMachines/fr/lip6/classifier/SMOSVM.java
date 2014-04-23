package JKernelMachines.fr.lip6.classifier;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import JKernelMachines.fr.lip6.density.SMODensity;
import JKernelMachines.fr.lip6.kernel.Kernel;
import JKernelMachines.fr.lip6.type.TrainingSample;

/**
 * Classifieur SVM utilisant l'algorithme SMO de J. Platt.<br />
 * N'importe quel type <T> peut être traité, pourvu qu'un noyau Kernel<T>
 * Soit fournit.
 * @author dpicard
 *
 * @param <T> Type de donnée de l'espace d'entrée.
 */
public class SMOSVM<T> implements Classifier<T>, Serializable, Cloneable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1224235635423748229L;
	
	
	//SV, y et alpha associés
	private double[] alphay;
	private double[] alpha;
	private ArrayList<TrainingSample<T>> ts;
	private int size;
	
	//le noyau
	private Kernel<T> kernel;
	private double[][] kcache;
	
	//outil pour l'optim (cache d'erreur et générateur aléatoire)
	private double[] ecache;
	private Random ran;
	
	//paramètres du SVM
	private double C = 1.0e5, b, eps = 1.0e-12, tolerance = 1e-12;
	
	
	
	/**
	 * Constructeur passant le noyau servant à calculer la similarité entre les éléments
	 * de l'espace d'entrée.
	 * @param k le noyau templatisé en <T>
	 */
	public SMOSVM(Kernel<T> k)
	{
		kernel = k;
		ran = new Random(System.currentTimeMillis());
	}
	
	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(java.lang.Object, int)
	 */
	public void train(TrainingSample<T> t) {

		if(ts == null)
		{
			ts = new ArrayList<TrainingSample<T>>();
			b = 0;
		}
		ts.add(t);

		
		double[] a_tmp = Arrays.copyOf(alpha, alpha.length+1);
		a_tmp[alpha.length] = 0.;
		alpha = a_tmp.clone();
		
		
		size = ts.size();
		
		train();
		
	}


	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#train(T[], int[])
	 */
	public void train(List<TrainingSample<T>> t) {

		ts = new ArrayList<TrainingSample<T>>();
		ts.addAll(t);
		
		alpha = new double[ts.size()];
		
		b = 0;
		
		size = ts.size();
		
		train();
	}

	/**
	 * réentraînement sur la base des alphas précédents.
	 */
	public void retrain()
	{
		train();
	}

	/**
	 * entraînement du classifieur
	 */
	private void train()
	{
		
		long timeStart = System.currentTimeMillis();

		//génération du tableau des alpha_i * y_i
		double[] t_alphay = new double[size];
		alphay = t_alphay;
		if(alpha != null)
			for(int i = 0 ; i < size; i++)
				alphay[i] = ts.get(i).label*alpha[i];
		else
			Arrays.fill(alphay, 0.);


		ecache = new double[size];
		
//		 choix du classifieur
		int pos = 0 , neg = 0;
		for(int i = 0 ; i < size ; i++)
		{
			int y = ts.get(i).label;
			if(y == 1)
				pos++;
			else if(y == -1)
				neg++;
		}
		
		if(pos > 0 && neg ==0)
		{
			eprintln(1, "exemple positifs uniquement, SMODensity choisi");
			SMODensity<T> density = new SMODensity<T>(kernel);
			ArrayList<T> tlist = new ArrayList<T>();
			for(TrainingSample<T> t: ts)
				tlist.add(t.sample);
			density.train(tlist);
			alpha = density.getAlphas();
			alphay = alpha.clone();
			return;
		}
		else if (pos == 0 && neg > 0)
		{
			eprintln(1, "exemple négatifs uniquement, SMODensity choisi");
			SMODensity<T> density = new SMODensity<T>(kernel);
			ArrayList<T> tlist = new ArrayList<T>();
			for(TrainingSample<T> t: ts)
				tlist.add(t.sample);
			density.train(tlist);
			alpha = density.getAlphas();
			for (int i = 0 ; i < alpha.length; i++)
				alphay[i] = -1 * alpha[i];
			return;
		}
		
		//cache de noyau
		eprintln(3, "building cache.");
		kcache = kernel.getKernelMatrix(ts);
		eprintln(4, "kcache size : "+kcache.length);
		eprintln(3, "kcache built.");
		
		////-----------------------------------------------------------------------------------------

		
		int nChange = 0;
		boolean bExaminerTout = true;

		//remplissage du cache d'erreur
		for (int i=0;i<size;i++)
		{
			double sum = 0;
			for(int j = 0 ; j < size; j++)
			{
				if(alphay[j] != 0)
					sum += alphay[j] * kcache[i][j];
			}
			ecache[i] =  (sum - b) - ts.get(i).label;
		}
		eprintln(4, "smotrain : ecache="+Arrays.toString(ecache));

		long timeCache = System.currentTimeMillis();
		
		
		// On examine les exemples, de préférence ceux qui ne sont pas au bords (qui ne
		//  sont pas des SV.

		int ite = 0;
		while (nChange > 0 || bExaminerTout)
		{
			nChange = 0;
			if (bExaminerTout)
			{
				//printf ("Boucle sur tous les points...");
				for (int i=0;i<size;i++)
					if (examiner (i))
						nChange ++;
			}
			else
			{
				//printf ("Boucle sur les points KKT...");
				for (int i=0;i<size;i++)
					if (alpha[i] > eps && alpha[i] < (C-eps))
						if (examiner (i))
							nChange ++;
			}
			
			if (bExaminerTout)
				bExaminerTout = false;
			else if (nChange == 0)
				bExaminerTout = true;

			ite++;
			if (ite > 1000000) 
			{
				eprintln(1, "trop d'itération...");
				break;
			}
			
			if(ite%10000 == 0)
				eprintln(1, "iteration : "+ite);

		}
		

		if(VERBOSITY_LEVEL >=4)
		{
			eprintln(3, "smotrain : after train ecache="+Arrays.toString(ecache));
			double errSum = 0;
			double alpySum = 0;
			double alpSum = 0;
			for(int i = 0 ; i < size; i++)
			{
				errSum += ts.get(i).label*ecache[i];
				alpySum += ts.get(i).label*alpha[i];
				alpSum += alpha[i];
			}
			eprintln(4, "smotrain : after training errSum="+errSum/size+" alpySum="+alpySum+" alpSum="+alpSum);
		}
		//----------------------------------------------------

		alphay = new double[alpha.length];
		for(int i = 0 ; i < alpha.length; i++)
			 alphay[i] = alpha[i]*ts.get(i).label; // alphai * yi
		
		long timeTrain = System.currentTimeMillis();
		
		eprintln(3, "training done in "+ite+" iterations timeCache="+(timeCache - timeStart)+" timeTrain="+(timeTrain-timeCache));
		kcache = null; // empty memory
	}
	
	/**
	 * selection des alpha à optimiser.
	 */
	private boolean examiner ( int i1)
	{
		int k;
		double y1 = ts.get(i1).label;
		double a1 = alpha[i1];
		double E1 = ecache[i1];
		double r1 = y1 * E1;

		// alpha[i1] doit-il est pris en compte pour optimiser ?
		if ((r1 < -tolerance && a1 < C-eps) || (r1 > tolerance && a1 > eps))
		{
			// On cherche i2 de 3 façon différentes...

			// Recherche 1: maximiser |E1-E2| parmis les exemples qui ne sont
			//   pas au bord (alpha strict entre 0 et C)

			int i2 = size;
			double rMax = 0,r;
			for (k=0;k< size;k++)
				if (alpha[k] > eps && alpha[k] < C-eps)
				{
					r = Math.abs (E1 - ecache[k]);
					if (r > rMax)
					{
						rMax = r;
						i2 = k;
					}
				}
			// Si on a trouvé un i2, on peut essayer d'optimiser !
			if (i2 < size)
				if (optimiser (i2,i1))
				{
					return true;
				}

			// Recherche 2: Si le meilleur ne convient pas... on choisit un exemple
			//   non borné au hazard
			int k0 = ran.nextInt(size);
			for (k=k0;k<k0+size;k++)
			{
				i2 = k % size;
				if (alpha[i2] > eps && alpha[i2] < C-eps)
					if (optimiser (i2,i1))
						return true;
			}

			// Recherche 3: Bon... et bien on va en prendre un au hazard
			k0 = (new Random()).nextInt(size);
			for (k=k0;k<k0+size;k++)
			{
				i2 = k % size;
				if (optimiser (i2,i1))
				{
					return true;
				}
			}

			// Si on arrive ici, c'est que l'on a fait bcq de calculs pour rien
		}

		// La condition KKT est repectée, il n'y a rien à faire
		return false;
	}
		
		/**
		 * optimisation locale de deux alpha
		 */
		private boolean optimiser( int i1, int i2)
		{

			if (i1 == i2)
				return false;



			// Détermine les valeurs nécessaires
			double a1prec = alpha[i1];
			double y1 = ts.get(i1).label;
			double E1 = ecache[i1];

			double a2prec = alpha[i2];
			double y2 = ts.get(i2).label;
			double E2 = ecache[i2];

			double s = y1 * y2;

			// Calcul L et H (domaine d'existence de alpha2nouveau)
			double L,H;
			if (y1 == y2)
			{
				
				L = Math.max(0, a2prec + a1prec - C);
				H = Math.min(C, a2prec + a1prec);
			}
			else
			{
				L = Math.max(0, a2prec - a1prec);
				H = Math.min(C, C + a2prec - a1prec);
				
			}
			// Si L == H, il y a un problème !
			if (L == H)
			{
				return false;
			}

			// Calcul le nouveau alpha2
			double a2nouv;
			double k11 = kcache[i1][i1];
			double k22 = kcache[i2][i2];
			double k12 = kcache[i1][i2];
			
			
			
			double eta = 2 * k12 - k11 - k22;
			if (eta < 0)
			{
				// Si eta est strictement négatif (non nul puique eta est négatif),
				//   alors une simple opération nous donne le nouveau alpha2
				a2nouv = a2prec + y2 * (E2 - E1) / eta;
				// On s'assure que alpha2nouveau est dans son domaine
				if (a2nouv < L) a2nouv = L;
				else if (a2nouv > H) a2nouv = H;
			}
			else
			{
				// Si eta est nul, alpha2nouveau est sur l'un des deux bords,
				//   il faut déterminer lequel s'il existe (à espilon près)
				double Lp = frLimite (i1,i2,L);
				double Hp = frLimite (i1,i2,H);
				if (Lp > (Hp + eps)) a2nouv = L;
				else if (Lp < (Hp - eps)) a2nouv = H;
				else a2nouv = a2prec;
			}

			// si le changement est en dessous de la précision numérique
			if (Math.abs(a2nouv - a2prec) < (eps * (a2nouv + a2prec + eps)))
			{
				return false;
			}


			// Calcul de alpha1nouveau à partir de alpha2nouveau
			double a1nouv = a1prec + s * (a2prec - a2nouv);
			// On s'assure que alpha1nouveau est entre 0 et C et on
			//   agit en conséquence
			if(a1nouv < 0)
			{
				//on ajout aux deux a1nouv pour respecter la somme constante
				a2nouv -= s*a1nouv;
				a1nouv = 0;
			}
			else if(a1nouv > C)
			{
				//on retranche aux deux a1nouv - C pour respecter la somme constante
				a2nouv -= s*(a1nouv - C);
				a1nouv = C;
			}
			

			// To prevent precision problems
			if (a2nouv > C - eps) {
				a2nouv = C;
				eprintln(4, "svm : i1="+i1+" i2="+i2+" a2nouv = C !!! a1nouv="+a1nouv+" a1prec="+a1prec+" a2prec="+a2prec+" eta="+eta+" k12="+k12+" k11="+k11+" k22="+k22+" L="+L+" H="+H+" y1="+y1+" y2="+y2+" s="+s+" E1="+E1+" e2="+E2);
			} else if (a2nouv <= eps) {
				a2nouv = 0;
			}
			if( a1nouv < eps)
			{
				a1nouv = 0;
			}
			else if(a1nouv > C - eps)
			{
				a1nouv = C;
				eprintln(4, "svm : i1="+i1+" i2="+i2+" a1nouv = C !!! a2nouv="+a2nouv+" a1prec="+a1prec+" a2prec="+a2prec+" eta="+eta+" k12="+k12+" k11="+k11+" k22="+k22+" L="+L+" H="+H+" y1="+y1+" y2="+y2+" s="+s+" E1="+E1+" e2="+E2);
			}
			

			

		

			// _______________________
			//
			// A présent nous avons les deux nouveaux alpha, on adapte l'ensemble

			// Calcul du nouveau b
			double db = 0.; // différence entre l'ancien et le nouveau b
			if (a1nouv > eps && a1nouv < (C-eps)) //a1 not on the bounds
			{
				db = E1 + y1*(a1nouv - a1prec)*k11 + y2*(a2nouv - a2prec)*k12;
			}
			else
			{
				if (a2nouv > eps && a2nouv < (C-eps)) // a2 not on the bounds
				{
					db = E2 + y1*(a1nouv - a1prec)*k12 + y2*(a2nouv - a2prec)*k22;
				}
				else // neither a1 nor a2 on the bounds
					db = (E1 + E2)/2 + y1*(a1nouv-a1prec)*(k11+k12) + y2*(a2nouv-a2prec)*(k12+k22);//)/2;
			}
			b += db;

			// Mise à jour du cache
			double t1 = y1 * (a1nouv - a1prec);
			double t2 = y2 * (a2nouv - a2prec);
			for (int i=0;i<size;i++)
				ecache[i] += t1*kcache[i1][i] + t2*kcache[i2][i] - db;

			// Mise à jour des deux alpha
			alpha[i1] = a1nouv;
			alpha[i2] = a2nouv;

			return true;
			
		}
		
		/**
		 * calcul des limites de la fonction objective
		 */
		private double frLimite ( int i1, int i2, double L)
		{
			// calcul réduit sur a1 et a2 puisqu'on va faire une comparaison
			double y1 = ts.get(i1).label;
			double y2 = ts.get(i2).label;
			double aa1 = alpha[i1] + y1*y2*(alpha[i2] - L);
			double t1 = -y1*aa1/2;
			double t2 = -y2*L/2;
			double r = aa1 + L;

			for (int i=0;i<size;i++)
				if (alpha[i] > eps)
				{
					
					r += t1*ts.get(i).label*kcache[i1][i];
					r += t2*ts.get(i).label*kcache[i2][i];
				}
			return r;
		}
		


	/* (non-Javadoc)
	 * @see fr.lip6.classifier.Classifier#valueOf(java.lang.Object)
	 */
	public double valueOf(T e) {

		double sum = 0;
		for(int i = 0 ; i < size; i++)
			if(alphay[i] != 0)
				sum += alphay[i] * kernel.valueOf(ts.get(i).sample, e);
		
		return sum - b;
	}
		
	/**
	 * returns support vectors weights
	 */
	public double[] getAlphas()
	{
		return alpha;
	}
	
	/**
	 * returns bias b of (w*x - b)
	 * @return the bias of the trained svm
	 */
	public double getB()
	{
		return b;
	}
	
	/**
	 * gets the hyperparameter C
	 * @return
	 */
	public double getC() {
		return C;
	}

	/**
	 * sets the hyperparameter C
	 * @param c
	 */
	public void setC(double c) {
		C = c;
	}

	/**
	 * the ArrayList of TrainingSample used for training
	 * @return the ArrayList of trainign samples
	 */
	public ArrayList<TrainingSample<T>> getTrainingSet()
	{
		return ts;
	}
	
	public void setKernel(Kernel<T> k)
	{
		kernel = k;
	}
	
	public void setAlphas(double[] a)
	{
		alpha = a;
	}
	
	public void setTrain(ArrayList<TrainingSample<T>> t)
	{
		ts = new ArrayList<TrainingSample<T>>(t);
	}

	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
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
}
