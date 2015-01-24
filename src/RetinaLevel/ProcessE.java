package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import bpsolver.HardParameters;
import java.util.ArrayList;

/**
 * finds line intersections
 * 
 */
public class ProcessE
{
    public void process(ArrayList<SingleLineDetector> lineDetectors, Map2d<Boolean> image)
    {
        // we examine ALL possible intersections of all lines
        // this is only possible if we have the whole image at an instance
        // we assume here that this is the case
        
        int outerI, innerI;
        
        for( outerI = 0; outerI < lineDetectors.size(); outerI++ )
        {
            for( innerI = 0; innerI < lineDetectors.size(); innerI++ )
            {
                if( innerI == outerI )
                {
                    continue;
                }
                
                Vector2d<Integer> intersectionPosition;
                SingleLineDetector lowLine;
                SingleLineDetector highLine;
                
                lowLine = lineDetectors.get(outerI);
                highLine = lineDetectors.get(innerI);
                
                intersectionPosition = intersectLineDetectors(lowLine, highLine);
                
                if( intersectionPosition == null )
                {
                    continue;
                }
                
                if( !isPointInsideImage(intersectionPosition, image) )
                {
                    continue;
                }
                
                // examine neighborhood of intersection position
                if( !isNeightborhoodPixelSet(intersectionPosition, image) )
                {
                    continue;
                }
                
                // create entry and register stuff ...
                // TODO< register it on the line itself? >
                Intersection createdIntersection = new Intersection();
                createdIntersection.intersectionPosition = intersectionPosition;
                createdIntersection.partners[0] = Intersection.IntersectionPartner.makeLine(lowLine);
                createdIntersection.partners[1] = Intersection.IntersectionPartner.makeLine(highLine);
                
                lowLine.intersections.add(createdIntersection);
                highLine.intersections.add(createdIntersection);
            }
        }
    }
    
    private static Vector2d<Integer> intersectLineDetectors(SingleLineDetector lineA, SingleLineDetector lineB)
    {
        Vector2d<Float> intersectionFloat;
        
        intersectionFloat = SingleLineDetector.intersectLineDetectors(lineA, lineB);
        if( intersectionFloat == null )
        {
            return null;
        }
        
        return new Vector2d<Integer>(Math.round(intersectionFloat.x), Math.round(intersectionFloat.y));
    }
    
    private static boolean isPointInsideImage(Vector2d<Integer> position, Map2d<Boolean> image)
    {
        return position.x >= 0 && position.x < image.getWidth() && position.y >= 0 && position.y < image.getLength();
    }
    
    private static boolean isNeightborhoodPixelSet(Vector2d<Integer> position, Map2d<Boolean> image)
    {
        Vector2d<Integer> min, max;
        int ix, iy;
        
        final int SEARCHRADIUS = HardParameters.ProcessE.NEIGHTBORHOODSEARCHRADIUS;
        
        min = new Vector2d<>(Math.max(0, position.x - SEARCHRADIUS), Math.max(0, position.y - SEARCHRADIUS));
        max = new Vector2d<>(Math.min(image.getWidth()-1, position.x + SEARCHRADIUS), Math.min(image.getLength()-1, position.y + SEARCHRADIUS));
        
        for( iy = min.y; iy <= max.y; iy++ )
        {
            for( ix = min.x; ix <= max.x; ix++ )
            {
                if( image.readAt(ix, iy) )
                {
                    return true;
                }
            }
        }
        
        return false;
    }
}
