package viralgraph;

import com.google.common.graph.GraphBuilder;
import org.junit.Test;

import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 11:15 AM
 */
public class GraphProcessTest {

    @Test
    public void testSimpleAddition() {

        Function<Integer, String> resultConverter = param -> "The result is: " + param;

        Function startNode = (x)->x; //HACK

        Function<Integer, Integer> plus2 = (Integer param1) -> param1 + 2;
        GraphProcess graph = new GraphProcess();
        graph.edge(startNode, plus2, resultConverter, (o) -> { System.out.println(o); return true; });
        graph.set(startNode, 1);

    }

    @Test
    public void testSimpleMultithreading() {
        Function startNode = param -> param;
        ConsumerNode<Integer[]> printNumbersForSomeReason = param -> IntStream.range(param[0], param[1]).forEach(System.out::println);
        ConsumerNode<Integer[]> printNumbersForSomeReason2 = param -> IntStream.range(param[0], param[1]).forEach(System.out::println);

        GraphProcess graph = new GraphProcess();
        graph.edge(startNode, printNumbersForSomeReason);
        graph.edge(startNode, printNumbersForSomeReason2);
        graph.set(startNode, new Integer[]{0, 1000});
    }
}