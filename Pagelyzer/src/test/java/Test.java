import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

import javax.imageio.ImageIO;

import org.apache.commons.codec.net.QCodec;

import pagelyzer.Capture;
import pagelyzer.JPagelyzer;


public class Test {

	/**
	 * @param args
	 * @throws IOException 
	 * args[0]  is a file that contains list of urls pairs to test url1 \t url2 
	 * args[1] = config file the same used with pagelyzer 
	 * args[2] = path file to save the results 
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

	        

	        JPagelyzer pagelyzer = new JPagelyzer(pagelyzerargs,false);
	        Capture capture1;
	        Capture capture2;
	        StringBuffer sb = new StringBuffer();
	       
	        double score = 0;
	        for(int i=0;i<lines.size();i++)
	        {
	            System.out.println("TEST " + i);
	            temp = (String) lines.get(i);
	            urls = temp.split("\t");
	            
	            capture1 = pagelyzer.GetCapture(urls[0], pagelyzer.browser1);
	            capture2 = pagelyzer.GetCapture(urls[1], pagelyzer.browser2);

	            if(capture1!=null && capture2!=null)
	            	score = pagelyzer.CallMarcalizerResult(capture1, capture2);
 
              try {
					capture1.cleanup();
					capture2.cleanup();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
              
         
              sb.append(i + "\t" + urls[0] + "\t" + urls[1] + "\t" + score  + "\n");
              System.out.println(sb.toString());
             
	        }
	       
	        org.apache.commons.io.FileUtils.writeStringToFile(new File(args[2]), sb.toString());
		
	}
	/* as for test dataset the online versions on IMF were changing frequently we saved all the files on disk 
	 * and we did the annotation manually again Thus we used the funtion below */
	 
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
       
        double score= -100, scorexml=-100,scorehybrid = -100;
        String parent = args[3]; // where to find files
        String page1_xml,page1_img;
        String page2_xml,page2_img;
        String label;
        int overallcounter=0;
        int countcorrect = 0;
        for(int i=0;i<lines.size();i++)
        {
            System.out.println("TEST " + i);
            temp = (String) lines.get(i);
            urls = temp.split("\t");
            
           // label = urls[2];
            
            page1_xml = parent+(urls[0])+"_1.xml";
            page2_xml = parent+(urls[0])+"_2.xml";
            page1_img = parent+(urls[0])+"_1";
            page2_img = parent+(urls[0])+"_2";

           if(i==10)
        	   page1_xml = parent+(urls[0])+"_1.xml";
         
			if(new File(page1_xml).exists() && new File(page2_xml).exists())
			{
				if(pagelyzer.comparemode.equals("hybrid") )
					score = pagelyzer.marcalizer.run(new Scanner(new File(page1_xml)).useDelimiter("\\Z").next(),new Scanner(new File(page2_xml)).useDelimiter("\\Z").next(),ImageIO.read(new File(page1_img)),ImageIO.read(new File(page2_img)));
            
				else if(pagelyzer.comparemode.equals("image"))
					score  = pagelyzer.marcalizer.run(ImageIO.read(new File(page1_img)),ImageIO.read(new File(page2_img)));
				else 
					score = pagelyzer.marcalizer.run(new Scanner(new File(page1_xml)).useDelimiter("\\Z").next(),new Scanner(new File(page2_xml)).useDelimiter("\\Z").next());
            
        
            sb.append(urls[0] + "\t" + urls[1] + "\t" + urls[2] + "\t" + urls[4] + "\t" +  score  + "\n");
            System.out.println(sb.toString());
          
           
        }

        }
        

        try {
			org.apache.commons.io.FileUtils.writeStringToFile(new File(args[2]), sb.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
      
	}
	
	
}
