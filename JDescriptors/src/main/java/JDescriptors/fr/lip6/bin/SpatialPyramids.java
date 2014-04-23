package JDescriptors.fr.lip6.bin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import JDescriptors.fr.lip6.Descriptor;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Coding;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Norm;
import JDescriptors.fr.lip6.bof.SpatialPyramidFactory.Pooling;
import JDescriptors.fr.lip6.io.DescriptorReader;


public class SpatialPyramids {

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
	static Option knn = OptionBuilder.withArgName("knn")
										.hasArg()
										.withDescription("number of neighbors to consider during the coding step (deafult 10).")
										.withLongOpt("knn")
										.create("k");
	static Option coding = OptionBuilder.withArgName("coding string")
										.hasArg()
										.withDescription("type of coding used. valid options are: sparse, sparsec, soft, hard (default soft).")
										.withLongOpt("coding")
										.create("d");
	static Option pooling = OptionBuilder.withArgName("polling string")
										.hasArg()
										.withDescription("type of pooling used. valid options are: sum, max (default sum).")
										.create("p");
	
	static Options options = new Options();
	
	static {
		options.addOption(input);
		options.addOption(output);
		
		options.addOption(codebook);
		options.addOption(scales);
		
		options.addOption(knn);
		options.addOption(coding);
		options.addOption(pooling);
		
		options.addOption(norm);
		
		options.addOption("l1","l1-vectors", false, "normalized entry vectors with l1 norm");
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception
	{
		
		String input = "";
		input = "";
		String output = "";
		String codebook = "";
		Coding coding = null;
		Pooling pooling = null;
		int knn = 0;
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
	        	formatter.printHelp( "SpatialPyramids", options );
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
	        	formatter.printHelp( "SpatialPyramids", options );
	        	System.exit(-1);
	        }
	        //codebook
	        codebook = line.getOptionValue("c", "codebook.obj");
	        if(!(new File(codebook)).exists())
	        {
	        	System.out.println(codebook+": no such codebook file");
	        	HelpFormatter formatter = new HelpFormatter();
	        	formatter.printHelp( "SpatialPyramids", options );
	        	System.exit(-3);
	        }
	        //knn
	        knn = Integer.parseInt(line.getOptionValue("k", "10"));
	        //coding
	        String c = line.getOptionValue("d", "soft");
	        if(c.equalsIgnoreCase("soft"))
	        	coding = Coding.SOFT;
	        else if (c.equalsIgnoreCase("hard"))
	        	coding = Coding.HARD;
	        else if (c.equalsIgnoreCase("sparse"))
	        	coding = Coding.SPARSE;
	        else if (c.equalsIgnoreCase("sparsec"))
	        	coding = Coding.SPARSEC;
	        
	        //pooling
	        String p = line.getOptionValue("p", "sum");
	        if(p.equalsIgnoreCase("sum"))
	        	pooling = Pooling.SUM;
	        else if(p.equalsIgnoreCase("max"))
	        	pooling = Pooling.MAX;
	        //norm
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
        	formatter.printHelp( "SpatialPyramids", options );
        	System.exit(-1);
	    }

	    //printing options
	    System.out.println("SpatialPyramids options : ");
	    System.out.println("input : "+input);
	    System.out.println("output : "+output);
	    System.out.println("codebook : "+codebook);
	    System.out.println("knn : "+knn);
	    System.out.println("coding : "+coding);
	    System.out.println("pooling : "+pooling);
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
		File directory2 = new File(input);
		if(!directory2.isDirectory())
		{
			System.out.println("input file "+directory2+" is not a directory.");
			System.exit(0);
		}
		File[] listFiles2 = directory2.listFiles();
		ArrayList<File> list = new ArrayList<File>();
		for (File f2 : listFiles2){
			//if (!f2.getName().equals("101")){
			//	continue;
			//}
			File[] listFiles = f2.listFiles();			
		for(File f : listFiles) {
			/*
			System.out.println(f.getName());
			list.add(f);
		}
		int size = list.size()/46;
		//doing 46 sequential accesses only
		for(int i = 0 ; i < 46; i++)
		{*/
			long timeblock = System.currentTimeMillis();
			System.out.println("reading block "+f.getName());
			//reading all files in the current subset
			Map<File, ArrayList<Descriptor>> map = new Hashtable<File, ArrayList<Descriptor>>();
			//for(int j = i * size; j < Math.min((i+1)*size, list.size()); j++)
			//{
				//File f = list.get(j);
				if(f.exists())
				{
					//System.out.println(f.getPath());
					map.put(f, DescriptorReader.readFile(f.getAbsolutePath()));
					System.out.print(".");
				}
			//}
			System.out.println();
			System.out.println("put "+map.size()+" images in block in "+((System.currentTimeMillis()-timeblock)/1000.0)+"s.");
			
			List<File> fileList = new ArrayList<File>();
			fileList.addAll(map.keySet());
			//Collections.shuffle(fileList);
			//for(File f : fileList)
			{
				long filetime = System.currentTimeMillis();
				String destRep = output+"/"+f2.getName();
				File destRepF = new File(destRep);
				if (!(destRepF.exists())){
					destRepF.mkdir();
				}
				String destName = destRep+"/"+(f.getName().substring(0, f.getName().indexOf(".")))+".obj";
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

				ArrayList<Descriptor> listOfWords = map.get(f);

				ArrayList<double[]> bag = new ArrayList<double[]>();
				//each scales
				for(int[] s : listOfScales)
				{
					System.out.println("doing scale "+Arrays.toString(s));
					SpatialPyramidFactory.lines = s[0];
					SpatialPyramidFactory.cols = s[1];

					SpatialPyramidFactory.knn = knn;
					SpatialPyramidFactory.coding = coding;
					SpatialPyramidFactory.pooling = pooling;

					SpatialPyramidFactory.norm = SpatialPyramidFactory.Norm.NONE;
					SpatialPyramidFactory.l1_norm = l1_vectors;

					ArrayList<double[]> l = SpatialPyramidFactory.createBagOfWindows(listOfWords, centers, sigma);

					bag.addAll(l);
				}
				long writetime = System.currentTimeMillis();
				System.out.println("done in "+(writetime-filetime)+"ms.");

				//writing
				ObjectOutputStream oout = new ObjectOutputStream(new FileOutputStream(outFile));
				oout.writeObject(bag);
				oout.flush();
				oout.close();

				System.out.println("written "+outFile+" in "+(System.currentTimeMillis()-writetime)+"ms.");
				System.out.println();

			}
			
			System.out.println("block done in "+ ((System.currentTimeMillis()-timeblock)/1000.0)+"s.");
		}
		}
	}


}
