package pagelyzer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;


/**
 * 
 * @author pehlivanz
 * 
 *	This class is used to train the data 
 *	and to create the necessary input for pagelyzer : svm files
 *
 *
 *  First argument shoulb the path to config file same as pagelyzer
 *  Second argument should be a file that has the structure as follows:
 * URL1 \t URL2 \t \ANNOTATION 
 */

public class Train {

	
	
	public static void main(String[] args) {
		
	    String[] pagelyzerargs = {"-config",args[0]};
	    JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,true);

	  
		FileReader fr=null;
		
		BufferedReader r=null;
		try {
			fr = new FileReader(args[1]);
			r = new BufferedReader(fr);
		}
		catch (FileNotFoundException e) {e.printStackTrace();} 
		
		try {
			
			while(r.ready()) {
				String []l=r.readLine().split("\t"); // 0 = url1, 1 = url2, 2 = label
		       // System.out.println(l[0]);
	           // System.out.println(l[1]);
	           // System.out.println(l[2]);

		        pagelyzer.changeDetection(l[0], l[1],l[2]);
	
	            
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pagelyzer.sc.train();
		pagelyzer.sc.saveSVM(pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir"));
		
		
		System.out.println("SVM results are successfully saved. You can start to use pagelyzer ! ");
	}

}
