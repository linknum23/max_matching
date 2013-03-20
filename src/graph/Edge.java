package graph;

public class Edge {
	public int weight = 1;
	public int right, left;
	
	public Edge(int right, int left){
		this.right = right;
		this.left = left;
	}
	
	public Edge(int right, int left, int weight){
		this.right = right;
		this.left = left;
		this.weight = weight;
	}

	public Edge(Edge e) {
		this(e.right, e.left, e.weight);
	}
}
