package JDescriptors.fr.lip6.bof;
import java.io.Serializable;
import java.util.ArrayList;

public class DistanceCenters implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4787841838982732835L;
	public ArrayList<Integer> list;
    public ArrayList<Double> dist;
	
    
    /**
	 * @param list
	 * @param dist
	 */
	public DistanceCenters(ArrayList<Integer> l , ArrayList<Double> d) {
		
		this.list = l;
		this.dist = d;
	}
 
}
