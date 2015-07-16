package ptrman.levels.retina;

import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.Deque;
import java.util.List;

import static ptrman.bpsolver.Helper.createMapByObjectIdsFromListOfRetinaPrimitives;
import static ptrman.bpsolver.Helper.isNeightborhoodPixelSet;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;

/**
 * finds line intersections
 * 
 */
public class ProcessE {
    // TODO< sort out only the line detectors or make sure only linedetectors get in, remove asserts if its made sure >

    public void process(List<RetinaPrimitive> lineDetectors, IMap2d<Boolean> image) {
        // we examine ALL possible intersections of all lines
        // this is only possible if we have the whole image at an instance
        // we assume here that this is the case

        IntObjectHashMap<Deque<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(lineDetectors);

        // detect line intersections only per object
        for( Deque<RetinaPrimitive> primitivesOfObject : objectIdToRetinaPrimitivesMap.values() ) {
            FindIntersectionOfLineDetectorsWithDifferentObjectIds(primitivesOfObject, image);
        }
    }

    private static void FindIntersectionOfLineDetectorsWithDifferentObjectIds(Deque<RetinaPrimitive> lineDetectors, IMap2d<Boolean> image) {
        for (RetinaPrimitive lowLinePrimitive : lineDetectors) {
            Assert.Assert(lowLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");

            for (RetinaPrimitive highLinePrimitive : lineDetectors) {

                Assert.Assert(lowLinePrimitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                Assert.Assert(highLinePrimitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                Assert.Assert(highLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");

                if( highLinePrimitive == lowLinePrimitive ) {
                    continue;
                }

                if( lowLinePrimitive.objectId != highLinePrimitive.objectId ) {
                    continue;
                }

                ArrayRealVector intersectionPosition = intersectLineDetectors(lowLinePrimitive.line, highLinePrimitive.line);
                
                if( intersectionPosition == null ) {
                    continue;
                }
                
                if( !image.inBounds(arrayRealVectorToInteger(intersectionPosition, ArrayRealVectorHelper.EnumRoundMode.DOWN)) ) {
                    continue;
                }
                
                // examine neighborhood of intersection position
                if( !isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPosition, ArrayRealVectorHelper.EnumRoundMode.DOWN), image) ) {
                    continue;
                }

                // create entry and register stuff ...
                // TODO< register it on the line itself? >
                Intersection createdIntersection = new Intersection(intersectionPosition,
                        new Intersection.IntersectionPartner(lowLinePrimitive, lowLinePrimitive.line.getIntersectionEndpoint(intersectionPosition)),
                        new Intersection.IntersectionPartner(highLinePrimitive, highLinePrimitive.line.getIntersectionEndpoint(intersectionPosition))
                        );

                lowLinePrimitive.line.intersections.add(createdIntersection);
                highLinePrimitive.line.intersections.add(createdIntersection);
            }
        }
    }
    
    private static ArrayRealVector intersectLineDetectors(SingleLineDetector lineA, SingleLineDetector lineB) {
        ArrayRealVector intersectionPosition = SingleLineDetector.intersectLineDetectors(lineA, lineB);
        return intersectionPosition;
    }
}
