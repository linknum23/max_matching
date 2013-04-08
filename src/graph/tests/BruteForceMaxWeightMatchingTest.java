/**
 * 
 */
package graph.tests;

import static org.junit.Assert.*;

import java.util.List;

import graph.Matching;
import graph.WeightedDigraph;
import graph.algorithms.matching.BruteForceMaxWeightedMatching;

import org.junit.Test;

/**
 * @author Lincoln
 *
 */
public class BruteForceMaxWeightMatchingTest {
	
	@Test
	public void test() {
		WeightedDigraph g = new WeightedDigraph(5);
		g.parse("0 1 5");
		g.parse("1 2 5");
		g.parse("2 3 4");
		g.parse("3 4 5");
		g.parse("4 0 4");
		
		Matching m1 = new Matching(g.numVertices());
		m1.add(g.eOuts(0).get(0));
		m1.add(g.eOuts(3).get(0));
		
		Matching m2 = new Matching(g.numVertices());
		m2.add(g.eOuts(1).get(0));
		m2.add(g.eOuts(3).get(0));
		
		BruteForceMaxWeightedMatching bm = new BruteForceMaxWeightedMatching(g);
		List<Matching> matchings = bm.run();
		assertEquals("incorrect number of matchings found", matchings.size(), 2); 
		assertTrue(matchings.get(0).equals(m1) ||matchings.get(0).equals(m2));
		assertTrue(matchings.get(1).equals(m1) ||matchings.get(1).equals(m2));
		assertFalse(matchings.get(0).equals(matchings.get(1)));
	}
	
}
