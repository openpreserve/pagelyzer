package JDescriptors.fr.lip6.copy;

import java.util.ArrayList;

public interface DescriptorCreator<T extends Descriptor> {
	
	public ArrayList<T> createDescriptors(String imageName);

}
