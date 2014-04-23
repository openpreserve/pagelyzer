package JDescriptors.fr.lip6.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import JDescriptors.fr.lip6.Descriptor;


public class XMLWriter {
	
	/**
	 * 
	 * @param d
	 * @return an XML String representing this descriptor
	 */
	public static String writeXMLString(Descriptor<?> d)
	{
		String s = "<descriptor class=\""+d.getClass().getName()+"\" shape=\""+d.getShape()+"\" ";
		s += "xmin=\""+d.getXmin()+"\" ymin=\""+d.getYmin()+"\" ";
		s += "xmax=\""+d.getXmax()+"\" ymax=\""+d.getYmax()+"\" ";
		s += "dimension=\""+d.getDimension()+"\" >";
		s += d.toString();
		s += "</descriptor>";
		
		return s;
	}
	
	/**
	 * 
	 * @param <T>
	 * @param l
	 * @return an XML String representing the list of these descriptors
	 */
	public static <T extends Descriptor<?>> String writeXMLString(ArrayList<T> l)
	{
		String s = "<image ";
		s+= " size=\""+l.size()+"\" >\n";
		if(l != null)
			for(T d : l)
				s += "\t"+writeXMLString(d)+"\n";
		s +="</image >"+"\n";
		
		return s;
	}

	/**
	 * Write the list of descriptors to a file, in XML format, possibly gziped
	 * @param <T>
	 * @param fileName name of the file
	 * @param outList list of descriptors
	 * @param gz if the file shall be gzipped
	 * @throws IOException
	 */
	public static <T extends Descriptor<?>> void writeXMLFile(String fileName, List<T> outList, boolean gz) throws IOException
	{
		File f;
		if(gz)
			f = new File(fileName+".xgz");
		else
			f = new File(fileName+"xml");
		
		
//		if(f.exists())
//		{
//			IOException ioe = new IOException(fileName+" : file exists");
//			throw ioe;
//		}
		
		PrintStream out = null;
		if(gz)
			out = new PrintStream(new GZIPOutputStream(new FileOutputStream(f)));
		else
			out = new PrintStream(new FileOutputStream(f));
		
		out.println("<?xml version=\"1.0\"?>");
		
		if(outList == null || outList.size() == 0)
		{
			out.println("<image>");
			out.println("</image>");
			return;
		}
		
		out.println("<image descriptor=\""+outList.get(0).getClass().getName()+"\" size=\""+outList.size()+"\" >");
		if(outList != null)
			for(T d : outList)
				out.println("\t"+writeXMLString(d));
		out.println("</image >");
		
		out.close();
	}
	
	/**
	 * Write the list of descriptors to a file, in XML format
	 * @param <T>
	 * @param fileName
	 * @param l
	 * @throws IOException
	 */
	public static <T extends Descriptor<?>> void writeXMLFile(String fileName, ArrayList<T> l) throws IOException
	{
		writeXMLFile(fileName, l, false);
	}
}
