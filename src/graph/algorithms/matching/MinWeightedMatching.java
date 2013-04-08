package graph.algorithms.matching;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.Edge;
import graph.Matching;
import graph.Vertex;
import graph.WeightedDigraph;

public class MinWeightedMatching {
	private final WeightedDigraph g;
	private final WeightedDigraph G_j;
	private final float[] alpha;
	private final float[] gamma;
	
	public MinWeightedMatching(WeightedDigraph g){
		this.g = g;
		alpha = new float[g.numVertices()/2];
		gamma = new float[g.numVertices()];
		G_j = new WeightedDigraph(g.numVertices()/2);
	}
	
	public void run(){
		//Weighted Matching Algorithm
		//Input: A n x n matrix [c_ij] for nonnegative integers; n is even 
		//Output: The complete matching M which has the smallest total cost under c_ij

		//begin
		//   forall v_i in V do a_i:=.5*min(c_{i j})
		for(Vertex v : g.vertices()){
			alpha[v.id()] = min(g.eOuts(v))/2;
		}
		
		//   forall k do \gamma_k:=0
		Arrays.fill(gamma, 0);
		
		//   M:=empty;
		Matching M = new Matching(g.numVertices());
		
		//create the min matching alg instance that will be used to keep track of the algorithm state in every iteration
		MinMatchingAlg mma = new MinMatchingAlg(G_j, M);
		
	  //  J_b:=empty; (J_b contains all odd sets S_k with \gamma_k<0)
		// NOTE: unsure what needs to be done with J_b at this time
		
		//   while |M| < n/2 do
		//      begin
		while (M.matches() < g.numVertices()){
			
		//         construct the admissible graph G_J by 
		//      	    - including all the edges [v_i,v_j] with \alpha_i+\alpha_j+\sum_{i,j in S_k}(gamma_k) = C_{i j} ( we shall call this set E^= ) 
		//	          - shrinking all sets S_k in J_b; //is this required? It may be required to have a blossom correspondence
	  // NOTE: these don't need to be done since G_j still has all of this information
			
			
		//      	 find the maximum matching in G_J starting from the current matching M; //note M is modified by the alg!
			
			mma.run();
			
		//      	 let G_c be the current graph at the conclusion of the (unweighted) maximum matching algorithm for G_J;
		//      	 let O be the set of outer vertices in G_c
		List<Vertex> O = mma.O();
		
		//      	 let I the set of inner vertices
		List<Vertex> I = mma.I();
		
		//	 let \Psi_O the set of outer psuedo nodes
		List<Vertex> Psi_O = mma.Psi_O();
		
		//	 let \Psi_I the set of inner psuedo nodes
		List<Vertex> Psi_I = mma.Psi_I();
		
		//	 calculate for possible candidates:
		Edge edge1 = null, edge2 = null;
		//	 delta1:=.5*min(c_{ij}-\alpha_i-\alpha_j for v_i and  v_j \in O not in the same psuedonode)
		float delta1 = Integer.MAX_VALUE;
		for(Vertex v : G_j.vertices()){
			for(Edge e : G_j.eOuts(v)){
				float possibleD1 = (e.weight-alpha[e.left] -alpha[e.right])/2;
				if(possibleD1 < delta1 && mma.inSamePseudoNode(e)){
					delta1 = possibleD1;
					edge1 = e;
				}
			}
		}
		
		//	 delta2:=c_{ij}-\alpha_i-\alpha_j for v_i \in O, v_j \in V - I - O
		float delta2 = Integer.MAX_VALUE;
		for(Vertex v : O){
			for(Edge e : G_j.eOuts(v)){
				float possibleD2 = e.weight-alpha[e.left] -alpha[e.right];
				Vertex w = G_j.vertex(e.right);
				if(possibleD2 < delta2 && !O.contains(w) && !I.contains(w)){
					delta2 = possibleD2;
					edge2 = e;
				}
			}
		}
		
		//	 delta3:=min(-gamma_k/2 forall S_k in Psi_I)
		float delta3 = Integer.MAX_VALUE;
		Vertex vertex3 = null;
		for(Vertex v : Psi_I){
			float possibleD3 = -gamma[v.id()]/2;
			if(possibleD3 < delta3){
				delta3 = possibleD3;
				vertex3 = v;//record the vertex so the corresponding blossom can be shrunk
			}
		}
		
		assert vertex3 != null : "Expected delta3 to be assigned atleast once in the loop";
		
		//   calculate theta_1=min(delta_1,delta_2,delta_3)
		final float theta1;
		Edge minEdge;
		if(delta1 <= delta2 && delta1 <= delta3){//delta1 is smallest
			theta1 = delta1;
			minEdge = edge1;
			G_j.addEdge(minEdge);
		}else if (delta2 <= delta3){//delta 2 is smallest
			theta1 = delta2;
			minEdge = edge2;
			G_j.addEdge(minEdge);
		}else{ // delta 3 is smallest
			theta1 = delta3;
			// no edge added however the corresponding blossom needs to be expanded!!
			mma.expandBlossom(vertex3);
		}
		
		
		//   forall v_j in O do \alpha_j:=alpha_j+theta_1;
		for(Vertex v : O){
			alpha[v.id()] += theta1;
		}
		
		//   forall v_j in I do \alpha_j:=alpha_j-theta_1;
		for(Vertex v : I){
			alpha[v.id()] -= theta1;
		}
		
		//   forall v_j in Psi_O do \gamma_j:=\gamma_j-2*\theta_1;
		for(Vertex v : Psi_O){
			gamma[v.id()] -= theta1*2;//we need to be sure this index is globally unique
		}
		
		//   forall v_j in Psi_I do \gamma_j:=\gamma_j+2*\theta_1;
		for(Vertex v : Psi_I){
			gamma[v.id()] += theta1*2;
		}
		
		//   recover the maximum proper matching M of (V,J_e) from the maximum matching of G_J;
		//   let J_b:=\{S_k \in J_b union Psi_o : \gamma_k < 0 \} 
		//   end 
		//end
		}
	}

	private int min(List<Edge> edges) {
		int min = Integer.MAX_VALUE;
		for(Edge e : edges){
			if(e.weight < min){
				min = e.weight;
			}
		}
		return min;
	}
}
