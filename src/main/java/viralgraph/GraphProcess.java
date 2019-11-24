package viralgraph;

import com.google.common.graph.*;

import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 10:45 AM
 */
public class GraphProcess {

    final Executor exe = ForkJoinPool.commonPool();

    final IdentityHashMap<Function,GraphNode> ref = new IdentityHashMap<>();

    /** data dependency graph */
    final MutableGraph<GraphNode> flow = GraphBuilder.directed().build();

    class GraphNode {
        final Function f;

        //TODO DAG dispatch caches

        GraphNode(Function f) {
            this.f = f;
        }

        protected boolean propagate(Object x, Object y) {
            //here buffering or dithering can be implemented
            return true;
        }

        protected void invalidate() {

        }
    }

    class DebouncedGraphNode extends GraphNode {

        DebouncedGraphNode(Function f) {
            super(f);
        }
    }

    class InstrumentedGraphNode extends DebouncedGraphNode {

        InstrumentedGraphNode(Function f) {
            super(f);
        }
    }

    public GraphProcess() {

    }

    public void set(Function anon, Object x) {
        set(nodeOrAdd(anon), x);
    }

    public GraphNode node(Function anon) {
        return ref.get(anon);
    }

    public GraphNode nodeOrAdd(Function anon) {
        return ref.computeIfAbsent(anon, GraphNode::new);
    }

    public void set(GraphNode f, Object x) {
        exe.execute(new Application(f, x));
    }

    public void edge(Function... sequence) {
        switch (sequence.length) {
            case 0:
            case 1:
                return;
            default: {
                synchronized (flow) {
                    for (int i = 1; i < sequence.length; i++)
                        flow.putEdge(nodeOrAdd(sequence[i - 1]), nodeOrAdd(sequence[i]));
                    //TODO invalidate flows
                }
            }
        }
    }

    private class Application implements Runnable {
        final GraphNode n;
        final Object x;

        public Application(GraphNode n, Object x) {
            this.n = n;
            this.x = x;
        }

        @Override
        public void run() {
            _apply(n, x);
        }
    }

    protected void _apply(GraphNode n, Object x) {
        Object y = n.f.apply(x);
        if (n.propagate(x,y)) {
            //TODO compile/cache this to some depth, and invalidate when graph changes, synchronized as necessary
            //
            Set<GraphNode> next = flow.successors(n);
            if (!next.isEmpty()) {
                next.forEach(nn -> set(nn, y));
            }
        }
    }

}
