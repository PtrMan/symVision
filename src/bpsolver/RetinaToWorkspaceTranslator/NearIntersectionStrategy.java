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
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import misc.Assert;

import java.util.*;

import static Datastructures.Vector2d.FloatHelper.getLength;
import static Datastructures.Vector2d.FloatHelper.sub;

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

        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap = createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(primitives, network, networkHandles, coderack, codeletLtmLookup);

        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, primitives);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, coderack, network, networkHandles, codeletLtmLookup);
        
        //retinaObjectsWithAssociatedPoints = convertPrimitivesToRetinaObjectsWithAssoc(primitives);
        //storeRetinaObjectWithAssocIntoMap(retinaObjectsWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        
        
        // NOTE< does hashmap work? >
        remainingRetinaObjects = new HashMap<>();
        
        resetAllMarkings(primitives);
        
        // algorithm
        
        // ...
        storeAllRetinaPrimitivesInMap(primitives, remainingRetinaObjects);
        
        // then pick one at random and put connected (assert unmarked) retinaObjects into the same object and put them out of the map
        // repeat this until no remaining retina object is in the map
        
        return pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(remainingRetinaObjects, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap, random, network, networkHandles, coderack, codeletLtmLookup);
    }

    private Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(ArrayList<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
    {
        Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> resultMap;

        resultMap = new HashMap<>();

        for( RetinaPrimitive iterationRetinaPrimitive : primitives )
        {
            RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssocPointsAndWorkspace;
            PlatonicPrimitiveInstanceNode createdPlatonicInstanceNodeForRetinaObject;

            createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, networkHandles);

            retinaObjectWithAssocPointsAndWorkspace = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(iterationRetinaPrimitive);
            retinaObjectWithAssocPointsAndWorkspace.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;

            // add all codelet's of it
            codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);
            
            resultMap.put(iterationRetinaPrimitive, retinaObjectWithAssocPointsAndWorkspace);
        }

        return resultMap;
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
        Object[] array;
        int index;
        
        array = (Object[])map.keySet().toArray();
        
        index = random.nextInt(array.length);
        return (RetinaPrimitive)array[index];
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
    

    private List<Node> pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(Map<RetinaPrimitive, Boolean> map, Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitveToRetinaObjectWithAssocMap, Random random, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
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
                    Node nodeForRetinaPrimitive;
                    Link createdForwardLink, createdBackwardLink;

                    nodeForRetinaPrimitive = primitveToRetinaObjectWithAssocMap.get(iterationPrimitive).workspaceNode;

                    // linkage
                    createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive);
                    objectNode.outgoingLinks.add(createdForwardLink);

                    createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                    nodeForRetinaPrimitive.outgoingLinks.add(createdBackwardLink);
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
    
    private void bundleAllIntersections(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, List<RetinaPrimitive> listOfRetinaPrimitives)
    {
        for( RetinaPrimitive iterationRetinaPrimitive : listOfRetinaPrimitives )
        {
            List<Intersection> intersections;
            
            intersections = iterationRetinaPrimitive.getIntersections();
            
            // we store the intersectionposition and the intersectionpartners
            
            for( Intersection iterationIntersection : intersections )
            {
                ArrayList<SpatialAcceleration<Crosspoint>.Element> crosspointsAtPosition;
                
                crosspointsAtPosition = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getElementsNearPoint(Vector2d.ConverterHelper.convertIntVectorToFloat(iterationIntersection.intersectionPosition), 1000.0f /* TODO const */);
                
                if( crosspointsAtPosition.isEmpty() )
                {
                    SpatialAcceleration<Crosspoint>.Element createdCrosspointElement;
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    
                    createdCrosspointElement = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.new Element();
                    createdCrosspointElement.data = new Crosspoint();
                    createdCrosspointElement.data.position = Vector2d.ConverterHelper.convertIntVectorToFloat(iterationIntersection.intersectionPosition);
                    createdCrosspointElement.position = Vector2d.ConverterHelper.convertIntVectorToFloat(iterationIntersection.intersectionPosition);
                    
                    RetinaPrimitive x = iterationIntersection.partners[0].primitive;
                    Set<RetinaPrimitive> y = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.keySet();
                    
                    Object[] array = y.toArray();
                    
                    for( int i = 0; i < array.length; i++ )
                    {
                        System.out.println(System.identityHashCode(array[i]));
                    }
                    
                    
                    System.out.println("searched " + System.identityHashCode(iterationIntersection.partners[0].primitive));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[0].primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[1].intersectionEndpointType));
                    
                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                }
                else
                {
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    SpatialAcceleration<Crosspoint>.Element nearestCrosspointElement;
                    
                    nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, Vector2d.ConverterHelper.convertIntVectorToFloat(iterationIntersection.intersectionPosition));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[0].primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");

                    if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) )
                    {
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                    }
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    
                    if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) )
                    {
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[1].intersectionEndpointType));
                    }
                    
                }
            }
        }
    }
    
    private static SpatialAcceleration<Crosspoint>.Element getNearestCrosspointElement(ArrayList<SpatialAcceleration<Crosspoint>.Element> crosspointElements, Vector2d<Float> position)
    {
        SpatialAcceleration<Crosspoint>.Element nearestElement;
        float nearestDistance;
        
        nearestElement = crosspointElements.get(0);
        nearestDistance = getLength(sub(crosspointElements.get(0).position, position));
        
        for( SpatialAcceleration<Crosspoint>.Element iterationCrosspointElement : crosspointElements )
        {
            float distance;
            
            distance = getLength(sub(iterationCrosspointElement.position, position));
            
            if( distance < nearestDistance )
            {
                nearestDistance = distance;
                nearestElement = iterationCrosspointElement;
            }
        }
        
        return nearestElement;
    }
    
    private Random random = new Random();

}
