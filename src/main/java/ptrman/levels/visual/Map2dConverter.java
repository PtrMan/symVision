package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;

public class Map2dConverter {
    public static void booleanToFloat(final IMap2d<Boolean> input, IMap2d<Float> output) {
        for( int iy = 0; iy < input.getWidth(); iy++ ) {
            for( int ix = 0; ix < input.getLength(); ix++ ) {
                boolean valueAtPosition = input.readAt(ix, iy);
                output.setAt(ix, iy, convertBooleanToFloat(valueAtPosition));
            }
        }
    }

    public static void floatToBoolean(final IMap2d<Float> input, IMap2d<Boolean> output, final float threshold) {
        for( int iy = 0; iy < input.getWidth(); iy++ ) {
            for( int ix = 0; ix < input.getLength(); ix++ ) {
                float valueAtPosition = input.readAt(ix, iy);
                output.setAt(ix, iy, floatAboveThreshold(valueAtPosition, threshold));
            }
        }
    }

    private static float convertBooleanToFloat(final boolean value) {
        if( value ) {
            return 1.0f;
        }
        else {
            return 0.0f;
        }
    }

    private static boolean floatAboveThreshold(final float value, final float threshold) {
        return value > threshold;
    }
}