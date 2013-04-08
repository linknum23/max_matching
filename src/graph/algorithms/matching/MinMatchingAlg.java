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

import graph.Edge;
import graph.Matching;
import graph.Vertex;
import graph.WeightedDigraph;


/** Calculates the minimum (unweighted) matching for a graph 
 * @author Lincoln
 */
public class MinMatchingAlg {
	private static final int NOT_AN_INDEX = -1;
	/**	Matching storage */
	//private final int[] mate; 
	private final Matching matching;
	/** Exposed Vertices*/
	private final int[] exposed; 
	/** */
	private final int[] label;
	
	//private final int[] blossom; //stores vertices blossom correspondence
	
	private final HashMap<Integer, LinkedList<Integer>> blossomCycles;
	
	private final boolean[] marked; // used for additional searches
	
	/** Original Graph**/
	private final WeightedDigraph gOrig;
	
	/** The auxiliary digraph**/
	//private WeightedDigraph A;
	private final HashSet<Edge> A;
	
	/** Vertex search queue**/
	private final Queue<Integer> Q;
	private final boolean[]	seen; //keep track of visited vertices
	private int largestBlossom;
	/**
	 * @param g graph to find the minimal matching of
	 * @param m initial matching
	 */
	public MinMatchingAlg(WeightedDigraph g, Matching m){
		int graphSzWithMaxBlossoms = g.numVertices();//*2;
		//mate = new int[graphSzWithMaxBlossoms];
		matching = m;//new Matching(g.numVertices());
		exposed = new int [graphSzWithMaxBlossoms];
		seen = new boolean[graphSzWithMaxBlossoms];
		label = new int[graphSzWithMaxBlossoms];
		//blossom = new int[graphSzWithMaxBlossoms];
		marked = new boolean[graphSzWithMaxBlossoms];
		A = new HashSet<Edge>(graphSzWithMaxBlossoms);
		blossomCycles = new HashMap<Integer,LinkedList<Integer>>(graphSzWithMaxBlossoms);
		largestBlossom = g.numVertices()-1;
		
		//A = new HashSet<Edge>(g.numVertices());
		Q = (Queue<Integer>) new PriorityQueue<Integer>(g.numVertices());
		gOrig = g;
	}
	
	public void run(){
		//for all v in V initialize mate and exposed to NOT_AN_INDEX
		//Arrays.fill(mate, NOT_AN_INDEX);
		
		//while there is a u in V with considered[u]=0 and mate[u]=0 do
		stage: for(int u = 0; u < gOrig.numVertices()/2; u++){
			//if(mate[u] == NOT_AN_INDEX){
			if(!matching.isMatched(u)){
				
				//considered[u]=1,A={empty}
				//A = new WeightedDigraph(gOrig.numVertices()/2);
				A.clear();
				
				//forall v in V do exposed[v]=0
				Arrays.fill(exposed, NOT_AN_INDEX);
				
				//Construct the auxiliary digraph
				
				//for all (v,w) in E do 
				for(int v = 0; v < gOrig.numVertices(); v++){
					for(Edge e : gOrig.eOuts(v)){
						int w = e.right;
						assert e.left == v : "vertex correspondence is wrong";
						
						//if mate[w]=0 and w!=u then exposed[v]=w else if mate[w] !=v,0 then A=union(A,{v,mate[w]})
						//if(mate[w] == NOT_AN_INDEX && w != u){
						//	exposed[v] = w;
						//}else if(mate[w] != v && mate[w] != NOT_AN_INDEX){
						//	A.addEdge(new Edge(v,mate[w]));
						//}
						
						if(!matching.isMatched(v) && w!= u){
							exposed[v] = w;
						}else if(matching.isMatched(v) && matching.mate(w) != v){
							A.add(new Edge(v,matching.mate(w)));
						}
					}
				}
				
				//forall v in V do seen[v]=0
				Arrays.fill(seen, false);
				
				//Q={u}; label[u]=0; if exposed[u]!=0 then augment(u), goto stage;
				Q.clear(); Q.add(u);
				label[u] = NOT_AN_INDEX;
				if(exposed[u]!= NOT_AN_INDEX){
					augment(u);
					continue;
				}
				//need to figure out how to handle blossom()
				
				//while Q != {empty} do 
				while(!Q.isEmpty()){
					int v = Q.poll();
					
					//forall unlabeled nodes w in V such that (v,w) in A
					for(Edge e: gOrig.eOuts(v)){
						if(A.contains(e)){
							int w = e.right;
							
							//Q=union(Q,w), label[w]=v
							Q.add(w);
							label[w] = v;
							
							//seen[mate[w]] = 1;
							seen[matching.mate(w)] = true;
							
							//if exposed[w]!=0 then augment(w) goto stage;
							if(exposed[w]!=0){
								augment(w);
								continue stage;
							}
							
							//if seen[w]=1 then blossom(w)
							if(seen[w]){
								blossom(w);
							}
						}
					}
				}
			}
		}
	}

