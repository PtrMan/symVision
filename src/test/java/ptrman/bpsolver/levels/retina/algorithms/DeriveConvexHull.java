package ptrman.bpsolver.levels.retina.algorithms;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DeriveConvexHull {
    @Test
    public void testInside() {
        ptrman.levels.retina.algorithms.DeriveConvexHull.State state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        boolean isInside = ptrman.levels.retina.algorithms.DeriveConvexHull.isPointInside(new ArrayRealVector(new double[]{0, 0}), state.cornerPositions);
        Assert.Assert(isInside, "");
    }

    @Test
    public void testOutside() {
        ptrman.levels.retina.algorithms.DeriveConvexHull.State state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        boolean isInside = ptrman.levels.retina.algorithms.DeriveConvexHull.isPointInside(new ArrayRealVector(new double[]{0, 3}), state.cornerPositions);
        Assert.Assert(!isInside, "");
    }

    @Test
    public void testAddPoint() {
        final ArrayRealVector additionalPoint = new ArrayRealVector(new double[]{2, 2});

        ptrman.levels.retina.algorithms.DeriveConvexHull.State state = new ptrman.levels.retina.algorithms.DeriveConvexHull.State();
        state.cornerPositions = getCornerPositionsOfTestConvexHull();

        final boolean pointWasAdded = ptrman.levels.retina.algorithms.DeriveConvexHull.considerPoint(additionalPoint, state);
        Assert.Assert(pointWasAdded, "");

        List<ArrayRealVector> correctConvexHull = new ArrayList<>();
        correctConvexHull.add(new ArrayRealVector(new double[]{-1, 1}));
        correctConvexHull.add(new ArrayRealVector(new double[]{2, 2})); // changed
        correctConvexHull.add(new ArrayRealVector(new double[]{0, -1}));

        final boolean allValuesWithinError = allValuesEqualWithErrorWithoutOrderForVectors(state.cornerPositions, correctConvexHull, 0.001f);
        Assert.Assert(allValuesWithinError, "");
    }

    private static List<ArrayRealVector> getCornerPositionsOfTestConvexHull() {
        List<ArrayRealVector> result = new ArrayList<>();
        result.add(new ArrayRealVector(new double[]{-1, 1}));
        result.add(new ArrayRealVector(new double[]{1, 1}));
        result.add(new ArrayRealVector(new double[]{0, -1}));
        return result;
    }

    private static boolean allValuesEqualWithErrorWithoutOrderForVectors(final List<ArrayRealVector> values, final List<ArrayRealVector> comparision, final double error) {
        for( final ArrayRealVector currentValue : values ) {
            boolean inComparisionInErrorBoundary = false;

            for( final ArrayRealVector currentComparision : comparision ) {
                if(
                        isValueEqualWithError(currentValue.getDataRef()[0], currentComparision.getDataRef()[0], error) ||
                                isValueEqualWithError(currentValue.getDataRef()[1], currentComparision.getDataRef()[1], error)
                        ) {
                    inComparisionInErrorBoundary = true;
                    break;
                }
            }

            if( !inComparisionInErrorBoundary ) {
                return false;
            }
        }

        return true;
    }

    private static boolean allValuesEqualWithErrorWithoutOrderForValues(final List<Double> values, final List<Double> comparision, final double error) {
        for( double currentValue : values ) {
            boolean inComparisionInErrorBoundary = false;

            for( double currentComparision : comparision ) {
                if( isValueEqualWithError(currentValue, currentComparision, error) ) {
                    inComparisionInErrorBoundary = true;
                    break;
                }
            }

            if( !inComparisionInErrorBoundary ) {
                return false;
            }
        }

        return true;
    }

    private static boolean isValueEqualWithError(final double value, final double comparision, final double error) {
        return value < comparision + error && value > comparision - error;
    }

}