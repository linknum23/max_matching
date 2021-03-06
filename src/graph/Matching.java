package graph;

import java.util.ArrayList;
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
	private final int[] matched;
	
	/** The accumulated weight of the matching 
	 */
	private int fullWeight; // keeps track of the weight of the matching

	private int	fullCount; //keeps track of the number of matches
	
	/** Create an empty matching on {@link numVertices}
	 * @param numVertices the number of vertices in the corresponding graph used in the matching
	 */
	public Matching(int numVertices){
		matched = new int[numVertices+1];
		Arrays.fill(matched, UNMATCHED);
		fullWeight = 0;
		fullCount = 0;
	}
	
	
	/** Add an edge, {@link match}, to the matching
	 * @param match
	 */
	public void add(Edge match){
		if(isMatch(key(match)) || isMatch(key2(match))){
			System.out.printf("error adding match %s removing matches to both ends\n", match);
			remove(match.left);
			remove(match.right);
			//throw new RuntimeException("adding match to already matched edge");
		}
		matched[key(match)] = value(match);
		matched[key2(match)] = value2(match);
		fullWeight += match.weight;
		fullCount ++;
	}	
	
	public boolean remove(int v){
		if(isMatched(v)){
			int mate = mate(v);
			return remove(new Edge(v,mate));
		}else{
			return false;
		}
	}
	
	/** Remove an edge, {@link match}, from the matching 
	 * @param match the arc to remove as a match
	 * @return success
	 */
	public boolean remove(Edge match){
		boolean hadMatching = isMatch(key(match)) && isMatch(key2(match));
		boolean validRemoval = isValidRemoval(match);
		if(hadMatching && !validRemoval){
			throw new RuntimeException("invalid removal, not the correct edge");
		}
		
		if(hadMatching){
			matched[key(match)] = UNMATCHED;
			matched[key2(match)] = UNMATCHED;
			fullWeight -= match.weight;
			fullCount --;
		}
		return hadMatching;
	}


	/**
	 * @param match
	 * @return
	 */
	private boolean isValidRemoval(Edge match) {
		return Math.abs(matched[key(match)]) == Math.abs(value(match)) &&  Math.abs(matched[key2(match)]) == Math.abs(value2(match));
	}
	
	/** Augment the matching along a path, starting be setting the first edge
	 * @param path an ordered sequence of arcs
	 */
	public void augment(List<Edge> path){
		boolean setMatch = true;
		List<Edge> wait = new ArrayList<Edge>();
		//remove any arcs in path
		for(Edge e : path){
			//if(!setMatch){
			if(isValidRemoval(e)){
				remove(e);
			}else{
				wait.add(e);
			}
			//}
			//setMatch = !setMatch;
		}
		for(Edge e : wait){
			remove(e);
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
	
	private static int mid2vid(int mid){
		return mid != UNMATCHED ? Math.abs(mid)-1 : -1;
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
		return mid2vid(matched[vid2mid(w)]);
	}


	public int matches() {
		return fullCount;
	}


	public void clear() {
		fullCount = 0;
		fullWeight = 0;
		for(int ii = 0; ii < matched.length; ii++){
			matched[ii] = UNMATCHED;
		}
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Matching){
			Matching m = (Matching)arg0;
			if(m.fullCount == fullCount && m.fullWeight == fullWeight){
				boolean equal = true;
				//check each mate
				for(int ii = 1; ii < matched.length; ii++){
					equal &= matched[ii] == m.matched[ii];
				}
				return equal;
			}else{
				return false;
			}
		}else{
			return false;
		}
	}
	
}
