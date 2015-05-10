package ptrman.levels.retina;

import ptrman.Algorithms.FloodFill;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 *
 * Segmentates the "objects" based on the rasterized image.
 * Too small segments get magnified with ProcessZ
 *
 */
public class ProcessZFacade {
    private class PixelChangeListener implements FloodFill.IPixelSetListener {
        public PixelChangeListener(ProcessZFacade processZFacade) {
            this.processZFacade = processZFacade;
        }

        @Override
        public void seted(Vector2d<Integer> position) {
            processZFacade.unprocessedPixels.remove(position.x + position.y * processZFacade.imageSize.x);
            setPixelPositions.add(position);
        }

        private ProcessZFacade processZFacade;

        public List<Vector2d<Integer>> setPixelPositions = new ArrayList<>();
    }

    public void setup(Vector2d<Integer> imageSize, int numberOfPixelsManificationThreshold) {
        this.imageSize = imageSize;
        this.numberOfPixelsMagnificationThreshold = numberOfPixelsManificationThreshold;

        processZ.setImageSize(imageSize);
    }

    public IMap2d<Boolean> getNotMagnifiedOutput() {
        return notMagnifiedOutput;
    }

    public IMap2d<Boolean> getMagnifiedOutput() {
        return magnifiedOutput;
    }

    public void process(IMap2d<Boolean> input) {
        IMap2d<Boolean> copiedInput;

        copiedInput = input.copy();

        storePixelsIntoHashmap(input);

        notMagnifiedOutput = new Map2d<>(imageSize.x, imageSize.y);
        toMagnify = createBlankBooleanMap(imageSize);
        magnifiedOutput = new Map2d<>(imageSize.x*2, imageSize.y*2);

        takeRandomPixelFromHashmapUntilNoCandidatesAndFillAndDecide(copiedInput);

        magnify();
    }

    private static IMap2d<Boolean> createBlankBooleanMap(Vector2d<Integer> imageSize) {
        IMap2d<Boolean> result = new Map2d<>(imageSize.x, imageSize.y);

        for( int y = 0; y < result.getLength(); y++ ) {
            for( int x = 0; x < result.getWidth(); x++ ) {
                result.setAt(x, y, false);
            }
        }

        return result;
    }

    private void storePixelsIntoHashmap(final IMap2d<Boolean> input) {
        for( int iy = 0; iy < input.getLength(); iy++ ) {
            for( int ix = 0; ix < input.getWidth(); ix++ ) {
                if( input.readAt(ix, iy) ) {
                    unprocessedPixels.put(ix + iy * imageSize.x, Boolean.TRUE);
                }
            }
        }
    }

    private void takeRandomPixelFromHashmapUntilNoCandidatesAndFillAndDecide(IMap2d<Boolean> input) {
        PixelChangeListener pixelChangeListener;

        pixelChangeListener = new PixelChangeListener(this);

        for(;;) {
            Integer[] setOfUnprocessedPixelsAsArray;
            int pixelAdress;
            int x, y;

            if( unprocessedPixels.isEmpty() ) {
                break;
            }

            setOfUnprocessedPixelsAsArray = unprocessedPixels.keySet().toArray(new Integer[0]);
            pixelAdress = setOfUnprocessedPixelsAsArray[0];
            x = pixelAdress % imageSize.x;
            y = pixelAdress / imageSize.x;

            FloodFill.fill(input, new Vector2d<>(x, y), Boolean.TRUE, Boolean.FALSE, pixelChangeListener);

            // TODO< decide with a propability if the filled patch should be magnified or not >
            if( pixelChangeListener.setPixelPositions.size() < numberOfPixelsMagnificationThreshold) {
                drawPixelsIntoMap(pixelChangeListener.setPixelPositions, toMagnify);
            }
            else {
                drawPixelsIntoMap(pixelChangeListener.setPixelPositions, notMagnifiedOutput);
            }

            pixelChangeListener.setPixelPositions.clear();

        }
    }

    private void drawPixelsIntoMap(List<Vector2d<Integer>> pixels, IMap2d<Boolean> map) {
        for( Vector2d<Integer> iterationPosition : pixels ) {
            map.setAt(iterationPosition.x, iterationPosition.y, true);
        }
    }

    private void magnify() {
        processZ.set(toMagnify);
        processZ.processData();
        magnifiedOutput = processZ.getMagnifiedOutput();
    }

    private Vector2d<Integer> imageSize;
    private int numberOfPixelsMagnificationThreshold;


    private HashMap<Integer, Boolean> unprocessedPixels = new HashMap<>();

    private IMap2d<Boolean> notMagnifiedOutput;
    private IMap2d<Boolean> magnifiedOutput;

    private IMap2d<Boolean> toMagnify;

    private ProcessZ processZ = new ProcessZ();
}