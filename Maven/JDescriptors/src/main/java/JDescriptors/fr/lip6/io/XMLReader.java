package JDescriptors.fr.lip6.io;

import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import JDescriptors.fr.lip6.Descriptor;


public class XMLReader {
	
	@SuppressWarnings(value={"unchecked"})
	public static ArrayList<Descriptor> readXMLStream(LineNumberReader iStream) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
	{
		
		
//		//xml
//		String xml = in.readLine().trim();
//		if(!xml.startsWith("<?xml"))
//		{
//			IOException ioe = new IOException("no valid xml");
//			throw ioe;
//		}
//		
		//construct ArrayList
		String image = iStream.readLine().trim();
		while(!image.startsWith("<image"))
		{
			System.out.println("line : "+image);
			image = iStream.readLine().trim();
		}
		
		String[] param = image.split(" ");
		for(String p : param)
		{
			if(p.startsWith("size"))
			{
				String[] s = p.split("=");
				if(s.length > 1)
				{
//					String size = s[1].trim().replaceAll("\"", "");
				}
			}
		}
		
		String line = iStream.readLine().trim();
		
		ArrayList<Descriptor> list = new ArrayList<Descriptor>();
		
		while(!line.startsWith("</image"))
		{			
			Descriptor d = null;
			
			//1. head
			String head = line.substring(line.indexOf("<")+1, line.indexOf(">"));
			String[] arguments = head.split(" ");
			Class c = Descriptor.class;
			
			for(String p : arguments)
			{
				if(p.startsWith("<"))
				{
					
				}
				else if(p.startsWith("class"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String descriptorClass = s[1].trim().replaceAll("\"", "");
						c = XMLReader.class.getClassLoader().loadClass(descriptorClass);
						
						d = (Descriptor<?>) c.newInstance();
					}
				}
				else if(p.startsWith("xmin"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String xmin = s[1].trim().replaceAll("\"", "");
						d.setXmin(Integer.parseInt(xmin));
						
					}
				}
				else if(p.startsWith("xmax"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String xmax = s[1].trim().replaceAll("\"", "");
						d.setXmax(Integer.parseInt(xmax));
						
					}
				}
				else if(p.startsWith("ymin"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String ymin = s[1].trim().replaceAll("\"", "");
						d.setYmin(Integer.parseInt(ymin));
						
					}
				}
				else if(p.startsWith("ymax"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String ymax = s[1].trim().replaceAll("\"", "");
						d.setYmax(Integer.parseInt(ymax));
						
					}
				}
				else if(p.startsWith("shape"))
				{
					String[] s = p.split("=");
					if(s.length > 1)
					{
						String shape = s[1].trim().replaceAll("\"", "");
						d.setShape(shape);
						
					}
				}
			}
			
			
			//2. body
			String body = line.substring(line.indexOf('>')+1, line.lastIndexOf('<')).trim();
			String[] values = body.split(",");
			try {
				d.initD();
				String dClass = d.getD().getClass().toString();
				//double
				if(dClass.equalsIgnoreCase("class [D"))
				{
					double[] v = new double[values.length];
					for(int i = 0 ; i < v.length; i++)
						v[i] = Double.parseDouble(values[i]);
					
					d.setD(v);
				}
				//float
				else if(dClass.equalsIgnoreCase("class [F"))
				{
					float[] v = new float[values.length];
					for(int i = 0 ; i < v.length; i++)
						v[i] = Float.parseFloat(values[i]);
					
					d.setD(v);		
				}
				else 
				{
					System.out.println("NOT VALID CLASS");
				}
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			list.add(d);			
			line = iStream.readLine().trim();
		}
		
		iStream.close();
		
		
		return list;
	}

}
