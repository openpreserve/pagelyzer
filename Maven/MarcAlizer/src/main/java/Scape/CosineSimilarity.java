package Scape;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class CosineSimilarity {

/**
* calculate the cosine similarity between feature vectors of two clusters
*
* The feature vector is represented as HashMap<String, Double>.
*
* @param firstFeatures The feature vector of the first cluster
* @param secondFeatures The feature vector of the second cluster
* @return the similarity measure
*/
	public static Double calculateCosineSimilarity(HashMap<String, Double> firstFeatures, HashMap<String, Double> secondFeatures) {
		
	
		Double similarity = 0.0;
		Double sum = 0.0;	// the numerator of the cosine similarity
		Double fnorm = 0.0;	// the first part of the denominator of the cosine similarity
		Double snorm = 0.0;	// the second part of the denominator of the cosine similarity
		Set<String> fkeys = firstFeatures.keySet();
		Iterator<String> fit = fkeys.iterator();
		
		while (fit.hasNext()) 
		{
			String featurename = fit.next();
			boolean containKey = secondFeatures.containsKey(featurename);
			if (containKey) 
			{
			sum = sum + firstFeatures.get(featurename) * secondFeatures.get(featurename);
			}
		}
		fnorm = calculateNorm(firstFeatures);
		snorm = calculateNorm(secondFeatures);
		similarity = sum / (fnorm * snorm);
		return similarity;
	}

/**
* calculate the norm of one feature vector
*
* @param feature of one cluster
* @return
*/
	public static Double calculateNorm(HashMap<String, Double> feature) {
		Double norm = 0.0;
		Set<String> keys = feature.keySet();
		Iterator<String> it = keys.iterator();
		while (it.hasNext()) {
		String featurename = it.next();
		norm = norm + Math.pow(feature.get(featurename), 2);
		}
		return Math.sqrt(norm);
	}
	
	
	public static HashMap<String, Double> getFeaturesFromString(String text)
	{
		HashMap<String, Double> results = new HashMap<String, Double>();
		//text = text.replaceAll("\\P{L}+\\s", "");// replace any non-letter characters with nothing.
		String[] tokens = text.split(" ");
		for(int i=0;i<tokens.length;i++)
		{
			if(results.containsKey(tokens[i]))
				results.put(tokens[i], results.get(tokens[i])+1);
			else 
				results.put(tokens[i], (double) 1);
			
			
		}
		return results;
		
	}
}

