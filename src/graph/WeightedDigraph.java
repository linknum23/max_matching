package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * A graph with weighted edges
 * 
 * @author Lincoln
 */
public class WeightedDigraph{
	
	LinkedList<Edge>[]				adjList;
	HashMap<Vertex, Integer>	vertices;
	HashMap<Integer, Vertex>	vertexLookup;
	
	@SuppressWarnings("unchecked")
	private WeightedDigraph(WeightedDigraph g) {
		adjList = new LinkedList[g.vertices.size()];
		for (int i = 0; i < g.adjList.length; i++ ) {
			adjList[i] = cloneEdges(g, i);
		}
		vertices = (HashMap<Vertex, Integer>) g.vertices.clone();
		vertexLookup = (HashMap<Integer, Vertex>) g.vertexLookup.clone();
	}
	
	/**
	 * @param g
	 * @param index
	 * @return
	 */
	private LinkedList<Edge> cloneEdges(WeightedDigraph g, int index) {
		LinkedList<Edge> newList = new LinkedList<Edge>();// g.adjList[i].size());
		for (Edge e : g.adjList[index]) {
			newList.add(new Edge(e));
		}
		return newList;
	}
	
	/**
	 * A weighted graph with {@link numVertices} vertices
	 * 
	 * @param numVertices
	 */
	public WeightedDigraph(int numVertices) {
		adjList = new LinkedList[numVertices];
		vertices = new HashMap<Vertex, Integer>(numVertices);
		vertexLookup = new HashMap<Integer, Vertex>(numVertices);
		for (int i = 0; i < numVertices; i++ ) {
			adjList[i] = new LinkedList<Edge>();
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
		
		if ( ! vertices.containsValue(X)) {
			addVertex(X);
		}
		if ( ! vertices.containsValue(Y)) {
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
		assert vertexLookup.containsKey(x) == false : "two vertices have the same index";
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
		int sinkIndex = vertices.get(sink);
		
		List<Edge> nList = adjList[sourceIndex];
		nList.add(new Edge(sourceIndex, sinkIndex, weight));
	}
	
	public void addEdge(Edge e) {
		List<Edge> nList = adjList[e.left];
		nList.add(e);
	}
	
	/**
	 * Returns the neighbors of {@link v}
	 * 
	 * @param v vertex
	 * @return neighbors
	 */
	public List<Vertex> vOuts(Vertex v) {
		return right(eOuts(v));
	}
	
	/**
	 * Returns the output vertices of {@link v}
	 * 
	 * @param vid vertex index
	 * @return neighbors
	 */
	public List<Vertex> vOuts(int vid) {
		return right(eOuts(vid));
	}
	
	public List<Edge> eOuts(int vid) {
		return adjList[vid];
	}
	
	public List<Edge> eOuts(Vertex v) {
		int index = vertices.get(v);
		return adjList[index];
	}
	
	public Vertex vertex(int x) {
		return vertexLookup.get(x);
	}
	
	public WeightedDigraph liftCycle(PseudoVertex root) {
		WeightedDigraph g = new WeightedDigraph(this);
		
		// remove all connections to the root
		removeOuts(g, root);
		removeIns(g, root);
		
		// foreach vertex in the pseudovertex
		// add its input and output connections back using the contained graph for reference
		for (Vertex v : root.vertices()) {
			g.addEdges(root.graph().eOuts(v));
			g.addEdges(root.graph().eIns(v));
		}
		
		return g;
	}
	
	private void addEdges(List<Edge> edges) {
		for (Edge e : edges) {
			addEdge(e);
		}
	}
	
	private List<Vertex> vIns(Vertex v){
		return right(eIns(v));
	}
	
	private List<Edge> eIns(Vertex v) {
		List<Edge> eins = new ArrayList<Edge>();
		for (List<Edge> edges : adjList) {
			for (Edge e : edges) {
				if (e.left == v.id()) {
					eins.add(e);
				}
			}
		}
		return eins;
	}
	
	/**
	 * contract a cycle into a single pseudonode
	 * 
	 * @param root
	 * @param cycle1
	 * @return
	 */
	public WeightedDigraph contractCycle(Vertex root, ArrayList<Vertex> cycle) {
		WeightedDigraph g = new WeightedDigraph(this);
		List<Vertex> rootNeighbors = g.vOuts(root);
		Vertex pseudo = new PseudoVertex(root.id(), cycle, this);
		
		// replace the input and output connections to the vertices in the cycle (not the root) with the pseudonode
		for (Vertex v : cycle) {
			if (v != root) {
				// add all of its neighbors to the pseudonode
				addOutArcs(g, pseudo, eOuts(v));
				// remove all of its outgoing arcs ( remember this node is not connected to anything anymore
				removeOuts(g, v);
				// replace all arcs to the node with connections to the pseudo node
				replaceVertexInArcs(g, v, pseudo);
			}
		}
		
		// replace the id references of root with the pseudonode
		g.vertexLookup.remove(root.id());
		g.vertexLookup.put(root.id(), pseudo);
		g.vertices.remove(root);
		g.vertices.put(pseudo, root.id());
		
		return g;
	}
	
	public static void removeOuts(WeightedDigraph g, Vertex v) {
		g.eOuts(v).clear();
	}
	
	private void removeIns(WeightedDigraph g, Vertex root) {
		for (List<Edge> ins : g.adjList) {
			Iterator<Edge> it = ins.iterator();
			while (it.hasNext()) {
				if (it.next().right == root.id()) {
					it.remove();
				}
			}
		}
	}
	
	/**
	 * Reassign a list of outgoing arcs {@link neighbors} to a vertex {@link v}
	 * 
	 * @param g graph
	 * @param v vertex to add outgoing arcs to
	 * @param neighbors list of neighbors to add to {@link v}
	 */
	public static void addOutArcs(WeightedDigraph g, Vertex v, List<Edge> arcs) {
		// reassign the right part of the edge to the new vertex
		for (Edge e : arcs) {
			e.right = v.id();
		}
		// add the edges (may contain duplicate edges)
		g.eOuts(v).addAll(arcs);
	}
	
	/**
	 * @param g
	 * @param old
	 * @param _new
	 */
	public static void replaceVertexInArcs(WeightedDigraph g, Vertex old, Vertex _new) {
		// foreach edge replace their connection with the old vertex with a connection to the new vertex
		for (int i = 0; i < g.adjList.length; i++ ) {
			List<Edge> outputs = g.eOuts(i);
			for (Edge oute : outputs) {
				if (oute.left == old.id()) {
					oute.left = _new.id();
				}
			}
		}
	}
	
	public ArrayList<Vertex> left(List<Edge> edges) {
		ArrayList<Vertex> left = new ArrayList<Vertex>();
		for (Edge e : edges) {
			left.add(vertexLookup.get(e.left));
		}
		return left;
	}
	
	public ArrayList<Vertex> right(List<Edge> edges) {
		ArrayList<Vertex> right = new ArrayList<Vertex>();
		for (Edge e : edges) {
			right.add(vertexLookup.get(e.right));
		}
		return right;
	}
	
	@Override
	public boolean equals(Object o){
		WeightedDigraph g = (WeightedDigraph) o;
		if(g.vertices.size() == vertices.size()){
			for(int ii=0; ii < g.vertices.size(); ii++){
				if(!equiv(g.adjList[ii], adjList[ii])){
					return false;
				}
			}
			return true;
		}else{
			return false;
		}
	}

	private boolean equiv(List<Edge> list1, List<Edge> list2) {
		for(Edge e1 : list1){
			for(Edge e2 : list2){
				if(e1.equals(e2)){
					break;
				}
			}
			return false;
		}
		return true;
	}
	
}
