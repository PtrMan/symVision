package ptrman.bpsolver;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Helper {
    public static Map<Integer, List<RetinaPrimitive>> createMapByObjectIdsFromListOfRetinaPrimitives(List<RetinaPrimitive> primitives) {
        Map<Integer, List<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = new HashMap<>();

        for( final RetinaPrimitive iterationPrimitive : primitives ) {
            Assert.Assert(iterationPrimitive.hasValidObjectId(), "RetinaPrimitive has no valid objectId");

            List<RetinaPrimitive> primitivesOfObject;
            if( objectIdToRetinaPrimitivesMap.containsKey(iterationPrimitive.objectId) ) {
                primitivesOfObject = objectIdToRetinaPrimitivesMap.get(iterationPrimitive.objectId);
            }
            else {
                primitivesOfObject = new ArrayList<>();
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
