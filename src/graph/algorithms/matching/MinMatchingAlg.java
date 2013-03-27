package graph.algorithms.matching;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;

import graph.Edge;
import graph.Matching;
import graph.WeightedDigraph;


/** Calculates the minimum (unweighted) matching for a graph 
 * @author Lincoln
 */
public class MinMatchingAlg {
	private static final int NOT_AN_INDEX = -1;
	private final int[] mate, exposed, seen;
	private final WeightedDigraph gOrig;
	private final HashSet<Edge> A;
	/**
	 * @param g graph to find the minimal matching of
	 * @param m initial matching
	 */
	public MinMatchingAlg(WeightedDigraph g, Matching m){
		mate = new int[g.numVertices()];
		exposed = new int [g.numVertices()];
		seen = new int[g.numVertices()];
		A = new HashSet<Edge>(g.numVertices());
		gOrig = g;
	}
	
	public void run(){
		//for all v in V initialize mate and exposed to NOT_AN_INDEX
		Arrays.fill(mate, NOT_AN_INDEX);
		
		//while there is a u in V with considered[u]=0 and mate[u]=0 do
		for(int u = 0; u < gOrig.numVertices(); u++){
			if(mate[u] == NOT_AN_INDEX){
				
				//considered[u]=1,A={empty}
				A.clear();
				
				//forall v in V do exposed[v]=0
				Arrays.fill(exposed, NOT_AN_INDEX);
				
				//Construct the auxiliary digraph
				
				//for all [v,w] in E do 
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
				Arrays.fill(seen, NOT_AN_INDEX);
				
				//Q={u}; label[u]=0; if exposed[u]!=0 then augment(u), goto stage;
				
				//need to figure out how to handle blossom()
			}
		}
	}
}
