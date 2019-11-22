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

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.IntStream;

/**
 *
 */
public enum Helper {
    ;

    public static IntObjectHashMap<Deque<RetinaPrimitive>> createMapByObjectIdsFromListOfRetinaPrimitives(final Collection<RetinaPrimitive> primitives) {

        final var objectIdToRetinaPrimitivesMap = new IntObjectHashMap<Deque<RetinaPrimitive>>(primitives.size());

        for( final var iterationPrimitive : primitives ) {

            assert iterationPrimitive.hasValidObjectId() : "ASSERT: " + "RetinaPrimitive has no valid objectId";

            var primitivesOfObject =
                objectIdToRetinaPrimitivesMap.get(iterationPrimitive.objectId);

            if( primitivesOfObject == null) {
                primitivesOfObject = new ConcurrentLinkedDeque<>();
                objectIdToRetinaPrimitivesMap.put(iterationPrimitive.objectId, primitivesOfObject);
            }

            primitivesOfObject.add(iterationPrimitive);
        }

        return objectIdToRetinaPrimitivesMap;
    }

    public static boolean isNeightborhoodPixelSet(final Vector2d<Integer> position, final IMap2d<Boolean> image) {

		final var SEARCHRADIUS = HardParameters.ProcessE.NEIGHTBORHOODSEARCHRADIUS;

		final var min = new Vector2d<Integer>(Math.max(0, position.x - SEARCHRADIUS), Math.max(0, position.y - SEARCHRADIUS));
		final var max = new Vector2d<Integer>(Math.min(image.getWidth() - 1, position.x + SEARCHRADIUS), Math.min(image.getLength() - 1, position.y + SEARCHRADIUS));

        final int miny = min.y;
        final int minx = min.x;
        final int maxy = max.y;
        final int maxx = max.x;

        return IntStream.rangeClosed(miny, maxy).anyMatch(iy -> IntStream.rangeClosed(minx, maxx).anyMatch(ix -> image.readAt(ix, iy)));
    }
}
