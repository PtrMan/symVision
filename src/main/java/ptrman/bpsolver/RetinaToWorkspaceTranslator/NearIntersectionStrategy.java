package ptrman.bpsolver.RetinaToWorkspaceTranslator;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.SpatialAcceleration;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.Intersection;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.*;

/**
 * implements a strategy which groups retina objects based on the intersections of retina objects
 * 
 */
public class NearIntersectionStrategy extends AbstractTranslatorStrategy {


    @Override
    public List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, Solver bpSolver) {
        SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects;
        List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;
        Map<RetinaPrimitive, Boolean> remainingRetinaObjects;
        
        // TODO< hard parameters >
        final int GRIDCOUNTX = 10;
        final int GRIDCOUNTY = 10;
        
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<>(GRIDCOUNTX, GRIDCOUNTY, bpSolver.getImageSize().x, bpSolver.getImageSize().y);

        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap = NearIntersectionStrategy.createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(primitives, bpSolver);

        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, primitives);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, bpSolver);
        
        //retinaObjectsWithAssociatedPoints = convertPrimitivesToRetinaObjectsWithAssoc(primitives);
        //storeRetinaObjectWithAssocIntoMap(retinaObjectsWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        
        
        // NOTE< does hashmap work? >
        remainingRetinaObjects = new HashMap<>();

        NearIntersectionStrategy.resetAllMarkings(primitives);
        
        // algorithm
        
        // ...
        storeAllRetinaPrimitivesInMap(primitives, remainingRetinaObjects);
        
        // then pick one at random and put connected (assert unmarked) retinaObjects into the same object and put them out of the map
        // repeat this until no remaining retina object is in the map
        
        return NearIntersectionStrategy.pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(remainingRetinaObjects, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap, random, bpSolver);
    }

    private static Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(List<RetinaPrimitive> primitives, Solver bpSolver) {
        Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> resultMap;

        resultMap = new HashMap<>();

        for( RetinaPrimitive iterationRetinaPrimitive : primitives ) {
            RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssocPointsAndWorkspace;
            PlatonicPrimitiveInstanceNode createdPlatonicInstanceNodeForRetinaObject;

            createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, bpSolver.networkHandles);

            retinaObjectWithAssocPointsAndWorkspace = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(iterationRetinaPrimitive);
            retinaObjectWithAssocPointsAndWorkspace.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;

            // add all codelet's of it
            bpSolver.codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, bpSolver.coderack, bpSolver.network, bpSolver.networkHandles);
            
            resultMap.put(iterationRetinaPrimitive, retinaObjectWithAssocPointsAndWorkspace);
        }

        return resultMap;
    }

    private List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> convertPrimitivesToRetinaObjectsWithAssoc(final List<RetinaPrimitive> primitives) {
        List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints = new ArrayList<>();
        
        for( RetinaPrimitive iterationPrimitive : primitives ) {
            retinaObjectsWithAssociatedPoints.add(associatePointsToRetinaPrimitive(iterationPrimitive));
        }
        
        return retinaObjectsWithAssociatedPoints;
    }
    
    private void storeAllRetinaPrimitivesInMap(List<RetinaPrimitive> primitives, Map<RetinaPrimitive, Boolean> map) {
        for( RetinaPrimitive iterationPrimitive : primitives ) {
            map.put(iterationPrimitive, Boolean.FALSE);
        }
    }
    
    private static List<RetinaPrimitive> pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(Map<RetinaPrimitive, Boolean> map, Random random) {
        List<RetinaPrimitive> resultPrimitives;
        List<RetinaPrimitive> openList;
        
        resultPrimitives = new ArrayList<>();
        openList = new ArrayList<>();
        
        {
            RetinaPrimitive chosenStartRetinaPrimitive;
            
            chosenStartRetinaPrimitive = pickRandomRetinaPrimitiveFromMap(map, random);
            openList.add(chosenStartRetinaPrimitive);
        }
        
        for(;;) {
            RetinaPrimitive retinaPrimitiveFromOpenList;
            List<RetinaPrimitive> notYetMarkedConnectedRetinaPrimitives;
            
            if( openList.size() == 0 ) {
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
    
    private static RetinaPrimitive pickRandomRetinaPrimitiveFromMap(Map<RetinaPrimitive, Boolean> map, Random random) {
        Object[] array;
        int index;
        
        array = map.keySet().toArray();
        
        index = random.nextInt(array.length);
        return (RetinaPrimitive)array[index];
    }

    private static List<RetinaPrimitive> getNotYetMarkedConnectedRetinaPrimitives(RetinaPrimitive retinaPrimitiveFromOpenList) {
        List<Intersection> allIntersections;
        List<RetinaPrimitive> unfilteredIntersectionPartners;
        List<RetinaPrimitive> filteredIntersectionPartners;
        
        unfilteredIntersectionPartners = new ArrayList<>();
        
        allIntersections = retinaPrimitiveFromOpenList.getIntersections();
        
        for( Intersection iterationIntersection : allIntersections ) {
            unfilteredIntersectionPartners.add(iterationIntersection.getOtherPartner(retinaPrimitiveFromOpenList).primitive);
        }
        
        filteredIntersectionPartners = filterRetinaPrimitivesForUnmarkedOnes(unfilteredIntersectionPartners);
        return filteredIntersectionPartners;
    }
    
    private static List<RetinaPrimitive> filterRetinaPrimitivesForUnmarkedOnes(List<RetinaPrimitive> unfilteredIntersectionPartners) {
        List<RetinaPrimitive> result;
        
        result = new ArrayList<>();
        
        for( RetinaPrimitive iterationRetinaPrimitive : unfilteredIntersectionPartners ) {
            if( !iterationRetinaPrimitive.marked ) {
                result.add(iterationRetinaPrimitive);
            }
        }
        
        return result;
    }
    

    private static List<Node> pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(Map<RetinaPrimitive, Boolean> map, Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitveToRetinaObjectWithAssocMap, Random random, Solver bpSolver) {
        List<Node> resultObjectNodes = new ArrayList<>();
        
        for(;;) {
            List<RetinaPrimitive> retinaPrimitivesOfObject;
            
            if( map.size() == 0 ) {
                return resultObjectNodes;
            }
            
            retinaPrimitivesOfObject = pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(map, random);
            
            PlatonicPrimitiveInstanceNode objectNode;
            
            objectNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.objectPlatonicPrimitiveNode);
            
            // create for the retinaPrimitives network nodes and link them
            {
                for( RetinaPrimitive iterationPrimitive : retinaPrimitivesOfObject ) {
                    Node nodeForRetinaPrimitive;
                    Link createdForwardLink, createdBackwardLink;

                    nodeForRetinaPrimitive = primitveToRetinaObjectWithAssocMap.get(iterationPrimitive).workspaceNode;

                    // linkage
                    createdForwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive);
                    objectNode.outgoingLinks.add(createdForwardLink);

                    createdBackwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                    nodeForRetinaPrimitive.outgoingLinks.add(createdBackwardLink);
                }
            }
            
            resultObjectNodes.add(objectNode);
        }
    }
    

    private static void resetAllMarkings(List<RetinaPrimitive> primitives) {
        for( RetinaPrimitive iterationPrimitive : primitives ) {
            iterationPrimitive.marked = false;
        }
    }
    
    private void bundleAllIntersections(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, List<RetinaPrimitive> listOfRetinaPrimitives) {
        for( RetinaPrimitive iterationRetinaPrimitive : listOfRetinaPrimitives ) {

            // we store the intersectionposition and the intersectionpartners

            final List<Intersection> intersections = iterationRetinaPrimitive.getIntersections();
            for( Intersection iterationIntersection : intersections ) {
                List<SpatialAcceleration<Crosspoint>.Element> crosspointsAtPosition = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getElementsNearPoint(iterationIntersection.intersectionPosition, 1000.0f /* TODO const */);
                
                if( crosspointsAtPosition.isEmpty() ) {
                    SpatialAcceleration<Crosspoint>.Element createdCrosspointElement;
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    
                    createdCrosspointElement = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.new Element();
                    createdCrosspointElement.data = new Crosspoint();
                    createdCrosspointElement.data.position = iterationIntersection.intersectionPosition;
                    createdCrosspointElement.position = iterationIntersection.intersectionPosition;



                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                    
                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                }
                else {
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;

                    SpatialAcceleration<Crosspoint>.Element nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, iterationIntersection.intersectionPosition);
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");

                    if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) ) {
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                    }
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    Assert.Assert(retinaObjectWithAssoc != null, "");
                    
                    if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) ) {
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                    }
                    
                }
            }
        }
    }
    
    private static SpatialAcceleration<Crosspoint>.Element getNearestCrosspointElement(final List<SpatialAcceleration<Crosspoint>.Element> crosspointElements, final ArrayRealVector position) {
        SpatialAcceleration<Crosspoint>.Element nearestElement = crosspointElements.get(0);
        double nearestDistance = crosspointElements.get(0).position.getDistance(position);

        for( SpatialAcceleration<Crosspoint>.Element iterationCrosspointElement : crosspointElements ) {
            final double distance = iterationCrosspointElement.position.getDistance(position);
            if( distance < nearestDistance ) {
                nearestDistance = distance;
                nearestElement = iterationCrosspointElement;
            }
        }
        
        return nearestElement;
    }
    
    private Random random = new Random();
}
