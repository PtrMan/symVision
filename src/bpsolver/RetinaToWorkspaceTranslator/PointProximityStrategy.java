package bpsolver.RetinaToWorkspaceTranslator;

import Datastructures.SpatialAcceleration;
import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.getLength;
import static Datastructures.Vector2d.FloatHelper.sub;
import FargGeneral.Coderack;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.RetinaPrimitive;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import misc.Assert;

// BUGS
// BUG 0001
//     because of the invalid handling of some things in the edge handling code/object custering code
//     leads to invalid edges/Angles

/**
 * Strategy for the Retina to Workspace translation which works based on point proximity
 * 
 */
public class PointProximityStrategy extends AbstractTranslatorStrategy
{
    /**
     * 
     * \param lines
     * \param network
     * \return the node which is the object node 
     */
    public ArrayList<Node> createObjectsFromRetinaPrimitives(ArrayList<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize)
    {
        ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints;
        HashMap<Integer, PlatonicPrimitiveInstanceNode> objectNodesByGroupId;
        ArrayList<Node> resultNodes;
        
        retinaObjectsWithAssociatedPoints = new ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode>();
        
        for( RetinaPrimitive iterationPrimitive : primitives )
        {
            retinaObjectsWithAssociatedPoints.add(associatePointsToRetinaPrimitive(iterationPrimitive));
        }
        
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints = createAndPropagateRetinaLevelObjects(retinaObjectsWithAssociatedPoints);
        ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints = createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(groupsOfRetinaObjectsWithAssociatedPoints, coderack, network, networkHandles, codeletLtmLookup, imageSize);
        resultNodes = getNodesOfNodeAndGroupOfRetinaObject(objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints);
        return resultNodes;
    }
    
    
    
    private static ArrayList<Node> getNodesOfNodeAndGroupOfRetinaObject(ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints)
    {
        ArrayList<Node> resultArray;
        
        resultArray = new ArrayList<>();
        
        for( ObjectNodeWithGroup iterationObjectNodeWithGroup : objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints )
        {
            resultArray.add(iterationObjectNodeWithGroup.objectNode);
        }
        
        return resultArray;
    }
    
