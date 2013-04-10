package graph.algorithms.matching;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.prefs.BackingStoreException;

import javax.management.RuntimeErrorException;

import graph.Blossom;
import graph.Edge;
import graph.Matching;
import graph.Vertex;
import graph.WeightedDigraph;

public class MaxWeightedMatching {
	private final WeightedDigraph												G;
	//private final WeightedDigraph												G_j;
	private final float[]																u;
	private final float[]																gamma;
	private final Matching															X;
	private float																				A;
	private final int[]																	S, T;
	private final Queue<Integer>												unscanned;
	private int																					currentIndex;
	private int[]																				b;
	private final HashMap<Integer, LinkedList<Blossom>>	blossoms;
	private final HashMap<Integer, Blossom>							outerBlossoms;
	private final List<Integer>													forwPath, backPath;
	private static final int														INF			= Integer.MAX_VALUE;
	private static final int														EMPTY		= - 1;
	private static final int														UNUSED	= - 1;
	
	enum Step {
		s0, s1_0, s1_1, s1_2, s1_3, s2, s3, s4, Done
	};
	
	private Step	lastStep;
	private final int[]	blossomIndices;
	
	public MaxWeightedMatching(WeightedDigraph g) {
		G = g;
		u = new float[g.numVertices() / 2];
		gamma = new float[g.numVertices()];
		S = new int[g.numVertices()];
		T = new int[g.numVertices()];
		//G_j = new WeightedDigraph(g.numVertices() / 2);
		X = new Matching(g.numVertices() / 2);
		unscanned = (Queue<Integer>) new LinkedList<Integer>();
		blossoms = new HashMap<Integer, LinkedList<Blossom>>(g.numVertices());
		outerBlossoms = new HashMap<Integer, Blossom>(g.numVertices());
		forwPath = new ArrayList<Integer>();
		backPath = new ArrayList<Integer>();
		blossomIndices = new int[g.numVertices()/2];
	}
	
	public void run() {
		Step nextStep = Step.s0;
		Step currentStep;
		while (nextStep != Step.Done) {
			currentStep = nextStep;
			switch( nextStep ) {
				case s0:
					nextStep = step0();
					break;
				case s1_0:
					nextStep = step1_0();
					break;
				case s1_1:
					nextStep = step1_1();
					break;
				case s1_2:
					nextStep = step1_2();
					break;
				case s1_3:
					nextStep = step1_3();
					break;
				case s2:
					nextStep = step2();
					break;
				// case s3:
				// nextStep = step3();
				// break;
				case s4:
					nextStep = step4();
					break;
			}
			lastStep = currentStep;
		}
		
	}
	
	private Step step0() {
		
		// Step 0 (Start) The graph G = (N. A) is given, with a weight wij for each arc (i,j).
		// Set mu_i = 1/2*max {w_ij}, for each node i in N.
		// Set A = +inf.
		// Set X = {empty}. There are no blossoms and no nodes are labeled.
		int max = 0;
		for (Vertex v : G.vertices()) {
			int possibleMax = max(G.eOuts(v));
			if (max < possibleMax) {
				max = possibleMax;
			}
		}
		Arrays.fill(u, max / 2);
		X.clear();
		A = INF;
		
		return Step.s1_0;
	}
	
	private Step step1_0() {
		// Apply the label S:{empty} to each exposed node
		for (Vertex v : G.vertices()) {
			if ( ! X.isMatched(v)) {
				S[v.id()] = EMPTY;
			}
		}
		return Step.s1_1;
	}
	
	private Step step1_1() {
		// If there are no unscanned labels, go to Step 4.
		// Otherwise, find a node i with an unscanned label.
		// If the label is an S-label, go to Step 1.2;
		// if it is a T-label, go to Step 1.3
		
		if (unscanned.isEmpty()) {
			return Step.s4;
		} else {
			int i = unscanned.poll();
			currentIndex = i;
			assert ! (hasSlabel(i) && hasTlabel(i)) : " i is not expected to have a S-label and a T-label";
			if (hasTlabel(i)) {
				return Step.s1_2;
			} else {
				assert hasSlabel(i) : "Labeling is not correct i should have a S-label";
				return Step.s1_3;
			}
		}
	}
	
