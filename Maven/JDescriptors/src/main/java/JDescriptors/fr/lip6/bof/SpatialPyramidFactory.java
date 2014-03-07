package JDescriptors.fr.lip6.bof;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import JDescriptors.fr.lip6.Descriptor;
import Jama.Matrix;


public class SpatialPyramidFactory {

	
	//norm
	public enum Norm { NONE, NB_POINTS, L1_NORM, L2_NORM };
	public static Norm norm = Norm.NONE;
	
	//entry vector norm
	public static boolean l1_norm = false;
	
	// Assignment
	public enum Coding { HARD, SOFT , SPARSE, SPARSEC };
	public static Coding coding = Coding.SOFT;
	
	// pooling
	public enum Pooling { SUM, MAX };
	public static Pooling pooling = Pooling.MAX;
	
	// construction variables
	public static int lines = 2;
	public static int cols = 2;
	public static int knn = 10;
	
	private static double [] SparseCode;
		

	/**
	 * Construct a Spatial Pyramidal BoF representation
	 * @param list descriptors in the image
	 * @param centers codebook entries
	 * @param sigma codebook variances
	 * @return
	 */
	public static ArrayList<double[]> createBagOfWindows(ArrayList<Descriptor> list, double[][] centers, double[] sigma)
	{
		//clear distance maps
		floatDistanceMap.clear();
		doubleDistanceMap.clear();
		
		//create bag
		ArrayList<double[]> bag = new ArrayList<double[]>(lines*cols);
		for(int i = 0 ; i < lines*cols; i++)
			bag.add(new double[centers.length]);
		
		
		//getting xmax and ymax
		float xmax = 0;
		float ymax = 0;
		for(Descriptor vw : list)
		{
			int x = vw.getXmin() + (vw.getXmax()-vw.getXmin())/2;
			int y = vw.getYmin() + (vw.getYmax()-vw.getYmin())/2;
			if(x>xmax)
				xmax = x;
			if(y>ymax)
				ymax = y;
		}
		

		ArrayList<Integer> nbp = new ArrayList<Integer>(bag.size());
		for(int i = 0; i < bag.size(); i++)
			nbp.add(0);
		
		//Letz go !
		for(Descriptor vw : list)
		{
			int x = vw.getXmin() + (vw.getXmax()-vw.getXmin())/2;
			int y = vw.getYmin() + (vw.getYmax()-vw.getYmin())/2;
			for(int i = 0 ; i < lines; i++)
			{
				for(int j = 0 ; j < cols; j++)
				{
					int xwmin = (int)(i*(xmax/(lines+1)));
					int xwmax = (int)((i+2)*(xmax/(lines+1)));
					int ywmin = (int)(j*(ymax/(cols+1)));
					int ywmax = (int)((j+2)*(ymax/(cols+1)));
					
					//ajout a la liste
					if( xwmin <= x && x <= xwmax && ywmin <= y && y <= ywmax)
					{
						addVisualWordToHist(bag.get(i*cols+j), vw, centers, sigma);
						nbp.set(i*cols+j, nbp.get(i*cols+j)+1);
					}

				}
			}
		}

		switch(norm)
		{
		case NB_POINTS:
			for(int i = 0 ; i < bag.size(); i++)
			{
				double[] h = bag.get(i);
				double n = nbp.get(i);
				for(int j = 0 ; j  < h.length; j++)
					h[j] /= n;
			}
			break;
		case L1_NORM:
			for(int i = 0 ; i < bag.size(); i++)
			{
				double[] h = bag.get(i);
				double sum = 0;
				for(int j = 0 ; j  < h.length; j++)
					sum += h[j];
				for(int j = 0 ; j  < h.length; j++)
					h[j] /= sum;
			}
			break;
		case L2_NORM:
			for(int i = 0 ; i < bag.size(); i++)
			{
				double[] h = bag.get(i);
				double sum = 0;
				for(int j = 0 ; j  < h.length; j++)
					sum += h[j]*h[j];
				sum = Math.sqrt(sum);
				for(int j = 0 ; j  < h.length; j++)
					h[j] /= sum;
			}
			break;
		case NONE:
		default:
			//nothing to do
		}
		
		return bag;
	}
	
