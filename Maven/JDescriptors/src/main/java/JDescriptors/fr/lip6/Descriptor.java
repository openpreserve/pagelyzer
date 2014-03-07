package JDescriptors.fr.lip6;

import java.io.Serializable;

/**
 * This is a basic descriptor intended to be subclassed by specific descriptors.
 * @author dpicard
 *
 * @param <T> the type of descriptor
 */
public abstract class Descriptor<T> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8748415632915358074L;
	
	public static final String CIRCLE = "circle";
	public static final String SQUARE = "square";
	public static final String RECTANGLE = "rectangle";
	
	private int xmin, xmax, ymin, ymax;
	private String shape;
	public T d;
	
	/**
	 * @return the xim
	 */
	public int getXmin() {
		return xmin;
	}
	/**
	 * @param xim the xim to set
	 */
	public void setXmin(int xim) {
		this.xmin = xim;
	}
	/**
	 * @return the xmax
	 */
	public int getXmax() {
		return xmax;
	}
	/**
	 * @param xmax the xmax to set
	 */
	public void setXmax(int xmax) {
		this.xmax = xmax;
	}
	/**
	 * @return the ymin
	 */
	public int getYmin() {
		return ymin;
	}
	/**
	 * @param ymin the ymin to set
	 */
	public void setYmin(int ymin) {
		this.ymin = ymin;
	}
	/**
	 * @return the ymax
	 */
	public int getYmax() {
		return ymax;
	}
	/**
	 * @param ymax the ymax to set
	 */
	public void setYmax(int ymax) {
		this.ymax = ymax;
	}
	/**
	 * @return the shape
	 */
	public String getShape() {
		return shape;
	}
	/**
	 * @param shape the shape to set
	 */
	public void setShape(String shape) {
		this.shape = shape;
	}
	/**
	 * @return the d
	 */
	public T getD() {
		return d;
	}
	/**
	 * @param d the d to set
	 */
	public void setD(T d) {
		this.d = d;
	}
	
	/**
	 * get the dimension of the descriptor
	 * @return the dimension of this descriptor if it is a vector.
	 */
	public abstract int getDimension();

	public abstract void initD();
}
