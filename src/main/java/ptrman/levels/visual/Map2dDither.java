package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.math.Maths;

/**
 *
 * from
 * https://gist.github.com/naikrovek/643a9799171d20820cb9
 * from
 * http://stackoverflow.com/questions/5940188/how-to-convert-a-24-bit-png-to-3-bit-png-using-floyd-steinberg-dithering
 *
 * GPU can't be parellized
 */
public class Map2dDither {
    public enum Generic {
		;

		/**
         *
         * a abstraction for a more generic algorithm
         *
         */
        private interface IValue
        {
            IValue add(IValue other);
            IValue sub(IValue other);
            IValue mulScalar(float other);
            double distanceSquared(IValue other); // sub and distanceSquared
        }

        private static class ValueFloat implements IValue {
            public ValueFloat(final float value) {
                this.value = value;
            }

            @Override
            public IValue add(final IValue other) {
                return new ValueFloat(value + ((ValueFloat)other).value);
            }

            @Override
            public IValue sub(final IValue other) {
                return new ValueFloat(value - ((ValueFloat)other).value);
            }

            @Override
            public IValue mulScalar(final float other) {
                return new ValueFloat(value * other);
            }

            @Override
            public double distanceSquared(final IValue other) {
                return Maths.squaredDistance(new double[]{value - ((ValueFloat) other).value});
            }

            private final float value;
        }


        private static IMap2d<IValue> calculate(final IMap2d<IValue> input, final IValue[] palette) {

            final var d = input.copy();

            for (int y = 0; y < input.getLength(); y++)
                for (int x = 0; x < input.getWidth(); x++) {

                    final var oldValue = d.readAt(x, y);
                    final var newValue = findClosestPaletteColor(oldValue, palette);

                    input.setAt(x, y, newValue);

                    final var error = oldValue.sub(newValue);

                    if (x + 1 < input.getWidth())
                        d.setAt(x + 1, y, d.readAt(x + 1, y).add(error.mulScalar(7.0f / 16.0f)));

                    if (x - 1 >= 0 && y + 1 < input.getLength())
                        d.setAt(x - 1, y + 1, d.readAt(x - 1, y + 1).add(error.mulScalar(3.0f / 16.0f)));

                    if (y + 1 < input.getLength())
                        d.setAt(x, y + 1, d.readAt(x, y + 1).add(error.mulScalar(5.0f / 16.0f)));

                    if (x + 1 < input.getWidth() && y + 1 < input.getLength())
                        d.setAt(x + 1, y + 1, d.readAt(x + 1, y + 1).add(error.mulScalar(1.0f / 16.0f)));
                }

            return input;
        }

        private static IValue findClosestPaletteColor(final IValue c, final IValue[] palette) {
            var closest = palette[0];

            for (final var n : palette) if (n.distanceSquared(c) < closest.distanceSquared(c)) closest = n;

            return closest;
        }

        public static void floydSteinbergDitheringFloatToBoolean(final IMap2d<Float> input, final IMap2d<Boolean> resultAboveThreshold) {
            final var palette = new ValueFloat[]{new ValueFloat(0.0f), new ValueFloat(1.0f)};

            final var temporaryInput = covertFromFloatToValueFloat(input);
            final var temporaryResult = calculate(temporaryInput, palette);
            final var result = convertFromValueFromToFloat(temporaryResult);
            convertFromFloatToBooleanNotEqualZeroInPlace(result, resultAboveThreshold);
        }

        private static void convertFromFloatToBooleanNotEqualZeroInPlace(final IMap2d<Float> input, final IMap2d<Boolean> result) {

            for(int y = 0; y < input.getLength(); y++ )
                for (int x = 0; x < input.getWidth(); x++)
                    result.setAt(x, y, input.readAt(x, y) != 0.0f);
        }

        private static IMap2d<Float> convertFromValueFromToFloat(final IMap2d<IValue> input) {

            final IMap2d<Float> result = new Map2d<>(input.getWidth(), input.getLength());

            for(int y = 0; y < input.getLength(); y++ )
                for (int x = 0; x < input.getWidth(); x++)
                    result.setAt(x, y, ((ValueFloat) input.readAt(x, y)).value);

            return result;
        }

        private static IMap2d<IValue> covertFromFloatToValueFloat(final IMap2d<Float> input) {

            final IMap2d<IValue> result = new Map2d<>(input.getWidth(), input.getLength());

            for(int y = 0; y < input.getLength(); y++ )
                for (int x = 0; x < input.getWidth(); x++)
                    result.setAt(x, y, new ValueFloat(input.readAt(x, y)));

            return result;
        }



    /* palette for rgb dithering
        C3[] palette = new C3[] {
                new C3(  0,   0,   0), // black
                new C3(  0,   0, 255), // green
                new C3(  0, 255,   0), // blue
                new C3(  0, 255, 255), // cyan
                new C3(255,   0,   0), // red
                new C3(255,   0, 255), // purple
                new C3(255, 255,   0), // yellow
                new C3(255, 255, 255)  // white
        };
        */
    }

}