	private Step step1_2() {
		// Scan the S-label on node i by carrying out the following procedure:
		// for each arc (i, j) not in X and incident to node i:
		// If b(i) = b(j), do nothing; otherwise continue.
		// If node b(j) has an S-label and Whij = 0. backtrace from the S-labels on nodes i and j.
		// If different root nodes are reached, go to Step 2 ( we need to augment );
		// if the same root node is reached, go to Step 3 ( a blossom has been found ).
		// If node b(j) has an S-label and Whij > 0, set A = min {A, Whij/2 } .
		// If node b(j) is unlabeled and Whij = 0, apply the label “T:i, j” to b(j).
		// If node b(j) is unlabeled and Whij > 0, set A == min {A, Wij/2} .
		// When the scanning of node i is complete, return to Step 1.1.
		List<Edge> arcs = G.eOuts(currentIndex);
		for (Edge arc : arcs) {
			int i = arc.left;
			int j = arc.right;
			if (b[i] == b[j]) {
				continue;
			} else {
				if (hasSlabel(b[j])) {
					if (whij(arc) == 0) {
						// If node b(j) has an S-label and Whij = 0. backtrace from the S-labels on nodes i and j.
						// If different root nodes are reached, go to Step 2 ( we need to augment );
						// if the same root node is reached, go to Step 3 ( a blossom has been found ).
						int root1 = backtrace(i, S, forwPath);
						int root2 = backtrace(j, S, backPath);
						if (root1 == root2) {
							step3();
						} else {
							return Step.s2;
						}
					} else if (whij(arc) > 0) {
						// If node b(j) has an S-label and Whij > 0, set A = min {A, Whij/2 } .
						A = Math.min(A, whij(arc) / 2);
					} else {
						assert false : "whij should not return negative";
					}
				} else if (isUnlabeled(b[j])) {
					if (whij(arc) == 0) {
						// If node b(j) is unlabeled and Whij = 0, apply the label “T:i, j” to b(j).
						label(b[j], T, i, j);
					} else if (whij(arc) > 0) {
						// If node b(j) is unlabeled and Whij > 0, set A == min {A, Wij/2} .
						A = Math.min(A, whij(arc) / 2);
					} else {
						assert false : "whij should not return negative";
					}
				}
			}
		}
		throw new RuntimeException("Unimplemented");
	}
	
	private boolean isUnlabeled(int i) {
		return hasTlabel(i) || hasSlabel(i);
	}
	
	private void label(int index, int[] array, int i) {
		array[index] = i;
	}
	
	private void label(int index, int[] array, int i, int j) {
		array[index] = i | (j << 16);
	}
	
	private float whij(Edge arc) {
		// Whij is used to represent
		// ui + uj - wij
		return u[arc.left] + u[arc.right] - arc.weight;
	}
	
