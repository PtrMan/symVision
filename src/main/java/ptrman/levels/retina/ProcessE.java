package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.misc.Assert;

import java.util.List;
import java.util.Map;

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

        Map<Integer, List<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(lineDetectors);

        // detect line intersections only per object
        for( List<RetinaPrimitive> primitivesOfObject : objectIdToRetinaPrimitivesMap.values() ) {
            FindIntersectionOfLineDetectorsWithDifferentObjectIds(primitivesOfObject, image);
        }
    }

    private static void FindIntersectionOfLineDetectorsWithDifferentObjectIds(List<RetinaPrimitive> lineDetectors, IMap2d<Boolean> image) {
        for( int outerI = 0; outerI < lineDetectors.size(); outerI++ ) {

            RetinaPrimitive lowLinePrimitive = lineDetectors.get(outerI);
            Assert.Assert(lowLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");

            for( int innerI = 0; innerI < lineDetectors.size(); innerI++ ) {
                if( innerI == outerI ) {
                    continue;
                }

                RetinaPrimitive highLinePrimitive;
                
                Assert.Assert(lineDetectors.get(outerI).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                Assert.Assert(lineDetectors.get(innerI).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                
                highLinePrimitive = lineDetectors.get(innerI);
                Assert.Assert(highLinePrimitive.hasValidObjectId(), "line detector RetinaPrimitive has no valid object id!");

                if( lowLinePrimitive.objectId != highLinePrimitive.objectId ) {
                    continue;
                }

                ArrayRealVector intersectionPosition = intersectLineDetectors(lowLinePrimitive.line, highLinePrimitive.line);
                
                if( intersectionPosition == null ) {
                    continue;
                }
                
                if( !image.inBounds(arrayRealVectorToInteger(intersectionPosition)) ) {
                    continue;
                }
                
                // examine neighborhood of intersection position
                if( !isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPosition), image) ) {
                    continue;
                }

                // create entry and register stuff ...
                // TODO< register it on the line itself? >
                Intersection createdIntersection = new Intersection();
                createdIntersection.intersectionPosition = intersectionPosition;
                createdIntersection.partners[0] = new Intersection.IntersectionPartner(lowLinePrimitive, lowLinePrimitive.line.getIntersectionEndpoint(intersectionPosition));
                createdIntersection.partners[1] = new Intersection.IntersectionPartner(highLinePrimitive, highLinePrimitive.line.getIntersectionEndpoint(intersectionPosition));
                
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
