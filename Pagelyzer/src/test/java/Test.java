import java.io.File;
import java.io.IOException;
import java.util.List;

import pagelyzer.JPagelyzer;


public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		
		    File f= new File(args[0]);// path to file that contains  list of urls url1 url2
	        List lines =     org.apache.commons.io.FileUtils.readLines(f,"UTF-8");
	        String temp;
	        String[] urls;
	       
	        //String[] pagelyzerargs = {"-get","score","-config","src/main/resources/ext/config.xml","-url1","http://www.lip6.fr" ,"-url2","http://www.lip6.fr"};
	        String[] pagelyzerargs = {"-config","/home/pehlivanz/Bureau/SettingsFiles/config.xml"};
	        // gibing urls not to have a config error

	        JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,false);
	        
	        StringBuffer sb = new StringBuffer();
	        double scoreimg, scorexml,scorehybrid;
	        for(int i=0;i<lines.size();i++)
	        {
	           
	            temp = (String) lines.get(i);
	            urls = temp.split(" ");
	            
	            // test images 
	            pagelyzer.getConfig().set("pagelyzer.run.default.comparison.mode","images");
                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.file","ex_images.xml");
	            scoreimg = pagelyzer.changeDetection(urls[0],urls[1]);
	            
	           // test content  
	            pagelyzer.getConfig().set("pagelyzer.run.default.comparison.mode","structure");
                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.file","ex_structure.xml");
                scorexml = pagelyzer.changeDetection(urls[0],urls[1]);
                
                
                // test  hybrid
                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.mode","hybrid");
                pagelyzer.getConfig().set("pagelyzer.run.default.comparison.file","ex_hybrid.xml");
                scorehybrid = pagelyzer.changeDetection(urls[0],urls[1]);
                
                sb.append(urls[0] + " " + urls[1] + " " + scoreimg + " " + scorexml +" " + scorehybrid + "\n");
	        }
	       
	        org.apache.commons.io.FileUtils.writeStringToFile(new File("/home/pehlivanz/SCAPE_ZP/Pagelyzer/testIM_450resultwithName.txt"), sb.toString());
		

	}

}