	private Step step1_3() {
		// Scan the T-label on node i by carrying out the following procedure
		// for the unique arc (i, j) in X and incident to node i.
		// If b(i) = b(j), do nothing; otherwise continue.
		// If node j has a T-label, backtrace from the T-labels on nodes i and j.
		// If different root nodes are reached, go to Step 2;
		// if the same root node is reached, go to Step 3.
		// Otherwise, give node j the label “S:i.” The S-labels on all nodes
		// within the outermost blossom with base node j are now considered to
		// be unscanned.
		// Return to Step 1.1.
		
		// for the unique arc (i, j) in X and incident to node i.
		List<Edge> arcs = G.eOuts(currentIndex);
		for (Edge arc : arcs) {
			int i = arc.left;
			int j = arc.right;
			if (b[i] == b[j]) {
				// If b(i) = b(j), do nothing; otherwise continue.
				continue;
			} else {
				// If node j has a T-label, backtrace from the T-labels on nodes i and j.
				// If different root nodes are reached, go to Step 2;
				// if the same root node is reached, go to Step 3.
				if (hasTlabel(j)) {
					backPath.clear();
					forwPath.clear();
					int root1 = backtrace(i, T, forwPath);
					int root2 = backtrace(j, T, backPath);
					if (root1 == root2) {
						step3();
					} else {
						return Step.s2;
					}
				} else {
					// Otherwise, give node j the label “S:i.” The S-labels on all nodes
					// within the outermost blossom with base node j are now considered to
					// be unscanned.
					label(j, S, i);
					if (blossoms.containsKey(j)) {
						for (Vertex v : outer(blossoms.get(j)).vertices()) {
							unscanned.add(v.id());
						}
					}
				}
			}
		}
		return Step.s1_1;
	}
	
	private int backtrace(int i, int[] t2, List<Integer> forwPath2) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
	
	private Blossom outer(LinkedList<Blossom> list) {
		for (Blossom b1 : list) {
			boolean found = false;
			for (Blossom b2 : list) {
				if (b2.vertices().contains(b1)) {
					found = true;
				}
			}
			if ( ! found) {
				return b1;
			}
		}
		throw new RuntimeException("not outer blossom found, this should not happen");
	}
	
