package graph;

import java.util.Arrays;
import java.util.List;

public class Matching {
	/** Representation of an Unmatched vertex 
	 */
	private static final int UNMATCHED = -1;
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
		matched = new int[numVertices];
		Arrays.fill(matched, UNMATCHED);
		fullWeight = 0;
	}
	
	
	/** Add an edge, {@link match}, to the matching
	 * @param match
	 */
	public void add(Edge match){
		if(isMatched(key(match))){
			throw new RuntimeException("adding match to already matched edge");
		}
		if(isMatched(value(match))){
			throw new RuntimeException("addition of match makes invalid matching, two vertices cannot be used in the same match");
		}
		matched[key(match)] = value(match);
		fullWeight += match.weight;
	}	
	
	/** Remove an edge, {@link match}, from the matching 
	 * @param match the edge to remove
	 * @return success
	 */
	public boolean remove(Edge match){
		boolean hadMatching = isMatched(key(match));
		boolean validRemoval = matched[key(match)] == value(match);
		if(hadMatching && !validRemoval){
			throw new RuntimeException("invalid removal, not the correct edge");
		}
		matched[key(match)] = UNMATCHED;
		fullWeight -= match.weight;
		return hadMatching;
	}
	
	/** Augment the matching along a path, starting be setting the first edge
	 * @param path
	 */
	public void augment(List<Edge> path){
		boolean setMatch = true;
		for(Edge e : path){
			if(!setMatch){
				remove(e);
			}
			setMatch = !setMatch;
		}
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
		return match.left;
	}

	/** Get the value used for determining a valid matching
	 * @param match
	 * @return
	 */
	private static int value(Edge match) {
		return match.right;
	}
	
	
	/** Check if a vertex belongs to the matching
	 * @param v vertex to check
	 * @return whether or not {@link v} belongs to the matching
	 */
	public boolean isMatched(Vertex v){
		return isMatched(v.id());
	}
	
	/** Check if a vertex belongs to the matching
	 * @param vid vertex id to check
	 * @return whether or not {@link v} belongs to the matching
	 */
	public boolean isMatched(int vid){
		return matched[vid] != UNMATCHED;
	}
	
	/** Check if a vertex belongs to the matching
	 * @param e edge to check
	 * @return whether or not {@link e} belongs to the matching
	 */
	public boolean isMatched(Edge e){
		return matched[key(e)] == value(e);
	}
	
	/** Full weight of the matching 
	 * @return matching weight
	 */
	public int weight(){
		return fullWeight;
	}
	
}