	private void blossom(int v) {
		
		LinkedList<Integer> cycle = new LinkedList<Integer>();
		 
		//create a new blossom
		int blossomId = nextAvailableBlossom();
		Arrays.fill(marked, false);
		
		//find the basis of the blossom adding all of the nodes on the way into the blossom correspondence 
		// this can be done by backtracking the label array until the first common node is found between the two paths
		
		//backtrack all the way to the top marking the traversed vertices
		int z = v;

		do{
			marked[z] = true;
			z = label[z];
		}while(z != NOT_AN_INDEX);
		
		//backtrack from the mate of v until the vertex is already marked, this is the start of the blossom
		z = matching.mate(v);
		do{
			addToBlossom(blossomId, z, cycle);
			addToBlossom(blossomId, matching.mate(z), cycle);
			z = label[z];
		}while(!marked[z]);
		
		int root = z;

		
		//backtrack from the original vertex again to the root;
		z = v;
		do{
			addToBlossom(blossomId, z, cycle);
			addToBlossom(blossomId, matching.mate(z), cycle);
			z = label[z];
		}while(z != root);
		
		addToBlossom(blossomId, z, cycle);//notice that the root is the last added node
		
		//connect the blossom to the roots backlink, then remove the roots backlink
		label[blossomId] = label[root];
		label[root] = NOT_AN_INDEX;
		
		//A.contractCycle(gOrig.vertex(root), cycle);
		blossomCycles.put(blossomId, cycle);
	}
	
	private int nextAvailableBlossom() {
		if(largestBlossom == maxBlossomId()){
			throw new RuntimeException("Out of blossom ids, we used too many!"); //in this case we should create a better method that reuses blossom ids
		}
		return ++largestBlossom;
	}

	private void addToBlossom( int b, int v, List<Integer> cycle){
		//blossom[b] = v;
		Q.remove(v);
		Q.add(b);
		
		//replace the backlink from v
		label[v] = NOT_AN_INDEX;
		
		//replace links to v with links to b
		for(int ii = 0; ii < largestBlossom; ii++){
			if(label[ii] == v){
				label[ii] = b;
			}
		}
		
		cycle.add(v);
	}

	private void augment(int u) {
		int w = exposed[u];
		int v = u;
		LinkedList<Integer> augmentingPath = new LinkedList<Integer>();
		augmentingPath.add(w);
		
		//start augmenting from u using label
		// if a blossom is encountered expand the blossom and search along the blossoms cycle 
		// ( when the blossom is expanded replace the blossom with the old root in label)
		//link all of the other nodes of the blossom in label so that a search will end at the root
		// finish when next label = null
		
		while(v != NOT_AN_INDEX){
			if(blossomCycles.containsKey(v)){
				addBlossomToPath_r(augmentingPath, blossomCycles.get(v));
			}else{
				augmentingPath.add(v);
			}
			v = label[v];
		}
		
		matching.augment(toEdges(augmentingPath));
	}
	
	private void addBlossomToPath_r(LinkedList<Integer> augmentingPath, LinkedList<Integer> blossomCycle) {
		for(Integer v : blossomCycle){
			if(blossomCycles.containsKey(v)){
				addBlossomToPath_r(augmentingPath, blossomCycles.get(v));
			}else{
				augmentingPath.add(v);
			}
		}
	}

	private List<Edge> toEdges(List<Integer> vertices){
		if(vertices.size() < 2){
			throw new RuntimeException("vertices size must be 2 or greater to convert to edge list");
		}
		List<Edge> edges = new LinkedList<Edge>();
		
		Iterator<Integer> it = vertices.iterator();
		int v = it.next();
		int w;
		while(it.hasNext()){
			w = v;
			v = it.next();
			edges.add(new Edge(w,v));
		}
		return edges;
	}
	
	private int maxBlossomId(){
		return label.length-1;
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
		//requires keeping track of the outermost pseudonode for each edge
		throw new RuntimeException("Unimplemented");
	}

	public void expandBlossom(Vertex vertex3) {
		// TODO Auto-generated method stub
		throw new RuntimeException("Unimplemented");
	}
}