	private Step step2() {
		// An augmenting path has been found in Step 1.2 or 1.3.
		// Augment the matching X. Correct labels on nodes in the augmenting
		// path, as described in the text.
		// Expand blossoms with zero dual variables (Z_k=0), resetting the blossom numbers b(i).
		// Remove labels from all base nodes.
		// Remaining labels are set to “scanned” state.
		// Set A = +INF.
		// go to Step 1.0.
		
		// TODO:figure out how to augment!!!
		/**
		 * After each augmentation, blossoms must be retained for use in the next application of the labeling procedure. We also wish to retain labels on nodes
		 * within blossoms. But the labels on nodes through which the augmenting path passes are no longer valid, and must be corrected. We carry out this task as
		 * follows. First, identify all the blossoms (not just the outermost ones) through which the augmenting path passes. For each blossom, find its new base
		 * node. (The augmenting path extends between the old base node and the new base node of each blossom through which it passes.) For all nodes in the
		 * augmenting path which are neither new base nodes nor old base nodes, simply interchange the indices of the labels. That is, if the labels on such a node
		 * are “S: i” and “T: j..” the new labels are “S: j ” and “T : i.” For a node b that is a new base node, find the innermost blossom in which it is contained
		 * and the old base node b’ of this blossom. Find arcs (b. i) and (b’.j) of the augmenting path, where i, ,j are not contained in the blossom. The new
		 * labels for b are “S: i” and “T: j, b’.” For a node b’ that is an old base node, find the innermost blossom in which it is contained, and the new base
		 * node b of this blossom. If b = b’, simply interchange the indices of the labels on b’. Otherwise, backtrace from the (old) T-label on b, to discover an
		 * arc (b’, j) not in X, where j is in the blossom. Let (b’, i) be the arc of the augmenting path, where i is in the blossom, incident to b’. The new labels
		 * for b’ are “S:i and “T: j.” An example of the effect of the label correction procedure is shown in Figures 6.15 and 6.16. An augmenting path, extends
		 * between nodes 1 and 10 in Figures 6.15. After augmentation and correction of labels, the labels on nodes within the outermost blossom are as shown in
		 * Figure 6.16. It should be clear that the procedure requires no more than O(n^2) running time, which is all that is required to attain the overall level
		 * of complexity of O(n^3) asserted for the algorithm developed in the next section.
		 */
		
		// determine path by reversing one of the traversal paths and combining them to create
		// the full augmentingPath
		List<Integer> augPath = determineAugPath(forwPath, backPath);
		
		// foreach vertex v in path
		// if isBlossomRoot(v)
		// for each blossom this is the root of
		// find the new blossom root b'
		// if b=b'
		// simply interchange the indices of the labels on b’
		// else
		// - For a node b that is a new base node, find the innermost blossom
		// in which it is contained and the old base node b’ of this blossom. Find arcs
		// (b. i) and (b’.j) of the augmenting path, where i, ,j are not contained in the
		// blossom. The new labels for b are “S: i” and “T: j, b’.”
		// - For a node b’ that is an old base node, find the innermost blossom
		// in which it is contained, and the new base node b of this blossom. Backtrace
		// from the (old) T-label on b, to discover an arc (b’, j) not in X, where j is in the
		// blossom. Let (b’, i) be the arc of the augmenting path, where i is in the blossom,
		// incident to b’. The new labels for b’ are “S:i and “T: j.”
		// else
		// For all nodes in the augmenting path which are neither new base
		// nodes nor old base nodes, simply interchange the indices of the labels.
		// That is, if the labels on such a node are “S: i” and “T: j..” the new labels are
		// “S: j ” and “T : i.”
		for (int vId : augPath) {
			if (isBlossomRoot(vId)) {
				for (Blossom bold : blossoms.get(vId)) {
					int bnew = findNewBlossomRoot(bold, augPath);
					if (bold.id() == bnew) {
						// simply interchange the indices of the labels on b’
						swap(S, T, bold.id());
					} else {						
						// - For a node b that is a new base node, find the innermost blossom
						// in which it is contained and the old base node b’ of this blossom. Find arcs
						// (b. i) and (b’.j) of the augmenting path, where i, ,j are not contained in the
						// blossom. The new labels for b are “S: i” and “T: j, b’.”
						int i1 = outsideArc(bold, augPath, bnew);
						int j1 = outsideArc(bold, augPath, bold.id());
						
						// - For a node b’ that is an old base node, find the innermost blossom
						// in which it is contained, and the new base node b of this blossom. Backtrace
						// from the (old) T-label on b, to discover an arc (b’, j) not in X, where j is in the
						// blossom. Let (b’, i) be the arc of the augmenting path, where i is in the blossom,
						// incident to b’. The new labels for b’ are “S:i and “T: j.”
						int i2 = backtraceTo(T, bnew, bold.id());
						int j2 = insideArc(bold, augPath, bold.id());
						
						// the labels all need to be set at once since changing the labels earlier could cause problems with the backtracing
						label(bnew, S, i1);
						label(bnew, T, j1, bold.id());
						label(bold.id(), S, i2);
						label(bold.id(), T, j2);
						
						// replace the blossoms root
						setNewBlossomRoot(bold, bnew);
					}
				}
			} else {
				// For all nodes in the augmenting path which are neither new base
				// nodes nor old base nodes, simply interchange the indices of the labels.
				// That is, if the labels on such a node are “S: i” and “T: j..” the new labels are
				swap(S, T, vId);
			}
		}
		
		// Expand blossoms with zero dual variables (gamma_k=0), resetting the blossom numbers b(i).
		boolean blossomRemoved;
		do {
			blossomRemoved = false;
			for (Blossom b : outerBlossoms()) {
				if (gamma(b) == 0) {
					expand(b);
					blossomRemoved = true;
				}
			}
		} while (blossomRemoved);
		
		// Remove labels from all base nodes.
		for (Vertex v : G.vertices()) {
			if (isBlossomRoot(v.id())) {
				removeLabel(S, v.id());
				removeLabel(T, v.id());
			}
		}
		
		// Remaining labels are set to “scanned” state.
		unscanned.clear();
		
		// Set A = +INF.
		A = INF;
		
		// go to Step 1.0.
		return Step.s1_0;
	}
	
