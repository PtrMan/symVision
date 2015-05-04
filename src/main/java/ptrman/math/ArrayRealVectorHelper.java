package ptrman.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;

/**
 *
 */
public class ArrayRealVectorHelper {
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
        return new Vector2d<Integer>((int)(vector.getDataRef()[0]), (int)(vector.getDataRef()[1]));
    }
}
