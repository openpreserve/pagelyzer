package JDescriptors.fr.lip6.texture;

public abstract class Filter {
	public abstract double[][] convolution(double [][] source, String nom);
}
