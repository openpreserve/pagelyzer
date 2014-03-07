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
import javax.media.jai.operator.FormatDescriptor;

import JDescriptors.fr.lip6.DescriptorCreator;
import JDescriptors.fr.lip6.detector.Detector;


public class MacroGaborDescriptorCreator implements DescriptorCreator<GaborDescriptor> {	
	
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
	private Orientation orientation = Orientation.NONE;
	
	public enum Counting { MEAN, MAX };
	private Counting counting = Counting.MEAN;
	
	
	public MacroGaborDescriptorCreator(){
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

				//				JAI.create("filestore", filteredImages.get(j), "filtered_"+j+".png", "PNG");

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

			double [] localfeature = new double[filter_size.length*filter_ori.length*4];

			GaborDescriptor gd = patches.get(i);
			int xmin = gd.getXmin();
			int xmax = gd.getXmax();
			int ymin = gd.getYmin();
			int ymax = gd.getYmax();
			int w = xmax - xmin;
			int h = ymax - ymin;
			int taille_bloc = w;
			if(taille_bloc == 0)
				continue;
			
			switch(counting)
			{

			case MEAN:
				for(int k=0;k<filteredImages.size()*4;k+=4){

					//top-left corner
					double sum = 0;
					Raster raster = filteredImages.get(k/4);
					for(int l = xmin ; l < xmin+w/2 ; l++){
						for(int m = ymin ; m < ymin+h/2 ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							sum += d;
						}
					}

					double moy = sum / (double) (taille_bloc*taille_bloc);
					localfeature[k] = moy;
					//global stat
					meanGabor[k] += moy;
					stdGabor[k] += moy*moy;
					
					//top-left corner
					sum = 0;
					for(int l = xmin + w/2 ; l < xmax ; l++){
						for(int m = ymin ; m < ymin+h/2 ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							sum += d;
						}
					}

					moy = sum / (double) (taille_bloc*taille_bloc);
					localfeature[k+1] = moy;
					//global stat
					meanGabor[k+1] += moy;
					stdGabor[k+1] += moy*moy;
					
					//bottom-left corner
					sum = 0;
					for(int l = xmin ; l < xmin+w/2 ; l++){
						for(int m = ymin + h/2 ; m < ymax ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							sum += d;
						}
					}
					moy = sum / (double) (taille_bloc*taille_bloc);
					localfeature[k+2] = moy;
					//global stat
					meanGabor[k+2] += moy;
					stdGabor[k+2] += moy*moy;
					
					//bottom-right corner
					sum = 0;
					for(int l = xmin+w/2 ; l < xmax ; l++){
						for(int m = ymin + h/2 ; m < ymax ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							sum += d;
						}
					}
					moy = sum / (double) (taille_bloc*taille_bloc);
					localfeature[k+3] = moy;
					//global stat
					meanGabor[k+3] += moy;
					stdGabor[k+3] += moy*moy;
				}
				break;
				
			case MAX:
				for(int k=0;k<filteredImages.size()*4;k+=4){

					//top-left corner
					double max = 0;
					Raster raster = filteredImages.get(k/4);
					for(int l = xmin ; l < xmin+w/2 ; l++){
						for(int m = ymin ; m < ymin+h/2 ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0));
							if(d > max)
								max = d;
						}
					}

					double moy = max ;
					localfeature[k] = moy;
					//global stat
					meanGabor[k] += moy;
					stdGabor[k] += moy*moy;
					
					//top-left corner
					max = 0;
					for(int l = xmin + w/2 ; l < xmax ; l++){
						for(int m = ymin ; m < ymin+h/2 ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							if(d > max)
								max = d;
						}
					}

					moy = max ;
					localfeature[k+1] = moy;
					//global stat
					meanGabor[k+1] += moy;
					stdGabor[k+1] += moy*moy;
					
					//bottom-left corner
					max = 0;
					for(int l = xmin ; l < xmin+w/2 ; l++){
						for(int m = ymin + h/2 ; m < ymax ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							if(d > max)
								max = d;
						}
					}
					moy = max ;
					localfeature[k+2] = moy;
					//global stat
					meanGabor[k+2] += moy;
					stdGabor[k+2] += moy*moy;
					
					//bottom-right corner
					max = 0;
					for(int l = xmin+w/2 ; l < xmax ; l++){
						for(int m = ymin + h/2 ; m < ymax ; m++){
							double d = Math.abs(raster.getSampleFloat(l, m, 0)); 
							if(d > max)
								max = d;
						}
					}
					moy = max ;
					localfeature[k+3] = moy;
					//global stat
					meanGabor[k+3] += moy;
					stdGabor[k+3] += moy*moy;
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
				for(int k= 0 ; k < filter_size.length; k++)
				{
					ArrayList<Double> l = new ArrayList<Double>();
					//				for(int j = k*16; j < (k+1)*16; j++)
					for(int j = k*filter_ori.length*4; j < (k+1)*filter_ori.length*4; j+=4)
					{
						l.add(localfeature[j]);
						l.add(localfeature[j+1]);
						l.add(localfeature[j+2]);
						l.add(localfeature[j+3]);
					}
					listOfScales.add(l);
				}
				//search max orient
				double sumOrient[] = new double[8];
				for(int k = 0 ; k < 8; k++)
					for(int j = 0 ; j < filter_ori.length; j++)
					{
						sumOrient[k] += localfeature[(k+j*filter_ori.length*4)];
						sumOrient[k+1] += localfeature[(k+1+j*filter_ori.length*4)];
						sumOrient[k+2] += localfeature[(k+2+j*filter_ori.length*4)];
						sumOrient[k+3] += localfeature[(k+3+j*filter_ori.length*4)];
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
				index = index*4;  // macro block are 4 bins in width
				for(ArrayList<Double> l : listOfScales)
				{
					//				Collections.rotate(l, -index*2);
					Collections.rotate(l, -index);
				}

				for(int k= 0 ; k < 8; k++)
				{
					ArrayList<Double> l = listOfScales.get(k);
					for(int j = 0; j < l.size(); j++)
						localfeature[k*filter_ori.length*4+j] = l.get(j);
				}
				break;
			case MAX:
				//search for max orientation
				max = Double.NEGATIVE_INFINITY;
				index = 0;
				for(int j = 0 ; j < localfeature.length; j+=4)
				{
					double localmax = Double.NEGATIVE_INFINITY;
					for(int l = 0 ; l < 4 ; l++)
						if(localfeature[j+l] > localmax)
						{
							localmax = localfeature[j+l];
						}
					if(localmax > max)
					{
						index = 0;
						max = localmax;
					}
				}
				//get max orientation
				int index_max = index % filter_ori.length*4;
				index_max = index_max*4; //macro block are 4 bins in width
				
				//split in scales lists
				listOfScales = new ArrayList<ArrayList<Double>>();
				for(int k= 0 ; k < filter_size.length; k++)
				{
					ArrayList<Double> l = new ArrayList<Double>();
					//				for(int j = k*16; j < (k+1)*16; j++)
					for(int j = k*filter_ori.length*4; j < (k+1)*filter_ori.length*4; j+=4)
					{
						l.add(localfeature[j]);
						l.add(localfeature[j+1]);
						l.add(localfeature[j+2]);
						l.add(localfeature[j+3]);
					}
					//rotate
					Collections.rotate(l, -index_max);
					listOfScales.add(l);
				}
				//final feature
				for(int k= 0 ; k < filter_size.length; k++)
				{
					ArrayList<Double> l = listOfScales.get(k);
					//				for(int j = 0; j < 16; j++)
					//					localfeature[k*16+j] = l.get(j);
					for(int j = 0; j < l.size(); j++)
						localfeature[k*l.size()+j] = l.get(j);
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

		ParameterBlock pb = new ParameterBlock();
		pb.addSource(i);
		pb.add(matrix);

		PlanarImage result = JAI.create("bandcombine", pb, null);

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
		meanGabor = new double[filter_size.length*filter_ori.length*4];
		stdGabor = new double[filter_size.length*filter_ori.length*4];
		
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
