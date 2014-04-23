package JDescriptors.fr.lip6.clustering;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class ThreadedKMeans implements Serializable 
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -253576488350574703L;

	private int nbThread = 1;
	
	float[][] points;
	double[][] centers;
	double[] meanDistance;
	int[] populationInCluster;
	
	int nbCenters;
	int dimension = -1;
	
	int pointAssignedToCenter[];
	double distanceMatrix[][];
	boolean hasMoved[];
	
	int maxIterations = 10000;

	public ThreadedKMeans(float[][] points, int nbCenters, int maxIterations, int nbThreads)
	{
		this.points = points;
		this.nbCenters = nbCenters;
		this.maxIterations = maxIterations;
		this.nbThread = nbThreads;
		
		//1. init centers
		initCenters();
		
		//2. starting kmeans
		System.out.println("Starting clustering with "+nbThreads+" threads.");
		int nbMoves = 0;
		int iteration = 1;
		do
		{

			//compute centers
			computeCenters();
			
			//compute distance
			computeDistance();
			
			//make Assignement
			nbMoves = makeAssignment();
			
			//number of moving centers
			int nbmc = 0;
			for(int c = 0 ; c < centers.length; c++)
				if(hasMoved[c])
					nbmc++;
			
			System.out.println("iteration "+iteration+", "+nbMoves+" points moved, "+nbmc+" centers moved.");
			iteration++;
		}
		while(nbMoves != 0 && iteration < maxIterations);
		System.out.println("Clustering done, cleaning...");
		
		//store variences also
		Arrays.fill(meanDistance, 0);
		for(int c = 0 ; c < centers.length; c++)
		{
			int nbp = 0;
			for(int i = 0; i < points.length; i++)
			{
				if(pointAssignedToCenter[i] == c)
				{
					meanDistance[c] += distanceMatrix[i][c];
					nbp ++;
				}
			}
			if(nbp > 0)
				meanDistance[c] /= (double) nbp;
		}
		
		//cleaning 0 clusters
		ArrayList<double[]> listOfCenters = new ArrayList<double[]>();
		ArrayList<Double> listOfMeanDist = new ArrayList<Double>();
		ArrayList<Integer> listOfPopulation = new ArrayList<Integer>();
		for(int c = 0 ; c < centers.length; c++)
		{
			if(populationInCluster[c] > 0)
			{
				listOfCenters.add(centers[c]);
				listOfMeanDist.add(meanDistance[c]);
				listOfPopulation.add(populationInCluster[c]);
			}
		}
		
		centers = new double[listOfCenters.size()][];
		meanDistance = new double[listOfCenters.size()];
		populationInCluster = new int[listOfCenters.size()];
		for(int c = 0 ; c < centers.length; c++)
		{
			centers[c] = listOfCenters.get(c);
			meanDistance[c] = listOfMeanDist.get(c);
			populationInCluster[c] = listOfPopulation.get(c);
		}
		
		System.out.println("Cleaning done. Clusters are ready.");
	}
	
	private void computeCenters() {
		for(int c = 0 ; c < centers.length; c++)
		{

			if(!hasMoved[c])
				continue;
			if(populationInCluster[c] == 0)
			{
				Arrays.fill(centers[c], Double.NaN);
				continue;
			}
			
			int nbp = 0;
			Arrays.fill(centers[c], 0);
			for(int i = 0 ; i < points.length; i++)
			{
				if(pointAssignedToCenter[i] == c)
				{
					for(int d = 0 ; d < dimension; d++)
						centers[c][d] += points[i][d];
					nbp++;
				}
			}
//			System.out.println("nbp : "+nbp+" populationInCluster[c] : "+populationInCluster[c]);
			if(nbp > 0)
				for(int d = 0 ; d < dimension; d++)
					centers[c][d] /= (double)nbp;
			else
				Arrays.fill(centers[c], Double.NaN);
		}
	}

	private int makeAssignment() {

		int nbm = 0;
		
		Arrays.fill(populationInCluster, 0);
		Arrays.fill(hasMoved, false);
		
		for(int i = 0 ; i < points.length; i++)
		{
			//find distance min
			int indexMin = 0;
			double dist = distanceMatrix[i][0];
			for(int m = 0 ; m < centers.length; m++)
			{
				if(distanceMatrix[i][m] < dist)
				{
					dist = distanceMatrix[i][m];
					indexMin = m;
				}
			}
			
			populationInCluster[indexMin]++;
			
			//compare to original
			int oldIndex = pointAssignedToCenter[i];
			if(oldIndex != indexMin)
			{
				//got one more move
				nbm++;
				//centers will change
				hasMoved[indexMin] = true;
				if(oldIndex != -1)
					hasMoved[oldIndex] = true;
				
				//make assignment
				pointAssignedToCenter[i] = indexMin;
			}
		}
		
		return nbm;
	}


	int startedThread = 0;
	int stoppedThread = 0;
	private void computeDistance() {
		
		startedThread = 0;
		stoppedThread = 0;
		int pointsPerBloc = points.length / nbThread +1;
		
		for(int n = 0 ; n < nbThread; n++)
		{
			final int startIndex = n*pointsPerBloc;
			final int stoppIndex = Math.min((n+1)*pointsPerBloc, points.length);
			startedThread++;
//			System.out.println("starting thread "+startedThread+" from "+startIndex+" to "+stoppIndex);
			(new Thread(){

				public void run()
				{
					for(int i = startIndex ; i < stoppIndex; i++)
					{
						for(int c = 0 ; c < centers.length; c++)
						{
							//if center didn't move, continue
							if(!hasMoved[c])
								continue;
							
							//if hasn't points in it, continue;
							if(populationInCluster[c] == 0)
							{
								distanceMatrix[i][c] = Double.POSITIVE_INFINITY;
								continue;
							}

							double distance = 0;
							float[] p = points[i];
							double[] center = centers[c];

							for(int d = 0; d < dimension; d++)
								distance += (p[d]-center[d])*(p[d]-center[d]);

							if(Double.isNaN(distance))
								distance = Double.POSITIVE_INFINITY;
							distanceMatrix[i][c] = Math.sqrt(distance);
						}
					}
					synchronized(centers)
					{
						stoppedThread++;
					}
//					System.out.println("stopping thread "+stoppedThread);
				};
			}).start();
		}
		
		while(stoppedThread != nbThread)
		{
			Thread.yield();
		}
	}

	private void initCenters()
	{
		
		dimension = points[0].length;
		centers = new double[nbCenters][dimension];
		meanDistance = new double[nbCenters];
		populationInCluster = new int[centers.length];
		Arrays.fill(populationInCluster, 0);
		
		//random assignement
		System.out.println("Initializing centers...");
		pointAssignedToCenter = new int[points.length];
		//-1 == no assignement
		Arrays.fill(pointAssignedToCenter, -1);
		Random ran = new Random(System.currentTimeMillis());
		//pick a random point for each cluster
		for(int i = 0 ; i < centers.length; i++)
		{
			int indexPoint = ran.nextInt(points.length);
			pointAssignedToCenter[indexPoint] = i;
			populationInCluster[i]++;
			if(i%(centers.length/20 + 1) == 0)
				System.out.print(".");
		}
		System.out.println();
		
		//distance matrix and has moved
		distanceMatrix = new double[points.length][centers.length];
		hasMoved = new boolean[centers.length];
		Arrays.fill(hasMoved, true);
		
		System.out.println("Centers randomly initialized.");
	}


	
	/**
	 * gets the centers of the clusters
	 * @return
	 */
	public double[][] getCenters()
	{
		return centers;
	}
	
	/**
	 * get the mean distance of each each cluster regarding its points
	 * @return
	 */
	public double[] getMeanDistance() {
		return meanDistance;
	}

	/**
	 * get the number of points in each cluster
	 * @return
	 */
	public int[] getPopulationInCluster() {
		return populationInCluster;
	}
}
