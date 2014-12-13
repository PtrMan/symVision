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
        workingImage = image.clone();
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
        int checkCounter;
        int samplePositionsI;
        
        final float CHECKAFTERITERATIONS = 3000;
        
        resultSamples = new ArrayList<>();
        
        hitCount = 0;
        sampleCount = 0;
        checkCounter = 0;
        samplePositionsI = 0;
        
        resizeSamplePositionsForSize(workingImage.getWidth(), workingImage.getLength());
        
        for(;;)
        {
            int x, y;
            boolean readBoolean;
            Vector2d<Integer> samplePosition;
            
            // if the array with the precomputed sample positions is depleated we exit
            if( samplePositions.length <= samplePositionsI )
            {
                break;
            }
            samplePosition = samplePositions[samplePositionsI];

            readBoolean = workingImage.readAt(samplePosition.x, samplePosition.y);
            
            if( readBoolean )
            {
                addSampleToList(resultSamples, samplePosition.x, samplePosition.y);
                workingImage.setAt(samplePosition.x, samplePosition.y, false);
                
                hitCount++;
            }
            
            if( checkCounter >= CHECKAFTERITERATIONS )
            {
                checkCounter = 0;
                
                if( (float)hitCount / (float)sampleCount < HardParameters.ProcessA.MINIMALHITRATIOUNTILTERMINATION )
                {
                    break;
                }
            }
            
            sampleCount++;
            checkCounter++;
            samplePositionsI++;
        }
        
        return resultSamples;
    }
    
    private static void addSampleToList(ArrayList<Sample> samples, int x, int y)
    {
        samples.add(new Sample(new Vector2d<Integer>(x, y)));
    }
    
    private void resizeSamplePositionsForSize(int width, int height)
    {
        int samplePositionsArraySize;
        int i;
        
        samplePositionsArraySize = (int)Math.round((float)(width*height)*(3.0f/4.0));

        if( samplePositions.length >= samplePositionsArraySize )
        {
            return;
        }
        
        samplePositions = new Vector2d[samplePositionsArraySize];
        
        for( i = 0; i < samplePositionsArraySize; i++ )
        {
            int vectorX, vectorY;
            
            vectorX = Math.round(RandomUtil.radicalInverse(i, 2)*(float)(width-1));
            vectorY = Math.round(RandomUtil.radicalInverse(i, 3)*(float)(height-1));

            samplePositions[i] = new Vector2d<Integer>(vectorX, vectorY);
        }
    }
    
    private Random random = new Random();
    private Map2d<Boolean> workingImage;
    
    private Vector2d<Integer>[] samplePositions = new Vector2d[0];
}
