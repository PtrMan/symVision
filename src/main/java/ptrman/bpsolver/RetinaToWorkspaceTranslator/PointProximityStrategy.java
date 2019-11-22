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
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.Coderack;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.CodeletLtmLookup;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.RetinaPrimitive;

import java.util.*;
import java.util.stream.Collectors;

// BUGS
// BUG 0001
//     because of the invalid handling of some things in the edge handling code/object custering code
//     leads to invalid edges/Angles

/**
 * Strategy for the Retina to Workspace translation which works based on point proximity
 * 
 */
public class PointProximityStrategy extends AbstractTranslatorStrategy {
    /**
     * 
     * \param lines
     * \param network
     * \return the node which is the object node 
     */
    @Override
    public List<Node> createObjectsFromRetinaPrimitives(final List<RetinaPrimitive> primitives, final Solver bpSolver) {
        HashMap<Integer, PlatonicPrimitiveInstanceNode> objectNodesByGroupId;

        final var retinaObjectsWithAssociatedPoints = primitives.stream().map(AbstractTranslatorStrategy::associatePointsToRetinaPrimitive).collect(Collectors.toCollection(ArrayList::new));

        final var groupsOfRetinaObjectsWithAssociatedPoints = createAndPropagateRetinaLevelObjects(retinaObjectsWithAssociatedPoints);
        final var objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints = createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(groupsOfRetinaObjectsWithAssociatedPoints, bpSolver.coderack, bpSolver.network, bpSolver.networkHandles, bpSolver.codeletLtmLookup, bpSolver, bpSolver.getImageSizeAsFloat());
        final var resultNodes = getNodesOfNodeAndGroupOfRetinaObject(objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints);
        return resultNodes;
    }
    
    
    
    private static ArrayList<Node> getNodesOfNodeAndGroupOfRetinaObject(final Iterable<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints) {

        final var resultArray = new ArrayList<Node>();
        
        for( final var iterationObjectNodeWithGroup : objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints )
            resultArray.add(iterationObjectNodeWithGroup.objectNode);
        
        return resultArray;
    }
    
    private ArrayList<ObjectNodeWithGroup> createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(final Iterable<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints, final Coderack coderack, final Network network, final NetworkHandles networkHandles, final CodeletLtmLookup codeletLtmLookup, final Solver bpSolver, final Vector2d<Float> imageSize) {

        final var resultObjectNodesWithGroup = new ArrayList<ObjectNodeWithGroup>();
        
        for( final var iterationGroupOfRetinaObjectWithAssociatedPoints : groupsOfRetinaObjectsWithAssociatedPoints ) {

            final var objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, objectNode, coderack, network, networkHandles, codeletLtmLookup);
            createAndLinkAnglePointsAndLink(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, coderack, network, networkHandles, codeletLtmLookup, bpSolver, imageSize);

            final var objectNodeWithGroup = new ObjectNodeWithGroup();
            objectNodeWithGroup.groupOfRetinaObjects = iterationGroupOfRetinaObjectWithAssociatedPoints;
            objectNodeWithGroup.objectNode = objectNode;
            
            resultObjectNodesWithGroup.add(objectNodeWithGroup);
        }
        
