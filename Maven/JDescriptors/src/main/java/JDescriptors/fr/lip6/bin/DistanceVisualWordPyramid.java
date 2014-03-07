package JDescriptors.fr.lip6.bin;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.bof.DistanceVisualWordFactory;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Norm;
import JDescriptors.fr.lip6.io.DescriptorReader;


public class DistanceVisualWordPyramid {

	static Option input = OptionBuilder.withArgName("directory")
											.hasArg()
											.withDescription("input directory containing descriptors")
											.withLongOpt("input")
											.create("i");
	static Option output = OptionBuilder.withArgName("output directory")
										.hasArg()
										.withDescription("output directory (default ./sp/)")
										.withLongOpt("output")
										.create("o");
	static Option codebook = OptionBuilder.withArgName("codebook file")
										.hasArg()
										.withDescription("codebook file (default codebook.obj)")
										.withLongOpt("codebook")
										.create("c");
	static Option scales = OptionBuilder.withArgName("scale1+scale2+...")
										.hasArg()
										.withDescription("scales of the spatial pyramids. ex : 1x1+3x3 means two scales with a 1 by 1 grid and a 3 by 3 grid. (default 1x1)")
										.withLongOpt("scales")
										.create("s");
	static Option norm = OptionBuilder.withArgName("norm")
										.hasArg()
										.withDescription("normalization of the spatial pyramid vectors. Valid options are : none, l1, l2, points. (default none).")
										.withLongOpt("norm")
										.create("n");
	static Option nbsigmas = OptionBuilder.withArgName("number of sigmas")
										  .hasArg()
										  .withDescription("number of sigmas to consider for the max of the Distance Histogram (default 5).")
										  .withLongOpt("nbsigmas")
										  .create("sigma");
	static Option nbbins = OptionBuilder.withArgName("number of bins")
										.hasArg()
										.withDescription("number of bins used in the quantization step to encode the distancesfrom one sifts to clusters (default 10).")
										.withLongOpt("nbbins")
										.create("bins");
	
	static Options options = new Options();
	
	static {
		options.addOption(input);
		options.addOption(output);
		
		options.addOption(codebook);
		options.addOption(scales);
		
		//options.addOption(knn);
		options.addOption(nbsigmas);
		options.addOption(nbbins);
		
		options.addOption(norm);
		
		options.addOption("l1","l1-vectors", false, "normalized entry vectors with l1 norm");
	}
	
	
	/**
	 * @param args
	 */

