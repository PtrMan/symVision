package viralgraph;


import java.util.function.Function;

/** TODO forms basis for realtime async graph */
abstract public class DebouncedFunction<X,Y> implements Function<X,Y> {
	long lastComputed  = Long.MIN_VALUE;
}
