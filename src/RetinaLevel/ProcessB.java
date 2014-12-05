package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Tuple2;
import Datastructures.Vector2d;
import java.util.ArrayList;


/**
 *
 * calculates the altitude
 */
public class ProcessB
{
    /**
     * 
     * we use the whole image, in phaeaco he worked with the incomplete image witht the guiding of processA, this is not implemented that way 
     */
    public void process(ArrayList<ProcessA.Sample> samples, Map2d<Boolean> image)
    {
        Vector2d<Integer> foundPosition;
        
        final int MAXRADIUS = 10;
        
        for( ProcessA.Sample iterationSample : samples )
        {
            Tuple2<Vector2d<Integer>, Integer> nearestResult;
            
            nearestResult = findNearestPositionWhereMapIs(false, iterationSample.position, image, MAXRADIUS);
            if( nearestResult == null )
            {
                iterationSample.altitude = ((MAXRADIUS+1)*2)*((MAXRADIUS+1)*2);
                continue;
            }
            // else here
            
            iterationSample.altitude = nearestResult.e1;
        }
        
    }
    
    // TODO< move into external function >
    /**
     * 
     * \return null if no point could be found in the radius 
     */
    private static Tuple2<Vector2d<Integer>, Integer> findNearestPositionWhereMapIs(boolean value, Vector2d<Integer> position, Map2d<Boolean> image, int radius)
    {
        Vector2d<Integer> outwardIteratorOffsetUnbound;
        Vector2d<Integer> borderMin;
        Vector2d<Integer> borderMax;
        Vector2d<Integer> one;
        Vector2d<Integer> positionAsInt;

        outwardIteratorOffsetUnbound = new Vector2d<Integer>(0, 0);
        borderMin = new Vector2d<Integer>(0, 0);
        borderMax = new Vector2d<Integer>(image.getWidth(), image.getLength());

        positionAsInt = position;

        one = new Vector2d<Integer>(1, 1);

        for(;;)
        {
            Vector2d<Integer> iteratorOffsetBoundMin;
            Vector2d<Integer> iteratorOffsetBoundMax;
            int x, y;
            int depthCounter;
            
            if (-outwardIteratorOffsetUnbound.x > radius)
            {
                break;
            }

            iteratorOffsetBoundMin = Vector2d.IntegerHelper.max4(borderMin, Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt), Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt), Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt));
            iteratorOffsetBoundMax = Vector2d.IntegerHelper.min4(borderMax, Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.getScaled(outwardIteratorOffsetUnbound, -1), one), positionAsInt), borderMax, borderMax);
            
            depthCounter = 0;
            
            for (y = iteratorOffsetBoundMin.y; y < iteratorOffsetBoundMax.y; y++ )
            {
                for( x = iteratorOffsetBoundMin.x; x < iteratorOffsetBoundMax.x; x++ )
                {
                    // just find at the border
                    if (y == (iteratorOffsetBoundMin.y) || y == iteratorOffsetBoundMax.y - 1 || x == (iteratorOffsetBoundMin.x) || x == iteratorOffsetBoundMax.x - 1)
                    {
                        boolean valueAtPoint;

                        valueAtPoint = image.readAt(x, y);

                        if (valueAtPoint == value)
                        {
                            return new Tuple2(new Vector2d<Integer>(x, y), depthCounter);
                        }
                    }
                    
                    depthCounter++;
                }
            }

            outwardIteratorOffsetUnbound.x--;
            outwardIteratorOffsetUnbound.y--;
        }
        
        return null;
    }
}
