package docsum.algorithm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Performs extractive summarization of a document
 * by algorithmically selecting a list of sentences 
 * to include in the summary.
 * <p>
 * Implementation of MEAD algorithm based on paper:
 * "Centroid-based summarization of multiple documents"
 * http://clair.si.umich.edu/~radev/papers/centroid.pdf
 * 
 * @author Evan Dempsey
 */
public class MeadAlgorithm implements SummarizationAlgorithm {
	
	Map<String, List<Integer>> docFrequencies;
	List<String> terms;
	Map<String, Double> averageTermFrequencies;
	
	/**
	 * No-argument constructor.
	 */
	public MeadAlgorithm() {
		
	}
	
	/**
	 * Initializes all data structures.
	 */
	private void initModel() {
		docFrequencies = new HashMap<String, List<Integer>>();
		terms = new ArrayList<String>();
		averageTermFrequencies = new HashMap<String, Double>();
	}
	
	// Gets selection of sentences to include in summary.
	public List<Integer> getSelection(List<List<String>> sentences, int percentage) {
		
		initModel();
		buildModel(sentences);
		
		List<Double> centroidValues = makeCentroidValues(sentences.size());
		List<String> centroidDoc = makeCentroidDocument(centroidValues);
		List<Double> docCentroidValues = makeDocumentCentroids(sentences, centroidValues, centroidDoc);
		double maxCentroidValue = Collections.max(docCentroidValues);
		List<Double> positionalValues = makePositionalValues(sentences.size(), maxCentroidValue);
		List<List<Integer>> sentenceVectors = makeSentenceVectors(sentences, terms);
		List<Integer> overlaps = makeFirstSentenceOverlaps(sentenceVectors);
		List<IndexValuePair> sentenceScores = makeSentenceScores(docCentroidValues, positionalValues, overlaps);
		List<Integer> summarySelection = makeSummarySelection(sentenceScores, percentage);
		
		return summarySelection;
	}
	
	/**
	 * Reads the sentences and extract document frequencies,
	 * unique terms, and average term frequencies.
	 * 
	 * @param 	sentences	List of tokenized sentences.
	 */
	private void buildModel(List<List<String>> sentences) {
		makeDocFrequencies(sentences);
		makeTerms();
		makeAverageTermFrequencies(sentences);	
	}


	/**
	 * Slices off a percentage of top ranking sentences.
	 * 
	 * @param 	sentenceScores	List of IndexValuePair (sentence and score).
	 * @param 	percent			Percentage of sentences to slice off.
	 * @return	List of indices of sentences to include in summary.
	 */
	private List<Integer> makeSummarySelection(List<IndexValuePair> sentenceScores,
			int percent) {
		
		// Calculate the number of sentences in the summary.
		int summaryLength = (int) (sentenceScores.size() * percent / (double) 100.0);
	
		// Make sure the summary is at least 1 sentence long.
		if (summaryLength < 1)
			summaryLength = 1;
		
		// Sort the sentence scores from top to bottom.
		Collections.sort(sentenceScores);
		Collections.reverse(sentenceScores);
		
		// Take the top scoring sentence indices.
		List<Integer> indices = new ArrayList<Integer>();
		for (int i=0; i<summaryLength; i++) {
			indices.add(sentenceScores.get(i).index);
		}
		
		// Sort the top sentence indices.
		Collections.sort(indices);
		
		return indices;
	}

	/**
	 * Calculates the score for each sentence in the collection.
	 * 
	 * @param 	docCentroidValues	List of centroid scores for each sentence.
	 * @param 	positionalValues	List of position scores for each sentence.
	 * @param 	overlaps			List of first sentence overlap scores for each sentence.
	 * @return	List of IndexValuePair with indices and scores for all sentences.
	 */
	private List<IndexValuePair> makeSentenceScores(List<Double> docCentroidValues,
			List<Double> positionalValues, List<Integer> overlaps) {
		
		List<IndexValuePair> pairs = new ArrayList<IndexValuePair>();
		
		for (int i=0; i<docCentroidValues.size(); i++) {
			IndexValuePair pair = new IndexValuePair();
			pair.index = i;
			pair.value = docCentroidValues.get(i) + positionalValues.get(i) + overlaps.get(i);
			pairs.add(pair);
		}
		
		return pairs;
	}

	/**
	 * Builds word occurrence vectors for all sentences.
	 * 
	 * @param 	sentences		List of tokenized sentences.
	 * @param 	sentenceTerms	List of all words in sentences.
	 * @return
	 */
	private List<List<Integer>> makeSentenceVectors(
			List<List<String>> sentences, List<String> sentenceTerms) {
		// Sentence vectors are vectors of length N where N is the number of
		// different words in the document and the value at the index
		// is the number of times that word occurs in the sentence.
		
		List<List<Integer>> sentenceVectors = new ArrayList<List<Integer>>();
		
		for (List<String> document : sentences) {
			List<Integer> sentenceVector = new ArrayList<Integer>();
			
			for (String term : sentenceTerms) {
				sentenceVector.add(Collections.frequency(document, term));
			}
			
			sentenceVectors.add(sentenceVector);
		}
		
		return sentenceVectors;
	}

