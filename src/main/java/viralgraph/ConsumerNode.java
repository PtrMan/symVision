package viralgraph;

import java.util.function.Consumer;

/**
 * Created with IntelliJ IDEA.
 * User: Muhatashim
 * Date: 12/23/2017
 * Time: 12:38 PM
 */
public interface ConsumerNode<I> extends Consumer<I>, java.util.function.Function<I, Void> {
    @Override
    default Void apply(I input){
        accept(input);
        return null;
    }
}
