package docsum.algorithm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 
 * Keyword extraction based on the paper:
 * "Graph-Based Keyword Extraction for Single-Document Summarization"
 * http://www.aclweb.org/anthology-new/W/W08/W08-1404.pdf
 * <p>
 * HITS algorithm implementation based on pseudo-code at:
 * http://en.wikipedia.org/wiki/HITS_algorithm
 * 
 * @author Evan Dempsey
 */
public class HITSAlgorithm implements KeywordAlgorithm {

	
	/**
	 * Default no-argument constructor.
	 */
	public HITSAlgorithm() {
		
	}
	
	// Generates ordered list of keywords.
	public List<String> getKeywords(List<List<String>> sentences) {
		
		List<String> wordList = makeWordList(sentences);
		Map<Integer, HITSNode> graph = makeGraph(sentences, wordList);
		List<Integer> orderedNodes = runHITS(graph, 10);
		List<String> keywords = makeKeywordList(orderedNodes, wordList);

		return keywords;
	}
	
	/**
	 * Makes alphabetically ordered list all words in all sentences.
	 * 
	 * @param 	sentences	List of sentences, each of which is a list.
	 * @return	Alphabetical list of words in sentences.
	 */
	private List<String> makeWordList(List<List<String>> sentences) {
		List<String> words = new ArrayList<String>();
		
		for (List<String> sentence : sentences) {
			for (String word : sentence) {
				if (!words.contains(word))
					words.add(word);
			}
		}
		
		// Sort alphabetically.
		Collections.sort(words);
		
		return words;
	}
	
	/**
	 * Builds directed word coocurrence graph. There is an
	 * edge from word A to word B if word B directly follows A in a 
	 * sentence. Each node has a list of incoming and outgoing edges.
	 * 
	 * @param	sentences	List of lists of word strings.
	 * @param 	wordList	Alphabetical list of all words in sentences.
	 * @return	Graph with words as nodes and cooccurrences as edges.
	 */
	private Map<Integer, HITSNode> makeGraph(List<List<String>> sentences,
			List<String> wordList) {
		
		Map<Integer, HITSNode> graph = new HashMap<Integer, HITSNode>();
		
		// Every word in the word list is represented
		// by a node in the graph. Create nodes now
		// to avoid problems with nodes that have no
		// incoming or outgoing edges.
		for (int i=0; i<wordList.size(); i++) {
			graph.put(i, new HITSNode());
		}
	
		for (List<String> sentence : sentences) {
			for (int i=0; i<sentence.size()-1; i++) {
				String current = sentence.get(i);
				String next = sentence.get(i+1);
				
				// Make an edge from the current
				// word to the next word.
				int cIndex = wordList.indexOf(current);
				int nIndex = wordList.indexOf(next);
				
				// All words are already in the graph,
				// so just add an outgoing edge to the
				// current word and an incoming edge
				// to the next word.
				graph.get(cIndex).addOutgoing(nIndex);
				graph.get(nIndex).addIncoming(cIndex);
			}
		}
		
		return graph;
	}
	
	/**
	 * Runs the HITS algorithm on the word graph.
	 * 
	 * @param 	graph	Word cooccurrence graph.
	 * @param 	k		Number of iterations to run HITS.
	 * @return	List of word indices sorted from highest to lowest score.
	 */
	private List<Integer> runHITS(Map<Integer, HITSNode> graph, int k) {
		
		int numNodes = graph.size();
		
		// Arrays for hub and authority scores.
		double[] authorityScores = new double[numNodes];
		double[] hubScores = new double[numNodes];
		
		// All scores are initially 1.
		Arrays.fill(authorityScores, 1.0);
		Arrays.fill(hubScores, 1.0);
		
		// Run authority update step and hub update step
		// sequentially for k iterations.
		for (int i=0; i<k; i++) {
			
			// Keep track of a normalization value.
			double norm = 0.0;
			
			// Update authority scores.
			for (int j=0; j<numNodes; j++) {
				
				// Authority update step: the authority score for a node
				// is the sum of the hub scores of the nodes that point to it.
				double authScore = 0.0;
				for (Integer incoming : graph.get(j).getIncoming()) {
					authScore += hubScores[incoming];
				}
				
				authorityScores[j] = authScore;
				norm += Math.pow(authScore, 2);
			}
			
			// Normalize authority scores.
			norm = Math.sqrt(norm);
			for (int j=0; j<numNodes; j++) {
				authorityScores[j] = authorityScores[j] / norm;
			}
			
			// Set normalization value back to zero.
			norm = 0.0;
			
			// Update hub scores.
			for (int j=0; j<numNodes; j++) {
				
				// Hub update step: the hub score for a node is the sum
				// of the authority scores of the nodes it points to.
				double hubScore = 0.0;
				for (Integer outgoing : graph.get(j).getOutgoing()) {
					hubScore += authorityScores[outgoing];
				}
				
				hubScores[j] = hubScore;
				norm += Math.pow(hubScore, 2);
			}
			
			// Normalize hub scores.
			norm = Math.sqrt(norm);
			for (int j=0; j<numNodes; j++) {
				hubScores[j] = hubScores[j] / norm;
			}
		}
		
		// Calculate final scores for nodes.
		List<IndexValuePair> scorePairs = new ArrayList<IndexValuePair>();
		for (int i=0; i<numNodes; i++) {
			IndexValuePair pair = new IndexValuePair();
			pair.index = i;
			
			// The score for the node is the average
			// of the authority and hub scores.
			pair.value = (authorityScores[i] + hubScores[i]) / 2;
			scorePairs.add(pair);
		}
		
		Collections.sort(scorePairs);
		Collections.reverse(scorePairs);
		
		List<Integer> sorted = new ArrayList<Integer>();
		for (int i=0; i<numNodes; i++) {
			sorted.add(scorePairs.get(i).index);
		}
	
		return sorted;
	}
	
	/**
	 * Converts list of keywords indices into list of strings.
	 * 
	 * @param 	ordered		Ordered list of word indices.
	 * @param 	words		List of words in sentences.
	 * @return List of ordered keyword strings.
	 */
	private List<String> makeKeywordList(List<Integer> ordered, List<String> words) {
		List<String> keywords = new ArrayList<String>();
		
		for (Integer index : ordered) {
			keywords.add(words.get(index));
		}
		
		return keywords;
	}
}
