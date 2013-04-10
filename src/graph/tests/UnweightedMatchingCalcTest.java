package graph.tests;

import static org.junit.Assert.*;
import graph.Matching;
import graph.WeightedDigraph;
import graph.algorithms.matching.MinMatchingAlg;

import org.junit.Test;

public class UnweightedMatchingCalcTest {

	@Test
	public void test() {
		WeightedDigraph g = new WeightedDigraph(18);
		g.parse("1 2 1");
		g.parse("1 4 1");
		g.parse("2 1 1");
		g.parse("2 3 1");
		g.parse("2 15 1");
		g.parse("3 2 1");
		g.parse("3 4 1");
		g.parse("4 1 1");
		g.parse("4 3 1");
		g.parse("4 5 1");
		g.parse("4 6 1");
		g.parse("4 8 1");
		g.parse("4 15 1");
		g.parse("5 4 1");
		g.parse("5 6 1");
		g.parse("5 16 1");
		g.parse("6 4 1");
		g.parse("6 5 1");
		g.parse("6 7 1");
		g.parse("6 8 1");
		g.parse("8 4 1");
		g.parse("8 6 1");
		g.parse("8 7 1");
		g.parse("8 9 1");
		g.parse("7 6 1");
		g.parse("7 8 1");
		g.parse("7 11 1");
		g.parse("7 13 1");
		g.parse("7 14 1");
		g.parse("7 16 1");
		g.parse("9 8 1");
		g.parse("9 10 1");
		g.parse("9 11 1");
		g.parse("10 9 1");
		g.parse("10 11 1");
		g.parse("10 17 1");
		g.parse("11 7 1");
		g.parse("11 9 1");
		g.parse("11 10 1");
		g.parse("11 12 1");
		g.parse("12 11 1");
		g.parse("12 13 1");
		g.parse("12 17 1");
		g.parse("13 7 1");
		g.parse("13 12 1");
		g.parse("13 14 1");
		g.parse("14 7 1");
		g.parse("14 13 1");
		g.parse("14 16 1");
		g.parse("15 2 1");
		g.parse("15 4 1");
		g.parse("16 5 1");
		g.parse("16 7 1");
		g.parse("16 14 1");
		g.parse("17 10 1");
		g.parse("17 12 1");
		
		MinMatchingAlg mma = new MinMatchingAlg(g);
		mma.run();
		assertTrue(mma.matching().weight() == 8);
	}

}
