package docsum.summarizer;

import java.util.List;

import docsum.algorithm.MeadAlgorithm;


/**
 * Preprocesses text, feeds it into the summarization algorithm,
 * and constructs summary text from returned sentence indices.
 * 
 * @author Evan Dempsey
 */
public class DocumentSummarizer {

	SentenceSegmenter segmenter;
	SentencePreprocessor preprocessor;
	MeadAlgorithm mead;
	
	/**
	 * Constructor for DocumentSummarizar class.
	 * 
	 * @param 	segmenter		SentenceSegmenter instance.
	 * @param 	preprocessor	SentencePreprocessor instance.
	 */
	public DocumentSummarizer(SentenceSegmenter segmenter,
			SentencePreprocessor preprocessor) {
		this.segmenter = segmenter;
		this.preprocessor = preprocessor;
		mead = new MeadAlgorithm();
	}
	
	/**
	 * Generates a summary of the input text of the required length.
	 * 
	 * @param 	text		Text string to summarize.
	 * @param 	percentage	Percentage of sentences to include in summary.
	 * @return	Summary string.
	 */
	public String summarize(String text, int percentage) {

		// Only run the summarization algorithm if there
		// is text in the source JTextArea.
		if (text.length() > 0) {
			List<List<String>> sentences = segmenter.segment(text);
			List<List<String>> preprocessed = preprocessor.process(sentences);
			List<Integer> selection = mead.getSelection(preprocessed, percentage);
			List<String> original = segmenter.getOriginalSentences(text);
			String summary = buildSummaryString(original, selection);

			return summary;
		}
		
		return "";
	}

	/**
	 * Puts the summary together using the original 
	 * sentences and the indices of the sentences selected by
	 * the summarization algorithm.
	 * 
	 * @param 	sentences	List of sentence strings.
	 * @param 	selection	List of sentence indices in summary.
	 * @return	Summary string.
	 */
	private String buildSummaryString(List<String> sentences,
			List<Integer> selection) {
		
		StringBuilder stringBuilder = new StringBuilder(selection.size());
		
		for (int i=0; i<sentences.size(); i++) {
			if (selection.contains(i)) {
				stringBuilder.append(sentences.get(i));
			}
		}

		return stringBuilder.toString();
	}
}
