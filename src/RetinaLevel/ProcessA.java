package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import bpsolver.Parameters;
import java.util.ArrayList;
import java.util.Random;

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
    
    // TODO< sample as long as the hit/miss ratio is large enought >
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
        
        final float CHECKAFTERITERATIONS = 1000;
        
        resultSamples = new ArrayList<>();
        
        hitCount = 0;
        sampleCount = 0;
        checkCounter = 0;
        
        for(;;)
        {
            int x, y;
            boolean readBoolean;
            
            x = random.nextInt(workingImage.getWidth());
            y = random.nextInt(workingImage.getLength());
            
            readBoolean = workingImage.readAt(x, y);
            
            if( readBoolean )
            {
                addSampleToList(resultSamples, x, y);
                workingImage.setAt(x, y, false);
                
                hitCount++;
            }
            
            if( checkCounter >= CHECKAFTERITERATIONS )
            {
                checkCounter = 0;
                
                if( (float)hitCount / (float)sampleCount < Parameters.ProcessA.MINIMALHITRATIOUNTILTERMINATION )
                {
                    break;
                }
            }
            
            
            sampleCount++;
            checkCounter++;
        }
        
        return resultSamples;
    }
    
    private static void addSampleToList(ArrayList<Sample> samples, int x, int y)
    {
        samples.add(new Sample(new Vector2d<Integer>(x, y)));
    }
    
    private Random random = new Random();
    private Map2d<Boolean> workingImage;
}
