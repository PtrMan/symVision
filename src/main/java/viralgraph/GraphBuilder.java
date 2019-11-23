package viralgraph;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 11:05 AM
 */
public class GraphBuilder {

    private IdentityHashMap<Function<?,?>, List<Function>> nodes;

    public GraphBuilder() {
        nodes = new IdentityHashMap<>();
    }

    public <I, O> GraphBuilder node(Function<I,O> function) {
        nodes.put(function, null);
        return this;
    }

    public <I, J, K> GraphBuilder edge(Edge<I, J, K> edge) {
        List<Function> edgeOutputs = null;
        if (nodes.get(edge.getInput()) != null) {
            edgeOutputs = nodes.get(edge.getInput());
        } else {
            edgeOutputs = new ArrayList<>();
            this.nodes.put(edge.getInput(), edgeOutputs);
        }

        edgeOutputs.add(edge.getOutput());
        return this;
    }

    public <X, Y> GraphBuilder edge(Function<X, Y> i, ConsumerNode<Y> o) {
        return edge(i, (Function)o);
    }


    public GraphBuilder edge(Function... x) {
        assert(x.length > 2);
        for (int i = 1; i < x.length; i++) {
            edge(x[i-1], x[i]);
        }
        return this;
    }

    public <X, Y, Z> GraphBuilder edge(Function<X, Y> i, Function<Y,Z> o) {
        edge(new Edge(i, o));
        return this;
    }

    public Graph build() {
        return new Graph(nodes);
    }
}
