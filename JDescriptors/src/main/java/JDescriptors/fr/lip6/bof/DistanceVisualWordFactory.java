package JDescriptors.fr.lip6.bof;

import java.util.ArrayList;

import JDescriptors.fr.lip6.Descriptor;



public class DistanceVisualWordFactory {

	
	//norm
	public enum Norm { NONE, NB_POINTS, L1_NORM, L2_NORM };
	public static Norm norm = Norm.NONE;
	
	//entry vector norm
	public static boolean l1_norm = false;
	
	// construction variables
	public static int lines = 2;
	public static int cols = 2;
	
	
	/**
	 * Construct a Spatial Pyramid of Distance Visual Words representation
	 * @param list descriptors in the image
	 * @param centers codebook entries
	 * @param sigma codebook variances
	 * @return
	 */
	
	public static ArrayList<double[]> createDistanceBagOfWindows(ArrayList<Descriptor> list, double[][] centers, double[] sigma, int nbsigmas, int nbbins)
	{

		//create bag
		ArrayList<double[]> bag = new ArrayList<double[]>(lines*cols);
		for(int i = 0 ; i < lines*cols; i++)
			bag.add(new double[centers.length*nbbins]);
		
		
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
						addDistanceVisualWordToHist(bag.get(i*cols+j), vw, centers, sigma, nbsigmas, nbbins);
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
	
	private static void addDistanceVisualWordToHist(double[] h, Descriptor d, double[][] centers, double[] sigma, int nbsigmas, int nbbins)
	{
		int nbClusters = centers.length;
		int indice = 0;
		
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
			
			//computing Euclidean distance from the descriptor to dictionary clusters
			DistanceCenters dcl = nearestClusterswithDistance(desc, centers, sigma);
			
			for(int i = 0 ; i < nbClusters; i++)
			{
				double distmax = (double) nbsigmas* sigma[i];
			
				if ((dcl.dist.get(i)) < (int)distmax)
				{
					indice = (int)Math.floor(((dcl.dist.get(i))/distmax)*nbbins);
					h[i*nbbins+indice] += 1;
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
			
			//computing Euclidean distance from the descriptor to dictionary clusters
			DistanceCenters dcl = nearestClusterswithDistance(desc, centers, sigma);
						
			for(int i = 0 ; i < nbClusters; i++)
			{
				double distmax = (double) nbsigmas*sigma[i];

				if ((dcl.dist.get(i)) < distmax)
				{
					indice = (int)Math.floor(((dcl.dist.get(i))/distmax)*nbbins);
					h[i*nbbins+indice] += 1;
					
				}
				
			}
		}

	}

    
	/**
     * Compute the Euclidean distance from the descriptor to the nearest cluster and orders it by nearest cluster    
     */
	
    private static DistanceCenters nearestClusterswithDistance(double[] coord, double[][] centers, double[] sigma) {

    		int numClusters = centers.length;

    		ArrayList<Integer> list = new ArrayList<Integer>();
    		ArrayList<Double> dist = new ArrayList<Double>();
    		DistanceCenters dc = new DistanceCenters(list, dist);

    		list.add(0);
    		dist.add(Math.sqrt(squareDistance(coord, centers[0])));

    		for (int c = 1; c < numClusters; c++) {

    			double d = Math.sqrt(squareDistance(coord, centers[c]));
    			list.add(c);
    			dist.add(d);
    		}

    		if(list.size() != centers.length)
    			System.err.println("Erreur, nearest clusters list wrong size : "+list.size());
 		
    		dc.dist = dist;
    		dc.list = list;
    		return dc;

    }
   
    /**
     * Compute the Euclidean distance from the descriptor to the nearest cluster and orders it by nearest cluster    
     */
	private static DistanceCenters nearestClusterswithDistance(float[] coord, double[][] centers, double[] sigma) {

        int numClusters = centers.length;
        
        ArrayList<Integer> list = new ArrayList<Integer>();
        ArrayList<Double> dist = new ArrayList<Double>();
        DistanceCenters dc = new DistanceCenters(list, dist);
        
        list.add(0);
        dist.add(Math.sqrt(squareDistance(coord, centers[0])));
                
        for (int c =1 ; c < numClusters; c++) {

        double d = Math.sqrt(squareDistance(coord, centers[c]));
               
        list.add(c);
        dist.add(d);
      			
        }
        
        if(list.size() != centers.length)
        	System.err.println("Erreur, nearest clusters list wrong size : "+list.size());

        dc.dist = dist;
        dc.list = list;
        return dc;
    }
    	   
    /**
     * Compute the Euclidean distance between the two arguments.
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
     * Compute the Euclidean distance between the two arguments.
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
	
}
