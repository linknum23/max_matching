package graph;

public class SimpleVertex implements Vertex {
	int id = 0;
	
	public SimpleVertex(int id){
		this.id = id;
	}
	
	@Override
	public int id() {
		return id;
	}
	
}
