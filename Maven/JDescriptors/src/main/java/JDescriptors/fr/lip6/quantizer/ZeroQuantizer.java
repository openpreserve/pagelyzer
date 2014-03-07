package JDescriptors.fr.lip6.quantizer;

/**
 * Quantizer that always returns 0
 * @author dpicard
 *
 */
public class ZeroQuantizer implements Quantizer {

	public int getBin(int[] p) {
		return 0;
	}

	public int getBinNumber() {
		return 1;
	}

}
