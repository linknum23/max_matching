package graph.algorithms.matching;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import javax.management.RuntimeErrorException;

import graph.Blossom;
import graph.Edge;
import graph.Matching;
import graph.Vertex;
import graph.WeightedDigraph;

/**
 * Calculates the minimum (unweighted) matching for a graph
 * 
 * @author Lincoln
 */
public class MinMatchingAlg {
	private static final int								NOT_AN_INDEX	= - 1;
	/** Matching storage */
	// private final int[] mate;
	private final Matching									matching;
	/** Exposed Vertices */
	private final int[]											exposed;
	/** */
	private final int[]											label;
	
	// private final int[] blossom; //stores vertices blossom correspondence
	
	// private final HashMap<Integer, LinkedList<Integer>> blossoms;
	private final HashMap<Integer, Blossom>	blossoms;
	
	private final boolean[]									marked;							// used for additional searches
																																
	/** Original Graph **/
	private final WeightedDigraph						gOrig;
	
	/** The auxiliary digraph **/
	// private WeightedDigraph A;
	private final List<Edge>								A;
	
	/** Vertex search queue **/
	private final LinkedList<Integer>				Q;
	private final boolean[]									seen;								// keep track of visited vertices
	private int															largestBlossom;
	
	private boolean													debug					= true; // turn on debug printing
																																
	/**
	 * @param g graph to find the minimal matching of
	 * @param m initial matching
	 */
	public MinMatchingAlg(WeightedDigraph g) {
		int graphSzWithMaxBlossoms = g.numVertices() * 2;
		// mate = new int[graphSzWithMaxBlossoms];
		matching = new Matching(g.numVertices());
		exposed = new int[graphSzWithMaxBlossoms];
		seen = new boolean[graphSzWithMaxBlossoms];
		label = new int[graphSzWithMaxBlossoms];
		// blossom = new int[graphSzWithMaxBlossoms];
		marked = new boolean[graphSzWithMaxBlossoms];
		A = new ArrayList<Edge>(graphSzWithMaxBlossoms);
		// blossoms = new HashMap<Integer,LinkedList<Integer>>(graphSzWithMaxBlossoms);
		blossoms = new HashMap<Integer, Blossom>(graphSzWithMaxBlossoms);
		largestBlossom = g.numVertices() - 1;
		
		// A = new HashSet<Edge>(g.numVertices());
		Q = new LinkedList<Integer>();
		gOrig = g;
	}
	
