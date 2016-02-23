package medical_classify.structure;

public class Edge {
	public String type;

	public Vertex vertex1, vertex2;

	public Vertex getOther(Vertex vertex) {
		if (vertex == vertex1) {
			return vertex2;
		} else if (vertex == vertex2) {
			return vertex1;
		} else {
			return null;
		}
	}
}
