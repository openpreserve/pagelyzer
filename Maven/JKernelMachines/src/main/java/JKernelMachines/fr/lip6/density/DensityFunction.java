package JKernelMachines.fr.lip6.density;

import java.util.ArrayList;


/**
 * Density estimator based on training exemples.
 * @author dpicard
 *
 */
public interface DensityFunction<T> {

	/**
	 * ajoute un exemple à l'ensemble d'apprentissage et ré-entraîne
	 * l'estimateur.
	 * @param e l'exemple à ajouter à l'ensemble d'apprentissage
	 */
	public void train(T e);
	
	/**
	 * Entraîne l'estimateur de densité sur l'ensemble d'apprentissage fourni 
	 * en argument.
	 * @param e l'ensemble d'apprentissage sur lequel entraîner l'estimateur.
	 */
	public void train(ArrayList<T> e);
	
	/**
	 * proba of being related to the underlying density
	 * @param e exemple a traiter
	 * @return valeur de la fonction de densité pour l'élément donné.
	 */
	public double valueOf(T e);
	
}
