package graph;

import java.util.ArrayList;
import java.util.List;

public class Blossom implements Vertex {
	int root;
	int ref;
	ArrayList<Vertex> innerVertices;
	
	public Blossom(int ref, int root, ArrayList<Vertex> underlyingVertices) {
		this.root = root;
		this.ref = ref;
		innerVertices = underlyingVertices;
	}

	@Override
	public int id() {
		return root;
	}

	public List<Vertex> vertices() {
		return innerVertices;
	}
	
	@Override
	public String toString(){
		return String.format("V%d{%s}", root, innerVertices.toString());
	}

	public int ref() {
		return ref;
	}

	public void setRootId(int id) {
		root = id;
	}

	public boolean contains(int vid) {
		for(Vertex v: vertices()){
			if(v.id() == vid){
				return true;
			}
		}
		return false;
	}
}