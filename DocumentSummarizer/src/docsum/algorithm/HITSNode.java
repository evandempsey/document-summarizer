package docsum.algorithm;

import java.util.ArrayList;
import java.util.List;

/**
 * Graph node with list of
 * incoming and outgoing edges.
 * 
 * @author Evan Dempsey
 */
public class HITSNode {
	
	List<Integer> incoming;
	List<Integer> outgoing;

	/**
	 * Default no-argument constructor that
	 * initializes incoming and outgoing ArrayLists.
	 */
	public HITSNode() {
		incoming = new ArrayList<Integer>();
		outgoing = new ArrayList<Integer>();
	}
	
	/**
	 * Adds an incoming edge to the node if 
	 * there is not already an edge from that node.
	 * 
	 * @param 	value	Incoming node index.	
	 */
	public void addIncoming(int value) {
		if (!incoming.contains(value)) {
			incoming.add(value);
		}
	}
	
	/**
	 * Adds an outgoing edge to the node if 
	 * there is not already an edge to that node.
	 * 
	 * @param 	value	Outgoing node index.
	 */
	public void addOutgoing(int value) {
		if (!outgoing.contains(value)) {
			outgoing.add(value);
		}
	}
	
	/**
	 * Gets list of incoming edges.
	 * 
	 * @return	List of incoming edges.
	 */
	public List<Integer> getIncoming() {
		return incoming;
	}
	
	/**
	 * Gets list of outgoing edges.
	 * 
	 * @return	List of outgoing edges.
	 */
	public List<Integer> getOutgoing() {
		return incoming;
	}
}
