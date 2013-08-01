package docsum.summarizer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Preprocesses tokenized sentences with the
 * aid of a stop word list read from a file.
 * 
 * @author Evan Dempsey
 */
public class SentencePreprocessor {
	
	List<String> stopwords;
	
	/**
	 * Default no-argument constructor.
	 * Reads the stop word list.
	 */
	public SentencePreprocessor() {
		stopwords = readStopwords();
	}

	/**
	 * Performs standard text preprocessing tasks on tokenized sentences.
	 * Stop word removal, case normalization, punctuation removal.
	 * 
	 * @param 	document	List of list of strings representing document.
	 * @return List of list of lower-case strings with stop words and punctuation removed.
	 */
	public List<List<String>> process(List<List<String>> document) {
		return removeStopwords(removePunctuation(makeLowercase(document)));
	}
	
	/**
	 * Removes words that appear in the stop word list.
	 * 
	 * @param 	document	List of lists of words in sentences.
	 * @return	Sentence list with stopwords removed.
	 */
	public List<List<String>> removeStopwords(List<List<String>> document) {
		
		List<List<String>> processed = new ArrayList<List<String>>();
		
		for (int i=0; i<document.size(); i++) {
			List<String> oldSentence = document.get(i);
			List<String> newSentence = new ArrayList<String>();
			
			for (int j=0; j<oldSentence.size(); j++) {
				if (!stopwords.contains(oldSentence.get(j))) {
					newSentence.add(oldSentence.get(j));
				}
			}
			
			processed.add(newSentence);
		}
		
		return processed;
	}
	
	/**
	 * Removes tokens that represent punctuation.
	 * 
	 * @param 	document	List of lists of words in sentences.
	 * @return	List of sentences with punctuation removed.
	 */
	public List<List<String>> removePunctuation(List<List<String>> document) {
			
		// Each sentence is a list of Strings and
		// the document is a list of sentences.
		List<List<String>> processed = new ArrayList<List<String>>();
			
		// Make a regex pattern to match strings with letters or numbers.
		Pattern notPuncPattern = Pattern.compile("[A-Za-z0-9]+");
			
		for (List<String> sentence: document) {
			List<String> newTokens = new ArrayList<String>();
				
			// If a string has letters or numbers, it is not just
			// punctuation, so add it to the list.
			for (String word : sentence) {
				Matcher matcher = notPuncPattern.matcher(word);
					
				if (matcher.find()) {
					newTokens.add(word);
				}
			}
				
			processed.add(newTokens);
		}
			
		return processed;
	}
	
	/**
	 * Makes all words in all sentences lower-case.
	 * 
	 * @param 	document	List of lists of words in sentences.
	 * @return List of sentences with all words lower-case.
	 */
	public List<List<String>> makeLowercase(List<List<String>> document) {
		
		List<List<String>> processed = new ArrayList<List<String>>();
		
		for (int i=0; i<document.size(); i++) {
			List<String> oldSentence = document.get(i);
			List<String> newSentence = new ArrayList<String>();
			
			for (int j=0; j<oldSentence.size(); j++) {
				String word = oldSentence.get(j).toLowerCase();
				newSentence.add(word);
			}
			
			processed.add(newSentence);
		}
		
		return processed;
	}

	/**
	 * Reads stop words from file.
	 * File format: one word per line.
	 * 
	 * @return	List of stop words.
	 */
	public List<String> readStopwords() {
		
		String stopword = null;
		List<String> stopwords = new ArrayList<String>();

		String fileName = "stoplist.txt";
		InputStream inputStream = getClass().getResourceAsStream(fileName);
		
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			
			while ((stopword = bufferedReader.readLine()) != null) {
				stopwords.add(stopword);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return stopwords;
	}
}
