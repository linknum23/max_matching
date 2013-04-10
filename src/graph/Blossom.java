package graph;

import java.util.ArrayList;
import java.util.List;

public class Blossom implements Vertex {
	int root;
	int ref;
	List<Integer> innerVertices;
	List<Edge> edges;
	
	public Blossom(int id, int root, List<Integer> underlyingVertices, List<Edge> edges) {
		this.root = root;
		this.ref = id;
		innerVertices = underlyingVertices;
		this.edges = edges;
	}

	@Override
	public int id() {
		return ref;
	}

	public List<Integer> vertices() {
		return innerVertices;
	}
	
	@Override
	public String toString(){
		return String.format("V%d{%s}", root, innerVertices.toString());
	}

	public int root() {
		return root;
	}

	public void setRootId(int id) {
		root = id;
	}

	public boolean contains(int vid) {
		for(Integer v: vertices()){
			if(v == vid){
				return true;
			}
		}
		return false;
	}
	
	public boolean connectedTo(int id1, int id2){
		for(Edge e : edges){
			if((e.right == id1 && e.left == id2) || (e.right == id2 && e.left == id1)){
				return true;
			}
		}
		//a connection was not found
		return false;
	}

	public List<Integer> cycle() {
		return vertices();
	}
}