	private static void addVisualWordToHist(double[] h, Descriptor d, double[][] centers, double[] sigma)
	{
		if(d.getD() instanceof float[])
		{
		
			float[] desc = (float[]) d.getD();
			//l1 norm
			if(l1_norm)
			{
				double sum = 0;
				for(int x = 0 ; x < desc.length; x++)
					sum += Math.abs(desc[x]);
				if(sum == 0)
					sum = 1;
				for(int x = 0 ; x < desc.length; x++)
					desc[x] /= sum;
			}
			
			//getting list of nearest clusters by decreasing likelihood
			ArrayList<Integer> cl = nearestClusters(desc, centers, sigma);
			
			//if sparse coding option, pre-compute the coefficients resulting of the sparse optimization
			if(coding==Coding.SPARSE || coding==Coding.SPARSEC){
				double[] descd = new double[desc.length];
				for(int i=0;i<desc.length;i++){
					descd[i] = (double)(desc[i]);
				}
				if(coding==Coding.SPARSE)
					ComputeSparseCode(descd,cl,centers);
				else
					ComputeConstrainedSparseCode(descd,cl,centers);
			}
			
			
			//doing assignment for the knn
			for(int i = 0 ; i < knn; i++)
			{
				//assignment value
				double likelihood = 0;
				switch(coding)
				{
				case SOFT:
					likelihood = likelihood(desc,centers[cl.get(i)], sigma[cl.get(i)]);
					break;
				case HARD:
					likelihood = 1;
					break;
					
				case SPARSE:
				case SPARSEC:
					likelihood = SparseCode[i];
					break;

				}
				
				
				//pooling
				switch(pooling)
				{
				case SUM:
					h[cl.get(i)] += likelihood;
					break;
				case MAX:
					if(likelihood > h[cl.get(i)])
						h[cl.get(i)] = likelihood; 
					break;
				}
			}
		}
		else if (d.getD() instanceof double[])
		{
			
			double[] desc = (double[])d.getD();
			//l1 norm
			if(l1_norm)
			{
				double sum = 0;
				for(int x = 0 ; x < desc.length; x++)
					sum += Math.abs(desc[x]);
				if(sum == 0)
					sum = 1;
				for(int x = 0 ; x < desc.length; x++)
					desc[x] /= sum;
			}
			
			//getting list of nearest clusters by decreasing likelihood
			ArrayList<Integer> cl = nearestClusters(desc, centers, sigma);
			
			if(coding==Coding.SPARSE){
				ComputeSparseCode(desc,cl,centers);
			}
			else if(coding==Coding.SPARSEC){
				ComputeConstrainedSparseCode(desc,cl,centers);
			}
			
			//doing assignment for the knn
			for(int i = 0 ; i < knn; i++)
			{
				//assignment value
				double likelihood = 0;
				switch(coding)
				{
				case SOFT:
					likelihood = likelihood(desc,centers[cl.get(i)], sigma[cl.get(i)]);
					break;
				case HARD:
					likelihood = 1;
					break;
				
				case SPARSE:
				case SPARSEC:
					likelihood = SparseCode[i];
					break;
				
				}
				
				//pooling
				switch(pooling)
				{
				case SUM:
					h[cl.get(i)] += likelihood;
					break;
				case MAX:
					if(likelihood > h[cl.get(i)])
						h[cl.get(i)] = likelihood; 
					break;
				}
			}
		}
		
	}
	
	
    /**
     * Find the nearest cluster to the coordinate identified by
     * the specified index.
     */
    private static ArrayList<Integer> nearestClusters(double[] coord, double[][] centers, double[] sigma) {

    	if(doubleDistanceMap.containsKey(coord))
    	{
    		return doubleDistanceMap.get(coord);
    	}
    	else
    	{
    		int numClusters = centers.length;
    		ArrayList<Integer> list = new ArrayList<Integer>();
    		ArrayList<Double> dist = new ArrayList<Double>();
    		list.add(0);
    		dist.add(Math.exp(-squareDistance(coord, centers[0])/(sigma[0]*sigma[0])));
    		for (int c = 1; c < numClusters; c++) {

    			double d = Math.exp(-squareDistance(coord, centers[c])/(sigma[c]*sigma[c]));
    			boolean mustadd = true;

    			for(int k = 0 ; k < Math.min(list.size(), knn+1) && mustadd; k++)
    			{
    				if (d > dist.get(k))
    				{
    					list.add(k, c);
    					dist.add(k, d);
    					mustadd = false;
    				}
    			}
    			if(mustadd)
    			{
    				list.add(c);
    				dist.add(d);
    			}

    		}

    		if(list.size() != centers.length)
    			System.err.println("Erreur, nearest clusters list wrong size : "+list.size());
    		doubleDistanceMap.put(coord, list);
    		return list;
    	}
    }
    
