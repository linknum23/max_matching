package graph;

public class Edge {
	public int weight = 1;
	public int right, left;
	
	public Edge(int right, int left){
		this.right = right;
		this.left = left;
	}
	
	public Edge(int left, int right, int weight){
		this.right = right;
		this.left = left;
		this.weight = weight;
	}

	public Edge(Edge e) {
		this(e.left, e.right, e.weight);
	}
	
	@Override
	public String toString() {
		return String.format("%d--(%d)->%d", left, weight, right);
	}

	public boolean isLoop() {
		return left == right;
	}
	
	@Override
	public boolean equals(Object obj) {
		Edge e = (Edge) obj;
		return e.right == right && e.left == left && e.weight == weight;
	}
	
	@Override
	public int hashCode() {
		return right ^ left ^ weight;}
}
