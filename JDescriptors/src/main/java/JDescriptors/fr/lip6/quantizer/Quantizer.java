package JDescriptors.fr.lip6.quantizer;

public interface Quantizer {

	/**
	 * find the bin corresponding to input vector
	 * @param p the input vector
	 * @return the bin corresponding to the vector
	 */
	public int getBin(int[] p);
	
	
	/**
	 * size of the histogram
	 * @return
	 */
	public int getBinNumber();
}
