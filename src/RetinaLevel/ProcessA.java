package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import bpsolver.HardParameters;
import java.util.ArrayList;
import java.util.Random;
import math.RandomUtil;

/**
 *
 * samples from the input image and puts the set pixels into a queue (is for now just a list)
 */
public class ProcessA
{
    public static class Sample
    {
        public enum EnumType
        {
            ENDOSCELETON,
            EXOSCELETON
        }
        
        public Sample(Vector2d<Integer> position)
        {
            this.position = position;
        }
        
        public boolean isAltidudeValid()
        {
            return altitude != -1;
        }
        
        public Vector2d<Integer> position;
        public int altitude = -1;
        public EnumType type;
        
        
    }
    
    
    public void setWorkingImage(Map2d<Boolean> image)
    {
        workingImage = image.copy();
    }

    /**
     * 
     * avoids samping the same pixel by setting the sampled positions to false
     * 
     * 
     *  
     */
    public ArrayList<Sample> sampleImage()
    {
        ArrayList<Sample> resultSamples;
        int hitCount;
        int sampleCount;

        resultSamples = new ArrayList<>();
        
        hitCount = 0;
        sampleCount = 0;


        int x, y;

        for( y = 0; y < workingImage.getLength(); y++ )
        {
            for( x = 0; x < workingImage.getWidth(); x++ )
            {
                sampleCount++;

                if( sampleMaskAtPosition(new Vector2d<>(x, y), MaskDetail0) )
                {
                    if( workingImage.readAt(x, y) )
                    {
                        hitCount++;
                        workingImage.setAt(x, y, false);

                        addSampleToList(resultSamples, x, y);
                    }
                }
            }
        }

        
        return resultSamples;
    }
    
    private static void addSampleToList(ArrayList<Sample> samples, int x, int y)
    {
        samples.add(new Sample(new Vector2d<>(x, y)));
    }

    private static boolean sampleMaskAtPosition(Vector2d<Integer> position, boolean[] mask4by4)
    {
        int modX, modY;

        modX = position.x % 4;
        modY = position.y % 4;

        return mask4by4[modX + modY * 4];
    }

    private Map2d<Boolean> workingImage;

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
