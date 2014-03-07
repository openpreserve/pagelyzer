package JDescriptors.fr.lip6.bof;

public class VladFactory {

	/**
     * Find the nearest cluster to the coordinate identified by
     * the specified index.
     */
    public static int nearestCluster(double[] coord, double[][] centers) {
        int nearest = -1;
        double min = Double.MAX_VALUE;
        int numClusters = centers.length;
        for (int c = 0; c < numClusters; c++) {
                double d = distance(centers[c], coord);
                if (d < min) {
                    min = d;
                    nearest = c;
                }
            
        }
        return nearest;
    }
 
	/**
     * Find the nearest cluster to the coordinate identified by
     * the specified index.
     */
    public static int nearestCluster(float[] coord, double[][] centers) {
        int nearest = -1;
        double min = Double.MAX_VALUE;
        int numClusters = centers.length;
        for (int c = 0; c < numClusters; c++) {
                double d = distance(centers[c], coord);
                if (d < min) {
                    min = d;
                    nearest = c;
                }
            
        }
        return nearest;
    }
    
    /**
     * Compute the euclidean distance between the two arguments.
     */
    public static double distance(double[] coord, double[] center) {
        int len = coord.length;
        double sumSquared = 0.0;
        for (int i=0; i<len; i++) {
            double v = coord[i] - center[i];
            sumSquared += v*v;
        }
        return Math.sqrt(sumSquared);
    }
	
    
    
    /**
     * Compute the euclidean distance between the two arguments.
     */
    public static double distance(double[] center, float[] coord) {
        int len = coord.length;
        double sumSquared = 0.0;
        for (int i=0; i<len; i++) {
            double v = coord[i] - center[i];
            sumSquared += v*v;
        }
        return Math.sqrt(sumSquared);
    }
}
