package graph.algorithms.matching;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;

import graph.Edge;
import graph.Matching;
import graph.WeightedDigraph;


/** Calculates the minimum (unweighted) matching for a graph 
 * @author Lincoln
 */
public class MinMatchingAlg {
	private static final int NOT_AN_INDEX = -1;
	private final int[] mate; 
	private final int[] exposed; 
	private final int[] label;
	
	/** Original Graph**/
	private final WeightedDigraph gOrig;
	
	/** The auxiliary digraph**/
	private final HashSet<Edge> A;
	
	/** Vertex search queue**/
	private final Queue<Integer> Q;
	private final boolean[]	seen; //keep track of visited vertices
	/**
	 * @param g graph to find the minimal matching of
	 * @param m initial matching
	 */
	public MinMatchingAlg(WeightedDigraph g, Matching m){
		mate = new int[g.numVertices()];
		exposed = new int [g.numVertices()];
		seen = new boolean[g.numVertices()];
		label = new int[g.numVertices()];
		
		A = new HashSet<Edge>(g.numVertices());
		Q = (Queue<Integer>) new PriorityQueue<Integer>(g.numVertices());
		gOrig = g;
	}
	
	public void run(){
		//for all v in V initialize mate and exposed to NOT_AN_INDEX
		Arrays.fill(mate, NOT_AN_INDEX);
		
		//while there is a u in V with considered[u]=0 and mate[u]=0 do
		stage: for(int u = 0; u < gOrig.numVertices(); u++){
			if(mate[u] == NOT_AN_INDEX){
				
				//considered[u]=1,A={empty}
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
						if(mate[w] == NOT_AN_INDEX && w != u){
							exposed[v] = w;
						}else if(mate[w] != v && mate[w] != NOT_AN_INDEX){
							A.add(new Edge(v,mate[w]));
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
							seen[mate[w]] = true;
							
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

	private void blossom(int w) {
		// TODO Auto-generated method stub
		
	}

	private void augment(int u) {
		// TODO Auto-generated method stub
		
	}
}
