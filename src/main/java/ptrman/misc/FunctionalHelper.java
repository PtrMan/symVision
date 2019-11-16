package ptrman.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum FunctionalHelper {
	;

	public static <ParameterType, ResultType> List<ResultType> apply(final Function<ParameterType,ResultType> function, final List<ParameterType> parameters) {
        List<ResultType> result = new ArrayList<>();

        for( final ParameterType currentParameter : parameters) {
            result.add(function.apply(currentParameter));
        }

        return result;
    }
}