    //caching distance map
    private static Map<float[], ArrayList<Integer>> floatDistanceMap = new Hashtable<float[], ArrayList<Integer>>();
    private static Map<double[], ArrayList<Integer>> doubleDistanceMap = new Hashtable<double[], ArrayList<Integer>>();
    
    /**
     * Find the nearest cluster to the coordinate identified by
     * the specified index.
     */
    private static ArrayList<Integer> nearestClusters(float[] coord, double[][] centers, double[] sigma) {

    	if(floatDistanceMap.containsKey(coord))
    	{
    		return floatDistanceMap.get(coord);
    	}
    	else
    	{
    		int numClusters = centers.length;
    		ArrayList<Integer> list = new ArrayList<Integer>();
    		ArrayList<Double> dist = new ArrayList<Double>();
    		list.add(0);
    		dist.add(Math.exp(-squareDistance(coord, centers[0])/(sigma[0]*sigma[0])));
    		for (int c = 1; c < numClusters; c++) {

    			double d = Math.exp(-squareDistance(coord, centers[c])/(sigma[c]*sigma[c]));
    			boolean mustadd = true;

    			for(int k = 0 ; k < Math.min(list.size(), knn+1) && mustadd; k++)
    			{
    				if (d > dist.get(k))
    				{
    					list.add(k, c);
    					dist.add(k, d);
    					mustadd = false;
    				}
    			}
    			if(mustadd)
    			{
    				list.add(c);
    				dist.add(d);
    			}

    		}

    		if(list.size() != centers.length)
    			System.err.println("Erreur, nearest clusters list wrong size : "+list.size());
    		
    		floatDistanceMap.put(coord, list);
    		return list;
    	}
    }
    

    /**
     * Compute the euclidean distance between the two arguments.
     */
    private static double squareDistance(double[] coord, double[] center) {
        int len = coord.length;
        double sumSquared = 0.0;
        for (int i=0; i<len; i++) {
            double v = coord[i] - center[i];
            sumSquared += v*v;
        }
        return sumSquared;
    }
    /**
     * Compute the euclidean distance between the two arguments.
     */
    private static double squareDistance(float[] coord, double[] center) {
        int len = coord.length;
        double sumSquared = 0.0;
        for (int i=0; i<len; i++) {
            double v = coord[i] - center[i];
            sumSquared += v*v;
        }
        return sumSquared;
    }
 
  	/**
	 * Compute the gaussian likelihood between the two arguments.
	 */
	private static double likelihood(double[] coord, double[] center, double sigma) {
		int len = center.length;
		double sumSquared = 0.0;
		for (int i = 0; i < len; i++) {
			double v = coord[i] - center[i];
			sumSquared += v * v;
		}
		return Math.exp(-sumSquared / (sigma * sigma));
	}
	/**
	 * Compute the gaussian likelihood between the two arguments.
	 */
	private static double likelihood(float[] coord, double[] center, double sigma) {
		int len = center.length;
		double sumSquared = 0.0;
		for (int i = 0; i < len; i++) {
			double v = coord[i] - center[i];
			sumSquared += v * v;
		}
		return Math.exp(-sumSquared / (sigma * sigma));
	}
	
	/**
	 * Compute the sparse code for descriptor d - minimyzing reconstruction error between d and sum.
	 */
	
