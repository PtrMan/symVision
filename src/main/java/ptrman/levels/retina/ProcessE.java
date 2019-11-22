/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.math.ArrayRealVectorHelper;

import java.util.Collection;

import static ptrman.bpsolver.Helper.createMapByObjectIdsFromListOfRetinaPrimitives;
import static ptrman.bpsolver.Helper.isNeightborhoodPixelSet;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;

/**
 * finds line intersections
 * 
 */
public class ProcessE {
    // TODO< sort out only the line detectors or make sure only linedetectors get in, remove asserts if its made sure >

    public static void process(final Collection<RetinaPrimitive> lineDetectors, final IMap2d<Boolean> image) {
        // we examine ALL possible intersections of all lines
        // this is only possible if we have the whole image at an instance
        // we assume here that this is the case

        final var objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(lineDetectors);

        // detect line intersections only per object
        for( final var primitivesOfObject : objectIdToRetinaPrimitivesMap.values() )
            findIntersectionOfLineDetectors(primitivesOfObject, image);
    }

    private static void findIntersectionOfLineDetectors(final Iterable<RetinaPrimitive> lineDetectors, final IMap2d<Boolean> image) {
        //commented because we don't consider object id
        //Assert.Assert(lowLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");
        for (final var lowLinePrimitive : lineDetectors)
            for (final var highLinePrimitive : lineDetectors) {

                assert lowLinePrimitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "";
                assert highLinePrimitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "";
                //commented because we don't consider object id
                //Assert.Assert(highLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");

                if (highLinePrimitive == lowLinePrimitive) continue;
                //commented because we don't consider object id
                //if( lowLinePrimitive.objectId != highLinePrimitive.objectId ) {
                //    continue;
                //}

                final var intersectionPosition = intersectLineDetectors(lowLinePrimitive.line, highLinePrimitive.line);

                if (intersectionPosition == null) continue;

                if (!image.inBounds(arrayRealVectorToInteger(intersectionPosition, ArrayRealVectorHelper.EnumRoundMode.DOWN)))
                    continue;

                // examine neighborhood of intersection position
                if (!isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPosition, ArrayRealVectorHelper.EnumRoundMode.DOWN), image))
                    continue;

                // create entry and register stuff ...
                // TODO< register it on the line itself? >
                final var createdIntersection = new Intersection(intersectionPosition,
                        new Intersection.IntersectionPartner(lowLinePrimitive, lowLinePrimitive.line.getIntersectionEndpoint(intersectionPosition)),
                        new Intersection.IntersectionPartner(highLinePrimitive, highLinePrimitive.line.getIntersectionEndpoint(intersectionPosition))
                );

                lowLinePrimitive.line.intersections.add(createdIntersection);
                highLinePrimitive.line.intersections.add(createdIntersection);
            }
    }
    
    public static ArrayRealVector intersectLineDetectors(final SingleLineDetector lineA, final SingleLineDetector lineB) {
        final var intersectionPosition = SingleLineDetector.intersectLineDetectors(lineA, lineB);
        return intersectionPosition;
    }
}
