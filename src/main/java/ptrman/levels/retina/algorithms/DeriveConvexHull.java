/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina.algorithms;

import org.apache.commons.math3.linear.ArrayRealVector;

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
public enum DeriveConvexHull {
	;

	public static class State {
        // edges go from [0] to [1]  from [1] to [2] and so on and from [last] to [0]
        public List<ArrayRealVector> cornerPositions = new ArrayList<>();
    }

    /**
     *
     * @return boolean of inclusion of point
     */
    public static boolean considerPoint(final ArrayRealVector position, final State state) {
        if( isPointInside(position, state.cornerPositions) ) return false;
        // if we are here the point was not inside the convex hull

        addPointToConvexHull(position, state);

        return true;
    }

    public static boolean isPointInside(final ArrayRealVector position, final List<ArrayRealVector> cornerPositions) {
        // the point is inside if no point is outside of an edge (if all edges are positive)
        return getIndexOfEdgeWherePointIsOutside(position, cornerPositions) == -1;
    }

    private static void addPointToConvexHull(final ArrayRealVector position, final State state) {
        // * create new edge where point is outside
        // * update convex hull structure so it stays convex

        final var indexOfCornerEdgeWherePointIsOutside = getIndexOfEdgeWherePointIsOutside(position, state.cornerPositions);


        state.cornerPositions.add(indexOfCornerEdgeWherePointIsOutside, position);

        // correct the convex hull so it stays convex
        while (true) {
            final var wasCorrected = keepConvexAndWasCorrected(state);
            if( !wasCorrected ) break;
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
    private static boolean keepConvexAndWasCorrected(final State state) {
        for(var i = 0; i < state.cornerPositions.size(); i++ ) {
            final var centerPointPosition = state.cornerPositions.get(i);

            final var cornerPositionA = getCornerPositionAtIndexWrapAround(i-1, state.cornerPositions);
            final var cornerPositionB = getCornerPositionAtIndexWrapAround(i + 1, state.cornerPositions);

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
        assert cornerPositions.size() >= 3 : "ASSERT: " + "not enougth corners";

        for(var i = 0; i < cornerPositions.size()-1; i++ ) {
            final var cornerPositionA = cornerPositions.get(i);
            final var cornerPositionB = cornerPositions.get(i+1);

            if( !isPointOnPositiveSide(position, cornerPositionA, cornerPositionB)) return i;
        }

        // check corner from lastIndex to 0
        final var cornerPositionA = cornerPositions.get(cornerPositions.size()-1);
        final var cornerPositionB = cornerPositions.get(0);

        if( !isPointOnPositiveSide(position, cornerPositionA, cornerPositionB) ) return cornerPositions.size() - 1;

        return -1;
    }

    private static boolean isPointOnPositiveSide(final ArrayRealVector position, final ArrayRealVector pa, final ArrayRealVector pb) {
        // project -> calculate projected position on line -> difference with point -> dot with rotated normal of pb-pa -> sign test

        final var normalizedDirection = normalize(pb.subtract(pa));
        final var unnormalizedDiff = position.subtract(pa);

        final var dot = normalizedDirection.dotProduct(unnormalizedDiff);

        final var projectedPosition = pa.add(getScaled(normalizedDirection, dot));
        final var positionDiff = position.subtract(projectedPosition);

        final var tangent = getTangent(normalizedDirection);

        return tangent.dotProduct(positionDiff) < 0.0;
    }
}
