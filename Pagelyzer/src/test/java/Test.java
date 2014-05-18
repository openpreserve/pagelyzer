import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

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
		
		//TestFromFiles(args);
		TestONLINEfromUrls(args);

	}
	
	public static void TestONLINEfromUrls(String[] args) throws IOException 
	{
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
              pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_hybrid.xml";
	            pagelyzer.comparemode = "hybrid";
	            try {
	            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
		              } catch (Exception ex) {
		                  System.err.println("Marcalize could not be initialized");
		                  System.exit(0);
		              }

              if(capture1!=null && capture2!=null)
              	scorehybrid = pagelyzer.CallMarcalizerResult(capture1, capture2);
	            
	            // test images 
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","image");
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_image.xml");
	            pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_image.xml";
	            pagelyzer.comparemode = "image";
	            try {
	            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
		              } catch (Exception ex) {
		                  System.err.println("Marcalize could not be initialized");
		                  System.exit(0);
		              }
	            if(capture1!=null && capture2!=null)
	            	scoreimg  = pagelyzer.CallMarcalizerResult(capture1, capture2);
	            
	           // test content  
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","content");
	            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_content.xml");
	            pagelyzer.comparemode = "content";
	            pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_content.xml";
	            try {
	            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
		              } catch (Exception ex) {
		                  System.err.println("Marcalize could not be initialized");
		                  System.exit(0);
		              }
	            if(capture1!=null && capture2!=null)
	            	scorexml = pagelyzer.CallMarcalizerResult(capture1, capture2);
              
              
              // test  hybrid
	            
	  	          
		         capture1.result.saveDebugFile("/home/pehlivanz/SCAPE_ZP/Roc/TESTIM/page"+ (i+1) + "_1" );
		         capture2.result.saveDebugFile("/home/pehlivanz/SCAPE_ZP/Roc/TESTIM/page"+ (i+1) + "_2" );
		         
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
	/* as for test ddataset the online versions on IMF were changing frequently we saved all the files on disk 
	 * and we did the annotation manually again */
	 
	public static void TestFromFiles(String[] args) throws IOException
	{
		File f= new File(args[0]);// path to file that contains  list of urls url1 url2
        List lines =     org.apache.commons.io.FileUtils.readLines(f,"UTF-8");
        String temp;
        String[] urls;
       
        String[] pagelyzerargs = {"-config",args[1],"-url1","http://www.lip6.fr" ,"-url2","http://www.lip6.fr"};
       // String[] pagelyzerargs = {"-config",args[1]};
        // gibing urls not to have a config error

        JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,false);
        StringBuffer sb = new StringBuffer();
       
        double scoreimg= -100, scorexml=-100,scorehybrid = -100;
        String parent = args[3]; // where to find files
        String page1_xml,page1_img;
        String page2_xml,page2_img;
        String label;
        for(int i=0;i<lines.size();i++)
        {
            System.out.println("TEST " + i);
            temp = (String) lines.get(i);
            urls = temp.split("\t");
            
            label = urls[2];
            
            page1_xml = parent+(i+1)+"_1.xml";
            page2_xml = parent+(i+1)+"_2.xml";
            page1_img = parent+(i+1)+"_1";
            page2_img = parent+(i+1)+"_2";
       
           
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","hybrid");
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_hybrid.xml");
            pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_hybrid.xml";
            pagelyzer.comparemode = "hybrid";
            try {
            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
	              } catch (Exception ex) {
	                  System.err.println("Marcalize could not be initialized");
	                  System.exit(0);
	              }
if(new File(page1_xml).exists() && new File(page2_xml).exists())
{
           
            scorehybrid = pagelyzer.marcalizer.run(new Scanner(new File(page1_xml)).useDelimiter("\\Z").next(),new Scanner(new File(page2_xml)).useDelimiter("\\Z").next(),ImageIO.read(new File(page1_img)),ImageIO.read(new File(page2_img)));
            
            // test images 
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","image");
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_image.xml");
            pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_image.xml";
            pagelyzer.comparemode = "image";
            try {
            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
	              } catch (Exception ex) {
	                  System.err.println("Marcalize could not be initialized");
	                  System.exit(0);
	              }
            
            	scoreimg  = pagelyzer.marcalizer.run(ImageIO.read(new File(page1_img)),ImageIO.read(new File(page2_img)));
            
           // test content  
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.mode","content");
            pagelyzer.config.setProperty("pagelyzer.run.default.comparison.file","ex_content.xml");
            pagelyzer.comparemode = "content";
            pagelyzer.cfile = pagelyzer.config.getString("pagelyzer.run.default.comparison.subdir")+ "ex_content.xml";
            try {
            	pagelyzer.marcalizer.init(new File(pagelyzer.cfile));
	              } catch (Exception ex) {
	                  System.err.println("Marcalize could not be initialized");
	                  System.exit(0);
	              }
            
            scorexml = pagelyzer.marcalizer.run(new Scanner(new File(page1_xml)).useDelimiter("\\Z").next(),new Scanner(new File(page2_xml)).useDelimiter("\\Z").next());
            
            
      
          
            sb.append(i + " " + urls[0] + " " + urls[1] + " " +    urls[3] + " " +  label + " " + scoreimg + " " + scorexml +" " + scorehybrid + "\n");
            System.out.println(sb.toString());
           
        }
       
        try {
			org.apache.commons.io.FileUtils.writeStringToFile(new File(args[2]), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
        }
		
	}

	
}
