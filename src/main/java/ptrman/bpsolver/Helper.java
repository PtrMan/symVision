/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver;

import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 *
 */
public enum Helper {
    ;

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

        final int miny = min.y;
        final int minx = min.x;
        final int maxy = max.y;
        final int maxx = max.x;

        for( int iy = miny; iy <= maxy; iy++ ) {
            for( int ix = minx; ix <= maxx; ix++ ) {
                if( image.readAt(ix, iy) ) {
                    return true;
                }
            }
        }

        return false;
    }
}
