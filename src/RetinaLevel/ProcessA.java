package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Vector2d;
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
        public Sample(Vector2d<Integer> position)
        {
            this.position = position;
        }
        
        public Vector2d<Integer> position;
        public int altitude = 0;
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
        int sampleI;
        
        resultSamples = new ArrayList<>();
        
        for( sampleI = 0; sampleI < 1000; sampleI++ )
        {
            int x, y;
            boolean readBoolean;
            
            x = random.nextInt(workingImage.getWidth());
            y = random.nextInt(workingImage.getLength());
            
            readBoolean = workingImage.getAt(x, y);
            
            if( readBoolean )
            {
                addSampleToList(resultSamples, x, y);
                workingImage.setAt(x, y, false);
            }
        }
        
        return resultSamples;
    }
    
    private static void addSampleToList(ArrayList<Sample> samples, int x, int y)
    {
        samples.add(new Sample(new Vector2d<Integer>(x, y)));
    }
    
    private Random random;
    private Map2d<Boolean> workingImage;
}
