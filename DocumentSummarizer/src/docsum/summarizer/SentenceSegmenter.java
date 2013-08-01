package docsum.summarizer;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;

/**
 * Splits text into list of tokenized sentences using
 * DocumentPreprocessor class from Stanford CoreNLP library.
 * 
 * @author Evan Dempsey
 */
public class SentenceSegmenter {
	
	/**
	 * No-argument constructor.
	 */
	public SentenceSegmenter() {
		
	}

	/**
	 * Splits text into list of tokenized sentences.
	 * 
	 * @param 	text	Text string.
	 * @return	List of lists of strings representing sentences.
	 */
	public List<List<String>> segment(String text) {
			
		List<List<String>> sentences = new ArrayList<List<String>>();
		Reader reader = new StringReader(text);
		DocumentPreprocessor preprocessor = new DocumentPreprocessor(reader);

		for (List<HasWord> sentence : preprocessor) {
			List<String> tokens = new ArrayList<String>();
				
			for (HasWord token : sentence) {
				tokens.add(token.word());
			}
			
			sentences.add(tokens);
		}

		return sentences;
	}
	
	/**
	 * Split texts string into list of untokenized sentences.
	 * 
	 * @param 	text	Test string.
	 * @return	List of sentence strings.
	 */
	public List<String> getOriginalSentences (String text) {
		
		List<String> sentenceList = new ArrayList<String>();
		
		Reader reader = new StringReader(text);
		DocumentPreprocessor preprocessor = new DocumentPreprocessor(reader);
		String tokenizerOptions = "invertible=true";
		TokenizerFactory<? extends HasWord> tf = PTBTokenizer.factory(
				new CoreLabelTokenFactory(), 
				tokenizerOptions);
		preprocessor.setTokenizerFactory(tf);
				
		// Reconstruct original sentence strings.
		for (List<HasWord> sentence : preprocessor) {
			String sentenceString = "";
			boolean printSpace = true;
					
			for (HasWord token : sentence) {
				CoreLabel cl = (CoreLabel) token;
				if (!printSpace) {
					sentenceString += cl.get(CoreAnnotations.BeforeAnnotation.class);
					printSpace = true;
				}
						
				sentenceString += cl.get(CoreAnnotations.OriginalTextAnnotation.class);
				sentenceString += cl.get(CoreAnnotations.AfterAnnotation.class);
			}
			
			sentenceList.add(sentenceString);
		}
		
		return sentenceList;
	}
}
