package ptrman.levels.retina.algorithms;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.Dimensions2.getTangent;
import static ptrman.math.ArrayRealVectorHelper.getScaled;
import static ptrman.math.ArrayRealVectorHelper.normalize;
import static ptrman.math.Maths.modNegativeWraparound;

/**
 * Algorithm as described in the foundalis dissertation at
 * 10.3.18 Other image-processing functions
 */
public class DeriveConvexHull {
    public static class State {
        // edges go from [0] to [1]  from [1] to [2] and so on and from [last] to [0]
        public List<ArrayRealVector> cornerPositions = new ArrayList<>();
    }

    /**
     *
     * @return boolean of inclusion of point
     */
    public static boolean considerPoint(final ArrayRealVector position, State state) {
        if( isPointInside(position, state.cornerPositions) ) {
            return false;
        }
        // if we are here the point was not inside the convex hull

        addPointToConvexHull(position, state);

        return true;
    }

    public static boolean isPointInside(final ArrayRealVector position, final List<ArrayRealVector> cornerPositions) {
        // the point is inside if no point is outside of an edge (if all edges are positive)
        return getIndexOfEdgeWherePointIsOutside(position, cornerPositions) == -1;
    }

    private static void addPointToConvexHull(final ArrayRealVector position, State state) {
        // * create new edge where point is outside
        // * update convex hull structure so it stays convex

        final int indexOfCornerEdgeWherePointIsOutside = getIndexOfEdgeWherePointIsOutside(position, state.cornerPositions);


        state.cornerPositions.add(indexOfCornerEdgeWherePointIsOutside, position);

        // correct the convex hull so it stays convex
        for(;;) {
            final boolean wasCorrected = keepConvexAndWasCorrected(state);
            if( !wasCorrected ) {
                break;
            }
        }
    }

    /**
     *
     * checks if the hull is not convex and fixes it
     *
     * it works by calculating if the i point is outside of the line described by the i-1 and i+1 point
     * if it is not the point gets removed (so the hull includes the point)
     *
     * @param state ...
     * @return true if the convex hull was corrected
     */
    private static boolean keepConvexAndWasCorrected(State state) {
        for( int i = 0; i < state.cornerPositions.size(); i++ ) {
            final ArrayRealVector centerPointPosition = state.cornerPositions.get(i);

            final ArrayRealVector cornerPositionA = getCornerPositionAtIndexWrapAround(i-1, state.cornerPositions);
            final ArrayRealVector cornerPositionB = getCornerPositionAtIndexWrapAround(i + 1, state.cornerPositions);

            if( !isPointOnPositiveSide(centerPointPosition, cornerPositionA, cornerPositionB)) {
                state.cornerPositions.remove(i);
                return true;
            }
        }

        return false;
    }

    private static ArrayRealVector getCornerPositionAtIndexWrapAround(final int index, final List<ArrayRealVector> cornerPositions) {
        return cornerPositions.get(modNegativeWraparound(index, cornerPositions.size()));
    }

    private static int getIndexOfEdgeWherePointIsOutside(final ArrayRealVector position, final List<ArrayRealVector> cornerPositions) {
        Assert.Assert(cornerPositions.size() >= 3, "not enougth corners");

        for( int i = 0; i < cornerPositions.size()-1; i++ ) {
            final ArrayRealVector cornerPositionA = cornerPositions.get(i);
            final ArrayRealVector cornerPositionB = cornerPositions.get(i+1);

            if( !isPointOnPositiveSide(position, cornerPositionA, cornerPositionB)) {
                return i;
            }
        }

        // check corner from lastIndex to 0
        final ArrayRealVector cornerPositionA = cornerPositions.get(cornerPositions.size()-1);
        final ArrayRealVector cornerPositionB = cornerPositions.get(0);

        if( !isPointOnPositiveSide(position, cornerPositionA, cornerPositionB) ) {
            return cornerPositions.size()-1;
        }

        return -1;
    }

    private static boolean isPointOnPositiveSide(final ArrayRealVector position, final ArrayRealVector pa, final ArrayRealVector pb) {
        // project -> calculate projected position on line -> difference with point -> dot with rotated normal of pb-pa -> sign test

        final ArrayRealVector normalizedDirection = normalize(pb.subtract(pa));
        final ArrayRealVector unnormalizedDiff = position.subtract(pa);

        final double dot = normalizedDirection.dotProduct(unnormalizedDiff);

        final ArrayRealVector projectedPosition = pa.add(getScaled(normalizedDirection, dot));
        final ArrayRealVector positionDiff = position.subtract(projectedPosition);

        final ArrayRealVector tangent = getTangent(normalizedDirection);

        return tangent.dotProduct(positionDiff) < 0.0;
    }
}
