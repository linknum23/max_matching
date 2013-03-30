package graph;

import java.util.ArrayList;
import java.util.List;

public class PseudoVertex implements Vertex {
	int id;
	int root;
	ArrayList<Vertex> innerVertices;
	WeightedDigraph g;
	
	public PseudoVertex(int id, int root, ArrayList<Vertex> underlyingVertices, WeightedDigraph graph) {
		this.id = id;
		this.root = root;
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
	
	@Override
	public String toString(){
		return String.format("V%d{%s}", id, innerVertices.toString());
	}

	public Vertex underlying() {
		return g.vertex(root);
	}
}
