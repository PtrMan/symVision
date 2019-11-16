package ptrman.Algorithms;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;

public enum FloodFill {
	;

	public interface IPixelSetListener {
        void seted(Vector2d<Integer> position);
    }

    private static class SpecificExecutor<Type> implements GeneralizedFloodFill.IFillExecutor {
        public SpecificExecutor(IPixelSetListener pixelSetListener, IMap2d<Type> map, final Type targetColor, final Type replacementColor) {
            this.pixelSetListener = pixelSetListener;
            this.map = map;
            this.targetColor = targetColor;
            this.replacementColor = replacementColor;
        }

        @Override
        public void fillAt(Vector2d<Integer> position, Vector2d<Integer> fromDirection) {
            map.setAt(position.x, position.y, replacementColor);

            pixelSetListener.seted(position);
        }

        @Override
        public boolean canAndShouldBeFilled(Vector2d<Integer> position) {
            if( targetColor.equals(replacementColor) ) {
                return false;
            }

            if( !(map.readAt(position.x, position.y).equals(targetColor)) ) {
                return false;
            }

            return true;
        }

        @Override
        public boolean inRange(Vector2d<Integer> position) {
            return map.inBounds(position);
        }

        private final Type replacementColor;
        private final Type targetColor;
        private final IMap2d<Type> map;
        private final IPixelSetListener pixelSetListener;
    }
    
    public static <Type> void fill(IMap2d<Type> map, Vector2d<Integer> centerPosition, final Type targetColor, final Type replacementColor, final boolean cross, IPixelSetListener pixelSetListener) {
        GeneralizedFloodFill.IFillExecutor fillExecutor = new SpecificExecutor<>(pixelSetListener, map, targetColor, replacementColor);
        GeneralizedFloodFill.fill(centerPosition, cross, fillExecutor);
    }
}
