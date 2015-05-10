package ptrman.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;

import java.util.List;

/**
 *
 */
public class ArrayRealVectorHelper {
    public static ArrayRealVector integerToArrayRealVector(final Vector2d<Integer> vector) {
        return new ArrayRealVector(new double[]{vector.x, vector.y});
    }

    public static class Dimensions2 {
        public static ArrayRealVector getTangent(final ArrayRealVector vector) {
            final double x = vector.getDataRef()[0];
            final double y = vector.getDataRef()[1];

            return new ArrayRealVector(new double[]{-y, x});
        }
    }

    public static ArrayRealVector normalize(ArrayRealVector vector) {
        ArrayRealVector result = new ArrayRealVector(vector.getDimension());
        double length = vector.getNorm();
        double inverseLength = 1.0/length;

        for( int i = 0; i < vector.getDimension(); i++ ) {
            result.getDataRef()[i] = inverseLength * vector.getDataRef()[i];
        }

        return result;
    }

    public static ArrayRealVector getScaled(ArrayRealVector vector, double scale) {
        ArrayRealVector result = new ArrayRealVector(vector.getDimension());

        for( int i = 0; i < vector.getDimension(); i++ ) {
            result.getDataRef()[i] = vector.getDataRef()[i] * scale;
        }

        return result;
    }

    public static Vector2d<Integer> arrayRealVectorToInteger(ArrayRealVector vector) {
        return new Vector2d<>((int)(vector.getDataRef()[0]), (int)(vector.getDataRef()[1]));
    }

    public static ArrayRealVector getAverage(final List<ArrayRealVector> elements) {
        ArrayRealVector result;

        result = new ArrayRealVector(elements.get(0).getDimension());

        for( final ArrayRealVector currentElement : elements ) {
            result.add(currentElement);
        }

        for( int i = 0; i < elements.get(0).getDimension(); i++ ) {
            result.getDataRef()[i] /= (double)elements.size();
        }

        return result;
    }
}
