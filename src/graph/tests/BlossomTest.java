package graph.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;

import graph.PseudoVertex;
import graph.Vertex;
import graph.WeightedDigraph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlossomTest {
	
	@Test
	public void test() {
		WeightedDigraph g = new WeightedDigraph(5);
		// cycle 1
		g.parse("0 1 1");
		g.parse("1 2 1");
		g.parse("2 0 1");
		// cycle 2
		g.parse("2 3 1");
		g.parse("3 4 1");
		g.parse("4 2 1");
		
		ArrayList<Vertex> cycle1 = new ArrayList<Vertex>();
		cycle1.add(g.vertex(0));
		cycle1.add(g.vertex(1));
		cycle1.add(g.vertex(2));
		
		WeightedDigraph g2 = g.contractCycle(g.vertex(2),cycle1);
		
		ArrayList<Vertex> cycle2 = new ArrayList<Vertex>();
		cycle2.add(g2.vertex(5));
		cycle2.add(g2.vertex(3));
		cycle2.add(g2.vertex(4));
		
		WeightedDigraph g3 = g2.contractCycle(g2.vertex(5),cycle2);
		WeightedDigraph g4 = g3.liftCycle((PseudoVertex)g3.vertex(6));
		WeightedDigraph g5 = g4.liftCycle((PseudoVertex)g4.vertex(5));
		
		assertTrue("blossom expand/contract sequence incorrect", g5.equals(g));
	}
	
}
