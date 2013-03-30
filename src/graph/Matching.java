package graph;

import java.util.Arrays;
import java.util.List;

/** Represents a selection of arcs in a weighted digraph, such that each vertex is included at most once
 * @author Lincoln
 */
public class Matching {
	/** Representation of an Unmatched vertex 
	 */
	private static final int UNMATCHED = Integer.MIN_VALUE;
	
	/**the matching, UNMATCHED represents an unmatched state, value >= 0 represents matched state 
	 * with value corresponding to the second vertex in the matching. The index represents the 
	 * first vertex in the matching.  
	 */
	private int[] matched;
	
	/** The accumulated weight of the matching 
	 */
	private int fullWeight; // keeps track of the weight of the matching
	
	/** Create an empty matching on {@link numVertices}
	 * @param numVertices the number of vertices in the corresponding graph used in the matching
	 */
	public Matching(int numVertices){
		matched = new int[numVertices+1];
		Arrays.fill(matched, UNMATCHED);
		fullWeight = 0;
	}
	
	
	/** Add an edge, {@link match}, to the matching
	 * @param match
	 */
	public void add(Edge match){
		if(isMatch(key(match)) || isMatch(key2(match))){
			throw new RuntimeException("adding match to already matched edge");
		}
		matched[key(match)] = value(match);
		matched[key2(match)] = value2(match);
		fullWeight += match.weight;
	}	
	
	/** Remove an edge, {@link match}, from the matching 
	 * @param match the arc to remove as a match
	 * @return success
	 */
	public boolean remove(Edge match){
		boolean hadMatching = isMatch(key(match)) && isMatch(key2(match));
		boolean validRemoval = matched[key(match)] == value(match) &&  matched[key2(match)] == value2(match);
		if(hadMatching && !validRemoval){
			throw new RuntimeException("invalid removal, not the correct edge");
		}
		matched[key(match)] = UNMATCHED;
		matched[key2(match)] = UNMATCHED;
		fullWeight -= match.weight;
		return hadMatching;
	}
	
	/** Augment the matching along a path, starting be setting the first edge
	 * @param path an ordered sequence of arcs
	 */
	public void augment(List<Edge> path){
		boolean setMatch = true;
		//remove any even arcs
		for(Edge e : path){
			if(!setMatch){
				remove(e);
			}
			setMatch = !setMatch;
		}
		//add all of the odd arcs to the matching
		setMatch = true;
		for(Edge e : path){
			if(setMatch){
				add(e);
			}
			setMatch = !setMatch;
		}
		
	}

	/** Get the key used to index into the matching
	 * @param match
	 * @return
	 */
	private static int key(Edge match) {
		return vid2mid(match.left);
	}

	/** Get the value used for determining a valid matching
	 * @param match
	 * @return
	 */
	private static int value(Edge match) {
		return vid2mid(match.right);
	}
	
	/** Get the second value used for determining a valid matching
	 * @param match
	 * @return
	 */
	private static int value2(Edge e) {
		return -key(e);
	}

	/** Get the second key used to index into the matching
	 * @param match
	 * @return
	 */
	private static int key2(Edge e) {
		return value(e);
	}
	
	/** Check if a vertex belongs to the matching
	 * @param v vertex to check
	 * @return whether or not {@link v} belongs to the matching
	 */
	public boolean isMatched(Vertex v){
		return isMatched(v.id());
	}
	
	/** Check if a vertex belongs to the matching
	 * @param vid vertex to check
	 * @return whether or not {@link v} belongs to the matching
	 */
	public boolean isMatched(int vid){
		return isMatch(vid2mid(vid));
	}
	
	private static int vid2mid(int vid){
		return vid+1;
	}
	
	/** Check if a vertex belongs to the matching
	 * @param mid matching id to check
	 * @return whether or not {@link v} belongs to the matching
	 */
	private boolean isMatch(int mid){
		return matched[mid] != UNMATCHED;
	}
	
	/** Check if a vertex belongs to the matching
	 * @param e edge to check
	 * @return whether or not {@link e} belongs to the matching
	 */
	public boolean isMatched(Edge e){
		return matched[key(e)] == value(e) || matched[key2(e)] == value2(e);
	}
	
	/** Full weight of the matching 
	 * @return matching weight
	 */
	public int weight(){
		return fullWeight;
	}
	
	@Override
	public String toString() {
		StringBuffer s = new StringBuffer();
		for(int ii = 1; ii < matched.length; ii++){
			if(matched[ii] > 0){
				s.append(String.format("(%s %s),", ii-1, matched[ii]-1));
			}
		}
		return s.substring(0, s.length()-1);
	}


	public int mate(int w) {
		return matched[vid2mid(w)];
	}
	
}
