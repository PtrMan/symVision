package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

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
    public void processData() {
        // TODO< refactor code so it uses this new interface >
    }

    public static class Sample {
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
        
        public ArrayRealVector position;
        public double altitude = Double.NaN;
        public EnumType type;
        public int objectId = -1;
    }
    
    
    public void set(IMap2d<Boolean> image, IMap2d<Integer> idMap) {
        workingImage = image.copy();
        this.idMap = idMap;
    }

    /**
     * 
     * avoids samping the same pixel by setting the sampled positions to false
     * 
     * 
     *  
     */
    public List<Sample> sampleImage() {
        List<Sample> resultSamples;

        resultSamples = new ArrayList<>();

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
                                addSampleToList(resultSamples, x, y, objectId);
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
                                addSampleToList(resultSamples, x, y, objectId);
                            }
                        }
                    }
                }
            }
        }

        return resultSamples;
    }
    
    private static void addSampleToList(List<Sample> samples, final int x, final int y, final int objectId) {
        Sample addSample = new Sample(new ArrayRealVector(new double[]{(double)x, (double)y}));
        addSample.objectId = objectId;

        samples.add(addSample);
    }

    private static boolean sampleMaskAtPosition(Vector2d<Integer> position, boolean[] mask4by4) {
        int modX, modY;

        modX = position.x % 4;
        modY = position.y % 4;

        return mask4by4[modX + modY * 4];
    }

    private IMap2d<Boolean> workingImage;
    private IMap2d<Integer> idMap;

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
