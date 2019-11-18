/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
public class ProcessZFacade implements IProcess {
    public static class Rect {
        public static Rect createFromSinglePoint(final Vector2d<Integer> position) {
            return new Rect(position, position);
        }

        public Rect(final Vector2d<Integer> min, final Vector2d<Integer> max) {
            this.min = min;
            this.max = max;
        }

        public void addPosition(final Vector2d<Integer> position) {
            min = Vector2d.IntegerHelper.min(position, min);
            max = Vector2d.IntegerHelper.max(position, max);
        }

        public Vector2d<Integer> min;
        public Vector2d<Integer> max;
    }

    public void set(IMap2d<Boolean> image) {
        this.alreadyCopiedImage = image.copy();
    }

    private static class PixelChangeListener implements FloodFill.IPixelSetListener {
        public PixelChangeListener() {
        }

        @Override
        public void seted(Vector2d<Integer> position) {
            setPixelPositions.add(position);
        }

        public final List<Vector2d<Integer>> setPixelPositions = new ArrayList<>();
    }

    public void preSetupSet(final int accelerationGridsize, int numberOfPixelsManificationThreshold) {
        this.accelerationGridsize = accelerationGridsize;
        this.numberOfPixelsMagnificationThreshold = numberOfPixelsManificationThreshold;
    }

    public IMap2d<Boolean> getNotMagnifiedOutput() {
        return notMagnifiedOutput;
    }

    public IMap2d<Boolean> getMagnifiedOutput() {
        return magnifiedOutput;
    }

    public IMap2d<Integer> getNotMagnifiedOutputObjectIds() {
        return notMagnifiedOutputObjectIds;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {

    }

    @Override
    public void preProcessData() {
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

        notMagnifiedOutputObjectIds = new Map2d<>(imageSize.x, imageSize.y);

        processZ.setImageSize(imageSize);



        idCounter = 0;
        resetIdMaps();
    }

    @Override
    public void processData() {
        storePixelsIntoAccelerationDatastructuresFromMap(alreadyCopiedImage);

        notMagnifiedOutput = new Map2d<>(imageSize.x, imageSize.y);
        toMagnify = createBlankBooleanMap(imageSize);
        magnifiedOutput = new Map2d<>(imageSize.x*2, imageSize.y*2);

        takeRandomPixelFromHashmapUntilNoCandidatesAndFillAndDecide(alreadyCopiedImage);

        magnify();
    }

    @Override
    public void postProcessData() {

    }

    private void resetIdMaps() {
        setMapToValue(notMagnifiedOutputObjectIds, -1);
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

        PixelChangeListener pixelChangeListener = new PixelChangeListener();

        rects.clear();

		while (!notCompletlyProcessedCells.isEmpty()) {

			HashableVector2dInteger[] arrayOfNotCompletlyProcessedCells = notCompletlyProcessedCells.keySet().toArray(new HashableVector2dInteger[0]);
			Vector2d<Integer> notCompletlyProcessedCellPosition = arrayOfNotCompletlyProcessedCells[random.nextInt(arrayOfNotCompletlyProcessedCells.length)];
			Vector2d<Integer> randomPixelFromNotCompletlyProcessCell = getRandomPixelPositionOfCell(notCompletlyProcessedCellPosition);

			FloodFill.fill(input, randomPixelFromNotCompletlyProcessCell, Boolean.TRUE, Boolean.FALSE, true, pixelChangeListener);

			// TODO< decide with a propability if the filled patch should be magnified or not >
			if (pixelChangeListener.setPixelPositions.size() < numberOfPixelsMagnificationThreshold) {
				drawValuesIntoMap(pixelChangeListener.setPixelPositions, toMagnify, true);
				//drawValuesIntoMap(pixelChangeListener.setPixelPositions, toMagnifiedOutputObjectIds, idCounter);
			} else {
				drawValuesIntoMap(pixelChangeListener.setPixelPositions, notMagnifiedOutput, true);
				drawValuesIntoMap(pixelChangeListener.setPixelPositions, notMagnifiedOutputObjectIds, idCounter);
			}

			idCounter++;

			removePixelsFromAccelerationDatastructures(pixelChangeListener.setPixelPositions);
			rects.add(getRectForPixelPositions(pixelChangeListener.setPixelPositions));

			pixelChangeListener.setPixelPositions.clear();

		}
    }

    private static Rect getRectForPixelPositions(final List<Vector2d<Integer>> setPixelPositions) {
        Rect rect = Rect.createFromSinglePoint(setPixelPositions.get(0));

        for( Vector2d<Integer> iterationPosition : setPixelPositions )
            rect.addPosition(iterationPosition);


        return rect;
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

    private static<Type> void drawValuesIntoMap(final Iterable<Vector2d<Integer>> positions, IMap2d<Type> map, final Type value) {
        for( Vector2d<Integer> iterationPosition : positions ) {
            map.setAt(iterationPosition.x, iterationPosition.y, value);
        }
    }

    private static<Type> void setMapToValue(IMap2d<Type> map, final Type value) {
        for( int y = 0; y < map.getLength(); y++ ) {
            for( int x = 0; x < map.getWidth(); x++ ) {
                map.setAt(x, y, value);
            }
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

    private void removePixelsFromAccelerationDatastructures(final Iterable<Vector2d<Integer>> pixels) {
        drawValuesIntoMap(pixels, unprocessedPixelsMap, false);

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
    private final Map<HashableVector2dInteger, Boolean> notCompletlyProcessedCells = new HashMap<>();

    private int accelerationGridsize;


    private IMap2d<Boolean> notMagnifiedOutput;
    private IMap2d<Boolean> magnifiedOutput;

    private IMap2d<Integer> notMagnifiedOutputObjectIds;
    private int idCounter;

    private IMap2d<Boolean> toMagnify;

    private final ProcessZ processZ = new ProcessZ();

    private final Random random = new Random();

    private IMap2d<Boolean> alreadyCopiedImage;

    // stores all rectangles of the filled regions
    public final Collection<Rect> rects = new ArrayList<>();
}