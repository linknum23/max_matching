package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

/**
 * A graph with weighted edges
 * 
 * @author Lincoln
 * 
 */
public class WeightedDigraph {
	
	ArrayList<ArrayList<Vertex>>	adjList;
	HashMap<Vertex, Integer>			vertices;
	HashMap<Integer, Vertex>			vertexLookup;
	
	/**
	 * A weighted graph with {@link numVertices} vertices
	 * 
	 * @param numVertices
	 */
	public WeightedDigraph(int numVertices) {
		adjList = new ArrayList<ArrayList<Vertex>>(numVertices);
		vertices = new HashMap<Vertex, Integer>(numVertices);
		vertexLookup = new HashMap<Integer, Vertex>(numVertices);
		for (int i = 0; i < numVertices; i++ ) {
			adjList.add(i, new ArrayList<Vertex>());
		}
	}
	
	/**
	 * Adds an arc specified in the format X Y W where X and Y are numbered vertices and W is the integer weight
	 * 
	 * @param line
	 */
	public void parse(String line) {
		Scanner s = new Scanner(line);
		
		int X = s.nextInt();
		int Y = s.nextInt();
		int W = s.nextInt();
		
		if (!vertices.containsValue(X)) {
			addVertex(X);
		}
		if (!vertices.containsValue(Y)) {
			addVertex(Y);
		}
		
		Vertex vx = vertexLookup.get(X);
		Vertex vy = vertexLookup.get(Y);
		addEdge(vx, vy, W);
	}
	
	/**
	 * Adds a vertex to the graph
	 * 
	 * @param x
	 */
	private void addVertex(int x) {
		Vertex v = new SimpleVertex(x);
		vertices.put(v, x);
		vertexLookup.put(x, v);
	}
	
	/**
	 * Adds an arc specified by the two numbered vertices {@link sourceVId},{@link sinkVId} and {@link weight}
	 * 
	 * @param sourceVId
	 * @param sinkVId
	 * @param weight
	 */
	public void addEdge(Vertex source, Vertex sink, int weight) {
		if (weight < 0) {
			throw new RuntimeException("Only non-negative weights accepted");
		}
		int sourceIndex = vertices.get(source);
		ArrayList<Vertex> nList = adjList.get(sourceIndex);
		nList.add(sink);
	}
	
	/**
	 * Returns the neighbors of {@link v}
	 * 
	 * @param v vertex
	 * @return neighbors
	 */
	public List<Vertex> neighbors(Vertex v) {
		int index = vertices.get(v);
		return adjList.get(index);
	}
}
