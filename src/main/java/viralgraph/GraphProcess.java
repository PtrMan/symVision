package viralgraph;

import cg4j.Eval;
import cg4j.node.TensorNode;
import cg4j.node.io.InputNode;
import com.google.common.graph.*;
import deepboof.Tensor;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
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

    final Map<Function,GraphNode> ref = Collections.synchronizedMap(new IdentityHashMap<>());

    /** data dependency graph */
    public final MutableGraph<GraphNode> flow = GraphBuilder.directed().build();



    public void set(TensorNode n, Eval e) {
        set(nodeOrAdd(n), n.apply(e));
    }

    public InputNode node(int... tensorShape) {
        InputNode i = new InputNode(tensorShape);
        nodeOrAdd(i);
        return i;
    }

    public static class GraphNode {
        final Function f;

        //TODO DAG dispatch caches

        protected GraphNode() {
            this.f = (Function)this;
        }

        @Override
        public String toString() {
            return "{" + ((f!=this) ? f : super.toString()) + '}';
        }

        public GraphNode(Function f) {
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

        float sampleRate = 0.1f;
        //TODO wallclock execution time Histogram...
        //TODO memory change? etc

        InstrumentedGraphNode(Function f) {
            super(f);
        }
    }

    public GraphProcess() {

    }


    public void set(Function anon, Object x) {
        set(nodeOrAdd(anon), x);
    }

    public GraphNode node(Function f) {
        GraphNode g = ref.get(f);


        return g;
    }

    /** add dependencies for all discovered dependent inputs */
    private void discover(GraphNode g, TensorNode f) {
        synchronized (flow) {
            f.forEachRecurse(n -> {
                GraphNode N = nodeOrAdd(n);
                edge(N, g);
            });
        }
    }

    public GraphNode nodeOrAdd(Function anon) {
        return ref.computeIfAbsent(anon, f -> {

            GraphNode g = nodeNew(f);

            if (f instanceof TensorNode)
                discover(g, ((TensorNode)f));
            return g;
        });
    }

    protected GraphNode nodeNew(Function f) {
        //TODO abstract into rules
        if (f instanceof InputNode) {
            InputNode F = (InputNode) f;
            return new GraphNode(new Function() {
                //HACK TODO use auto-type adaptation
                volatile cg4j.Tensor val = null;
                @Override
                public Object apply(Object x) {
                    if (x instanceof cg4j.Tensor) {
                        val = (cg4j.Tensor) x;
                        return null;
                    } else {
                        Eval ee = (Eval) x;
                        cg4j.Tensor v = val;
                        if (v!=null)
                            ee.set(F, v);
                        return F.apply(ee);
                    }
                }
            });
        }
        return new GraphNode(f);
    }

    public void set(GraphNode f, Object x) {
        exe.execute(new Application(f, x));
    }

    public void edge(GraphNode a, GraphNode b) {
        synchronized(flow) {
            flow.putEdge(a, b);
        }
    }

    public void edge(Function... sequence) {
        switch (sequence.length) {
            case 0:
            case 1:
                return;
            default: {
                synchronized (flow) {
                    for (int i = 1; i < sequence.length; i++)
                        flow.putEdge(nodeOrAdd(sequence[i - 1]), nodeOrAdd(sequence[i])); //TODO save target for next prev instead of lookup
                    //TODO invalidate upstream flows that have caches involving nodes downstream of this
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
        try {
            Object y = n.f.apply(x);
            if (y!=null) {
                if (n.propagate(x, y)) {
                    //TODO compile/cache this to some depth, and invalidate when graph changes, synchronized as necessary
                    Set<GraphNode> next = flow.successors(n);
                    if (!next.isEmpty())
                        next.forEach(nn -> set(nn, y));
                }
            }
        } catch (ClassCastException cce) {
            System.err.println("TODO autoadapt: " + x + "->" + n.f + " " + cce.getMessage());
        }
    }


}
