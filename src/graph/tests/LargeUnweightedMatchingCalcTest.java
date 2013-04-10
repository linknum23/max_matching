package graph.tests;

import static org.junit.Assert.*;

import java.util.List;

import graph.Matching;
import graph.WeightedDigraph;
import graph.algorithms.matching.BruteForceMaxWeightedMatching;
import graph.algorithms.matching.MinMatchingAlg;

import org.junit.Test;

public class LargeUnweightedMatchingCalcTest {
	
	private static final int  NUM_TESTS = 1000;
	private static final int MAX_NUM_VERTICES = 40;
	
	@Test
	public void test() {
		for(int ii = 0; ii < NUM_TESTS; ii++){
			int numVertices = (int)(Math.random()*MAX_NUM_VERTICES-1)+2;
			WeightedDigraph g = WeightedDigraph.random(numVertices);
			System.out.printf("\nTest %d : %s\n", ii, g.toString());
			MinMatchingAlg mma = new MinMatchingAlg(g);
			mma.run();
			if(mma.matching().matches() == numVertices/2){
				System.out.printf("TEST PASSED (mm:%d)\n", mma.matching().matches());
			}else{
				BruteForceMaxWeightedMatching bfTest = new BruteForceMaxWeightedMatching(g);
				List<Matching> possibleMatchings = bfTest.runUnweighted();
				int bruteForceMatches = possibleMatchings.get(0).matches();
				int minMatchMatches = mma.matching().matches();
				System.out.printf("TEST %s (bf:%d,mm:%d)\n", bruteForceMatches==minMatchMatches? "PASSED" : "FAILED", bruteForceMatches,minMatchMatches);
				assertTrue(possibleMatchings.get(0).matches()<=mma.matching().matches());
			}
		}
	}
	
}