	public void run() {
		// for all v in V initialize mate and exposed to NOT_AN_INDEX
		// Arrays.fill(mate, NOT_AN_INDEX);
		Arrays.fill(label, NOT_AN_INDEX);
		
		// while there is a u in V with considered[u]=0 and mate[u]=0 do
		stage: for (int u = 0; u < gOrig.numVertices(); u++ ) {
			// if(mate[u] == NOT_AN_INDEX){
			if (gOrig.vertex(u) == null) {
				continue;
			}
			if (matching.matches() == gOrig.numVertices() / 2) {
				// we are done
				return;
			}
			System.out.print(String.format("considering vertex u%d\n", u));
			if ( ! matching.isMatched(u)) {
				
				// considered[u]=1,A={empty}
				// A = new WeightedDigraph(gOrig.numVertices()/2);
				A.clear();
				
				// forall v in V do exposed[v]=0
				Arrays.fill(exposed, NOT_AN_INDEX);
				
				// Construct the auxiliary digraph
				
				// for all (v,w) in E do
				for (int v = 0; v < gOrig.numVertices(); v++ ) {
					for (Edge e : gOrig.eOuts(v)) {
						int w = e.right;
						assert e.left == v : "vertex correspondence is wrong";
						
						// if mate[w]=0 and w!=u then exposed[v]=w else if mate[w] !=v,0 then A=union(A,{v,mate[w]})
						// if(mate[w] == NOT_AN_INDEX && w != u){
						// exposed[v] = w;
						// }else if(mate[w] != v && mate[w] != NOT_AN_INDEX){
						// A.addEdge(new Edge(v,mate[w]));
						// }
						
						if ( ! matching.isMatched(w) && w != u) {
							// DEBUG(String.format("adding (%d,%d) to exposed\n", v, w));
							if (exposed[v] == NOT_AN_INDEX) {
								exposed[v] = w;
							}
						} else if (matching.mate(w) != v && matching.isMatched(w)) {
							// DEBUG(String.format("adding (%d,%d) to A\n", v, matching.mate(w)));
							A.add(new Edge(v, matching.mate(w)));
						}
					}
				}
				DEBUG(String.format("Exposed vertices={%s}\n", printExposed()));
				if (exposed().size() == 0) {
					continue;
				}
				
				// forall v in V do seen[v]=0
				Arrays.fill(seen, false);
				
				// Q={u}; label[u]=0; if exposed[u]!=0 then augment(u), goto stage;
				Q.clear();
				Q.add(u);
				Arrays.fill(label, NOT_AN_INDEX);// unsure whether it was meant to clear label or just unset label[u] OLD_CODE=label[u] = NOT_AN_INDEX;
				if (isExposed(u)) {
					augment(u);
					DEBUG(String.format("new matching %s\n", matching.toString()));
					continue;
				}
				// need to figure out how to handle blossom()
				
				// while Q != {empty} do
				while ( ! Q.isEmpty()) {
					int v = Q.pop();
					
					// forall unlabeled nodes w in V such that (v,w) in A
					for (Edge e : A) {
						int w = e.left;
						if (e.right == v && label[w] == NOT_AN_INDEX && label[v] != w) {
							
							// Q=union(Q,w), label[w]=v
							if ( ! Q.contains(w)) {
								Q.offer(w);
							}else{
								continue; ///THIS CONTINUE WAS ADDED LATE AT NIGHT
							}
							label[w] = v;
							
							// seen[mate[w]] = 1;
							try{
								int mate = findMate(w);
								seen[mate] = true;
							}catch(Exception err){
								DEBUG(String.format("error marking mate of %d as seen, mate not found\n", w));
							}
							// if exposed[w]!=0 then augment(w) goto stage;
							if (isExposed(w)) {
								augment(w);
								DEBUG(String.format("new matching %s\n", matching.toString()));
								continue stage;
							}
							
							// if seen[w]=1 then blossom(w)
							if (seen[w]) {
								blossom(w);
							}
						}
					}
					// remove loops created by the blossoms
					removeSelfLoops(A);
				}
			}
		}
	}
	
	private List<Integer> exposed() {
		List<Integer> expList = new LinkedList<Integer>();
		for (int ii = 0; ii < exposed.length; ii++ ) {
			if (isExposed(ii)) {
				expList.add(ii);
			}
		}
		return expList;
	}
	
	private String printExposed() {
		StringBuffer p = new StringBuffer();
		for (int exp : exposed()) {
			p.append(exp);
			p.append(",");
		}
		if (p.length() > 0) {
			return p.substring(0, p.length() - 1);
		} else {
			return "";
		}
	}
	
	/**
	 * @param ii
	 * @return
	 */
	private boolean isExposed(int ii) {
		return exposed[ii] != NOT_AN_INDEX;
	}
	
	private void blossom(int v) {
		
		LinkedList<Integer> cycle = new LinkedList<Integer>();
		
		// create a new blossom
		int blossomId = nextAvailableBlossom();
		Arrays.fill(marked, false);
		List<Integer> fix = new ArrayList<Integer>();
		// find the basis of the blossom adding all of the nodes on the way into the blossom correspondence
		// this can be done by backtracking the label array until the first common node is found between the two paths
		
		// backtrack all the way to the top marking the traversed vertices
		int z = v;
		
		do {
			marked[z] = true;
			z = label[z];
		} while (z != NOT_AN_INDEX);
		
		// backtrack from the mate of v until the vertex is already marked, this is the start of the blossom
		z = matching.mate(v);
		do {
			// addToBlossom(blossomId, z, cycle);
			// addToBlossom(blossomId, matching.mate(z), cycle);
			cycle.push(z);
			fix.add(z);
			int mate = findMate(z);
			cycle.push(mate);
			fix.add(mate);
			z = label[z];
		} while ( ! marked[z]);
		
		int root = z;
		
		fix.add(v);
		// backtrack from the original vertex again to the root;
		z = label[v];// skip over already visited vertex
		while (z != root) {
			// addToBlossom(blossomId, z, cycle);
			// addToBlossom(blossomId, matching.mate(z), cycle);
			cycle.offer(z);
			fix.add(z);
			int mate = findMate(z);
			cycle.offer(mate);
			fix.add(mate);
			z = label[z];
		}
		
		cycle.push(root);// cycle.offer(root);
		// addToBlossom(blossomId, z, cycle);//notice that the root is the last added node
		
		// remember all of the edges
		List<Edge> edges = new LinkedList<Edge>();
		for (Edge e : A) {
			for (Integer vid : cycle) {
				if (e.left == vid || e.right == vid) {
					edges.add(new Edge(e));
				}
			}
		}
		
		for (int id : fix) {
			label[id] = NOT_AN_INDEX;
			Q.remove((Integer) id);
			relabel(id, blossomId);
		}
		
		// connect the blossom to the roots backlink, then remove the roots backlink
		label[blossomId] = label[root]; //CHANGED LATE AT NIGHT
		label[root] = NOT_AN_INDEX;
		relabel(root, blossomId);
		Q.add(blossomId);
		
		// A.contractCycle(gOrig.vertex(root), cycle);
		Blossom b = new Blossom(blossomId, root, cycle, edges);
		blossoms.put(blossomId, b);
		
		//remove the valid edges in b from the matching to avoid issues
		fixMatching(b);
		DEBUG(String.format("Blossom added : %s\n", b.toString()));
	}
	
