package graph.algorithms.matching;

import java.util.LinkedList;
import java.util.List;

import graph.Edge;
import graph.Matching;
import graph.WeightedDigraph;

public class BruteForceMaxWeightedMatching {
	
	private static final Matching	ZERO_WEIGHT_MATCHING	= new Matching(1);
	private final WeightedDigraph	G;
	
	public BruteForceMaxWeightedMatching(WeightedDigraph g) {
		G = g;
	}
	
	public List<Matching> run() {
		List<Edge> edges = G.edges();
		List<Matching> matchings = new LinkedList<Matching>();
		Matching bestMatching = new Matching(G.numVertices());
		int numPermutations =  powerOf2(edges.size());
		for (int ii = 0; ii < numPermutations; ii++ ) {
			Matching test = numToMatching(edges, ii);
			if (test.weight() == bestMatching.weight()) {
				matchings.add(test);
			} else if (test.weight() > bestMatching.weight()) {
				matchings.clear();
				bestMatching = test;
				matchings.add(bestMatching);
			}
		}
		return matchings;
	}
	
	private Matching numToMatching(List<Edge> edges, int num) {
		Matching m = new Matching(G.numVertices());
		int count = 0;
		for (Edge e : edges) {
			if (bitset(num, count)) {
				boolean alreadyMatched = m.isMatched(e.left)||m.isMatched(e.right);
				if(!alreadyMatched){
					m.add(e);
				}else{
					return ZERO_WEIGHT_MATCHING;
				}
			}
			count++ ;
		}
		
		return m;
	}
	
	private boolean bitset(int num, int bit) {
		int bitmask = powerOf2(bit);
		return (num & bitmask) == bitmask;
	}
	
	private int powerOf2(int exponent){
		return 1 << exponent;
	}
	
}
