package docsum.algorithm;

import java.util.List;

/**
 * Interface for keyword extraction algorithms.
 * 
 * @author Evan Dempsey
 */
public interface KeywordAlgorithm {
	
	/**
	 * Runs keyword algorithm on tokenized sentence list.
	 * 
	 * @param 	sentences	List of lists of strings.
	 * @return	List of keyword strings.
	 */
	public List<String> getKeywords(List<List<String>> sentences);
}
