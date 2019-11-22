/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.RetinaToWorkspaceTranslator;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.SpatialAcceleration;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.RetinaPrimitive;

import java.util.*;
import java.util.stream.Collectors;

/**
 * implements a strategy which groups retina objects based on the intersections of retina objects
 * 
 */
public class NearIntersectionStrategy extends AbstractTranslatorStrategy {


    @Override
    public List<Node> createObjectsFromRetinaPrimitives(final List<RetinaPrimitive> primitives, final Solver bpSolver) {
        List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;

        // TODO< hard parameters >
        final var GRIDCOUNTX = 10;
        final var GRIDCOUNTY = 10;

        final var spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<>(GRIDCOUNTX, GRIDCOUNTY, bpSolver.getImageSize().x, bpSolver.getImageSize().y);

        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap = NearIntersectionStrategy.createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(primitives, bpSolver);

        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, primitives);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, bpSolver);
        
        //retinaObjectsWithAssociatedPoints = convertPrimitivesToRetinaObjectsWithAssoc(primitives);
        //storeRetinaObjectWithAssocIntoMap(retinaObjectsWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        
        
        // NOTE< does hashmap work? >

        NearIntersectionStrategy.resetAllMarkings(primitives);
        
        // algorithm
        
        // ...
        final Map<RetinaPrimitive, Boolean> remainingRetinaObjects = new HashMap<>();
        storeAllRetinaPrimitivesInMap(primitives, remainingRetinaObjects);
        
        // then pick one at random and put connected (assert unmarked) retinaObjects into the same object and put them out of the map
        // repeat this until no remaining retina object is in the map
        
        return NearIntersectionStrategy.pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(remainingRetinaObjects, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap, random, bpSolver);
    }

    private static Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> createWorkspaceNodeAndRegisterCodeletsAndOutputAsMapFromRetinaPrimitives(final Iterable<RetinaPrimitive> primitives, final Solver bpSolver) {

        final Map<RetinaPrimitive, RetinaObjectWithAssociatedPointsAndWorkspaceNode> resultMap = new HashMap<>();

        for( final var iterationRetinaPrimitive : primitives ) {

            final var createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, bpSolver.networkHandles);

            final var retinaObjectWithAssocPointsAndWorkspace = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(iterationRetinaPrimitive);
            retinaObjectWithAssocPointsAndWorkspace.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;

            // add all codelet's of it
            bpSolver.codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, bpSolver.coderack, bpSolver.network, bpSolver.networkHandles);
            
            resultMap.put(iterationRetinaPrimitive, retinaObjectWithAssocPointsAndWorkspace);
        }

        return resultMap;
    }

    private static List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> convertPrimitivesToRetinaObjectsWithAssoc(final Iterable<RetinaPrimitive> primitives) {
        final List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints = new ArrayList<>();
        
        for( final var iterationPrimitive : primitives )
            retinaObjectsWithAssociatedPoints.add(associatePointsToRetinaPrimitive(iterationPrimitive));
        
        return retinaObjectsWithAssociatedPoints;
    }
    
    private static void storeAllRetinaPrimitivesInMap(final Iterable<RetinaPrimitive> primitives, final Map<RetinaPrimitive, Boolean> map) {
        for( final var iterationPrimitive : primitives ) map.put(iterationPrimitive, Boolean.FALSE);
    }
    
    private static List<RetinaPrimitive> pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(final Map<RetinaPrimitive, Boolean> map, final Random random) {

        final List<RetinaPrimitive> openList = new ArrayList<>();
        
        {

            final var chosenStartRetinaPrimitive = pickRandomRetinaPrimitiveFromMap(map, random);
            openList.add(chosenStartRetinaPrimitive);
        }

        final List<RetinaPrimitive> resultPrimitives = new ArrayList<>();
        while (true) {

            final var os = openList.size();

            if( os == 0 )
                break;


            final var retinaPrimitiveFromOpenList = openList.get(os - 1);
            openList.remove(os -1);
            
            // we do this because we are "done" with it
            map.remove(retinaPrimitiveFromOpenList);
            // mark it for the same reason
            retinaPrimitiveFromOpenList.marked = true;
            
            resultPrimitives.add(retinaPrimitiveFromOpenList);

            final var notYetMarkedConnectedRetinaPrimitives = getNotYetMarkedConnectedRetinaPrimitives(retinaPrimitiveFromOpenList);
            
            openList.addAll(notYetMarkedConnectedRetinaPrimitives);
        }

        return resultPrimitives;
    }
    
    private static RetinaPrimitive pickRandomRetinaPrimitiveFromMap(final Map<RetinaPrimitive, Boolean> map, final Random random) {

        final var array = map.keySet().toArray();

        final var index = random.nextInt(array.length);
        return (RetinaPrimitive)array[index];
    }

    private static List<RetinaPrimitive> getNotYetMarkedConnectedRetinaPrimitives(final RetinaPrimitive retinaPrimitiveFromOpenList) {

        final var allIntersections = retinaPrimitiveFromOpenList.getIntersections();

        final Collection<RetinaPrimitive> unfilteredIntersectionPartners = allIntersections.stream().map(iterationIntersection -> iterationIntersection.getOtherPartner(retinaPrimitiveFromOpenList).primitive).collect(Collectors.toList());

        final var filteredIntersectionPartners = filterRetinaPrimitivesForUnmarkedOnes(unfilteredIntersectionPartners);
        return filteredIntersectionPartners;
    }
    
    private static List<RetinaPrimitive> filterRetinaPrimitivesForUnmarkedOnes(final Iterable<RetinaPrimitive> unfilteredIntersectionPartners) {

        final List<RetinaPrimitive> result = new ArrayList<>();
        
        for( final var iterationRetinaPrimitive : unfilteredIntersectionPartners )
            if (!iterationRetinaPrimitive.marked) result.add(iterationRetinaPrimitive);
        
        return result;
    }
    

    private static List<Node> pickRetinaPrimitiveAtRandomUntilNoCandidateIsLeftAndReturnItAsObjects(final Map<RetinaPrimitive, Boolean> map, final Map<RetinaPrimitive,RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitveToRetinaObjectWithAssocMap, final Random random, final Solver bpSolver) {
        final List<Node> resultObjectNodes = new ArrayList<>();

        while (true) {

            if( map.size() == 0 ) return resultObjectNodes;

            final var retinaPrimitivesOfObject = pickRetinaPrimitiveAtRandomAndMarkAndRemoveConnectedPrimitivesAndRetunListOfPrimitives(map, random);

            final var objectNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.objectPlatonicPrimitiveNode);
            
            // create for the retinaPrimitives network nodes and link them
            {
                for( final var iterationPrimitive : retinaPrimitivesOfObject ) {

                    final var nodeForRetinaPrimitive = primitveToRetinaObjectWithAssocMap.get(iterationPrimitive).workspaceNode;

                    // linkage
                    //      forward
                    objectNode.out(bpSolver.network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive));
                    //      reverse
                    nodeForRetinaPrimitive.out(bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode));
                }
            }
            
            resultObjectNodes.add(objectNode);
        }
    }
    

    private static void resetAllMarkings(final Iterable<RetinaPrimitive> primitives) {
        for( final var iterationPrimitive : primitives ) iterationPrimitive.marked = false;
    }
    
    private void bundleAllIntersections(final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, final Iterable<RetinaPrimitive> listOfRetinaPrimitives) {
        // we store the intersectionposition and the intersectionpartners
        for( final var iterationRetinaPrimitive : listOfRetinaPrimitives )
            for (final var iterationIntersection : iterationRetinaPrimitive.getIntersections()) {
                final var crosspointsAtPosition = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getElementsNearPoint(iterationIntersection.intersectionPosition, 1000.0f /* TODO const */);

                if (crosspointsAtPosition.isEmpty()) {

                    final var createdCrosspointElement = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.new Element();
                    createdCrosspointElement.data = new Crosspoint();
                    createdCrosspointElement.data.position = iterationIntersection.intersectionPosition;
                    createdCrosspointElement.position = iterationIntersection.intersectionPosition;


                    var retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    assert retinaObjectWithAssoc != null : "ASSERT: " + "";
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));

                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    assert retinaObjectWithAssoc != null : "ASSERT: " + "";
                    createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));

                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                } else {

                    final var nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, iterationIntersection.intersectionPosition);

                    var retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    assert retinaObjectWithAssoc != null : "ASSERT: " + "";

                    if (!nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc))
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));

                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    assert retinaObjectWithAssoc != null : "ASSERT: " + "";

                    if (!nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc))
                        nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));

                }
            }
    }
    
    private static SpatialAcceleration<Crosspoint>.Element getNearestCrosspointElement(final List<SpatialAcceleration<Crosspoint>.Element> crosspointElements, final ArrayRealVector position) {
        var nearestElement = crosspointElements.get(0);
        var nearestDistance = crosspointElements.get(0).position.getDistance(position);

        for( final var iterationCrosspointElement : crosspointElements ) {
            final var distance = iterationCrosspointElement.position.getDistance(position);
            if( distance < nearestDistance ) {
                nearestDistance = distance;
                nearestElement = iterationCrosspointElement;
            }
        }
        
        return nearestElement;
    }
    
    private final Random random = new Random();
}
