package ptrman.bpsolver.levels.retina.algorithms;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DeriveConvexHull {
    @Test
    public void testInside() {
        final var state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        final var isInside = ptrman.levels.retina.algorithms.DeriveConvexHull.isPointInside(new ArrayRealVector(new double[]{0, 0}), state.cornerPositions);
        assert isInside : "ASSERT: " + "";
    }

    @Test
    public void testOutside() {
        final var state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        final var isInside = ptrman.levels.retina.algorithms.DeriveConvexHull.isPointInside(new ArrayRealVector(new double[]{0, 3}), state.cornerPositions);
        final boolean value = !isInside;
        assert value : "ASSERT: " + "";
    }

    @Test
    public void testAddPoint() {
        final var additionalPoint = new ArrayRealVector(new double[]{2, 2});

        final var state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        final var pointWasAdded = ptrman.levels.retina.algorithms.DeriveConvexHull.considerPoint(additionalPoint, state);
        assert pointWasAdded : "ASSERT: " + "";

        final List<ArrayRealVector> correctConvexHull = new ArrayList<>();
        correctConvexHull.add(new ArrayRealVector(new double[]{-1, 1}));
        correctConvexHull.add(new ArrayRealVector(new double[]{2, 2})); // changed
        correctConvexHull.add(new ArrayRealVector(new double[]{0, -1}));

        final var allValuesWithinError = allValuesEqualWithErrorWithoutOrderForVectors(state.cornerPositions, correctConvexHull, 0.001f);
        assert allValuesWithinError : "ASSERT: " + "";
    }

    private static List<ArrayRealVector> getCornerPositionsOfTestConvexHull() {
        final List<ArrayRealVector> result = new ArrayList<>();
        result.add(new ArrayRealVector(new double[]{-1, 1}));
        result.add(new ArrayRealVector(new double[]{1, 1}));
        result.add(new ArrayRealVector(new double[]{0, -1}));
        return result;
    }

    private static boolean allValuesEqualWithErrorWithoutOrderForVectors(final List<ArrayRealVector> values, final List<ArrayRealVector> comparision, final double error) {
        for( final var currentValue : values ) {
            final var inComparisionInErrorBoundary = comparision.stream().anyMatch(currentComparision -> isValueEqualWithError(currentValue.getDataRef()[0], currentComparision.getDataRef()[0], error) ||
                    isValueEqualWithError(currentValue.getDataRef()[1], currentComparision.getDataRef()[1], error));

            if( !inComparisionInErrorBoundary ) return false;
        }

        return true;
    }

    private static boolean allValuesEqualWithErrorWithoutOrderForValues(final List<Double> values, final List<Double> comparision, final double error) {
        for( final double currentValue : values ) {
            final var inComparisionInErrorBoundary = comparision.stream().mapToDouble(currentComparision -> currentComparision).anyMatch(currentComparision -> isValueEqualWithError(currentValue, currentComparision, error));

            if( !inComparisionInErrorBoundary ) return false;
        }

        return true;
    }

    private static boolean isValueEqualWithError(final double value, final double comparision, final double error) {
        return value < comparision + error && value > comparision - error;
    }

}