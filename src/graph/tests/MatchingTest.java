package graph.tests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import graph.Edge;
import graph.Matching;
import graph.Vertex;
import graph.WeightedDigraph;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class MatchingTest {
	
	@Before
	public void setUp() throws Exception {
	}
	
	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void test() {
		WeightedDigraph g = new WeightedDigraph(4);
		g.parse("0 1 1");
		g.parse("1 2 1");
		g.parse("2 3 1");
		
		Vertex v0 = g.vertex(0);
		Vertex v1 = g.vertex(1);
		Vertex v2 = g.vertex(2);
		
		List<Edge> path = new ArrayList<Edge>();
		path.addAll(g.eOuts(v0));
		path.addAll(g.eOuts(v1));
		path.addAll(g.eOuts(v2));
		
		Matching m = new Matching(4);
		m.add(g.eOuts(1).get(0));
		m.augment(path);
		assertEquals(2, m.weight());
		
	}
	
}
