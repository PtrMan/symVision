package ptrman.bpsolver;

import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 */
public class Helper {
    public static IntObjectHashMap<Deque<RetinaPrimitive>> createMapByObjectIdsFromListOfRetinaPrimitives(List<RetinaPrimitive> primitives) {

        IntObjectHashMap<Deque<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = new IntObjectHashMap<>(primitives.size());

        for( final RetinaPrimitive iterationPrimitive : primitives ) {

            Assert.Assert(iterationPrimitive.hasValidObjectId(), "RetinaPrimitive has no valid objectId");

            Deque<RetinaPrimitive> primitivesOfObject =
                objectIdToRetinaPrimitivesMap.get(iterationPrimitive.objectId);

            if( primitivesOfObject == null) {
                primitivesOfObject = new ConcurrentLinkedDeque<>();
                objectIdToRetinaPrimitivesMap.put(iterationPrimitive.objectId, primitivesOfObject);
            }

            primitivesOfObject.add(iterationPrimitive);
        }

        return objectIdToRetinaPrimitivesMap;
    }

    public static boolean isNeightborhoodPixelSet(Vector2d<Integer> position, IMap2d<Boolean> image) {
        Vector2d<Integer> min, max;

        final int SEARCHRADIUS = HardParameters.ProcessE.NEIGHTBORHOODSEARCHRADIUS;

        min = new Vector2d<>(Math.max(0, position.x - SEARCHRADIUS), Math.max(0, position.y - SEARCHRADIUS));
        max = new Vector2d<>(Math.min(image.getWidth()-1, position.x + SEARCHRADIUS), Math.min(image.getLength()-1, position.y + SEARCHRADIUS));

        for( int iy = min.y; iy <= max.y; iy++ ) {
            for( int ix = min.x; ix <= max.x; ix++ ) {
                if( image.readAt(ix, iy) ) {
                    return true;
                }
            }
        }

        return false;
    }
}
