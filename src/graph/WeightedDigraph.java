package graph;

import java.util.ArrayList;
import java.util.Collection;
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
		if(!vertexLookup.containsKey(e.left)){
			addVertex(e.left);
		}
		if(!vertexLookup.containsKey(e.right)){
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
		Vertex pseudo = new PseudoVertex(root.id(), cycle, this);
		
		// replace the id references of root with the pseudonode
		replaceVertex(g, root, pseudo);
		
		// replace the input and output connections to the vertices in the cycle (not the root) with the pseudonode
		for (Vertex v : cycle) {
			if (v != root) {
				// add all of its neighbors to the pseudonode
				addOutArcs(g, pseudo, g.eOuts(v));
				// remove all of its outgoing arcs ( remember this node is not connected to anything anymore
				removeOuts(g, v);
				// replace all arcs to the node with connections to the pseudo node
				replaceVertexInArcs(g, v, pseudo, REMOVE_SELF_LOOPS);
			}
		}
		
		//replace the outputs from the root that are contained within the cycle
		removeOuts(g, pseudo, cycle);
				
		return g;
	}

	/**
	 * @param g
	 * @param vBefore
	 * @param vAfter
	 */
	private void replaceVertex(WeightedDigraph g, Vertex vBefore, Vertex vAfter) {
		g.vertexLookup.remove(vBefore.id());
		g.vertexLookup.put(vBefore.id(), vAfter);
		g.vertices.remove(vBefore);
		g.vertices.put(vAfter, vBefore.id());
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
				if (oute.left == old.id()) {
					oute.left = _new.id();
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

	public List<Edge> edges() {
		List<Edge> edges = new LinkedList<Edge>();
		
		for(Vertex v : vertices.keySet()){
			edges.addAll(eOuts(v));
		}
		
		return edges;
	}
	

	public static WeightedDigraph random(int numVertices){
		WeightedDigraph g = new WeightedDigraph(numVertices);
		
		//simple algorithm:
		// generate a walk through the entire graph by sequentially connecting unconnected vertices until there arent any left
		//  at this point the graph is a simple path
		int[] path = new int[numVertices];
		for(int ii = 0; ii < numVertices; ii++){
			path[ii] = ii;
		}
		
		for(int ii=0; ii < numVertices-1; ii++){
			int temp = path[ii];
			int swapIndex = ((int)(Math.random()*(numVertices-ii)))+ii;
			path[ii] = path[swapIndex];
			path[swapIndex] = temp;
		}
		
		//foreach edge on the path add the edge to the graph with a 
		for(int ii=0; ii < numVertices-1; ii++){
			int edgeWeight =  ((int)(Math.random()*numVertices))+1;
			g.addEdge(new Edge(path[ii], path[ii+1],edgeWeight));
		}
		
		// sprinkle random edges by using a random number generator 
		// first generate the number of edges that you are going to create
		// for each of these edges generate the two vertices that you are going to connect avoiding loops
		int additionalEdges =  (int)(Math.random()*numVertices*(numVertices-1));
		while(additionalEdges > 0){
			int v1 = (int)(Math.random()*numVertices);
			int v2 = (int)(Math.random()*numVertices);
			List<Vertex> adj = g.vOuts(v1);
			boolean found = false;
			for(Vertex v : adj){
				if(v.id()==v2){
					found |= true;
				}
			}
			if(!found){
				g.addEdge(new Edge(v1, v2, ((int)(Math.random()*numVertices))+1));
				additionalEdges--;
			}
		}
		//remove self loops and add opposite edges
		clean(g);
		
		return g;
	}

	public Collection<Vertex> vertices() {
		return vertexLookup.values();
	}
	
	static void clean(WeightedDigraph g){
		// add opposite edges
		for(Vertex v : g.vertices()){
			Iterator<Edge> it = g.eOuts(v).iterator();
			while(it.hasNext()){
				Edge e = it.next();
				if(e.left == e.right){
					it.remove();
				}else if(!g.vOuts(e.right).contains(v)){
					g.addEdge(new Edge(e.right, e.left, e.weight)); //something is wrong here!!!
				}
			}
		}
		if(g.edges().size() % 2 != 0){
			throw new RuntimeException("Uneven number of edges, fail");
		}
	}
}