	private int findMate(int z) {
		if (isBlossom(z)) {
			int mate = findMate_R(z);
			if(mate == NOT_AN_INDEX){
				throw new RuntimeException("mate to blossom not found");
			}else{
				return mate;
			}
		} else {
			return matching.mate(z);
		}
	}

	/**
	 * @param z
	 */
	private int findMate_R(int z) {
		List<Integer> innerCycle = blossoms.get(z).cycle();
		for (int vid : innerCycle) {
			if ( ! isBlossom(vid)) {
				int mate = matching.mate(vid);
				if (mate >= 0 && ! innerCycle.contains(mate)) {
					return mate;
				}
			}
		}
		for (int vid : innerCycle) {
			if (isBlossom(vid)) {
				int mate = findMate_R(vid);
				if(mate != NOT_AN_INDEX){
					return mate;
				}
			}
		}
		return NOT_AN_INDEX;
	}
	
	private void removeSelfLoops(List<Edge> a2) {
		Iterator<Edge> it = a2.iterator();
		while (it.hasNext()) {
			Edge e = it.next();
			if (e.right == e.left) {
				it.remove();
			}
		}
	}
	
	/**
	 * @param oldId
	 * @param newId
	 */
	private void relabel(int oldId, int newId) {
		for (int ii = 0; ii < label.length; ii++ ) {
			if (label[ii] == oldId) {
				// set to blossom id
				label[ii] = newId;
			}
		}
		for (Edge e : A) {
			if (e.right == oldId) {
				// set to blossom id
				e.right = newId;
			}
			if (e.left == oldId) {
				e.left = newId;
			}
		}
	}
	
	private int nextAvailableBlossom() {
		if (largestBlossom == maxBlossomId()) {
			throw new RuntimeException("Out of blossom ids, we used too many!"); // in this case we should create a better method that reuses blossom ids
		}
		return ++ largestBlossom;
	}
	
	private void augment(int u) {
		int w = exposed[u];
		int v = u;
		LinkedList<Integer> augmentingPath = new LinkedList<Integer>();
		augmentingPath.add(w);
		
		// start augmenting from u using label
		// if a blossom is encountered expand the blossom and search along the blossoms cycle
		// ( when the blossom is expanded replace the blossom with the old root in label)
		// link all of the other nodes of the blossom in label so that a search will end at the root
		// finish when next label = null
		
		while (v != NOT_AN_INDEX) {
			if (isBlossom(v)) {
				Blossom b = blossoms.remove(v);
				
				int lastAugIndex = augmentingPath.size()-1;
				if(b.cycle().contains(augmentingPath.get(lastAugIndex))){
					//augmentingPath.remove(lastAugIndex);
				}
				
				//fixMatching(b);
				addBlossomToPath_r(augmentingPath, reorderBlossomCycle(b, augmentingPath.getLast())); // the way blossom cycles are inserted needs to be redone
			} else {
				augmentingPath.add(v);
				if (matching.isMatched(v)) {
					augmentingPath.add(matching.mate(v));
				}
			}
			v = label[v];
		}
		
		System.out.printf("Augmenting along Path p=%s\n", augmentingPath.toString());
		
		matching.augment(toEdges(augmentingPath));
	}
	
