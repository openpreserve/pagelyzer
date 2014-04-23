package JDescriptors.fr.lip6.sift;

import java.util.Arrays;

import JDescriptors.fr.lip6.Descriptor;


public class SIFTDescriptor extends Descriptor<float[]> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4430645532112665250L;

	@Override
	public int getDimension() {
		return d.length;
	}

	@Override
	public void initD() {
		//default sift filled with 0
		d = new float[128];
	}

	@Override
	public String toString()
	{
		return Arrays.toString(getD()).replaceAll("\\[", "").replaceAll("\\]", "");
		
	}
}
