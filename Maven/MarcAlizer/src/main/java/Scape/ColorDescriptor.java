package Scape;
import java.io.File;
import java.io.IOException;


public class ColorDescriptor {
	/**
	 * Permet de calculer les descripteurs SIFT d'un dossier d'images
	 * @param input dossiers contenant les images a traiter
	 * @param output dossiers où placer les descripteurs SIFT correspondant à chaque image 
	 * @param colorDescriptor path du programme de conversion
	 */
	public static void run(File repInput,String new_path,String colorDescriptor){
		String s;
		String out;
		Process p;
					
		File featureDir = new File(new_path+"/"+repInput.getName());
		if(!featureDir.exists()){
			if(!featureDir.mkdirs()){
				System.err.println("Erreur dans la création du repertoire "+new_path+"/"+repInput.getName());
				System.exit(-1);
			}
		}
		for(File img: repInput.listFiles()){
			if(img.getName().substring(img.getName().length()-4).equals(".png")){
				s = repInput.getAbsolutePath()+"/"+img.getName();
				out = new_path+"/"+repInput.getName()+"/"+img.getName().substring(0, img.getName().length()-4);
				try {
					p = Runtime.getRuntime().exec(colorDescriptor+" "+s+" --detector densesampling --ds_spacing 24 --descriptor sift --output "+out+".txt");
					p.waitFor();
				} catch (IOException e) {e.printStackTrace();}
				catch (InterruptedException e) {e.printStackTrace();}
			}
			
		}

	}	
}
