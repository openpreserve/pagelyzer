package JDescriptors.fr.lip6.color;

import java.awt.image.ColorModel;

public class DefaultColorQuantizer implements ColorQuantizer {

	public ColorModel getColorModel() {
		return ColorModel.getRGBdefault();
	}

	public void setColorModel(ColorModel colorModel) {
		//Do nothing
	}

	public int getBin(int[] p) {
		return 0;
	}

	public int getBinNumber() {
		return 1;
	}

	public float[] getColorFromBin(int b) {
		return new float[] {0, 0, 0};
	}

	public int getBin(float[] fcol) {
		return 0;
	}

}
