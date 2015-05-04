package ptrman.levels.retina;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Tuple2;
import ptrman.Datastructures.Vector2d;

import java.util.List;

import static ptrman.Datastructures.Vector2d.FloatHelper.getLength;
import static ptrman.Datastructures.Vector2d.FloatHelper.sub;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;


/**
 *
 * calculates the altitude
 */
public class ProcessB {
    /**
     * 
     * we use the whole image, in phaeaco he worked with the incomplete image witht the guiding of processA, this is not implemented that way 
     */
    public void process(List<ProcessA.Sample> samples, IMap2d<Boolean> image) {
        Vector2d<Integer> foundPosition;
        
        final int MAXRADIUS = 30;
        
        for( ProcessA.Sample iterationSample : samples ) {
            Tuple2<Vector2d<Integer>, Double> nearestResult;
            
            nearestResult = findNearestPositionWhereMapIs(false, arrayRealVectorToInteger(iterationSample.position), image, MAXRADIUS);
            if( nearestResult == null ) {
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
    private static Tuple2<Vector2d<Integer>, Double> findNearestPositionWhereMapIs(boolean value, Vector2d<Integer> position, IMap2d<Boolean> image, int radius) {
        Vector2d<Integer> outwardIteratorOffsetUnbound;
        Vector2d<Integer> borderMin;
        Vector2d<Integer> borderMax;
        Vector2d<Integer> one;
        Vector2d<Integer> positionAsInt;

        outwardIteratorOffsetUnbound = new Vector2d<>(0, 0);
        borderMin = new Vector2d<>(0, 0);
        borderMax = new Vector2d<>(image.getWidth(), image.getLength());

        positionAsInt = position;

        one = new Vector2d<>(1, 1);

        for(;;) {
            if (-outwardIteratorOffsetUnbound.x > radius) {
                break;
            }

            Vector2d<Integer> iteratorOffsetBoundMin = Vector2d.IntegerHelper.max4(borderMin, Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt), Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt), Vector2d.IntegerHelper.add(outwardIteratorOffsetUnbound, positionAsInt));
            Vector2d<Integer> iteratorOffsetBoundMax = Vector2d.IntegerHelper.min4(borderMax, Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.add(Vector2d.IntegerHelper.getScaled(outwardIteratorOffsetUnbound, -1), one), positionAsInt), borderMax, borderMax);

            for( int y = iteratorOffsetBoundMin.y; y < iteratorOffsetBoundMax.y; y++ ) {
                for( int x = iteratorOffsetBoundMin.x; x < iteratorOffsetBoundMax.x; x++ ) {
                    // just find at the border
                    if (y == (iteratorOffsetBoundMin.y) || y == iteratorOffsetBoundMax.y - 1 || x == (iteratorOffsetBoundMin.x) || x == iteratorOffsetBoundMax.x - 1) {
                        boolean valueAtPoint;

                        valueAtPoint = image.readAt(x, y);

                        if (valueAtPoint == value) {
                            return new Tuple2(new Vector2d<>(x, y), (double)getLength(sub(new Vector2d<>((float) x, (float) y), Vector2d.ConverterHelper.convertIntVectorToFloat(position))));
                        }
                    }
                }
            }

            outwardIteratorOffsetUnbound.x--;
            outwardIteratorOffsetUnbound.y--;
        }
        
        return null;
    }
}
