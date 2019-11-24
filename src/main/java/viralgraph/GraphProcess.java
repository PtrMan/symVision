package viralgraph;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 10:45 AM
 */
public class GraphProcess {

    private IdentityHashMap<Function<?,?>, List<Function>> nodes;

    public GraphProcess(IdentityHashMap<Function<?,?>, List<Function>> nodes) {
        this.nodes = nodes;
    }

    public void fire(ConsumerNode<Object> node) {
        fire(node, null);
    }

    <I> void fire(Function<I,?> node, I param) {
        Object output = node.apply(param);
        List<Function> childFunctions = this.nodes.get(node);
        if (childFunctions != null)
            fire(childFunctions, output);
    }


    private void fire(List<Function> children, Object param) {
        switch (children.size()) {
            case 0: break;
            case 1: fire(children.get(0), param); break;
            default: children.parallelStream().forEach(iNode -> fire(iNode, param)); break;
        }

    }
}
