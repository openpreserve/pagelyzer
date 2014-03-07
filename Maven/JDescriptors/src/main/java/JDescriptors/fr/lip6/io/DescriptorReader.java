package JDescriptors.fr.lip6.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import JDescriptors.fr.lip6.Descriptor;


/**
 * Abstract class for reading descriptors files
 * @author dpicard
 *
 */
public abstract class DescriptorReader {

	
	public static ArrayList<Descriptor> readFile(String fileName) throws ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		//check if exist
		File file = new File(fileName);
		if(!file.exists())
		{
			System.err.println("File does not exist : "+fileName);
			return null;
		}
		
		//check if gzip
		InputStream in;
		try {
			
			in = new FileInputStream(file);
			
			//read header
			int head = in.read() & 0x000000ff;
			head = head | (in.read() << 8 & 0x0000ff00);


			in.close();
			if(head == GZIPInputStream.GZIP_MAGIC)
			{
				in = new GZIPInputStream(new FileInputStream(file));
			}
			else
			{
				in = new FileInputStream(file);
			}

		} catch (FileNotFoundException e) {
			System.err.println("no such file : "+fileName);
			return null;
		} catch (IOException e) {
			System.err.println("IOException raised :");
			e.printStackTrace();
			return null;
		}
		
		//check whether xml or koen1
		LineNumberReader lin = new LineNumberReader(new InputStreamReader(in));
		try {
			
			String firstLine = lin.readLine().trim();
			
			if(firstLine == null)
			{
				System.out.println("empty file : "+fileName);
				return null;
			}
			if(firstLine.startsWith("<?xml"))
			{
				return XMLReader.readXMLStream(lin);				
			}
			else if(firstLine.startsWith("KOEN1"))
			{
				return KoenReader.readXMLStream(lin);
			}
			

			System.err.println("File format not recognized : "+firstLine);
			
		} catch (IOException e) {
			System.err.println("IOException raised :");
			e.printStackTrace();
			return null;
		}
		
		
		return null;
	}
	
}
