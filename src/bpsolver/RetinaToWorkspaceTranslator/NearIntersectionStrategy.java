package bpsolver.RetinaToWorkspaceTranslator;

import Datastructures.SpatialAcceleration;
import Datastructures.Vector2d;
import FargGeneral.Coderack;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.RetinaPrimitive;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * implements a strategy which groups retina objects based on the intersections of retina objects
 * 
 */
public class NearIntersectionStrategy extends AbstractTranslatorStrategy
{
    @Override
    public ArrayList<Node> createObjectsFromRetinaPrimitives(ArrayList<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize)
    {
        SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects;
        List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;
        Map<RetinaPrimitive, Boolean> remainingRetinaObjects;
        
        // TODO< hard parameters >
        final int GRIDCOUNTX = 10;
        final int GRIDCOUNTY = 10;
        
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<>(GRIDCOUNTX, GRIDCOUNTY, imageSize.x, imageSize.y);
        
        retinaObjectsWithAssociatedPoints = convertPrimitivesToRetinaObjectsWithAssoc(primitives);
        storeRetinaObjectWithAssocIntoMap(retinaObjectsWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        
        
        // NOTE< does hashmap work? >
        remainingRetinaObjects = new HashMap<>();
        
        // TODO< reset markings >
        
        // algorithm
        
        // ...
        storeAllRetinaPrimitivesInMap(primitives, remainingRetinaObjects);
        
        // then pick one at random and put connected (assert unmarked) retinaObjects into the same object and put them out of the map
        // repeat this until no remaining retina object is in the map
        
        
        
        // TODO< return the objects >
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> convertPrimitivesToRetinaObjectsWithAssoc(List<RetinaPrimitive> primitives)
    {
        ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;
        
        retinaObjectsWithAssociatedPoints = new ArrayList<>();
        
        for( RetinaPrimitive iterationPrimitive : primitives )
        {
            retinaObjectsWithAssociatedPoints.add(associatePointsToRetinaPrimitive(iterationPrimitive));
        }
        
        return retinaObjectsWithAssociatedPoints;
    }
    
    private void storeAllRetinaPrimitivesInMap(List<RetinaPrimitive> primitives, Map<RetinaPrimitive, Boolean> map)
    {
        for( RetinaPrimitive iterationPrimitive : primitives )
        {
            map.put(iterationPrimitive, Boolean.FALSE);
        }
    }
    
    private List<RetinaPrimitive> pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(Map<RetinaPrimitive, Boolean> map, Random random)
    {
        List<RetinaPrimitive> resultPrimitives;
        List<RetinaPrimitive> openList;
        
        resultPrimitives = new ArrayList<>();
        openList = new ArrayList<>();
        
        {
            RetinaPrimitive chosenStartRetinaPrimitive;
            
            chosenStartRetinaPrimitive = pickRandomRetinaPrimitiveFromMap(map, random);
            map.remove(chosenStartRetinaPrimitive);
            openList.add(chosenStartRetinaPrimitive);
        }
        
        for(;;)
        {
            RetinaPrimitive retinaPrimitiveFromOpenList;
            List<RetinaPrimitive> notYetMarkedConnectedRetinaPrimitives;
            
            if( openList.size() == 0 )
            {
                break;
            }
            
            retinaPrimitiveFromOpenList = openList.get(openList.size()-1);
            openList.remove(openList.size()-1);
            
            resultPrimitives.add(retinaPrimitiveFromOpenList);
            
            notYetMarkedConnectedRetinaPrimitives = getNotYetMarkedConnectedRetinaPrimitives(retinaPrimitiveFromOpenList);
            
            // TODO< mark them >
            
            openList.addAll(notYetMarkedConnectedRetinaPrimitives);
        }
        
        return resultPrimitives;
    }
    
    private static RetinaPrimitive pickRandomRetinaPrimitiveFromMap(Map<RetinaPrimitive, Boolean> map, Random random)
    {
        RetinaPrimitive[] array;
        int index;
        
        array = (RetinaPrimitive[])map.keySet().toArray();
        
        index = random.nextInt(array.length);
        return array[index];
    }

    private static List<RetinaPrimitive> getNotYetMarkedConnectedRetinaPrimitives(RetinaPrimitive retinaPrimitiveFromOpenList)
    {
        // TODO< get all connected objects and check if they are not marked, if so, add them to the resultlist >
        
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
