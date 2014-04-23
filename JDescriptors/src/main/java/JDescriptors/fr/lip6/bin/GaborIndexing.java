package JDescriptors.fr.lip6.bin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import JDescriptors.fr.lip6.detector.Detector;
import JDescriptors.fr.lip6.detector.HoneycombDetector;
import JDescriptors.fr.lip6.io.XMLWriter;
import JDescriptors.fr.lip6.texture.GaborDescriptor;
import JDescriptors.fr.lip6.texture.GaborDescriptorCreator;
import JDescriptors.fr.lip6.texture.GaborDescriptorCreator.Counting;
import JDescriptors.fr.lip6.texture.GaborDescriptorCreator.Orientation;


public class GaborIndexing {

	//option creation
	static Option input = OptionBuilder.withArgName("directory")
									.hasArg()
									.withDescription("input directory containing images")
									.withLongOpt("input")
									.create("i");
	static Option output = OptionBuilder.withArgName("directory")
									.hasArg()
									.withDescription("output directory containing descriptor files (default ./desc/)")
									.withLongOpt("output")
									.create("o");
	static Option scaling = OptionBuilder.withArgName("scaling")
									.hasArg()
									.withDescription("scaling of ROI in pixels (default 6px)")
									.withLongOpt("scaling")
									.create("c");
	static Option spacing = OptionBuilder.withArgName("spacing")
									.hasArg()
									.withDescription("spacing between sampled ROI in pixels (default 6px)")
									.withLongOpt("spacing")
									.create("p");
	static Option orientation = OptionBuilder.withArgName("invariance")
									.hasArg()
									.withDescription("orientation invariance method. Valid options are: none, mean, max, all (default max)")
									.withLongOpt("rotation")
									.create("r");
	static Option counting = OptionBuilder.withArgName("low level counting")
									.hasArg()
									.withDescription("counting method for low level feature construction. valid option are: lmean, max (default max)")
									.withLongOpt("counting")
									.create("l");

	static Option help = new Option("help", "print this message");
	
	static Options options;
	
	static
	{
		options = new Options();
		
		options.addOption(input);
		options.addOption(output);
		options.addOption(scaling);
		options.addOption(spacing);
		options.addOption(orientation);
		options.addOption(counting);
		options.addOption(help);
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		

		int spacing = 6;
		int scaling = 6;
		String inDir = "";
		String outDir = "";
		Orientation orientation = Orientation.MAX;
		Counting counting = Counting.MAX;
		
		//parsing options
		 // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        //help
	        if(line.hasOption("help"))
	        {
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "GaborIndexing", options );
	        	System.exit(1);
	        }
	        //input
	        if(!line.hasOption("input"))
	        {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "GaborIndexing", options );
	        	System.exit(-1);
	        }
	        else
	        {
	        	inDir = line.getOptionValue("i");
	        	if(! (new File(inDir)).isDirectory())
	        	{
		        	HelpFormatter formatter = new HelpFormatter();
		        	formatter.printHelp( "GaborIndexing", options );
		        	System.exit(-1);	        		
	        	}
	        }
	        // output
	        outDir = line.getOptionValue("output", "desc/");
	        File f = new File(outDir);
	        if(!f.exists())
	        	f.mkdir();
	        else if( !f.isDirectory())
	        {
	        	System.out.println("output "+outDir+" exists and is no directory.");

	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "GaborIndexing", options );
	        	System.exit(-1);
	        }
	        // scaling
	        scaling = Integer.parseInt(line.getOptionValue("c", "6"));
	        // spacing
	        spacing = Integer.parseInt(line.getOptionValue("p", "6"));
	        // orientation invariance
	        String o = line.getOptionValue("r", "max");
	        if(o.equalsIgnoreCase("none"))
	        	orientation = Orientation.NONE;
	        else if(o.equalsIgnoreCase("mean"))
	        	orientation = Orientation.MEAN;
	        else if(o.equalsIgnoreCase("max"))
	        	orientation = Orientation.MAX;
	        else if(o.equalsIgnoreCase("all"))
	        	orientation = Orientation.ALL;
	        // low level counting
	        String c = line.getOptionValue("l", "max");
	        if(c.equalsIgnoreCase("mean"))
	        	counting = Counting.MEAN;
	        else if (c.equalsIgnoreCase("max"))
	        	counting = Counting.MAX;
	        
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.printHelp( "GaborIndexing", options );
        	System.exit(-1);
	    }

		
		//print options
	    System.out.println("GaborIndexing options : ");
	    System.out.println("input : "+inDir);
	    System.out.println("output : "+outDir);
	    System.out.println("scaling : "+scaling);
	    System.out.println("spacing : "+spacing);
	    System.out.println("orientation : "+orientation);
	    System.out.println("low level counting : "+counting);
		
		int i = 0;
		//creation extracteur de gaborettes
		GaborDescriptorCreator creator = new GaborDescriptorCreator();
		Detector detector = new HoneycombDetector(spacing, scaling);
		creator.setDetector(detector);
		creator.setOrientation(orientation);
		creator.setCounting(counting);
		
		//processing all files
		String[] files = (new File(inDir)).list(new FilenameFilter(){


			public boolean accept(File dir, String name) {
				if(name.endsWith("lck"))
						return false;
				if(!name.endsWith("jpg") && !name.endsWith("JPG") && !name.endsWith("png") && !name.endsWith("PNG"))
					return false;
				return true;
			}});
		ArrayList<String> listOfImages = new ArrayList<String>();
		listOfImages.addAll(Arrays.asList(files));
		
		System.out.println(listOfImages.size()+" to do.");
		
		while(!listOfImages.isEmpty())
		{
			Collections.shuffle(listOfImages);
			String s = listOfImages.remove(0);
			
			System.out.println("processing "+s);
			long tim = System.currentTimeMillis();
			
			String outFileName = s.substring(0, s.lastIndexOf("."))+".gab";
			File outFile=  new File(outDir+"/"+outFileName+".xgz");
			if(outFile.exists())
			{
				System.out.println("already done.");
				continue;
			}
			
			String lockFileName = s+".lck";
			File lockFile = new File(lockFileName);
			FileLock ifl = (new FileOutputStream(lockFile)).getChannel().tryLock();
			if(ifl == null)
			{
				System.out.println("already in processing");
				continue;
			}
			
			//extract descriptors
			ArrayList<GaborDescriptor> list = creator.createDescriptors(inDir+"/"+s);
			XMLWriter.writeXMLFile(outDir+"/"+outFileName, list, true);
			
			ifl.release();
			lockFile.delete();
			System.out.println(s+" done ("+(System.currentTimeMillis()-tim)+")");
		}
		
		//formatting stat
		double mean[] = creator.getMeanGabor();
		double squareMean[] = creator.getStdGabor();
		for(i = 0 ; i < mean.length; i++)
		{
			mean[i] /= creator.getNbProcessedDescriptors();
			squareMean[i] /= creator.getNbProcessedDescriptors();
		}

		//printing stats
		System.out.println("writing stats");
		System.out.println("nb descriptors done : "+creator.getNbProcessedDescriptors());
//		objOut.writeInt(creator.getNbProcessedDescriptors());
		System.out.println("sum descriptor : "+Arrays.toString(mean));
//		objOut.writeObject(mean);
		System.out.println("sum square descriptors : "+Arrays.toString(squareMean));
//		objOut.writeObject(squareMean);
		System.out.println("done.");
		
		//remove lock
//		fl.release();
//		objOut.close();
//		fos.close();
	}

}
