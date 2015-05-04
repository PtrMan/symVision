package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.misc.Assert;

import java.util.List;

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

        for( int outerI = 0; outerI < lineDetectors.size(); outerI++ ) {
            for( int innerI = 0; innerI < lineDetectors.size(); innerI++ ) {
                if( innerI == outerI ) {
                    continue;
                }

                RetinaPrimitive lowLinePrimitive;
                RetinaPrimitive highLinePrimitive;
                
                Assert.Assert(lineDetectors.get(outerI).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                Assert.Assert(lineDetectors.get(innerI).type == RetinaPrimitive.EnumType.LINESEGMENT, "");
                
                lowLinePrimitive = lineDetectors.get(outerI);
                highLinePrimitive = lineDetectors.get(innerI);

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
    
    // TODO< move into some helper function or integrate it into the spartial scheme or something >
    // public because its used in processG    
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
