package bpsolver.RetinaToWorkspaceTranslator;

import Datastructures.SpatialAcceleration;
import Datastructures.Vector2d;
import FargGeneral.Coderack;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.RetinaPrimitive;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;
import static bpsolver.RetinaToWorkspaceTranslator.AbstractTranslatorStrategy.createPlatonicInstanceNodeForRetinaObject;
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
    public List<Node> createObjectsFromRetinaPrimitives(ArrayList<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize)
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
        
        resetAllMarkings(primitives);
        
        // algorithm
        
        // ...
        storeAllRetinaPrimitivesInMap(primitives, remainingRetinaObjects);
        
        // then pick one at random and put connected (assert unmarked) retinaObjects into the same object and put them out of the map
        // repeat this until no remaining retina object is in the map
        
        return pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(remainingRetinaObjects, random, network, networkHandles, coderack, codeletLtmLookup);
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
            
            // we do this because we are "done" with it
            map.remove(retinaPrimitiveFromOpenList);
            // mark it for the same reason
            retinaPrimitiveFromOpenList.marked = true;
            
            resultPrimitives.add(retinaPrimitiveFromOpenList);
            
            notYetMarkedConnectedRetinaPrimitives = getNotYetMarkedConnectedRetinaPrimitives(retinaPrimitiveFromOpenList);
            
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
        List<Intersection> allIntersections;
        List<RetinaPrimitive> unfilteredIntersectionPartners;
        List<RetinaPrimitive> filteredIntersectionPartners;
        
        unfilteredIntersectionPartners = new ArrayList<>();
        
        allIntersections = retinaPrimitiveFromOpenList.getIntersections();
        
        for( Intersection iterationIntersection : allIntersections )
        {
            unfilteredIntersectionPartners.add(iterationIntersection.getOtherPartner(retinaPrimitiveFromOpenList).primitive);
        }
        
        filteredIntersectionPartners = filterRetinaPrimitivesForUnmarkedOnes(unfilteredIntersectionPartners);
        return filteredIntersectionPartners;
    }
    
    private static List<RetinaPrimitive> filterRetinaPrimitivesForUnmarkedOnes(List<RetinaPrimitive> unfilteredIntersectionPartners)
    {
        List<RetinaPrimitive> result;
        
        result = new ArrayList<>();
        
        for( RetinaPrimitive iterationRetinaPrimitive : unfilteredIntersectionPartners )
        {
            if( !iterationRetinaPrimitive.marked )
            {
                result.add(iterationRetinaPrimitive);
            }
        }
        
        return result;
    }
    

    private List<Node> pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(Map<RetinaPrimitive, Boolean> map, Random random, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
    {
        List<Node> resultObjectNodes;
        
        resultObjectNodes = new ArrayList<>();
        
        for(;;)
        {
            List<RetinaPrimitive> retinaPrimitivesOfObject;
            
            if( map.size() == 0 )
            {
                return resultObjectNodes;
            }
            
            retinaPrimitivesOfObject = pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(map, random);
            
            PlatonicPrimitiveInstanceNode objectNode;
            
            objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            
            // create for the retinaPrimitives network nodes and link them
            {
                for( RetinaPrimitive iterationPrimitive : retinaPrimitivesOfObject )
                {
                    PlatonicPrimitiveInstanceNode createdPlatonicInstanceNodeForRetinaObject;
                    Link createdForwardLink, createdBackwardLink;

                    createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationPrimitive, networkHandles);

                    // linkage
                    createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdPlatonicInstanceNodeForRetinaObject);
                    objectNode.outgoingLinks.add(createdForwardLink);

                    createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                    createdPlatonicInstanceNodeForRetinaObject.outgoingLinks.add(createdBackwardLink);

                    // add all codelet's of it
                    codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);
                }
            }
            
            resultObjectNodes.add(objectNode);
        }
    }
    

    private void resetAllMarkings(ArrayList<RetinaPrimitive> primitives)
    {
        for( RetinaPrimitive iterationPrimitive : primitives )
        {
            iterationPrimitive.marked = false;
        }
    }
    
    private Random random = new Random();

}
