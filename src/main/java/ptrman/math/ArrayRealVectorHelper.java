package ptrman.math;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;

import java.util.List;

/**
 *
 */
public enum ArrayRealVectorHelper {
    ;

    public enum EnumRoundMode {
        DOWN,
        NEAREST
    }

    public static ArrayRealVector integerToArrayRealVector(final Vector2d<Integer> vector) {
        return new ArrayRealVector(new double[]{vector.x, vector.y}, false);
    }

    public enum Dimensions2 {
        ;

        public static ArrayRealVector getTangent(final ArrayRealVector vector) {
            final double[] vectorData = vector.getDataRef();

            final double x = vectorData[0];
            final double y = vectorData[1];

            return new ArrayRealVector(new double[]{-y, x}, false);
        }
    }

    public static ArrayRealVector normalize(ArrayRealVector vector) {
        ArrayRealVector result = new ArrayRealVector(vector.getDimension());
        double length = vector.getNorm();
        double inverseLength = 1.0/length;

        final double[] resultData = result.getDataRef();
        final double[] vectorData = vector.getDataRef();

        for( int i = 0; i < vectorData.length; i++ ) {
            resultData[i] = inverseLength * vectorData[i];
        }

        return result;
    }

    public static ArrayRealVector getScaled(ArrayRealVector vector, double scale) {
        ArrayRealVector result = new ArrayRealVector(vector.getDimension());

        final double[] resultData = result.getDataRef();
        final double[] vectorData = vector.getDataRef();

        for( int i = 0; i < vectorData.length; i++ ) {
            resultData[i] = vectorData[i] * scale;
        }

        return result;
    }
    public static ArrayRealVector getScaled(final ArrayRealVector vector, final double scale, final ArrayRealVector result) {
        final double[] resultData = result.getDataRef();
        final double[] vectorData = vector.getDataRef();

        for( int i = 0; i < vector.getDimension(); i++ ) {
            resultData[i] = vectorData[i] * scale;
        }

        return result;
    }

    public static Vector2d<Integer> arrayRealVectorToInteger(ArrayRealVector vector, EnumRoundMode roundMode) {
        final double[] vectorData = vector.getDataRef();
        if( roundMode == EnumRoundMode.NEAREST ) {
            return new Vector2d<>((int)(vectorData[0]), (int)(vectorData[1]));
        }
        else {
            return new Vector2d<>((int)java.lang.Math.floor(vectorData[0]), (int)java.lang.Math.floor(vectorData[1]));
        }

    }

    public static ArrayRealVector getAverage(final List<ArrayRealVector> elements) {
        ArrayRealVector result = new ArrayRealVector(elements.get(0).getDimension());

        for( final ArrayRealVector currentElement : elements ) {
            //result = result.add(currentElement);
            result.combineToSelf(1,1, currentElement);

        }

        final double[] d = result.getDataRef();
        for( int i = 0; i < elements.get(0).getDimension(); i++ ) {
            d[i] /= elements.size();
        }

        return result;
    }
}
