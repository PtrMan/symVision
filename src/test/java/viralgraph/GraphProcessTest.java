package viralgraph;

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
        GraphProcess graph = new GraphBuilder()
                .edge(startNode, plus2, resultConverter, (o) -> { System.out.println(o); return true; })
                .build();

        graph.fire(startNode, 1);
    }

    @Test
    public void testSimpleMultithreading() {
        Function startNode = param -> param;
        ConsumerNode<Integer[]> printNumbersForSomeReason = param -> IntStream.range(param[0], param[1]).forEach(System.out::println);
        ConsumerNode<Integer[]> printNumbersForSomeReason2 = param -> IntStream.range(param[0], param[1]).forEach(System.out::println);

        GraphProcess graph = new GraphBuilder()
                .edge(startNode, printNumbersForSomeReason)
                .edge(startNode, printNumbersForSomeReason2)
                .build();

        graph.fire(startNode, new Integer[]{0, 1000});
    }
}