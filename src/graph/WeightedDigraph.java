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
	private int	blossomStart;
	
	@SuppressWarnings("unchecked")
	private WeightedDigraph(WeightedDigraph g) {
		blossomStart = g.blossomStart;
		adjList = new LinkedList[g.adjList.length];
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
		int requiredVertices = numVertices*2; //for blossoms
		blossomStart = numVertices;
		adjList = new LinkedList[requiredVertices];
		vertices = new HashMap<Vertex, Integer>(requiredVertices);
		vertexLookup = new HashMap<Integer, Vertex>(requiredVertices);
		for (int i = 0; i < requiredVertices; i++ ) {
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
		
		if(!vertices.containsKey(e.left)){
			addVertex(e.left);
		}
		if(!vertices.containsKey(e.right)){
			addVertex(e.right);
		}
		
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
		
		//swap the root out for its underlying vertex
		replaceVertex(g, root, root.underlying());
		
		// foreach vertex in the pseudovertex
		// add its input and output connections back using the contained graph for reference
		for (Vertex v : root.vertices()) {
			g.addEdges(root.graph().eOuts(v));
			//we need to be careful to not add internal edges twice, so the inputs will have to be pruned of internal edges
			for(Edge e : root.graph().eIns(v.id())){
				if(!containsId(root.vertices(), e.left)){
					g.addEdge(e);
				}
			}
		}
		
		return g;
	}

	/**
	 * @param root
	 * @param e
	 * @return
	 */
	private boolean containsId(List<Vertex> vertices, int id) {
		boolean idFound = false;
		for(Vertex w : vertices){
			if(w.id() == id){
				idFound = true;
				break;
			}
		}
		return idFound;
	}
	
	private void addEdges(List<Edge> edges) {
		for (Edge e : edges) {
			addEdge(e);
		}
	}
	
	private List<Vertex> vIns(Vertex v){
		return right(eIns(v));
	}
	
	private List<Edge> eIns(int id) {
		List<Edge> eins = new ArrayList<Edge>();
		for (List<Edge> edges : adjList) {
			for (Edge e : edges) {
				if (e.right == id) {
					eins.add(e);
				}
			}
		}
		return eins;
	}
	
	private List<Edge> eIns(Vertex v) {
		return eIns(v.id());
	}
	
	/**
	 * contract a cycle into a single pseudonode
	 * 
	 * @param root
	 * @param cycle1
	 * @return
	 */
	public WeightedDigraph contractCycle(Vertex root, ArrayList<Vertex> cycle) {
		final boolean REMOVE_SELF_LOOPS = true;
		WeightedDigraph g = new WeightedDigraph(this);
		Vertex pseudo = new PseudoVertex(nextUnusedBlossom(), root.id(), cycle, this);
		
		//replace the outputs from the root that are contained within the cycle
		removeOuts(g, root, cycle);
		
		// replace the id references of root with the pseudonode
		replaceVertex(g, root, pseudo);
		
		// replace the input and output connections to the vertices in the cycle (not the root) with the pseudonode
		for (Vertex v : cycle) {
			if (v != root) {
				// add all of its neighbors to the pseudonode
				addOutArcs(g, pseudo, trim(g.eOuts(v), cycle));
				// remove all of its outgoing arcs ( remember this node is not connected to anything anymore
				removeOuts(g, v);
				// replace all arcs to the node with connections to the pseudo node
				replaceVertexInArcs(g, v, pseudo, REMOVE_SELF_LOOPS);
			}
		}
		
		replaceVertexInArcs(g, root, pseudo, REMOVE_SELF_LOOPS);
		
		return g;
	}

	private List<Edge> trim(List<Edge> eOuts, List<Vertex> vertices) {
		List<Edge> trimmed = new LinkedList<Edge>();
		Iterator<Edge> it = eOuts.iterator();
		while(it.hasNext()){
			Edge e = it.next();
			if(!vertices.contains(vertex(e.right))){
				trimmed.add(e);
			}
		}
		
		return trimmed;
	}

	private int nextUnusedBlossom() {
		for(int ii = blossomStart; ii < numVertices(); ii++){
			if(!vertexLookup.containsKey(ii)){
				return ii;
			}
		}
		throw new RuntimeException("No unused blossoms, this should not be possible");
	}

	/**
	 * @param g
	 * @param vBefore
	 * @param vAfter
	 */
	private void replaceVertex(WeightedDigraph g, Vertex vBefore, Vertex vAfter) {
		g.vertexLookup.remove(vBefore.id());
		g.vertexLookup.put(vAfter.id(), vAfter);
		g.vertices.remove(vBefore);
		g.vertices.put(vAfter, vAfter.id());
		
		LinkedList<Edge> edgesToSwap =cloneEdges(g, vBefore.id());
		for(Edge e : edgesToSwap){
			e.left = vAfter.id();
		}
		g.adjList[vAfter.id()] = edgesToSwap;
		g.adjList[vBefore.id()].clear();
	}
	
	private void removeOuts(WeightedDigraph g, Vertex v, ArrayList<Vertex> cycle) {
		Iterator<Edge> it = g.eOuts(v).iterator();
		while(it.hasNext()){
			Edge e = it.next();
			for(Vertex w : cycle){
				if(e.right == w.id()){
					it.remove();
				}
			}
		}
		
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
	public static void replaceVertexInArcs(WeightedDigraph g, Vertex old, Vertex _new, boolean removeSelfLoops) {
		// foreach edge replace their connection with the old vertex with a connection to the new vertex
		for (int i = 0; i < g.adjList.length; i++ ) {
			Iterator<Edge> it = g.eOuts(i).iterator();
			while(it.hasNext()){
				Edge oute = it.next();
				if (oute.right == old.id()) {
					oute.right = _new.id();
					//if(removeSelfLoops && oute.isLoop()){
					//	it.remove();
					//}
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
			boolean equiv = false;
			for(Edge e2 : list2){
				if(e1.equals(e2)){
					equiv = true;
				}
			}
			if(equiv == false){
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer print = new StringBuffer("[");
		for(List<Edge> list : adjList){
			print.append(String.format("%s,", list.toString()));
		}
		//handle empty adjacency list
		if(print.length() >1){
			print.setCharAt(print.length()-1, ']');
		}else{
			print.append(']');
		}
		return print.toString();
	}

	public int numVertices() {
		return adjList.length;
	}

	public boolean contains(Edge e) {
		return adjList[e.left].contains(e);
	}
}
