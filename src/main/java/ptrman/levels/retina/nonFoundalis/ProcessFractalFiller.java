/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina.nonFoundalis;


//import com.gs.collections.impl.list.mutable.FastList;
import boofcv.struct.image.InterleavedF32;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.FastBooleanMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.IProcess;
import ptrman.levels.retina.ProcessA;
import ptrman.misc.Assert;
import ptrman.misc.BooleanHelper;

import java.util.Collection;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * * Segmentates the image in a fractal like fashion to save computationtime and not waste time on "unimportant" pixels.
 * * Subdives the area on boundaries as needed
 */
public class ProcessFractalFiller implements IProcess {
    /*
    private static class AccelerationMapElement {
        // was this pixels reachable by the scanGridCell call?
        public boolean touchedByLastGridCellScan;
    }
    */

    private static class GaussianContext {
        public final SummaryStatistics statistics = new SummaryStatistics();

        public boolean isValueInsideVariance(final float value, final float varianceMultiplier) {
            final float variance = (float)Math.sqrt(statistics.getPopulationVariance());
            final float maxVariance = variance * varianceMultiplier;

            return value > (float)statistics.getMean() - maxVariance && value < (float)statistics.getMean() + maxVariance;
        }
    }

    protected static class FillContext {
        // for each channel one context
        public GaussianContext[] gaussianContext;

        public final Collection<ProcessA.Sample> samples = new FastList<>();

        public float[] varianceMultiplier;
    }

    public void set(int accelerationGridSize, InterleavedF32 inputImage) {
        this.accelerationGridSize = accelerationGridSize;
        this.inputImage = inputImage;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        alreadySegmentatedPixelsMap = new FastBooleanMap2d(imageSize.x, imageSize.y);
    }

    @Override
    public void setup() {

    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {

    }

    @Override
    public void postProcessData() {

    }

    /**
     *
     * @param gridCellPosition
     * \param entryDirection values can be -1 or 1, tells from which side the scanning should start (used for inter-Gridcell filling)
     */
    protected void scanGridCell(final IntIntPair gridCellPosition, final IntIntPair entryDirection, final boolean[] entryPixels, FillContext fillContext) {
        //final Vector2d<Integer> absoluteMapTopLeftPosition = Vector2d.IntegerHelper.getScaled(gridCellPosition, accelerationGridSize);

        boolean[] lastFrontEntryPixels = entryPixels;

        for( int scanFrontCounter = 0; scanFrontCounter < accelerationGridSize; scanFrontCounter++ ) {
            // call method to calculate the scanabled pixels from the direction and scanFrontCounter offset
            boolean[] scanablePixels = calculateScanablePixels(lastFrontEntryPixels, gridCellPosition, entryDirection, scanFrontCounter);

            // fill with the help of the gaussian channel estimation
            // TODO< fill the array with useful information >
            boolean[] filledPixelsOfCurrentFront = fillWithGaussianEstimationAndReturnFilledPixels(gridCellPosition, entryDirection, scanFrontCounter, fillContext, scanablePixels);

            addSamplesForCurrentFront(gridCellPosition, entryDirection, scanFrontCounter, filledPixelsOfCurrentFront, fillContext.samples);

            lastFrontEntryPixels = filledPixelsOfCurrentFront;
        }
    }

    private boolean[] calculateScanablePixels(final boolean[] frontEntryPixels, final IntIntPair gridCellPosition, final IntIntPair entryDirection, int scanFrontCounter) {
        boolean[] result = new boolean[accelerationGridSize];

        final boolean[] broadcastedEntryFront = booleanBroadcast(frontEntryPixels);

        for( int i = 0; i < accelerationGridSize; i++ ) {
            final IntIntPair samplePosition = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, i);

            if( !alreadySegmentatedPixelsMap.readAt(samplePosition.getOne(), samplePosition.getTwo()) && broadcastedEntryFront[i] ) {
                result[i] = true;
            }
        }

        return result;
    }

    private boolean[] fillWithGaussianEstimationAndReturnFilledPixels(final IntIntPair gridCellPosition, final IntIntPair entryDirection, int scanFrontCounter, FillContext fillContext, final boolean[] scanablePixels) {
        boolean[] filledResult = new boolean[accelerationGridSize];

        for( int i = 0; i < accelerationGridSize; i++ ) {
            final IntIntPair samplePosition = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, i);
            final ArrayRealVector sampledPixel = readInputImageAt(samplePosition);
            final boolean pixelShouldBeFilled = calculateIfPixelShouldBeFilled(sampledPixel, fillContext);
            if( pixelShouldBeFilled ) {
                filledResult[i] = true;

                // fill
                for( int channelI = 0; channelI < inputImage.getNumBands(); channelI++ )
                    fillContext.gaussianContext[channelI].statistics.addValue(sampledPixel.getDataRef()[channelI]);

                alreadySegmentatedPixelsMap.setAt(samplePosition.getOne(), samplePosition.getTwo(), true);
            }
        }