        return resultObjectNodesWithGroup;
    }
    
    
    private void createAndLinkAnglePointsAndLink(final Iterable<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, final Coderack coderack, final Network network, final NetworkHandles networkHandles, final CodeletLtmLookup codeletLtmLookup, final Solver bpSolver, final Vector2d<Float> imageSize) {

        // TODO< hard parameters >
        final var GRIDCOUNTX = 10;
        final var GRIDCOUNTY = 10;

        final var spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<>(GRIDCOUNTX, GRIDCOUNTY, imageSize.x, imageSize.y);
        
        storeRetinaObjectWithAssocIntoMap(arrayOfRetinaObjectWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, arrayOfRetinaObjectWithAssociatedPoints);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        AbstractTranslatorStrategy.createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, bpSolver);
    }
    
    private static List<RetinaPrimitive> getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(final Iterable<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints) {

        final List<RetinaPrimitive> resultRetinaPrimitives = new ArrayList<>();
        
        for( final var iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
            resultRetinaPrimitives.add(iterationRetinaObject.primitive);
        
        return resultRetinaPrimitives;
    }
    
    /**
     * 
     * stores all intersections into a spatial acceleration structure
     *  
     */
    private void bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, final Iterable<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints) {
        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(arrayOfRetinaObjectWithAssociatedPoints));
    }
    
    private void bundleAllIntersections(final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, final Iterable<RetinaPrimitive> listOfRetinaPrimitives) {
        for( final var iterationRetinaPrimitive : listOfRetinaPrimitives ) {

            final var intersections = iterationRetinaPrimitive.getIntersections();
            
            // we store the intersectionposition and the intersectionpartners
            
            for( final var iterationIntersection : intersections ) {
                final var crosspointsAtPosition = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getElementsNearPoint(iterationIntersection.intersectionPosition, 1000.0f /* TODO const */);
                
                if( crosspointsAtPosition.isEmpty() ) {

                    final var createdCrosspointElement = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.new Element();
                    createdCrosspointElement.data = new Crosspoint();
                    createdCrosspointElement.data.position = iterationIntersection.intersectionPosition;
                    createdCrosspointElement.position = iterationIntersection.intersectionPosition;
                    
                    final var x = iterationIntersection.p0.primitive;
                    final var y = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.keySet();
                    
                    final var array = y.toArray();

                    for (final var o : array) System.out.println(System.identityHashCode(o));
                    
                    
                    System.out.println("searched " + System.identityHashCode(iterationIntersection.p0.primitive));

                    var retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    // we can have intersectionpartners which are not inside
                    // seems to be a bug in clustering or something fishy is going on
                    // TODO< investigate and add here a assert that the assoc can't be null >
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                    
                    
                    
                    
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                    
                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                }
                else {

                    final var nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, iterationIntersection.intersectionPosition);

                    var retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                        if (!nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc))
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                        if (!nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc))
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                }
            }
        }
    }
    
    private static SpatialAcceleration<Crosspoint>.Element getNearestCrosspointElement(final Iterable<SpatialAcceleration<Crosspoint>.Element> crosspointElements, final ArrayRealVector position) {
        SpatialAcceleration<Crosspoint>.Element nearestElement = null;
        var nearestDistance = Double.POSITIVE_INFINITY;
        
        for( final var iterationCrosspointElement : crosspointElements ) {
            final var distance = iterationCrosspointElement.position.getDistance(position);
            if( distance < nearestDistance ) {
                nearestDistance = distance;
                nearestElement = iterationCrosspointElement;
            }
        }
        
        return nearestElement;
    }
    
    private static void createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(final Iterable<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, final PlatonicPrimitiveInstanceNode objectNode, final Coderack coderack, final Network network, final NetworkHandles networkHandles, final CodeletLtmLookup codeletLtmLookup) {
        for( final var iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
            createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(iterationRetinaObject, objectNode, coderack, network, networkHandles, codeletLtmLookup);
    }
    
    private static void createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(final RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject, final PlatonicPrimitiveInstanceNode objectNode, final Coderack coderack, final Network network, final NetworkHandles networkHandles, final CodeletLtmLookup codeletLtmLookup) {

        final var createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaObject.primitive, networkHandles);
        iterationRetinaObject.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;
        
        // linkage
        final var createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdPlatonicInstanceNodeForRetinaObject);
        objectNode.out(createdForwardLink);

        final var createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
        createdPlatonicInstanceNodeForRetinaObject.out(createdBackwardLink);

        // add all codelet's of it
        codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);

    }
    
    private static ArrayList<GroupOfRetinaObjectWithAssociatedPoints> createAndPropagateRetinaLevelObjects(final ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints) {

        final var groups = new ArrayList<GroupOfRetinaObjectWithAssociatedPoints>();
        
        groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(retinaObjectsWithAssociatedPoints.get(0)));
        
        // we first try to cluster as many RetinaObjects in a lear fashion, after this we try to combine these "mini clusters" to final large clusters
        
        for(int retinaObjectI = 1; retinaObjectI < retinaObjectsWithAssociatedPoints.size(); retinaObjectI++ ) {


            final var currentRetinaObject = retinaObjectsWithAssociatedPoints.get(retinaObjectI);


            var wasIncludedInCluster = false;
            
            for( final var iterationGroup : groups )
                if (iterationGroup.canBeIncludedInCluster(currentRetinaObject)) {
                    wasIncludedInCluster = true;
                    iterationGroup.arrayOfRetinaObjectWithAssociatedPoints.add(currentRetinaObject);
                    break;
                }
            
            if( !wasIncludedInCluster )
                groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(currentRetinaObject));
            
        }
        
        
        
        // cluster

        repeatSearch:
        for(int lowerI = 0; lowerI < groups.size(); lowerI++ )
            for (int upperI = lowerI + 1; upperI < groups.size(); upperI++) {

                final var lower = groups.get(lowerI);
                final var upper = groups.get(upperI);

                if (GroupOfRetinaObjectWithAssociatedPoints.shareCommonPoint(lower, upper)) {
                    lower.append(upper);
                    groups.remove(upperI);

                    break repeatSearch;
                }
            }
        
        return groups;
    }
    
    
    private static boolean doRetinaObjectsShareCommonPoints(final RetinaObjectWithAssociatedPointsAndWorkspaceNode a, final RetinaObjectWithAssociatedPointsAndWorkspaceNode b) {
        return getRetinaObjectSharedPoints(a, b, COMMONPOINTSMAXIMALDISTANCE).size() != 0;
    }
    
    private static ArrayList<IndexTuple> getRetinaObjectSharedPoints(final RetinaObjectWithAssociatedPointsAndWorkspaceNode a, final RetinaObjectWithAssociatedPointsAndWorkspaceNode b, final float maximalDistance) {

        final boolean value = !a.equals(b);
        assert value : "ASSERT: " + "must be not the same";

        final var resultIndexTuples = new ArrayList<IndexTuple>();

        var Ia = 0;
        
        for( final var pointFromA : a.pointPositions ) {
            int Ib = 0;

            for( final var pointFromB : b.pointPositions ) {
                final var distance = pointFromA.getDistance(pointFromB);
                if( distance < maximalDistance) resultIndexTuples.add(new IndexTuple(Ia, Ib));
                
                Ib++;
            }
            
            Ia++;
        }
        
        return resultIndexTuples;
    }
    
    
    
    
    private static class ObjectNodeWithGroup {
        Node objectNode;
        GroupOfRetinaObjectWithAssociatedPoints groupOfRetinaObjects;
    }
    
    private static class GroupOfRetinaObjectWithAssociatedPoints {
        public final Collection<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints = new ArrayList<>();
        
        public boolean canBeIncludedInCluster(final RetinaObjectWithAssociatedPointsAndWorkspaceNode candidate) {

            return arrayOfRetinaObjectWithAssociatedPoints.stream().anyMatch(iterationRetinaObject -> doRetinaObjectsShareCommonPoints(iterationRetinaObject, candidate));
        }
        
        public static GroupOfRetinaObjectWithAssociatedPoints createFromSingleRetinaObject(final RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObject) {

            final var result = new GroupOfRetinaObjectWithAssociatedPoints();
            result.arrayOfRetinaObjectWithAssociatedPoints.add(retinaObject);
            return result;
        }
        
        public static boolean shareCommonPoint(final GroupOfRetinaObjectWithAssociatedPoints a, final GroupOfRetinaObjectWithAssociatedPoints b) {

            return a.arrayOfRetinaObjectWithAssociatedPoints.stream().anyMatch(outerRetinaObjectWithAssosciatedPoints -> b.arrayOfRetinaObjectWithAssociatedPoints.stream().anyMatch(innerRetinaObjectWithAssosciatedPoints -> PointProximityStrategy.doRetinaObjectsShareCommonPoints(outerRetinaObjectWithAssosciatedPoints, innerRetinaObjectWithAssosciatedPoints)));
        }

        private void append(final GroupOfRetinaObjectWithAssociatedPoints appendix) {
            arrayOfRetinaObjectWithAssociatedPoints.addAll(appendix.arrayOfRetinaObjectWithAssociatedPoints);
        }
    }
    
    
    
    private static class IndexTuple {
        public IndexTuple(final int a, final int b)
        {
            values = new int[]{a, b};
        }
        
        public final int[] values;
    }
    
    
    
    // TODO< move to hard parameters >
    private static final float COMMONPOINTSMAXIMALDISTANCE = 10.0f;
}
