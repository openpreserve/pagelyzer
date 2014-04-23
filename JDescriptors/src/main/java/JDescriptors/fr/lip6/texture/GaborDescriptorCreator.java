package JDescriptors.fr.lip6.texture;

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.renderable.ParameterBlock;
import java.util.ArrayList;
import java.util.Collections;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.operator.AbsoluteDescriptor;
import javax.media.jai.operator.BandCombineDescriptor;
import javax.media.jai.operator.FormatDescriptor;

import JDescriptors.fr.lip6.DescriptorCreator;
import JDescriptors.fr.lip6.detector.Detector;


public class GaborDescriptorCreator implements DescriptorCreator<GaborDescriptor> {	
	
	private final int [] filter_size = {7,11,15,19,23,27,31,35};
	private final double [] filter_ori = {0.0 , Math.PI/8.0 , Math.PI/4.0 , 3.0*Math.PI/8.0, Math.PI/2.0, 5.0 * Math.PI/8.0 , 6.0 * Math.PI/8.0, 7.0 * Math.PI/8.0};
	private final double [] sigmas = {2.8 , 4.5 , 6.3 , 8.2 , 10.2 , 12.3 , 14.6, 17.0};
	private final double [] lambdas = {3.5 , 5.6 , 7.9 , 10.3 , 12.7 , 15.4 , 18.2 , 21.2};	
	
	private Detector detector;
//	ArrayList<Filter> listOfFilters;
	ArrayList<KernelJAI> kernelList;
	private double [] meanGabor;
	private double [] stdGabor;
	private int nbProcessedDescriptors = 0;
	
	public enum Orientation { NONE, MEAN, MAX, ALL};
	private Orientation orientation = Orientation.MAX;
	
	public enum Counting { MEAN, MAX };
	private Counting counting = Counting.MAX;
	
	
	public GaborDescriptorCreator(){
		initFilters();
		
		resetStat();
	}
	
	public ArrayList<KernelJAI> getListOfFilters() {
//		return listOfFilters;
		return kernelList;
	}

	public void setListOfFilters(ArrayList<KernelJAI> kernelList) {
//		this.listOfFilters = listOfFilters;
		this.kernelList = kernelList;
	}


	public ArrayList<GaborDescriptor> createDescriptors(String imageName) {
		ArrayList<GaborDescriptor> listOfPatches = null;
		try{
			//source image
			PlanarImage image = JAI.create("fileload", imageName);

			// Convert to gray levels
			PlanarImage gray = image;
			if(image.getSampleModel().getNumBands() == 3)
				gray = ColorToGray(image);
			
			//getting filtered images
			System.out.println("filtrage");
			ArrayList<Raster> filteredImages = new ArrayList<Raster>();
			BorderExtender extender = BorderExtender.createInstance(BorderExtender.BORDER_REFLECT); 
			for(int j = 0; j < kernelList.size(); j++){
				
				KernelJAI kernel = kernelList.get(j);
				 ParameterBlock pb = new ParameterBlock();
				  pb.addSource(gray);
				  pb.add(kernel.getWidth()/2);
				  pb.add(kernel.getHeight()/2);
				  pb.add(kernel.getWidth()/2);
				  pb.add(kernel.getHeight()/2);
				  pb.add(extender);
				  PlanarImage temp =  JAI.create("border", pb);

				  PlanarImage filtered = JAI.create("convolve", temp, kernel);
				  
				  pb = new ParameterBlock();
				  pb.addSource(filtered);
				  pb.add(-kernel.getWidth()/2);
				  pb.add(-kernel.getHeight()/2);
				  pb.add(-kernel.getWidth()/2);
				  pb.add(-kernel.getHeight()/2);
				  pb.add(extender);
				  
				  PlanarImage result = JAI.create("border", pb); 
				  
				  //absolute
				  result = AbsoluteDescriptor.create(result, null);
				  
				filteredImages.add(result.getData());

				System.out.print(".");
			}
			System.out.println();


			System.out.println("fin du filtrage "+filteredImages.size()+") pour l'image "+imageName);

			// ROI Sampling
			listOfPatches = detector.getDescriptors(GaborDescriptor.class, gray);
			
			// Descriptor computation
			long time = System.currentTimeMillis();
			System.out.println("début extraction descripteurs");
			ExtractionDescripteurs(filteredImages,listOfPatches);
			System.out.println("extraction des descripteurs faite. ("+(System.currentTimeMillis()-time)+")");
			filteredImages.clear();

		}
		catch (Exception exception) {
			System.out.println("erreur lors de l'ouverture de l'image "+imageName);
			exception.printStackTrace();
		}

		return listOfPatches;
	}
	