	private boolean isBlossom(int v) {
		return blossoms.containsKey(v);
	}
	
	private List<Integer> reorderBlossomCycle(Blossom b, Integer last) {
		int first = - 1;
		boolean started = false;
		LinkedList<Integer> reordered = new LinkedList<Integer>();
		List<Integer> lastConnections = vList2IList(gOrig.vOuts(last));
		
		// add the cycle from the start of the actually connected vertex
		for (Integer v : b.cycle()) {
			if (started) {
				reordered.add(v);
			} else if (lastConnections.contains(v) || (isBlossom(v) && canConnect_R(blossoms.get(v), lastConnections))) {
				started = true;
				first = v;
				reordered.add(v);
			}
		}
		
		// add the rest
		for (int v : b.cycle()) {
			if (v == first) {
				break;
			} else {
				reordered.add(v);
			}
		}
		return reordered;
	}
	
	private boolean canConnect_R(Blossom b, List<Integer> lastConnections) {
		for (Integer v : b.cycle()) {
			if (lastConnections.contains(v)) {
				return true;
			} else if (isBlossom(v)) {
				if (canConnect_R(blossoms.get(v), lastConnections)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private List<Integer> vList2IList(List<Vertex> vList) {
		List<Integer> iList = new LinkedList<Integer>();
		for (Vertex v : vList) {
			iList.add(v.id());
		}
		return iList;
	}
	
	private void addBlossomToPath_r(LinkedList<Integer> augmentingPath, List<Integer> cycle) {
		for (Integer v : cycle) {
			if (blossoms.containsKey(v)) {
				Blossom b = blossoms.remove(v);
				//fixMatching(b);
				addBlossomToPath_r(augmentingPath, reorderBlossomCycle(b, augmentingPath.getLast()));
			} else {
				augmentingPath.add(v);
			}
		}
	}
	
	/**
	 * @param b
	 */
	private void fixMatching(Blossom b) {
		if(true){
		for (Edge e : toEdges(b.cycle())) {
			if(!isBlossom(e.left) && !isBlossom(e.right)){
				//if(matching.isMatched(e)){
				try{
					matching.remove(e);
				}catch(Exception exc){
					DEBUG(String.format("Error fixing matching, failed to remove e=%s\n",e.toString()));
				}
				//}
			}
		}
		// remove connecting edge of cycle
		Edge connecting = new Edge(b.cycle().get(0), b.cycle().get(b.cycle().size() - 1));
		try{
			matching.remove(connecting);
		}catch(Exception exc){
			DEBUG(String.format("Error fixing matching, failed to remove e=%s\n",connecting.toString()));			
		}
		}else{
			for(int v : b.cycle()){
				if(!isBlossom(v)){
					matching.remove(v);
				}
			}
		}
	}
	
	private List<Edge> toEdges(List<Integer> vertices) {
		if (vertices.size() < 2) {
			throw new RuntimeException("vertices size must be 2 or greater to convert to edge list");
		}
		List<Edge> edges = new LinkedList<Edge>();
		
		Iterator<Integer> it = vertices.iterator();
		int v = it.next();
		int w;
		while (it.hasNext()) {
			w = v;
			v = it.next();
			edges.add(new Edge(w, v));
		}
		return edges;
	}
	
	private int maxBlossomId() {
		return label.length * 2;
	}
	
	public List<Vertex> O() {
		throw new RuntimeException("Unimplemented");
	}
	
	public List<Vertex> Psi_O() {
		throw new RuntimeException("Unimplemented");
	}
	
	public List<Vertex> I() {
		throw new RuntimeException("Unimplemented");
	}
	
	public List<Vertex> Psi_I() {
		throw new RuntimeException("Unimplemented");
	}
	
	public boolean inSamePseudoNode(Edge e) {
		// requires keeping track of the outermost pseudonode for each edge
		throw new RuntimeException("Unimplemented");
	}
	
	public void expandBlossom(Vertex vertex3) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
	
	public Matching matching() {
		return matching;
	}
	
	private String DEBUG(String s) {
		if (debug) {
			System.out.print(s);
		}
		return s;
	}
}
