package graph.tests;

import static org.junit.Assert.*;
import graph.WeightedDigraph;
import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class GraphLoadingTest {
	
	@Test
	public void testParse() {
		WeightedDigraph wd = new WeightedDigraph(4);
		wd.parse("0 1 6");			
		wd.parse("1 2 6");			
		wd.parse("2 3 6");
	}
	
}
