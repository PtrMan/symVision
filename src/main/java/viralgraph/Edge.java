package viralgraph;

import java.util.function.Function;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 11:06 AM
 */
public class Edge<I, J, K> {

    private Function<I,J> input;
    private Function<J,K> output;

    public Edge(Function<I,J> input, Function<J,K> output) {
        this.input = input;
        this.output = output;
    }

    public Function<J,K> getOutput() {
        return output;
    }

    public Function<I,J> getInput() {
        return input;
    }
//
//    public void setOutput(Function<J,K> output) {
//        this.output = output;
//    }

//
//    public void setInput(Function<I,J> input) {
//        this.input = input;
//    }
}
