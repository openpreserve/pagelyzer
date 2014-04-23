package JDescriptors.fr.lip6.texture;

public class GaborFilterFactory {
	
	private static final double gamma=0.3;
	
	public static float[] getGaborFilter(int size, double orientation, double sigma, double lambda)
	{
		
		double fact = 1.0;
		
		float[] data = new float[size*size];
		
		double sum = 0.0;
		
		for(int i=0;i<size;i++){
			for(int j=0;j<size;j++){
				
				double X = i - size/2;
				double Y = j - size/2;
				double X1 = X*Math.cos(orientation) - Y*Math.sin(orientation);
				double Y1 = X*Math.sin(orientation) + Y*Math.cos(orientation);
				
				double intexp = (X1*X1 + gamma*gamma*Y1*Y1)/(2.0*sigma*sigma);
				data[i*size+j] =  (float)( Math.exp(-1.0*intexp) * Math.cos(2*Math.PI*X1/lambda) );	
				
				sum += data[i*size+j];
				
			}			
		}
		
//		double norm = 0;
//		//centrage
//		for(int i=0;i<size;i++){
//			for(int j=0;j<size;j++){
//				data[i*size+j] -= sum;
//				norm += data[i*size+j]*data[i*size+j];
//			}
//		}
//		norm = Math.sqrt(norm);		
//		//norme l2 à 1
//		for(int i=0;i<size;i++){
//			for(int j=0;j<size;j++){
////				data[i*size+j] /= norm;
//				data[i*size+j] /= sum;
//			}
//		}
				
		return data;
		
	}

}
