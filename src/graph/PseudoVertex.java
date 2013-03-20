package graph;

import java.util.ArrayList;
import java.util.List;

public class PseudoVertex implements Vertex {
	int id;
	ArrayList<Vertex> innerVertices;
	WeightedDigraph g;
	
	public PseudoVertex(int id, ArrayList<Vertex> underlyingVertices, WeightedDigraph graph) {
		this.id = id;
		innerVertices = underlyingVertices;
		g = graph;
	}

	@Override
	public int id() {
		return id;
	}

	public List<Vertex> vertices() {
		return innerVertices;
	}

	public WeightedDigraph graph() {
		return g;		
	}
	
}