	public static void main(String[] args) throws Exception{
		// TODO Auto-generated method stub
		
		String input = "";
		String output = "";
		String codebook = "";
		int nbsigmas = 5;
		int nbbins = 10;
		Norm norm = null;
		String scales = "";
		boolean l1_vectors = false;
		
		//option parsing		
	    // create the parser
	    CommandLineParser parser = new GnuParser();
	    try {
	        // parse the command line arguments
	        CommandLine line = parser.parse( options, args );
	        //input
	        if(!line.hasOption("input"))
	        {
	        	// automatically generate the help statement
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "DistanceVisualWordsPyramid", options );
	        	System.exit(-1);
	        }
	        else
	        {
	        	input = line.getOptionValue("i");
	        }
	        // output
	        output = line.getOptionValue("o", "sp");
	        File outdir = new File(output);
	        if(!outdir.exists())
	        	outdir.mkdir();
	        else if(!outdir.isDirectory())
	        {
	        	System.out.println("outdir "+outdir+" exists and is not a directory.");
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "DistanceVisualWordsPyramid", options );
	        	System.exit(-1);
	        }
	        //codebook
	        codebook = line.getOptionValue("c", "codebook.obj");
	        if(!(new File(codebook)).exists())
	        {
	        	System.out.println(codebook+": no such codebook file");
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "DistanceVisualWordsPyramid", options );
	        	System.exit(-3);
	        }
	        //nbsigmas
	        nbsigmas= Integer.parseInt(line.getOptionValue("sigma", "5"));

	        //nbbins
	        nbbins = Integer.parseInt(line.getOptionValue("bins", "10"));

	        String n = line.getOptionValue("n", "none");
	        if(n.equalsIgnoreCase("none"))
	        	norm = Norm.NONE;
	        else if(n.equalsIgnoreCase("points"))
	        	norm = Norm.NB_POINTS;
	        else if (n.equalsIgnoreCase("l1"))
	        	norm = Norm.L1_NORM;
	        else if(n.equalsIgnoreCase("l2"))
	        	norm = Norm.L2_NORM;
	        //scales
	        scales = line.getOptionValue("s", "1x1");
	        //l1norm
	        l1_vectors = line.hasOption("l1");
	        
	    }
	    catch( ParseException exp ) {
	        // oops, something went wrong
	        System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        	HelpFormatter formatter = new HelpFormatter();
        	formatter.printHelp( "DistanceVisualWordsPyramid", options );
        	System.exit(-1);
	    }

	    //printing options
	    System.out.println("DistanceVisualWordsPyramid options : ");
	    System.out.println("input : "+input);
	    System.out.println("output : "+output);
	    System.out.println("codebook : "+codebook);
	    System.out.println("nbsigmas : "+nbsigmas);
	    System.out.println("nbbins : "+nbbins);
	    System.out.println("scales : "+scales);
	    System.out.println("norm : "+norm);
	    System.out.println("has norml1 : "+l1_vectors);
	    System.out.println();

	    
	    //visual codebook
	    System.out.println("Reading visual codebook.");
		ObjectInputStream oin = new ObjectInputStream(new FileInputStream(codebook));
		double[][] centers = (double[][]) oin.readObject();
		double[] sigma = (double[]) oin.readObject();
		oin.close();
		
		
	    //parsing scales
	    ArrayList<int[]> listOfScales = new ArrayList<int[]>();
		for(String s : scales.trim().split("\\+"))
		{
			String[] lc = s.split("x");
			if(lc.length != 2)
				continue;
			
			int[] linecols = new int[2];
			linecols[0] = Integer.parseInt(lc[0]);
			linecols[1] = Integer.parseInt(lc[1]);
			
			listOfScales.add(linecols);
			
		}
		System.out.println("processing scales : ");
		for(int[] s : listOfScales)
			System.out.println(Arrays.toString(s));
	    
		//letz go
		File directory = new File(input);
		if(!directory.isDirectory())
		{
			System.out.println("input file "+directory+" is not a directory.");
			System.exit(0);
		}
		File[] listFiles = directory.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		for(File f : listFiles)
			list.add(f);
		Collections.shuffle(list);
		for(File f : list)
		{
			String destName = output+"/"+(f.getName().substring(0, f.getName().indexOf(".")))+".obj";
			File outFile = new File(destName);
			
			if(outFile.exists())
			{
				System.out.println("not doing  : "+outFile+" ("+f+")");
				continue;
			}
			else
			{
				System.out.println("doing : "+outFile+" ("+f+")");
			}
			
			outFile.createNewFile();
			
			ArrayList<Descriptor> listOfWords = DescriptorReader.readFile(f.getAbsolutePath());
			ArrayList<double[]> bag = new ArrayList<double[]>();
			//each scales
			for(int[] s : listOfScales)
			{
				System.out.println("doing scale "+Arrays.toString(s));
				DistanceVisualWordFactory.lines = s[0];
				DistanceVisualWordFactory.cols = s[1];
						
				DistanceVisualWordFactory.norm = DistanceVisualWordFactory.Norm.NONE;
				DistanceVisualWordFactory.l1_norm = l1_vectors;
				              
				ArrayList<double[]> l = DistanceVisualWordFactory.createDistanceBagOfWindows(listOfWords, centers, sigma, nbsigmas, nbbins);
				                    
				bag.addAll(l);
			}
			
			//writing
			ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(outFile));
			oout.writeObject(bag);
			oout.flush();
			oout.close();
			
			System.out.println("written "+outFile);
			System.out.println();
			
		}
		
		
	}

}
