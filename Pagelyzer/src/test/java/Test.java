import java.io.File;
import java.io.IOException;
import java.util.List;

import pagelyzer.Capture;
import pagelyzer.JPagelyzer;


public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * 
	 * To run this test you shoud use hybrid settings as a default type to optimize other tests (not to use capture for each type of tests)
	 */
	public static void main(String[] args) throws IOException {
		
		
		    File f= new File(args[0]);// path to file that contains  list of urls url1 url2
	        List lines =     org.apache.commons.io.FileUtils.readLines(f,"UTF-8");
	        String temp;
	        String[] urls;
	       
	        String[] pagelyzerargs = {"-config",args[1],"-url1","http://www.lip6.fr" ,"-url2","http://www.lip6.fr"};
	       // String[] pagelyzerargs = {"-config",args[1]};
	        // gibing urls not to have a config error

	        JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,false);
	        Capture capture1;
	        Capture capture2;
	        StringBuffer sb = new StringBuffer();
	       
	        double scoreimg= -100, scorexml=-100,scorehybrid = -100;
	        for(int i=0;i<lines.size();i++)
	        {
	            System.out.println("TEST " + i);
	            temp = (String) lines.get(i);
	            urls = temp.split("\t");
	            
	            capture1 = pagelyzer.GetCapture(urls[0], pagelyzer.browser1);
	            capture2 = pagelyzer.GetCapture(urls[1], pagelyzer.browser2);
	            
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","hybrid");
                pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_hybrid.xml");
                pagelyzer.comparemode = "hybrid";
                if(capture1!=null && capture2!=null)
                	scorehybrid = pagelyzer.CallMarcalizerResult(capture1, capture2);
	            
	            // test images 
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","image");
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_image.xml");
	            pagelyzer.comparemode = "image";
	            if(capture1!=null && capture2!=null)
	            	scoreimg  = pagelyzer.CallMarcalizerResult(capture1, capture2);
	            
	           // test content  
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","content");
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_content.xml");
	            pagelyzer.comparemode = "content";
	            if(capture1!=null && capture2!=null)
	            	scorexml = pagelyzer.CallMarcalizerResult(capture1, capture2);
                
                
                // test  hybrid
                
                try {
					capture1.cleanup();
					capture2.cleanup();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                sb.append(urls[0] + " " + urls[1] + " " + scoreimg + " " + scorexml +" " + scorehybrid + "\n");
                System.out.println(sb.toString());
	        }
	       
	        org.apache.commons.io.FileUtils.writeStringToFile(new File(args[2]), sb.toString());
		

	}
	
	public static void NewTest(String[] args)
	{
		 String[] pagelyzerargs = {"-config",args[0]};
		 JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,true);
		 
		 
		
		
	}

	
}