        return filledResult;
    }

    private boolean calculateIfPixelShouldBeFilled(ArrayRealVector sampledPixel, FillContext fillContext) {
        for( int channelI = 0; channelI < inputImage.getNumBands(); channelI++ ) {
            final boolean isChannelValueInVariance = fillContext.gaussianContext[channelI].isValueInsideVariance((float)sampledPixel.getDataRef()[channelI], fillContext.varianceMultiplier[channelI]);

            if( !isChannelValueInVariance ) {
                return false;
            }
        }

        return true;
    }

    private void addSamplesForCurrentFront(IntIntPair gridCellPosition, IntIntPair entryDirection, int scanFrontCounter, boolean[] filledPixelsOfCurrentFront, Collection<ProcessA.Sample> samples) {
        final int MIDDLE_INDEX = (accelerationGridSize-1)/2;

        final boolean WRITE_MIDDLE_SAMPLE = true;


        Assert.Assert((accelerationGridSize % 2) == 1, "accelerationGridSize must be uneven!");

        if( arrayAreAllValues(filledPixelsOfCurrentFront, true) ) {
            // add two samples on each side
            final IntIntPair sample1Position = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, 0);
            samples.add(new ProcessA.Sample(sample1Position));

            final IntIntPair sample2Position = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, accelerationGridSize-1);
            samples.add(new ProcessA.Sample(sample2Position));

            // add sample in the middle if constant flag says so
            if( WRITE_MIDDLE_SAMPLE ) {
                final IntIntPair middleSamplePosition = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, MIDDLE_INDEX);

                samples.add(new ProcessA.Sample(middleSamplePosition));
            }
        }
        else {
            final boolean[] frontWhereSamplesShouldBeTakes = getFrontWhereSamplesShouldBeTaken(filledPixelsOfCurrentFront);
            // TODO< try to optimize sample positions >

            addSamples(gridCellPosition, entryDirection, scanFrontCounter, frontWhereSamplesShouldBeTakes, samples);
        }
    }

    private void addSamples(final IntIntPair gridCellPosition, final IntIntPair entryDirection, int scanFrontCounter, final boolean[] frontWhereSamplesShouldBeTakes, Collection<ProcessA.Sample> samples) {
        for (boolean frontWhereSamplesShouldBeTake : frontWhereSamplesShouldBeTakes) {
            final IntIntPair sample1Position = getAbsolutePosition(gridCellPosition, entryDirection, scanFrontCounter, 0);

            if (frontWhereSamplesShouldBeTake) {
                samples.add(new ProcessA.Sample(sample1Position));
            }
        }
    }

    private static boolean[] getFrontWhereSamplesShouldBeTaken(boolean[] front) {
        return BooleanHelper.booleanAnd(booleanBroadcast(front), BooleanHelper.booleanNot(front));
    }

    private IntIntPair getAbsolutePosition(final IntIntPair gridCellPosition, final IntIntPair entryDirection, int scanFrontCounter, int frontIndex) {
        final IntIntPair absoluteMapTopLeftPosition = Vector2d.IntegerHelper.getScaled(gridCellPosition, accelerationGridSize);

        final IntIntPair scanFrontOffset = Vector2d.IntegerHelper.getScaled(entryDirection, scanFrontCounter);

        final IntIntPair indexOffset = Vector2d.IntegerHelper.getScaled(getRotatedEntryDirection(entryDirection), frontIndex);

        return Vector2d.IntegerHelper.add(absoluteMapTopLeftPosition, Vector2d.IntegerHelper.add(scanFrontOffset, indexOffset));
    }

    static final IntIntPair UP = pair(0, 1);
    static final IntIntPair DOWN = pair(1, 0);

    // returns the direction for the other axis
    // is not just a 90 degrees rotation
    private static IntIntPair getRotatedEntryDirection(IntIntPair entryDirection) {
        return entryDirection.getTwo() == 0 ? UP : DOWN;
    }


    // TODO< move to misc >
    private static boolean arrayAreAllValues(final boolean[] array, final boolean value) {
        for( final boolean currentValue : array ) {
            if( currentValue != value ) {
                return false;
            }
        }

        return true;
    }

    // TODO< move to external boolean class >
    private static boolean[] booleanBroadcast(final boolean[] input) {
        boolean[] result = new boolean[input.length];

        for( int i = 0; i < input.length; i++ ) {
            if( i > 0 ) {
                result[i] |= input[i-1];
            }

            if( i < input.length-1 ) {
                result[i] |= input[i+1];
            }
        }

        return result;
    }

    private ArrayRealVector readInputImageAt(final IntIntPair position) {
        int bands = inputImage.getNumBands();
        double[] resultDoubles = new double[bands];

        for(int band = 0; band < bands; band++ )
            resultDoubles[band] = inputImage.getBand(position.getOne(), position.getTwo(), band);

        return new ArrayRealVector(resultDoubles);
    }



    /*
    private ArrayRealVector[] scanAbsoluteInDirection(final Vector2d<Integer> position, final Vector2d<Integer> direction, int length) {
        ArrayRealVector[] result = new ArrayRealVector[length];
        Vector2d<Integer> currentPosition = position;

        for( int i = 0; i < length; i++ ) {
            result[i] = readInputImageAt(currentPosition);

            currentPosition = Vector2d.IntegerHelper.add(currentPosition, direction);
        }

        return result;
    }

    private Vector2d<Integer> getOffsetOfDirection(final Vector2d<Integer> direction) {
        int x, y;

        if( direction.x == -1 ) {
            x = 0;
        }
        else {
            x = accelerationGridSize;
        }

        if( direction.y == -1 ) {
            y = 0;
        }
        else {
            y = accelerationGridSize;
        }

        return new Vector2d<>(x, y);
    }
    */


    private InterleavedF32 inputImage;

    // map used for the acceleration of the "fractal" image segmentation
    //private IMap2d<AccelerationMapElement> accelerationMap;

    protected FastBooleanMap2d alreadySegmentatedPixelsMap;

    private int accelerationGridSize; // must be uneven
}
