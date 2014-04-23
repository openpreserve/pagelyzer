package JDescriptors.fr.lip6.color;

import java.util.Arrays;

import JDescriptors.fr.lip6.Descriptor;


public class ColorVQFloatDescriptor extends Descriptor<float[]> {
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 5342655382053910903L;

	@Override
	public String toString()
	{
		return Arrays.toString(getD()).replaceAll("\\[", "").replaceAll("\\]", "");
		
	}

	@Override
	public int getDimension() {
		return d.length;
	}

	@Override
	public void initD() {
		d = new float[0];
	}

}