	private static void ComputeSparseCode(double[] desc, ArrayList<Integer> cl, double[][] centers){
		
		// Matrix S : initial SIFT vector to approximate : S = C*Alpha + epsilon
		Matrix S = new Matrix(desc, desc.length);
		// Matrix S : Matrix containing coordinates of the k-nn Centers from S   
		double [][] ClosestCenters = new double[centers[0].length][knn];
		for(int i=0;i<knn;i++){
			for(int j=0;j<centers[0].length;j++){
				ClosestCenters[j][i] = centers[cl.get(i)][j];
			}
		}
		Matrix C = new Matrix(ClosestCenters);
		// Least-square Optimisation
		Matrix Ct = C.transpose();
		Matrix Gamma = (Ct.times(C)).inverse();
		Matrix SC = (Gamma.times(Ct)).times(S);
		
		SparseCode = new double[knn];
		for(int i=0;i<knn;i++){
			SparseCode[i] = Math.abs(SC.get(i,0));
		}	
			
//		// Debog
//		System.out.println("********************* Sparse code computation ********************");
//		Matrix Sr = C.times(SC);
//		double epsRec = 0.0;
//		for(int i=0;i<S.getRowDimension();i++){
//			epsRec += (S.get(i, 0)-Sr.get(i, 0))*(S.get(i, 0)-Sr.get(i, 0));
//		}
//		System.out.println("erreur de reconstrction= "+Math.sqrt(epsRec));
//		epsRec = 0.0;
//		for(int i=0;i<S.getRowDimension();i++){
//			epsRec += (S.get(i, 0)-C.get(i,0))*(S.get(i, 0)-C.get(i, 0));
//		}
//		System.out.println("Distance au centre le + proche = "+Math.sqrt(epsRec));
		
		
	}
	
	private static void ComputeConstrainedSparseCode(double[] desc, ArrayList<Integer> cl, double[][] centers){
		
		// Matrix S : initial SIFT vector to approximate : S = C*Alpha + epsilon
		Matrix S = new Matrix(desc, desc.length);
		// Matrix S : Matrix containing coordinates of the k-nn Centers from S   
		double [][] ClosestCenters = new double[centers[0].length][knn];
		for(int i=0;i<knn;i++){
			for(int j=0;j<centers[0].length;j++){
				ClosestCenters[j][i] = centers[cl.get(i)][j];
			}
		}
		Matrix C = new Matrix(ClosestCenters);
		// Contraint R*Alpha=r - Here we choose the constraint Sum Alpha = 1 
		// R : row vector (1,...,1) - r scalar value (1)
		double [] constraints = new double[knn];
		for(int i=0;i<knn;i++){
			constraints[i] = 1.0;
		}
		Matrix R = new Matrix(constraints, 1);
		Matrix Rt = R.transpose();
		Matrix r = new Matrix(1, 1);
		r.set(0, 0,1.0);
		// Least-square Optimisation
		Matrix Ct = C.transpose();
		Matrix Gamma = (Ct.times(C)).inverse();
		// Least square solution without constraints
		Matrix SC = (Gamma.times(Ct)).times(S);
		// Least square solution with constraint R*Alpha=r 
		Matrix SCc = SC.minus( (Gamma.times(Rt)).times( (R.times(Gamma).times(Rt) ).inverse() ).times((R.times(SC)).minus(r)) ) ;
		
		// Sparse code : abs(Alpha)
		SparseCode = new double[knn];
		for(int i=0;i<knn;i++){
			SparseCode[i] = Math.abs(SCc.get(i,0));
		}

		// Debog
		//System.out.println("********************* Constrained Sparse code computation ********************");
//		Matrix Src = C.times(SCc);
//		double sumc = 0.0;
//		for(int i=0;i<knn;i++){
//			sumc += SCc.get(i,0);
//		}
//		System.out.println("Somme alpha contraints= "+sumc);
//		double epsRecc = 0.0;
//		for(int i=0;i<S.getRowDimension();i++){
//			epsRecc+= (S.get(i, 0)-Src.get(i, 0))*(S.get(i, 0)-Src.get(i, 0));
//		}
//		System.out.println("erreur de reconstrction avec contraintes= "+Math.sqrt(epsRecc));
//		// Distance au centre le + proche 
//		epsRec = 0.0;
//		for(int i=0;i<S.getRowDimension();i++){
//			epsRec += (S.get(i, 0)-C.get(i,0))*(S.get(i, 0)-C.get(i, 0));
//		}
//		System.out.println("Distance au centre le + proche = "+Math.sqrt(epsRec));
		
	}
	
	
}
