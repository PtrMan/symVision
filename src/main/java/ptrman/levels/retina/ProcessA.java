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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.misc.Assert;

import java.awt.*;
import java.util.Random;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 *
 * samples from the input image and puts the set pixels into a queue (is for now just a list)
 */
public class ProcessA implements IProcess {
    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        Assert.Assert((imageSize.x % 4) == 0, "imageSize.x must be divisable by 4");
        Assert.Assert((imageSize.y % 4) == 0, "imageSize.y must be divisable by 4");
    }

    @Override
    public void setup() {

    }

    @Override
    public void preProcessData() {

    }

    /**
     *
     * avoids samping the same pixel by setting the sampled positions to false
     *
     *
     *
     */
    @Override
    public void processData() {
        processData(1f);
    }

    final Random rng =
        //new RandomAdaptor( new MersenneTwister() ); //<- expensive
        new Random();

    public void processData(float throttle) {
        int H = workingImage.getLength() / 4;
        int W = workingImage.getWidth() / 4;
        for(int blockY = 0; blockY < H; blockY++ ) {
            for(int blockX = 0; blockX < W; blockX++ ) {
                int hitCount = 0;

                for( int y = blockY*4; y < (blockY+1)*4; y++ ) {
                    for (int x = blockX; x < (blockX+1)*4; x++) {
                        if (throttle < 1f && rng.nextDouble() > throttle) continue;

                        if( sampleMaskAtPosition(x, y, MaskDetail0) ) {
                            if( workingImage.readAt(x, y) ) {
                                hitCount++;
                                workingImage.setAt(x, y, false);

                                final int objectId = idMap != null ? idMap.readAt(x, y) : -1;
                                //Assert.Assert(objectId  != -1, "");
                                /*if( objectId != -1 ) {
                                    int d = 0;
                                }*/

                                output(x, y, objectId);
                            }
                        }
                    }
                }

                if( hitCount == 8 )
                    continue;

                // sample it a second time for nearly all of the missing pixels
                for( int y = blockY*4; y < (blockY+1)*4; y++ ) {
                    for (int x = blockX; x < (blockX+1)*4; x++) {
                        if (throttle < 1f && rng.nextDouble() > throttle) continue;

                        if( sampleMaskAtPosition(x, y, MaskDetail1) ) {
                            if( workingImage.readAt(x, y) ) {
                                hitCount++;
                                workingImage.setAt(x, y, false);

                                final int objectId = idMap != null ? idMap.readAt(x, y) : -1;
                                //Assert.Assert(objectId  != -1, "");
                                /*if( objectId != -1 ) {
                                    int d = 0;
                                }*/

                                output(x, y, objectId);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postProcessData() {

    }

    public final double defaultSampleConf = 0.1; // confidence of one sample

    public static class Sample {

        public final IntIntPair position;
        public double altitude = Double.NaN;
        public EnumType type;
        public int objectId = -1;
        public double conf; // confidence from NAL

        public int refCount = 0; // used to see in process-D if sample is already used

        public Sample getClone() {
            Sample clone = new Sample(position);
            clone.altitude = this.altitude;
            clone.type = this.type;
            clone.objectId = this.objectId;
            clone.conf = this.conf;

            return clone;
        }

        public void debugPlot(Graphics2D detectorImageGraphics) {

            if (isObjectIdValid()) {
                detectorImageGraphics.setColor(Color.GREEN);
            } else {
                detectorImageGraphics.setColor(Color.BLUE);
            }


            int positionX = position.getOne();
            int positionY = position.getTwo();

            detectorImageGraphics.fillRect(positionX, positionY, 1, 1);

            if (isAltitudeValid()) {
                detectorImageGraphics.setColor(Color.RED);
                float a = (float) (
                    //altitude * 4
                    altitude/4f
                );
                detectorImageGraphics.drawOval(Math.round(positionX-a/2), Math.round(positionY-a/2), Math.round(a), Math.round(a));
            }

        }

        /** sKeletor */
        public enum EnumType {
            ENDOSCELETON,
            EXOSCELETON
        }

        public Sample(double x, double y) {
            this(new ArrayRealVector(new double[] { x, y}, false));
        }
        public Sample(float x, float y) {
            this(new ArrayRealVector(new double[] { x, y}, false));
        }
        public Sample(int x, int y) {
            this(pair(x, y));
        }
        public Sample(IntIntPair position) {
            this.position = position;
        }
        public Sample(ArrayRealVector position) {
            this(
                (int)Math.round(position.getEntry(0)),
                (int)Math.round(position.getEntry(1))
            );
        }

        
        public boolean isAltitudeValid() {
            return Double.isFinite(altitude);
        }

        public boolean isObjectIdValid() {
            return objectId != -1;
        }

    }
    
    
    public void set(IMap2d<Boolean> image, IMap2d<Integer> idMap, ProcessConnector<Sample> outputSampleConnector) {
        workingImage = image.copy();
        this.idMap = idMap;

        this.outputSampleConnector = outputSampleConnector;
    }

    private void output(final int x, final int y, final int objectId) {
        Sample createdSample = new Sample(x, y);
        createdSample.objectId = objectId;
        createdSample.conf = defaultSampleConf;
        outputSampleConnector.add(createdSample);
    }

    private static boolean sampleMaskAtPosition(int px, int py, boolean[] mask4by4) {

        int modX = px % 4;
        int modY = py % 4;

        return mask4by4[modX + modY * 4];
    }

    private IMap2d<Boolean> workingImage;
    private IMap2d<Integer> idMap;
    private ProcessConnector<Sample> outputSampleConnector;

    private static final boolean[] MaskDetail0 =
            {
                    true, false, false, true,
                    false, true, true, false,
                    true, false, true, false,
                    false, true, false, true
            };

    private static final boolean[] MaskDetail1 =
            {
                    false, false, false, true,
                    true, false, false, true,
                    false, true, false, false,
                    false, false, true, false
            };


}
