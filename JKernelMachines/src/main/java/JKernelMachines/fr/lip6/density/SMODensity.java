package JKernelMachines.fr.lip6.density;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import JKernelMachines.fr.lip6.kernel.Kernel;

/**
 * Estimateur de densité one class SVM basé sur l'algorithme SMO de J. Platt.<br/>
 * Fonctionne pour n'importe quel objet, tant qu'un noyau sur cet objet est fourni.
 * @author dpicard
 *
 * @param <T> Type d'objet à traiter
 */
public class SMODensity<T> implements DensityFunction<T>, Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4738328902335184013L;
	
	
	private Kernel<T> K;
	private double[] alphas;
	//training set
	private ArrayList<T> set;
	
	private int size;
	
	static private Logger logger = Logger.getLogger(SMODensity.class.toString()); 
	

	//parametres
	private final double epsilon=0.001;
	private double C=1;
	double tolerance = 0.01;
	//cache d'erreur
	double cache[];
	
	/**
	 * Constructeur par défaut, fournissant le noyau servant à évaluer la similarité
	 * entre les élement de l'espace d'entrée.
	 * @param K le noyau templatisé du type d'objet <T> à traiter
	 */
	public SMODensity(Kernel<T> K)
	{
		this.K = K;
	}
	
	

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#train(fr.lip6.type.MLVector)
	 */
	public void train(T e) {
		
		if(set == null)
		{
			set = new ArrayList<T>();
		}

		set.add(e);
				
		double[] a_tmp = Arrays.copyOf(alphas, alphas.length+1);
		a_tmp[alphas.length] = 0.;
		alphas = a_tmp.clone();
		
		train();
	}

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#train(T[])
	 */
	public void train(ArrayList<T> e) {
		if(set == null)
		{
			set = new ArrayList<T>();
		}
		
		for(T t : e)
			set.add(t);
		
		alphas = new double[set.size()];
		Arrays.fill(alphas, 0.);
		alphas[0] = 1.;
		
		size = set.size();
		
		train();
	}
	
	//calcul de l'optimisation
	private void train()
	{		
		cache = new double[size];
		Arrays.fill(cache, 1.);

		C = 1. / size;


		int nChange = 0;
		boolean bExaminerTout = true;

		int ite = 0;
		// On examine les exemples, de préférence ceux qui ne sont pas au bords (qui ne
		//  sont pas des SV).
		
		while (nChange > 0 || bExaminerTout)
		{
			nChange = 0;
			if (bExaminerTout)
			{
				for (int i=0;i<size;i++)
					if (examiner (i))
						nChange ++;
			}
			else
			{
				for (int i=0;i<size;i++)
					if (alphas[i] > epsilon && alphas[i] < C-epsilon)
						if (examiner (i))
							nChange ++;
			}
			if (bExaminerTout)
				bExaminerTout = false;
			else if (nChange == 0)
				bExaminerTout = true;

			ite++;
		}
		
		logger.info("trained in "+ite+" iterations.");
		
	}
	
//	 Regarde si le alpha[i1] viole la condition de KKT, et si c'est le cas,
//	 cherche un autre alpha[i2] pour l'optimisation
	private boolean examiner ( int i1)
	{ 
		// alpha[i1] doit-il est pris en compte pour optimiser ?
		//if (cache[i1]*alphas[i1] > epsilon || cache[i1]*(alphas[i1]-C) > epsilon)
		if ((cache[i1] < -tolerance && alphas[i1] < C-epsilon) || (cache[i1] > tolerance && alphas[i1] > epsilon))
		{
			// On cherche i2 de 3 fa�on diff�rentes...

			double rMax = 0;
			int i2 = alphas.length;
			for (int i=0;i<alphas.length;i++)
				if (alphas[i] > epsilon && alphas[i] < C-epsilon)
				{
					double r = Math.abs(cache[i1]-cache[i]);
					if (r > rMax)
					{
						rMax = r;
						i2 = i;
					}
				}
			if (i2 < alphas.length)
				if (optimiser (i1,i2))
				{
					return true;
				}

			int k0 = (new Random()).nextInt(alphas.length);
			for (int k=k0;k<k0+alphas.length;k++)
			{
				i2 = k % alphas.length;
				if (alphas[i2] > epsilon && alphas[i2] < C-epsilon)
					if (optimiser (i1,i2))
					{
						return true;
					}
			}
			
			// Recherche 3: Bon... et bien on va en prendre un au hazard
			k0 = (new Random()).nextInt(size);
			for (int k=k0;k<k0+size;k++)
			{
				i2 = k % size;
				if (optimiser (i2,i1))
				{
					return true;
				}
			}
			// Si on arrive ici, c'est que l'on a fait bcq de calculs pour rien
		}

		// La condition KKT est repect�e, il n'y a rien � faire
		return false;
	}

	//résolution du sous problème de manière analytique
	boolean		optimiser ( int i1, int i2)
	{
		if (i1 == i2)
			return false;


		int i;
		double delta = alphas[i1]+alphas[i2];

		double L,H;
		if (delta > C)
		{
			L = delta - C;
			H = C;
		}
		else
		{
			L = 0;
			H = delta;
		}

		if (L == H)
		{
			return false;
		}

		double k11 = K.valueOf(set.get(i1),set.get(i1));
		double k22 = K.valueOf(set.get(i2),set.get(i2));
		double k12 = K.valueOf(set.get(i1),set.get(i2));

		double a1,a2;
		double eta = 2*k12 - k11 - k22;
		if (eta < 0)
		{
			a2 = alphas[i2] + (cache[i2]-cache[i1])/eta;
			if (a2 < L) a2 = L;
			else if (a2 > H) a2 = H;
		}
		else
		{
			double c1 = eta/2;
			double c2 = cache[i1]-cache[i2] - eta * alphas[i2];
			double Lp = c1 * L * L + c2 * L;
			double Hp = c1 * H * H + c2 * H;
			if (Lp > Hp + epsilon) a2 = L;
			else if (Lp < Hp + epsilon) a2 = H;
			else a2 = alphas[i2];
		}

		if (Math.abs(a2 - alphas[i2]) < epsilon * (a2 + alphas[i2] + epsilon))
		{
			return false;
		}

		a1 = delta - a2;

		if (a1 < 0)
		{
			a2 += a1;
			a1 = 0;
		}
		else if (a1 > C)
		{
			a2 += a1-C;
			a1 = C;
		}

		double t1 = a1 - alphas[i1];
		double t2 = a2 - alphas[i2];
		for (i=0;i<alphas.length;i++)
			//if (alphas[i] > epsilon && alphas[i] < C-epsilon)
				cache[i] += t1*K.valueOf(set.get(i1),set.get(i)) + t2*K.valueOf(set.get(i2),set.get(i));

		alphas[i1] = a1;
		alphas[i2] = a2;


		return true;
	}

	/* (non-Javadoc)
	 * @see fr.lip6.density.DensityFunction#valueOf(fr.lip6.type.MLVector)
	 */
	public double valueOf(T e) {

		double sum = 0.;
		for(int i = 0 ; i < size ; i++)
			sum += alphas[i]*K.valueOf(e, set.get(i));
		
		return sum;
	}
	
	/**
	 * get the weights of each support vector
	 * @return the weights of the support vectors
	 */
	public double[] getAlphas()
	{
		return alphas;
	}

}
