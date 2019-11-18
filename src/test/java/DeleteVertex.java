import com.syncleus.dann.graph.AbstractDirectedEdge;
import com.syncleus.dann.graph.MutableBidirectedGraph;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;

/**
 * Created by r0b3 on 11.07.15.
 */
public class DeleteVertex {
    public final static class Vertex {

    }

    public final static class Edge extends AbstractDirectedEdge<Vertex> {
        public Edge(Vertex source, Vertex destination, float strength) {
            super(source, destination);
            this.strength = strength;
        }

        public final float strength;
    }

    public static void main(String[] args) {
        DeleteVertex deleteVertex = new DeleteVertex();
        deleteVertex.setup();
    }

    public void setup() {
        Vertex vertexA = new Vertex();
        Vertex vertexB = new Vertex();
        Vertex vertexC = new Vertex();

        graph.add(vertexA);
        graph.add(vertexB);
        graph.add(vertexC);

        graph.add(new Edge(vertexA, vertexB, 0.0f));
        graph.add(new Edge(vertexB, vertexA, 0.0f));

        graph.add(new Edge(vertexB, vertexC, 0.0f));
        graph.add(new Edge(vertexC, vertexB, 0.0f));

        graph.add(new Edge(vertexA, vertexC, 0.0f));
        graph.add(new Edge(vertexC, vertexA, 0.0f));

        // crashes
        graph.remove(vertexA);
    }

    public final MutableBidirectedGraph<Vertex, Edge> graph = new MutableDirectedAdjacencyGraph<>();
}
