package docsum.algorithm;

/**
 * Convenience class for sorting arrays by
 * a floating point value associated with each element.
 * 
 * @author Evan Dempsey
 */
public class IndexValuePair implements Comparable<IndexValuePair>{

	double value;
	int index;
	
	// Comparison takes place on value.
	public int compareTo(IndexValuePair c) {
		return Double.compare(this.value, c.value);
	}
}