	/**
	 * @param backPath
	 * @param forwardPath
	 * @return
	 */
	private List<Integer> determineAugPath(List<Integer> forwardPath, List<Integer> backwardPath) {
		Collections.reverse(forwardPath);
		List<Integer> augPath = forwardPath;
		augPath.addAll(backwardPath);
		return augPath;
	}
	
	private void expand(Blossom bl) {
		// expand the blossom by:
		// resetting the blossom numbers b(i).
		// set the blossom index to unused so its reference can be used again
		assert gamma(bl) == 0 : "we should only expand blossoms with gamma == 0";
		for (Vertex v : bl.vertices()) {
			b[v.id()] = v.id();
		}
		
		blossomIndices[bl.ref()] = UNUSED;
	}
	
	private List<Blossom> outerBlossoms() {
		// return all of the outermost blossoms
		
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
	
	private void setNewBlossomRoot(Blossom bOld, int bNew) {
		blossoms.get(bOld.id()).remove(bOld);
		bOld.setRootId(bNew);
		blossoms.get(bOld.id()).add(bOld);
	}
	
	private int backtraceTo(int[] trace, int id, int id2) {
		// backtrace using trace from id until id2 is reached returning the id3 where trace[id3]=id2
		int id3 = id;
		while (trace[id3] != id2) {
			id2 = trace[id3];
		}
		return id3;
	}
	
	private int outsideArc(Blossom b, List<Integer> path, int vId) {
		// find an arc that connects v (of vid) from the outside of b along path
		int vPathIndex = path.indexOf(vId);
		int vLeft = path.get(vPathIndex - 1);
		int vRight = path.get(vPathIndex + 1);
		
		if (b.contains(vLeft)) {
			assert ! b.contains(vRight);
			return vRight;
		} else {
			assert b.contains(vRight);
			return vLeft;
		}
	}
	
	private int insideArc(Blossom b, List<Integer> path, int vId){
		int vPathIndex = path.indexOf(vId);
		int vLeft = path.get(vPathIndex - 1);
		int vRight = path.get(vPathIndex + 1);
		
		if (b.contains(vLeft)) {
			assert ! b.contains(vRight);
			return vLeft;
		} else {
			assert b.contains(vRight);
			return vRight;
		}
	}
	
	private void swap(int[] a1, int[] a2, int id) {
		int temp = a1[id];
		a1[id] = a2[id];
		a2[id] = temp;
	}
	
	private int findNewBlossomRoot(Blossom b, List<Integer> augPath) {
		//first search forward through the path from b.id
		//	is next vertex in blossom?
		//  	set direction=forward
		//    else set direction=backward
		//search in the given direction from b.id until until outside the blossom
		// new blossom id is the id before going outside the blossom
		final int bIndex = augPath.indexOf(b.id());
		int direction;
		if(b.contains(augPath.get(bIndex+1))){
			// set search direction to forward
			direction = 1;
		}else{
			// set search direction to backward
			direction = -1;
		}
		int i;
		for(i = bIndex; b.contains(augPath.get(i)); i+=direction){
		}
		return augPath.get(i-direction);
		
	}
	
	private void removeLabel(int[] array, int i) {
		array[i] = EMPTY;
	}
	
	private float gamma(Blossom b) {
		return gamma[b.ref()];
	}
	
	private boolean isBlossomRoot(int vId) {
		return blossoms.get(vId) != null;
	}
	
	private void step3() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
	
	private Step step4() {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
	
	private boolean hasTlabel(int i) {
		return T[i] != EMPTY;
	}
	
	private boolean hasSlabel(int i) {
		return S[i] != EMPTY;
	}
	
	private int max(List<Edge> edges) {
		int max = Integer.MIN_VALUE;
		for (Edge e : edges) {
			if (e.weight > max) {
				max = e.weight;
			}
		}
		return max;
	}
}
