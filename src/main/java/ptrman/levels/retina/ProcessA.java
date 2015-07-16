package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.misc.Assert;

import java.awt.*;

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
        for( int blockY = 0; blockY < workingImage.getLength()/4; blockY++ ) {
            for( int blockX = 0; blockX < workingImage.getWidth()/4; blockX++ ) {
                int hitCount = 0;

                for( int y = blockY*4; y < (blockY+1)*4; y++ ) {
                    for (int x = blockX; x < (blockX+1)*4; x++) {
                        if( sampleMaskAtPosition(new Vector2d<>(x, y), MaskDetail0) ) {
                            if( workingImage.readAt(x, y) ) {
                                hitCount++;
                                workingImage.setAt(x, y, false);

                                final int objectId = idMap.readAt(x, y);
                                //Assert.Assert(objectId  != -1, "");
                                if( objectId != -1 ) {
                                    int d = 0;
                                }
                                addSampleToOutput(x, y, objectId);
                            }
                        }
                    }
                }

                if( hitCount == 8 ) {
                    continue;
                }

                // sample it a second time for nearly all of the missing pixels
                for( int y = blockY*4; y < (blockY+1)*4; y++ ) {
                    for (int x = blockX; x < (blockX+1)*4; x++) {
                        if( sampleMaskAtPosition(new Vector2d<>(x, y), MaskDetail1) ) {
                            if( workingImage.readAt(x, y) ) {
                                hitCount++;
                                workingImage.setAt(x, y, false);

                                final int objectId = idMap.readAt(x, y);
                                //Assert.Assert(objectId  != -1, "");
                                if( objectId != -1 ) {
                                    int d = 0;
                                }

                                addSampleToOutput(x, y, objectId);
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

    public static class Sample {
        public Sample getClone() {
            Sample clone = new Sample(position);
            clone.altitude = this.altitude;
            clone.type = this.type;
            clone.objectId = this.objectId;

            return clone;
        }

        public void debugPlot(Graphics2D detectorImageGraphics) {

            if (isObjectIdValid()) {
                detectorImageGraphics.setColor(Color.GREEN);
            } else {
                detectorImageGraphics.setColor(Color.BLUE);
            }

            final double[] pos = position.getDataRef();
            int positionX = (int) pos[0];
            int positionY = (int) pos[1];


            detectorImageGraphics.fillRect(positionX, positionY, 1, 1);


        }

        public enum EnumType {
            ENDOSCELETON,
            EXOSCELETON
        }
        
        public Sample(ArrayRealVector position)
        {
            this.position = position;
        }
        
        public boolean isAltitudeValid() {
            return altitude != Double.NaN;
        }

        public boolean isObjectIdValid() {
            return objectId != -1;
        }
        
        public final ArrayRealVector position;
        public double altitude = Double.NaN;
        public EnumType type;
        public int objectId = -1;
    }
    
    
    public void set(IMap2d<Boolean> image, IMap2d<Integer> idMap, ProcessConnector<Sample> outputSampleConnector) {
        workingImage = image.copy();
        this.idMap = idMap;

        this.outputSampleConnector = outputSampleConnector;
    }

    private void addSampleToOutput(final int x, final int y, final int objectId) {
        Sample addSample = new Sample(new ArrayRealVector(new double[]{(double)x, (double)y}));
        addSample.objectId = objectId;

        outputSampleConnector.add(addSample);
    }

    private static boolean sampleMaskAtPosition(Vector2d<Integer> position, boolean[] mask4by4) {
        int modX, modY;

        modX = position.x % 4;
        modY = position.y % 4;

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