	/**
	 * Calculates dot products of all sentence vectors and the
	 * first sentence in the collection of sentence vectors.
	 * 
	 * @param 	sentenceVectors	List of sentence vectors.
	 * @return	List of first sentence overlap values.
	 */
	private List<Integer> makeFirstSentenceOverlaps(List<List<Integer>> sentenceVectors) {
		List<Integer> overlaps = new ArrayList<Integer>();
		List<Integer> firstSentence = sentenceVectors.get(0);
		
		for (int i=0; i<sentenceVectors.size(); i++) {
			int overlap = 0;
			List<Integer> vector = sentenceVectors.get(i);
			
			for (int j=0; j<vector.size(); j++) {
				overlap += vector.get(j) * firstSentence.get(j);
			}
			
			overlaps.add(overlap);
		}
		
		return overlaps;
	}

	/**
	 * Calculate positional values of sentences.
	 * 
	 * @param 	size				Number of sentences.
	 * @param 	maxCentroidValue	Max centroid value of all sentences.
	 * @return	List of positional values of all sentences.
	 */
	private List<Double> makePositionalValues(int size, double maxCentroidValue) {
		List<Double> posValues = new ArrayList<Double>();
		
		for (int i=0; i<size; i++) {
			double posValue = ((size - i) / (double) size) * maxCentroidValue;
			posValues.add(posValue);
		}
		
		return posValues;
	}

	/**
	 * Calculates centroid values of sentences.
	 * 
	 * @param 	sentences		List of tokenized sentences.
	 * @param 	centroidValues	List of centroid values of words.
	 * @param 	centroidDoc		Centroid pseudo-document.
	 * @return	List of centroid values of all sentences.
	 */
	private List<Double> makeDocumentCentroids(List<List<String>> sentences,
			List<Double> centroidValues,
			List<String> centroidDoc) {
	
		List<Double> docCentroidValues = new ArrayList<Double>();
		
		for (List<String> document : sentences) {
			double total = 0.0;
			
			for (String term : centroidDoc) {
				if (document.contains(term)) {
					total += centroidValues.get(terms.indexOf(term));
				}
			}
			
			docCentroidValues.add(total);
		}
		
		return docCentroidValues;
	}

	/**
	 * Determines the frequency of each word in the document.
	 *
	 * @param 	sentences		List of tokenized sentences.
	 */
	private void makeDocFrequencies(List<List<String>> sentences) {
		
		for (int i=0; i<sentences.size(); i++) {
			for (String word: sentences.get(i)) {
				if (!docFrequencies.containsKey(word)) {
					List<Integer> docsWithTerm = new ArrayList<Integer>();
					docsWithTerm.add(i);
					docFrequencies.put(word, docsWithTerm);
				}
				else {
					if (!docFrequencies.get(word).contains(i)) {
						docFrequencies.get(word).add(i);
					}
				}
			}
		}
	}
	
	/**
	 * Creates alphabetized list of words in document.
	 */
	private void makeTerms() {
		Set<String> keys = docFrequencies.keySet();
		
		for (String key : keys) {
			terms.add(key);
		}
		
		Collections.sort(terms);
	}
	
	/**
	 * Computes average term frequency for each word in document.
	 * 
	 * @param	sentences	List of tokenized sentences.
	 */
	private void makeAverageTermFrequencies(List<List<String>> sentences) {
		// Average term frequency = total occurrences in collection / documents in collection
		
		// Count the total occurrences of each word.
		for (List<String> document : sentences) {
			for (String term : document) {
				if (averageTermFrequencies.containsKey(term)) {
					averageTermFrequencies.put(term, averageTermFrequencies.get(term)+1.0);
				}
				else {
					averageTermFrequencies.put(term, 1.0);
				}
			}
		}
		
		// Average the occurrences over the documents.
		int numDocs = sentences.size();
		for (String term : averageTermFrequencies.keySet()) {
			averageTermFrequencies.put(term, averageTermFrequencies.get(term) / (float) numDocs);
		}
	}
	
	/**
	 * Calculates the centroid value for each unique word.
	 * 
	 * @param 	numSentences	Number of sentences in document.	
	 * @return	List of centroid values for each word.
	 */
	private List<Double> makeCentroidValues(int numSentences) {
		List<Double> centroidValues = new ArrayList<Double>();
		
		for(String term : terms) {
			double tf = averageTermFrequencies.get(term);
			int df = docFrequencies.get(term).size();
			
			centroidValues.add(tf * Math.log10(numSentences / (double) df));
		}
		
		return centroidValues;
	}
	
	/**
	 * Builds centroid document by taking words with
	 * a centroid value above a certain threshold.
	 * 
	 * @param 	centroidValues	Centroid values of words.
	 * @return	Centroid document as list of strings.
	 */
	private List<String> makeCentroidDocument(List<Double> centroidValues) {
		
		// Put centroid values into pairs.
		ArrayList<IndexValuePair> pairs = new ArrayList<IndexValuePair>();
		
		for (int i=0; i<centroidValues.size(); i++) {
			IndexValuePair pair = new IndexValuePair();
			pair.index = i;
			pair.value = centroidValues.get(i);
			
			pairs.add(pair);
		}
		
		// Sort the pairs by centroid value, then reverse them.
		Collections.sort(pairs);
		Collections.reverse(pairs);
		
		// Take a portion of the top terms as the centroid.
		// Currently, 10% of terms are used.
		int totalTerms = pairs.size();
		int topTerms = (int) (totalTerms * 0.1);
		
		// Avoid a zero-length centroid sentence, unless
		// there are zero sentences in the document.
		if (topTerms < 1 && centroidValues.size() > 0)
			topTerms = 1;
		
		List<String> centroidDoc = new ArrayList<String>();
		for (int i=0; i<topTerms; i++) {
			centroidDoc.add(terms.get(pairs.get(i).index));
		}

		return centroidDoc;
	}
}
