package ptrman.bpsolver.RetinaToWorkspaceTranslator;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.SpatialAcceleration;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.Coderack;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.CodeletLtmLookup;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.Intersection;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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
    public List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, BpSolver bpSolver) {
        ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;
        HashMap<Integer, PlatonicPrimitiveInstanceNode> objectNodesByGroupId;
        ArrayList<Node> resultNodes;
        
        retinaObjectsWithAssociatedPoints = new ArrayList<>();
        
        for( RetinaPrimitive iterationPrimitive : primitives ) {
            retinaObjectsWithAssociatedPoints.add(associatePointsToRetinaPrimitive(iterationPrimitive));
        }
        
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints = createAndPropagateRetinaLevelObjects(retinaObjectsWithAssociatedPoints);
        ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints = createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(groupsOfRetinaObjectsWithAssociatedPoints, bpSolver.coderack, bpSolver.network, bpSolver.networkHandles, bpSolver.codeletLtmLookup, bpSolver, bpSolver.getImageSizeAsFloat());
        resultNodes = getNodesOfNodeAndGroupOfRetinaObject(objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints);
        return resultNodes;
    }
    
    
    
    private static ArrayList<Node> getNodesOfNodeAndGroupOfRetinaObject(ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints) {
        ArrayList<Node> resultArray;
        
        resultArray = new ArrayList<>();
        
        for( ObjectNodeWithGroup iterationObjectNodeWithGroup : objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints ) {
            resultArray.add(iterationObjectNodeWithGroup.objectNode);
        }
        
        return resultArray;
    }
    
    private ArrayList<ObjectNodeWithGroup> createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup, BpSolver bpSolver, Vector2d<Float> imageSize) {
        ArrayList<ObjectNodeWithGroup> resultObjectNodesWithGroup;
        
        resultObjectNodesWithGroup = new ArrayList<>();
        
        for( GroupOfRetinaObjectWithAssociatedPoints iterationGroupOfRetinaObjectWithAssociatedPoints : groupsOfRetinaObjectsWithAssociatedPoints ) {
            PlatonicPrimitiveInstanceNode objectNode;
            ObjectNodeWithGroup objectNodeWithGroup;
            
            objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, objectNode, coderack, network, networkHandles, codeletLtmLookup);
            createAndLinkAnglePointsAndLink(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, coderack, network, networkHandles, codeletLtmLookup, bpSolver, imageSize);
            
            objectNodeWithGroup = new ObjectNodeWithGroup();
            objectNodeWithGroup.groupOfRetinaObjects = iterationGroupOfRetinaObjectWithAssociatedPoints;
            objectNodeWithGroup.objectNode = objectNode;
            
            resultObjectNodesWithGroup.add(objectNodeWithGroup);
        }
        
        return resultObjectNodesWithGroup;
    }
    
    
    private void createAndLinkAnglePointsAndLink(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup, BpSolver bpSolver, Vector2d<Float> imageSize) {
        SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects;
        
        // TODO< hard parameters >
        final int GRIDCOUNTX = 10;
        final int GRIDCOUNTY = 10;
        
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<>(GRIDCOUNTX, GRIDCOUNTY, imageSize.x, imageSize.y);
        
        storeRetinaObjectWithAssocIntoMap(arrayOfRetinaObjectWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, arrayOfRetinaObjectWithAssociatedPoints);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        AbstractTranslatorStrategy.createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, bpSolver);
    }
    
    private static List<RetinaPrimitive> getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints) {
        List<RetinaPrimitive> resultRetinaPrimitives;
        
        resultRetinaPrimitives = new ArrayList<>();
        
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints ) {
            resultRetinaPrimitives.add(iterationRetinaObject.primitive);
        }
        
        return resultRetinaPrimitives;
    }
    
    /**
     * 
     * stores all intersections into a spatial acceleration structure
     *  
     */
    private void bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints) {
        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(arrayOfRetinaObjectWithAssociatedPoints));
    }
    
    private void bundleAllIntersections(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, List<RetinaPrimitive> listOfRetinaPrimitives) {
        for( RetinaPrimitive iterationRetinaPrimitive : listOfRetinaPrimitives ) {
            List<Intersection> intersections;
            
            intersections = iterationRetinaPrimitive.getIntersections();
            
            // we store the intersectionposition and the intersectionpartners
            
            for( Intersection iterationIntersection : intersections ) {
                final List<SpatialAcceleration<Crosspoint>.Element> crosspointsAtPosition = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getElementsNearPoint(iterationIntersection.intersectionPosition, 1000.0f /* TODO const */);
                
                if( crosspointsAtPosition.isEmpty() ) {
                    SpatialAcceleration<Crosspoint>.Element createdCrosspointElement;
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    
                    createdCrosspointElement = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.new Element();
                    createdCrosspointElement.data = new Crosspoint();
                    createdCrosspointElement.data.position = iterationIntersection.intersectionPosition;
                    createdCrosspointElement.position = iterationIntersection.intersectionPosition;
                    
                    RetinaPrimitive x = iterationIntersection.p0.primitive;
                    Set<RetinaPrimitive> y = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.keySet();
                    
                    Object[] array = y.toArray();
                    
                    for( int i = 0; i < array.length; i++ ) {
                        System.out.println(System.identityHashCode(array[i]));
                    }
                    
                    
                    System.out.println("searched " + System.identityHashCode(iterationIntersection.p0.primitive));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    // we can have intersectionpartners which are not inside
                    // seems to be a bug in clustering or something fishy is going on
                    // TODO< investigate and add here a assert that the assoc can't be null >
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null ) {
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                    }
                    
                    
                    
                    
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null ) {
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                    }
                    
                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                }
                else {
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    SpatialAcceleration<Crosspoint>.Element nearestCrosspointElement;
                    
                    nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, iterationIntersection.intersectionPosition);
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p0.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null ) {
                        if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) ) {
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p0.intersectionEndpointType));
                        }
                    }
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.p1.primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null ) {
                        if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) ) {
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.p1.intersectionEndpointType));
                        }
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
    
    private void createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup) {
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints ) {
            createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(iterationRetinaObject, objectNode, coderack, network, networkHandles, codeletLtmLookup);
        }
    }
    
    private static void createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup) {
        PlatonicPrimitiveInstanceNode createdPlatonicInstanceNodeForRetinaObject;
        Link createdForwardLink, createdBackwardLink;
        
        createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaObject.primitive, networkHandles);
        iterationRetinaObject.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;
        
        // linkage
        createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdPlatonicInstanceNodeForRetinaObject);
        objectNode.outgoingLinks.add(createdForwardLink);

        createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
        createdPlatonicInstanceNodeForRetinaObject.outgoingLinks.add(createdBackwardLink);

        // add all codelet's of it
        codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);

    }
    
    private ArrayList<GroupOfRetinaObjectWithAssociatedPoints> createAndPropagateRetinaLevelObjects(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints) {
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groups;
        int retinaObjectI;
        
        groups = new ArrayList<>();
        
        groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(retinaObjectsWithAssociatedPoints.get(0)));
        
        // we first try to cluster as many RetinaObjects in a lear fashion, after this we try to combine these "mini clusters" to final large clusters
        
        for( retinaObjectI = 1; retinaObjectI < retinaObjectsWithAssociatedPoints.size(); retinaObjectI++ ) {
            RetinaObjectWithAssociatedPointsAndWorkspaceNode currentRetinaObject;
            boolean wasIncludedInCluster;
            
            
            currentRetinaObject = retinaObjectsWithAssociatedPoints.get(retinaObjectI);
            
            
            wasIncludedInCluster = false;
            
            for( GroupOfRetinaObjectWithAssociatedPoints iterationGroup : groups ) {
                if( iterationGroup.canBeIncludedInCluster(currentRetinaObject) ) {
                    wasIncludedInCluster = true;
                    iterationGroup.arrayOfRetinaObjectWithAssociatedPoints.add(currentRetinaObject);
                    break;
                }
            }
            
            if( !wasIncludedInCluster ) {
                groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(currentRetinaObject));
            }
            
        }
        
        
        
        // cluster
        int lowerI, upperI;

        repeatSearch:
        for( lowerI = 0; lowerI < groups.size(); lowerI++ ) {
            for( upperI = lowerI+1; upperI < groups.size(); upperI++ ) {
                GroupOfRetinaObjectWithAssociatedPoints lower, upper;

                lower = groups.get(lowerI);
                upper = groups.get(upperI);

                if( GroupOfRetinaObjectWithAssociatedPoints.shareCommonPoint(lower, upper) ) {
                    lower.append(upper);
                    groups.remove(upperI);
                    
                    break repeatSearch;
                }
            }
        }
        
        return groups;
    }
    
    
    private static boolean doRetinaObjectsShareCommonPoints(RetinaObjectWithAssociatedPointsAndWorkspaceNode a, RetinaObjectWithAssociatedPointsAndWorkspaceNode b) {
        return getRetinaObjectSharedPoints(a, b, COMMONPOINTSMAXIMALDISTANCE).size() != 0;
    }
    
    private static ArrayList<IndexTuple> getRetinaObjectSharedPoints(RetinaObjectWithAssociatedPointsAndWorkspaceNode a, RetinaObjectWithAssociatedPointsAndWorkspaceNode b, float maximalDistance) {
        ArrayList<IndexTuple> resultIndexTuples;
        int Ia, Ib;
        
        Assert.Assert(!a.equals(b), "must be not the same");
        
        resultIndexTuples = new ArrayList<>();
        
        Ia = 0;
        
        for( ArrayRealVector pointFromA : a.pointPositions ) {
            Ib = 0;
            
            for( ArrayRealVector pointFromB : b.pointPositions ) {
                final double distance = pointFromA.getDistance(pointFromB);
                if( distance < maximalDistance) {
                    resultIndexTuples.add(new IndexTuple(Ia, Ib));
                }
                
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
        public ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints = new ArrayList<>();
        
        public boolean canBeIncludedInCluster(RetinaObjectWithAssociatedPointsAndWorkspaceNode candidate) {
            for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints ) {
                if( doRetinaObjectsShareCommonPoints(iterationRetinaObject, candidate) ) {
                    return true;
                }
            }
            
            return false;
        }
        
        public static GroupOfRetinaObjectWithAssociatedPoints createFromSingleRetinaObject(RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObject) {
            GroupOfRetinaObjectWithAssociatedPoints result;
            
            result = new GroupOfRetinaObjectWithAssociatedPoints();
            result.arrayOfRetinaObjectWithAssociatedPoints.add(retinaObject);
            return result;
        }
        
        public static boolean shareCommonPoint(GroupOfRetinaObjectWithAssociatedPoints a, GroupOfRetinaObjectWithAssociatedPoints b) {
            for( RetinaObjectWithAssociatedPointsAndWorkspaceNode outerRetinaObjectWithAssosciatedPoints : a.arrayOfRetinaObjectWithAssociatedPoints ) {
                for( RetinaObjectWithAssociatedPointsAndWorkspaceNode innerRetinaObjectWithAssosciatedPoints : b.arrayOfRetinaObjectWithAssociatedPoints ) {
                    if( PointProximityStrategy.doRetinaObjectsShareCommonPoints(outerRetinaObjectWithAssosciatedPoints, innerRetinaObjectWithAssosciatedPoints) ) {
                        return true;
                    }
                }
            }
            
            return false;
        }

        private void append(GroupOfRetinaObjectWithAssociatedPoints appendix) {
            arrayOfRetinaObjectWithAssociatedPoints.addAll(appendix.arrayOfRetinaObjectWithAssociatedPoints);
        }
    }
    
    
    
    private static class IndexTuple {
        public IndexTuple(int a, int b)
        {
            values = new int[]{a, b};
        }
        
        public int[] values;
    }
    
    
    
    // TODO< move to hard parameters >
    private static final float COMMONPOINTSMAXIMALDISTANCE = 10.0f;
}
