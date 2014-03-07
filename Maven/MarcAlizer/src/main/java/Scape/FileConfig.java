package Scape;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class FileConfig implements Serializable{
	/**
	 * numéro de version du fichier 
	 */
	private static final long serialVersionUID = 2L;
	//path des fichiers de dictionnaire de HSV
	private ArrayList<String> dicoHsv = new ArrayList<String>();
	//path du SVM utilisé
	private String binSVM;
	
	/**
	 * Permet de lire un fichier XML de configuration de Scape et de le transformer au format lissible par le programme
	 * @param file fichier XML contenant la configuration de SCAPE
	 * @return un objet java contenant la configuration correspondant au fichier XML fournie
	 * @throws FileNotFoundException 
	 * @throws Exception
	 */
	public static FileConfig deserializeXMLToObject(String file) throws FileNotFoundException{
		FileInputStream os = new FileInputStream(file);
		XMLDecoder decoder = new XMLDecoder (os);
		Object ob = decoder.readObject();
		decoder.close();
		
		return (FileConfig)ob;
	}
	
	public static FileConfig deserializeXMLToObject(File file) throws FileNotFoundException{
		FileInputStream os = new FileInputStream(file);
		XMLDecoder decoder = new XMLDecoder (os);
		Object ob = decoder.readObject();
		decoder.close();
		
		return (FileConfig)ob;
	}
	
	public static FileConfig deserializeXMLToObject(InputStream os) throws FileNotFoundException{
		XMLDecoder decoder = new XMLDecoder (os);
		Object ob = decoder.readObject();
		decoder.close();
		
		return (FileConfig)ob;
	}
	
	
	/**
	 * permet de sauvegarder au format XML un fichier de configuration
	 * @param file nom du fichier de configuration a créer
	 * @throws Exception
	 */
	public void serializeXMLToObject(String file) throws FileNotFoundException, IOException{
		FileOutputStream os = new FileOutputStream(file);
		XMLEncoder encoder = new XMLEncoder(os);
		try{
			encoder.writeObject(this);
			encoder.flush();
		} finally{
			encoder.close();
		}
	}
	//Exemple d'option a lancer pour creer un fichier XML de configuration
	//-snapshot1 /home/lechervya/code/CArlosSureda/exemple/images/4/7.png 
	//-snapshot2 /home/lechervya/code/CArlosSureda/exemple/images/4/8.png
	//-vips1 /home/lechervya/code/CArlosSureda/exemple/xml/toto/
	//-vips2 /home/lechervya/code/CArlosSureda/exemple/xml/toto/
	//-vidiff /home/lechervya/code/CArlosSureda/exemple/xml/toto/
	//-binDecoupage /home/lechervya/code/CArlosSureda/Scape/bin/in/decoupe_image_python_sift.py
	//-binSIFT /home/lechervya/code/CArlosSureda/Scape/bin/in/colorDescriptor
	//-binSVM /home/lechervya/code/CArlosSureda/Scape/bin/in/svm
	//-dicoSift 2 /home/lechervya/code/CArlosSureda/Scape/bin/in/dictionary/sift/100/output.obj /home/lechervya/code/CArlosSureda/Scape/bin/in/dictionary/sift/200/output.obj
	//-dicoHsv 2 /home/lechervya/code/CArlosSureda/Scape/bin/in/dictionary/color/100/output.obj /home/lechervya/code/CArlosSureda/Scape/bin/in/dictionary/color/200/output.obj
	//-workDir /home/lechervya/code/CArlosSureda/exemple/work/
	//-fichier /home/lechervya/code/CArlosSureda/exemple/test.xml
	public static void main(String[] args){
		String fichier="";
		//String binDecoupage="";
		String binSVM="";
		ArrayList<String> dicoHsv = new ArrayList<String>();
		for(int i=0 ; i<args.length-1 ; i++){
			if(args[i].equals("-dicoHsv")){
				int numDicoHsv = new Integer(args[i+1]);
				i++;
				for(int j=0 ; i<args.length && j<numDicoHsv ; i++,j++){
					dicoHsv.add(args[i+1]);
				}
				continue;
			}
			if(args[i].equals("-fichier")){
				fichier = args[i+1];
				i++;
				continue;
			}
			if(args[i].equals("-binSVM")){
				binSVM = args[i+1];
				i++;
				continue;
			}
		}
		if(fichier.equals("")){
			System.err.println("Ajouter -fichier <nom du fichier de sortie>");
			return;
		}
		FileConfig f= new FileConfig(binSVM,dicoHsv);
		try {
			f.serializeXMLToObject(fichier);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Fichier XML de configuration créer");
	}
	public FileConfig(){
	}

	private FileConfig(String binSVM,ArrayList<String> dicoHsv){
		this.setBinSVM(binSVM);
		this.setDicoHsv(dicoHsv);
	}
	
	/**
	 * Permet de savoir si des dico de Hsv sont dans la configuration
	 * @return true si il y a des dico de Sift
	 */
	public boolean hasHsv(){
		return !dicoHsv.isEmpty();
	}
	/**
	 * @return the dicoHsv
	 */
	public ArrayList<String> getDicoHsv() {
		return dicoHsv;
	}
	/**
	 * @param dicoHsv the dicoHsv to set
	 */
	public void setDicoHsv(ArrayList<String> dicoHsv) {
		this.dicoHsv = dicoHsv;
	}
	/**
	 * @return the binSVM
	 */
	public String getBinSVM() {
		return binSVM;
	}
	/**
	 * @param binSVM the binSVM to set
	 */
	public void setBinSVM(String binSVM) {
		this.binSVM = binSVM;
	}
}