	private void ExtractionDescripteurs(ArrayList<Raster> filteredImages,ArrayList<GaborDescriptor> patches){
		

//		double sum;//,sum2;
		for(int i=0;i<patches.size();i++){

//			double [] localfeature = new double[2*filter_size.length*filter_ori.length];
			double [] localfeature = new double[filter_size.length*filter_ori.length];

			GaborDescriptor gd = patches.get(i);
			int xmin = gd.getXmin();
			int xmax = gd.getXmax();
			int ymin = gd.getYmin();
			int ymax = gd.getYmax();
			int w = xmax - xmin;
//			int h = ymax - ymin;
			int taille_bloc = w;
			if(taille_bloc == 0)
				continue;
			
			switch(counting)
			{

			case MEAN:
				for(int k=0;k<filteredImages.size();k++){

					double sum = 0;

					Raster raster = filteredImages.get(k);
					for(int l = xmin ; l < xmax ; l++){
						for(int m = ymin ; m < ymax ; m++){
							double d = raster.getSampleFloat(l, m, 0); 
							sum += d;
							//						sum2 += d*d;
							//						System.out.print(".");
						}
					}

					double moy = sum / (double) (taille_bloc*taille_bloc);
					//mean
					//				localfeature[2*k] = moy;
					localfeature[k] = moy;
					//global stat
					//				meanGabor[2*k] += moy;
					meanGabor[k] += moy;
					//				stdGabor[2*k] += moy*moy;
					stdGabor[k] += moy*moy;
					//std
					//				localfeature[2*k+1] = std;
					//global stats
					//				meanGabor[2*k+1] += std;
					//				stdGabor[2*k+1] += std*std;
					//				System.out.print(".");
				}
				break;
				
			case MAX:
				for(int k=0;k<filteredImages.size();k++){

					double max = 0;

					Raster raster = filteredImages.get(k);
					for(int l = xmin ; l < xmax ; l++){
						for(int m = ymin ; m < ymax ; m++){
							double d = raster.getSampleFloat(l, m, 0);
							if(d > max)
								max = d;
						}
					}
					
					localfeature[k] = max;
					
					meanGabor[k] += max;
					stdGabor[k] += max*max;
					
				}				
				break;
			}

			//orientation invariance
			switch(orientation)
			{
			case NONE:
				//do nothing
				break;
			case MEAN:
				//split in 8 list
				ArrayList<ArrayList<Double>> listOfScales = new ArrayList<ArrayList<Double>>();
				for(int k= 0 ; k < 8; k++)
				{
					ArrayList<Double> l = new ArrayList<Double>();
					//				for(int j = k*16; j < (k+1)*16; j++)
					for(int j = k*8; j < (k+1)*8; j++)
						l.add(localfeature[j]);
					listOfScales.add(l);
				}
				//search max orient
				double sumOrient[] = new double[8];
				for(int k = 0 ; k < 8; k++)
					for(int j = 0 ; j < 8; j++)
					{
						//				sumOrient[k] += localfeature[(k+j*8)*2];
						sumOrient[k] += localfeature[(k+j*8)];
					}
				double max = Double.NEGATIVE_INFINITY;
				int index = 0;
				for(int k = 0; k < sumOrient.length; k++)
				{
					if(sumOrient[k] > max)
					{
						max = sumOrient[k];
						index = k;
					}
				}
				for(ArrayList<Double> l : listOfScales)
				{
					//				Collections.rotate(l, -index*2);
					Collections.rotate(l, -index);
				}

				for(int k= 0 ; k < 8; k++)
				{
					ArrayList<Double> l = listOfScales.get(k);
					//				for(int j = 0; j < 16; j++)
					//					localfeature[k*16+j] = l.get(j);
					for(int j = 0; j < 8; j++)
						localfeature[k*8+j] = l.get(j);
				}
				break;
			case MAX:
				//search for max orientation
				max = Double.NEGATIVE_INFINITY;
				index = 0;
				for(int j = 0 ; j < localfeature.length; j++)
					if(localfeature[j] > max)
					{
						index = 0;
						max = localfeature[j];
					}
				//get max orientation
				int index_max = index % 8;
				
				//split in scales lists
				listOfScales = new ArrayList<ArrayList<Double>>();
				for(int k= 0 ; k < 8; k++)
				{
					ArrayList<Double> l = new ArrayList<Double>();
					//				for(int j = k*16; j < (k+1)*16; j++)
					for(int j = k*8; j < (k+1)*8; j++)
						l.add(localfeature[j]);
					//rotate
					Collections.rotate(l, -index_max);
					listOfScales.add(l);
				}
				//final feature
				for(int k= 0 ; k < 8; k++)
				{
					ArrayList<Double> l = listOfScales.get(k);
					//				for(int j = 0; j < 16; j++)
					//					localfeature[k*16+j] = l.get(j);
					for(int j = 0; j < 8; j++)
						localfeature[k*8+j] = l.get(j);
				}
				break;
			case ALL:
				//search for max orientation
				max = Double.NEGATIVE_INFINITY;
				index = 0;
				ArrayList<Double> l = new ArrayList<Double>();
				for(int j = 0 ; j < localfeature.length; j++)
				{
					if(localfeature[j] > max)
					{
						index = 0;
						max = localfeature[j];
					}
					l.add(localfeature[j]);
				}
				//rotate
				Collections.rotate(l, -index);
				//final descriptor
				for(int j = 0; j < l.size(); j++)
					localfeature[j] = l.get(j);
				break;
			}

			gd.setD(localfeature);
			//stats
			nbProcessedDescriptors++;
			if( i%(patches.size()/20) == 0)
				System.out.print(".");

		}				
	}