    private ArrayList<ObjectNodeWithGroup> createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize)
    {
        ArrayList<ObjectNodeWithGroup> resultObjectNodesWithGroup;
        
        resultObjectNodesWithGroup = new ArrayList<>();
        
        for( GroupOfRetinaObjectWithAssociatedPoints iterationGroupOfRetinaObjectWithAssociatedPoints : groupsOfRetinaObjectsWithAssociatedPoints )
        {
            PlatonicPrimitiveInstanceNode objectNode;
            ObjectNodeWithGroup objectNodeWithGroup;
            
            objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, objectNode, coderack, network, networkHandles, codeletLtmLookup);
            createAndLinkAnglePointsAndLink(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, coderack, network, networkHandles, codeletLtmLookup, imageSize);
            
            objectNodeWithGroup = new ObjectNodeWithGroup();
            objectNodeWithGroup.groupOfRetinaObjects = iterationGroupOfRetinaObjectWithAssociatedPoints;
            objectNodeWithGroup.objectNode = objectNode;
            
            resultObjectNodesWithGroup.add(objectNodeWithGroup);
        }
        
        return resultObjectNodesWithGroup;
    }
    
    
    private void createAndLinkAnglePointsAndLink(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize)
    {
        SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects;
        
        // TODO< hard parameters >
        final int GRIDCOUNTX = 10;
        final int GRIDCOUNTY = 10;
        
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects = new SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects();
        spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints = new SpatialAcceleration<Crosspoint>(GRIDCOUNTX, GRIDCOUNTY, imageSize.x, imageSize.y);
        
        storeRetinaObjectWithAssocIntoMap(arrayOfRetinaObjectWithAssociatedPoints, spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, arrayOfRetinaObjectWithAssociatedPoints);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, coderack, network, networkHandles, codeletLtmLookup);
    }
    
    private static List<RetinaPrimitive> getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints)
    {
        List<RetinaPrimitive> resultRetinaPrimitives;
        
        resultRetinaPrimitives = new ArrayList<>();
        
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
        {
            resultRetinaPrimitives.add(iterationRetinaObject.primitive);
        }
        
        return resultRetinaPrimitives;
    }
    
    /**
     * 
     * stores all intersections into a spatial acceleration structure
     *  
     */
    private void bundleAllIntersectionsOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints)
    {
        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, getRetinaPrimitivesOfRetinaObjectWithAssociatedPointsAndWorkspaceNode(arrayOfRetinaObjectWithAssociatedPoints));
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
                    // we can have intersectionpartners which are not inside
                    // seems to be a bug in clustering or something fishy is going on
                    // TODO< investigate and add here a assert that the assoc can't be null >
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                    }
                    
                    
                    
                    
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[1].intersectionEndpointType));
                    }
                    
                    spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.addElement(createdCrosspointElement);
                }
                else
                {
                    RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssoc;
                    SpatialAcceleration<Crosspoint>.Element nearestCrosspointElement;
                    
                    nearestCrosspointElement = getNearestCrosspointElement(crosspointsAtPosition, Vector2d.ConverterHelper.convertIntVectorToFloat(iterationIntersection.intersectionPosition));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[0].primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) )
                        {
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                        }
                    }
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) )
                        {
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[1].intersectionEndpointType));
                        }
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
    
    private void createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
        {
            createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(iterationRetinaObject, objectNode, coderack, network, networkHandles, codeletLtmLookup);
        }
    }
    
    private static void createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
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
    
    private ArrayList<GroupOfRetinaObjectWithAssociatedPoints> createAndPropagateRetinaLevelObjects(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> retinaObjectsWithAssociatedPoints)
    {
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groups;
        int retinaObjectI;
        
        groups = new ArrayList<>();
        
        groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(retinaObjectsWithAssociatedPoints.get(0)));
        
        // we first try to cluster as many RetinaObjects in a lear fashion, after this we try to combine these "mini clusters" to final large clusters
        
        for( retinaObjectI = 1; retinaObjectI < retinaObjectsWithAssociatedPoints.size(); retinaObjectI++ )
        {
            RetinaObjectWithAssociatedPointsAndWorkspaceNode currentRetinaObject;
            boolean wasIncludedInCluster;
            
            
            currentRetinaObject = retinaObjectsWithAssociatedPoints.get(retinaObjectI);
            
            
            wasIncludedInCluster = false;
            
            for( GroupOfRetinaObjectWithAssociatedPoints iterationGroup : groups )
            {
                if( iterationGroup.canBeIncludedInCluster(currentRetinaObject) )
                {
                    wasIncludedInCluster = true;
                    iterationGroup.arrayOfRetinaObjectWithAssociatedPoints.add(currentRetinaObject);
                    break;
                }
            }
            
            if( !wasIncludedInCluster )
            {
                groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(currentRetinaObject));
            }
            
        }
        
        
        
        // cluster
        int lowerI, upperI;

        repeatSearch:
        for( lowerI = 0; lowerI < groups.size(); lowerI++ )
        {
            for( upperI = lowerI+1; upperI < groups.size(); upperI++ )
            {
                GroupOfRetinaObjectWithAssociatedPoints lower, upper;

                lower = groups.get(lowerI);
                upper = groups.get(upperI);

                if( GroupOfRetinaObjectWithAssociatedPoints.shareCommonPoint(lower, upper) )
                {
                    lower.append(upper);
                    groups.remove(upperI);
                    
                    break repeatSearch;
                }
            }
        }
        
        return groups;
    }
    
    
    private static boolean doRetinaObjectsShareCommonPoints(RetinaObjectWithAssociatedPointsAndWorkspaceNode a, RetinaObjectWithAssociatedPointsAndWorkspaceNode b)
    {
        return getRetinaObjectSharedPoints(a, b, COMMONPOINTSMAXIMALDISTANCE).size() != 0;
    }
    
    private static ArrayList<IndexTuple> getRetinaObjectSharedPoints(RetinaObjectWithAssociatedPointsAndWorkspaceNode a, RetinaObjectWithAssociatedPointsAndWorkspaceNode b, float maximalDistance)
    {
        ArrayList<IndexTuple> resultIndexTuples;
        int Ia, Ib;
        
        Assert.Assert(!a.equals(b), "must be not the same");
        
        resultIndexTuples = new ArrayList<>();
        
        Ia = 0;
        
        for( Vector2d<Float> pointFromA : a.pointPositions )
        {
            Ib = 0;
            
            for( Vector2d<Float> pointFromB : b.pointPositions )
            {
                Vector2d<Float> diff;
                
                diff = sub(pointFromA, pointFromB);
                if( getLength(diff) < maximalDistance)
                {
                    resultIndexTuples.add(new IndexTuple(Ia, Ib));
                }
                
                Ib++;
            }
            
            Ia++;
        }
        
        return resultIndexTuples;
    }
    
    
    
    
    private static class ObjectNodeWithGroup
    {
        Node objectNode;
        GroupOfRetinaObjectWithAssociatedPoints groupOfRetinaObjects;
    }
    
    private static class GroupOfRetinaObjectWithAssociatedPoints
    {
        public ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints = new ArrayList<>();
        
        public boolean canBeIncludedInCluster(RetinaObjectWithAssociatedPointsAndWorkspaceNode candidate)
        {
            for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
            {
                if( doRetinaObjectsShareCommonPoints(iterationRetinaObject, candidate) )
                {
                    return true;
                }
            }
            
            return false;
        }
        
        public static GroupOfRetinaObjectWithAssociatedPoints createFromSingleRetinaObject(RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObject)
        {
            GroupOfRetinaObjectWithAssociatedPoints result;
            
            result = new GroupOfRetinaObjectWithAssociatedPoints();
            result.arrayOfRetinaObjectWithAssociatedPoints.add(retinaObject);
            return result;
        }
        
        public static boolean shareCommonPoint(GroupOfRetinaObjectWithAssociatedPoints a, GroupOfRetinaObjectWithAssociatedPoints b)
        {
            for( RetinaObjectWithAssociatedPointsAndWorkspaceNode outerRetinaObjectWithAssosciatedPoints : a.arrayOfRetinaObjectWithAssociatedPoints )
            {
                for( RetinaObjectWithAssociatedPointsAndWorkspaceNode innerRetinaObjectWithAssosciatedPoints : b.arrayOfRetinaObjectWithAssociatedPoints )
                {
                    if( PointProximityStrategy.doRetinaObjectsShareCommonPoints(outerRetinaObjectWithAssosciatedPoints, innerRetinaObjectWithAssosciatedPoints) )
                    {
                        return true;
                    }
                }
            }
            
            return false;
        }

        private void append(GroupOfRetinaObjectWithAssociatedPoints appendix) 
        {
            arrayOfRetinaObjectWithAssociatedPoints.addAll(appendix.arrayOfRetinaObjectWithAssociatedPoints);
        }
    }
    
    
    
    private static class IndexTuple
    {
        public IndexTuple(int a, int b)
        {
            values = new int[]{a, b};
        }
        
        public int[] values;
    }
    
    
    
    // TODO< move to hard parameters >
    private static final float COMMONPOINTSMAXIMALDISTANCE = 10.0f;
}
