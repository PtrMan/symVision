package ptrman.levels.retina;

import ptrman.Algorithms.FloodFill;
import ptrman.Datastructures.HashableVector2dInteger;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.misc.Assert;

import java.util.*;

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
            setPixelPositions.add(position);
        }

        private ProcessZFacade processZFacade;

        public List<Vector2d<Integer>> setPixelPositions = new ArrayList<>();
    }

    public void preSetupSet(final int accelerationGridsize, int numberOfPixelsManificationThreshold) {
        this.accelerationGridsize = accelerationGridsize;
        this.numberOfPixelsMagnificationThreshold = numberOfPixelsManificationThreshold;
    }

    public void setup(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;

        unprocessedPixelsMap = new Map2d<>(imageSize.x, imageSize.y);
        for( int y = 0; y < unprocessedPixelsMap.getLength(); y++ ) {
            for( int x = 0; x < unprocessedPixelsMap.getWidth(); x++ ) {
                unprocessedPixelsMap.setAt(x, y, false);
            }
        }

        Assert.Assert(accelerationGridsize != 0, "gridsize must be nonzero");

        unprocessedPixelsCellCount = new Map2d<>(imageSize.x / accelerationGridsize, imageSize.y / accelerationGridsize);
        for( int y = 0; y < unprocessedPixelsCellCount.getLength(); y++ ) {
            for( int x = 0; x < unprocessedPixelsCellCount.getWidth(); x++ ) {
                unprocessedPixelsCellCount.setAt(x, y, 0);
            }
        }

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

        storePixelsIntoAccelerationDatastructuresFromMap(input);

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

    private void takeRandomPixelFromHashmapUntilNoCandidatesAndFillAndDecide(IMap2d<Boolean> input) {
        PixelChangeListener pixelChangeListener;

        pixelChangeListener = new PixelChangeListener(this);

        for(;;) {
            if( notCompletlyProcessedCells.isEmpty() ) {
                break;
            }

            HashableVector2dInteger[] arrayOfNotCompletlyProcessedCells = notCompletlyProcessedCells.keySet().toArray(new HashableVector2dInteger[0]);
            Vector2d<Integer> notCompletlyProcessedCellPosition = (Vector2d<Integer>)arrayOfNotCompletlyProcessedCells[random.nextInt(arrayOfNotCompletlyProcessedCells.length)];
            Vector2d<Integer> randomPixelFromNotCompletlyProcessCell = getRandomPixelPositionOfCell(notCompletlyProcessedCellPosition);

            FloodFill.fill(input, randomPixelFromNotCompletlyProcessCell, Boolean.TRUE, Boolean.FALSE, pixelChangeListener);

            // TODO< decide with a propability if the filled patch should be magnified or not >
            if( pixelChangeListener.setPixelPositions.size() < numberOfPixelsMagnificationThreshold) {
                drawPixelsIntoMap(pixelChangeListener.setPixelPositions, toMagnify, true);
            }
            else {
                drawPixelsIntoMap(pixelChangeListener.setPixelPositions, notMagnifiedOutput, true);
            }

            removePixelsFromAccelerationDatastructures(pixelChangeListener.setPixelPositions);

            pixelChangeListener.setPixelPositions.clear();

        }
    }

    private Vector2d<Integer> getRandomPixelPositionOfCell(final Vector2d<Integer> cellPosition) {
        List<Vector2d<Integer>> candidateList = new ArrayList<>();

        for( int y = cellPosition.y * accelerationGridsize; y < (cellPosition.y+1) * accelerationGridsize; y++ ) {
            for( int x = cellPosition.x * accelerationGridsize; x < (cellPosition.x+1) * accelerationGridsize; x++ ) {
                if( unprocessedPixelsMap.readAt(x, y) ) {
                    candidateList.add(new Vector2d<>(x, y));
                }
            }
        }

        final int candidateIndex = random.nextInt(candidateList.size());
        return candidateList.get(candidateIndex);
    }

    private void drawPixelsIntoMap(final List<Vector2d<Integer>> pixels, IMap2d<Boolean> map, final boolean value) {
        for( Vector2d<Integer> iterationPosition : pixels ) {
            map.setAt(iterationPosition.x, iterationPosition.y, value);
        }
    }

    private void storePixelsIntoAccelerationDatastructuresFromMap(final IMap2d<Boolean> input) {
        for( int iy = 0; iy < input.getLength(); iy++ ) {
            for( int ix = 0; ix < input.getWidth(); ix++ ) {
                if( input.readAt(ix, iy) ) {
                    unprocessedPixelsMap.setAt(ix, iy, true);

                    final Vector2d<Integer> cellPositionOfPixel = pixelPositionToCellPosition(new Vector2d<>(ix, iy));
                    unprocessedPixelsCellCount.setAt(cellPositionOfPixel.x, cellPositionOfPixel.y, unprocessedPixelsCellCount.readAt(cellPositionOfPixel.x, cellPositionOfPixel.y) + 1);
                }
            }
        }

        // setup the hashmap
        for( int y = 0; y < unprocessedPixelsCellCount.getLength(); y++ ) {
            for( int x = 0; x < unprocessedPixelsCellCount.getWidth(); x++ ) {
                final int cellCountForCell = unprocessedPixelsCellCount.readAt(x, y);

                if( cellCountForCell > 0 ) {
                    notCompletlyProcessedCells.put(HashableVector2dInteger.fromVector2dInteger(new Vector2d<>(x, y)), null);
                }
            }
        }
    }

    private void removePixelsFromAccelerationDatastructures(final List<Vector2d<Integer>> pixels) {
        drawPixelsIntoMap(pixels, unprocessedPixelsMap, false);

        for( final Vector2d<Integer> iterationPixel : pixels ) {
            final Vector2d<Integer> cellPositionOfPixel = pixelPositionToCellPosition(iterationPixel);

            int numberOfSetPixelsInUnprocessedPixelsMapCell = unprocessedPixelsCellCount.readAt(cellPositionOfPixel.x, cellPositionOfPixel.y);
            Assert.Assert(numberOfSetPixelsInUnprocessedPixelsMapCell > 0, "");
            numberOfSetPixelsInUnprocessedPixelsMapCell--;
            unprocessedPixelsCellCount.setAt(cellPositionOfPixel.x, cellPositionOfPixel.y, numberOfSetPixelsInUnprocessedPixelsMapCell);

            if( numberOfSetPixelsInUnprocessedPixelsMapCell == 0 ) {
                notCompletlyProcessedCells.remove(HashableVector2dInteger.fromVector2dInteger(cellPositionOfPixel));
            }
        }
    }

    private Vector2d<Integer> pixelPositionToCellPosition(final Vector2d<Integer> pixelPosition) {
        return new Vector2d<>(pixelPosition.x / accelerationGridsize, pixelPosition.y / accelerationGridsize);
    }

    private void magnify() {
        processZ.set(toMagnify);
        processZ.processData();
        magnifiedOutput = processZ.getMagnifiedOutput();
    }

    private Vector2d<Integer> imageSize;
    private int numberOfPixelsMagnificationThreshold;

    // each cell contains the count of unprocessedPixels in that cell
    private IMap2d<Integer> unprocessedPixelsCellCount;
    // map which contains all pixels which are not processed
    private IMap2d<Boolean> unprocessedPixelsMap;
    // map which stores the adresses of the cells in unprocessedPixelsCellCount which are non-zero
    private Map<HashableVector2dInteger, Boolean> notCompletlyProcessedCells = new HashMap<>();

    private int accelerationGridsize;


    private IMap2d<Boolean> notMagnifiedOutput;
    private IMap2d<Boolean> magnifiedOutput;

    private IMap2d<Boolean> toMagnify;

    private ProcessZ processZ = new ProcessZ();

    private Random random = new Random();
}