	/**
	 * initialise les filtres
	 */
	private void initFilters(){
		
		kernelList = new ArrayList<KernelJAI>();
		for(int i=0;i<filter_size.length;i++){
			for(int j=0;j<filter_ori.length;j++){
//				GaborFilter f = new GaborFilter(filter_size[i],filter_ori[j],sigmas[i],lambdas[i]);
				kernelList.add(new KernelJAI(filter_size[i], filter_size[i], GaborFilterFactory.getGaborFilter(filter_size[i], filter_ori[j], sigmas[i], lambdas[i])));
			}
		}
	}

	public static PlanarImage ColorToGray(PlanarImage i) {
		double[][] matrix = { {0.114D, 0.587D, 0.299D, 0.0D} };


		if (i.getSampleModel().getNumBands() != 3) {
			throw new IllegalArgumentException("Image # bands <> 3");
		}

		PlanarImage result = BandCombineDescriptor.create(i, matrix, null);

		result = FormatDescriptor.create(result, DataBuffer.TYPE_FLOAT, null);

		return result;
	} 

	
	public Detector getDetector() {
		return detector;
	}

	public void setDetector(Detector detector) {
		this.detector = detector;
	}
	
	public void resetStat()
	{
		meanGabor = new double[128];
		stdGabor = new double[128];
		
		nbProcessedDescriptors = 0;
	}

	public double[] getMeanGabor() {
		return meanGabor;
	}

	public double[] getStdGabor() {
		return stdGabor;
	}

	public int getNbProcessedDescriptors() {
		return nbProcessedDescriptors;
	}

	public Orientation getOrientation() {
		return orientation;
	}

	public void setOrientation(Orientation orientation) {
		this.orientation = orientation;
	}

	public Counting getCounting() {
		return counting;
	}

	public void setCounting(Counting counting) {
		this.counting = counting;
	}
	
}
