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
import bpsolver.HelperFunctions;
import bpsolver.NetworkHandles;
import bpsolver.nodes.AttributeNode;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import misc.AngleHelper;
import misc.Assert;

// BUGS
// BUG 0001
//     because of the invalid handling of some things in the edge handling code/object custering code
//     leads to invalid edges/Angles

/**
 * Strategy for the Retina to Workspace translation which works based on point proximity
 * 
 */
public class PointProximityStrategy implements ITranslatorStrategy
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
    
    private RetinaObjectWithAssociatedPointsAndWorkspaceNode associatePointsToRetinaPrimitive(RetinaPrimitive primitive)
    {
        RetinaObjectWithAssociatedPointsAndWorkspaceNode resultAssosciation;
        
        Assert.Assert(primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "only implemented for linesegment");
        
        resultAssosciation = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(primitive);
        resultAssosciation.pointPositions = new ArrayList<>();
        resultAssosciation.pointPositions.add(primitive.line.getAProjected());
        resultAssosciation.pointPositions.add(primitive.line.getBProjected());
        
        return resultAssosciation;
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
        bundleAllIntersections(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, arrayOfRetinaObjectWithAssociatedPoints);
        calculateAnglePointType(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects);
        createLinksAndNodesForAnglePoints(spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, coderack, network, networkHandles, codeletLtmLookup);
    }
    
    private void createLinksAndNodesForAnglePoints(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
        for( SpatialAcceleration<Crosspoint>.Element currentElement : spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getContentOfAllCells() )
        {
            Crosspoint crosspoint;
            
            crosspoint = currentElement.data;
            
            PlatonicPrimitiveInstanceNode createdAnglePointNode;
            AttributeNode createdAnglePointAttributeNode;
            Link createdFeatureTypeNodeLink;
            Link createdPositionLink;
            PlatonicPrimitiveInstanceNode createdAnglePointPosition;
            
            createdAnglePointNode = new PlatonicPrimitiveInstanceNode(networkHandles.anglePointNodePlatonicPrimitiveNode);
            // add codelets
            codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdAnglePointNode, coderack, network, networkHandles);
            
            // linkage
            for(  Crosspoint.RetinaObjectWithAssocWithIntersectionType iterationRetinaObjectWithAssoc : crosspoint.adjacentRetinaObjects )
            {
                Node workspaceNode;
                Link createdBackwardLink, createdForwardLink;
                
                workspaceNode = iterationRetinaObjectWithAssoc.retinaObjectWithAssociatedPointsAndWorkspaceNode.workspaceNode;
                
                createdForwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, workspaceNode);
                createdAnglePointNode.outgoingLinks.add(createdForwardLink);

                createdBackwardLink = network.linkCreator.createLink(Link.EnumType.HASNODE, createdAnglePointNode);
                workspaceNode.outgoingLinks.add(createdBackwardLink);
            }
            
            
            
            createdAnglePointAttributeNode = AttributeNode.createIntegerNode(networkHandles.anglePointFeatureTypePrimitiveNode, crosspoint.type.ordinal());
            createdFeatureTypeNodeLink = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdAnglePointAttributeNode);
            createdAnglePointNode.outgoingLinks.add(createdFeatureTypeNodeLink);
            
            createdAnglePointPosition = HelperFunctions.createVectorAttributeNode(crosspoint.position, networkHandles.anglePointPositionPlatonicPrimitiveNode, network, networkHandles);
            createdPositionLink = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdAnglePointPosition);
            createdAnglePointNode.outgoingLinks.add(createdPositionLink);
        }
    }
    
    
    /**
     * 
     * figure out the types of the angle points (if its T, K, V, X)
     * 
     */
    private void calculateAnglePointType(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects)
    {
        for( SpatialAcceleration<Crosspoint>.Element currentElement : spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getContentOfAllCells() )
        {
            Crosspoint crosspoint;
            Vector2d<Float> tangents[];
            int crosspointI;
            
            final float ANGLEEPSILONINDEGREE = 5.0f;
            
            crosspoint = currentElement.data;
            
            tangents = new Vector2d[crosspoint.adjacentRetinaObjects.size()];
            for( crosspointI = 0; crosspointI < tangents.length; crosspointI++ )
            {
                // TODO< pass in T from the intersectioninfo >
                tangents[crosspointI] = crosspoint.adjacentRetinaObjects.get(crosspointI).retinaObjectWithAssociatedPointsAndWorkspaceNode.primitive.getNormalizedTangentForIntersectionTypeAndT(crosspoint.adjacentRetinaObjects.get(crosspointI).intersectionPartnerType, 0.0f);
            }
                 
            // HACK TODO< after bugremoval uncomment this assert
            // relates propably to BUG 0001
            //Assert.Assert(crosspoint.adjacentRetinaObjects.size() >= 2, "");
            
            if( crosspoint.adjacentRetinaObjects.size() < 2 )
            {
                // we land here when a angle is invalid
                // relates propably to BUG 0001
                
                // we just do nothing
            }
            else if( crosspoint.adjacentRetinaObjects.size() == 2 )
            {
                // its either T, V, or X with two partners
                
                if( crosspoint.adjacentRetinaObjects.get(0).intersectionPartnerType ==Intersection.IntersectionPartner.EnumIntersectionEndpointType.MIDDLE || crosspoint.adjacentRetinaObjects.get(1).intersectionPartnerType ==Intersection.IntersectionPartner.EnumIntersectionEndpointType.MIDDLE )
                {
                    // its a X
                    crosspoint.type = Crosspoint.EnumAnglePointType.X;
                }
                else
                {
                    // its either V or T
                    
                    float angleInDegree;
                    
                    angleInDegree = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);
                    
                    if( angleInDegree < 45.0f )
                    {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                    }
                    else
                    {
                        crosspoint.type = Crosspoint.EnumAnglePointType.V;
                    }
                }
            }
            else if( crosspoint.adjacentRetinaObjects.size() == 3 )
            {
                final float angleInDegreeBetween01, angleInDegreeBetween02, angleInDegreeBetween12;
                
                
                // its either T (with three partners), X, or K
                
                angleInDegreeBetween01 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);
                angleInDegreeBetween02 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[2]);
                angleInDegreeBetween12 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[2]);
                
                // check for T
                // (one angle must be close to 0, the others must be close to 90)
                if( angleInDegreeBetween01 < ANGLEEPSILONINDEGREE )
                {
                    if( angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE )
                    {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                }
                else if( angleInDegreeBetween12 < ANGLEEPSILONINDEGREE )
                {
                    if( angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE )
                    {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                }
                else if( angleInDegreeBetween02 < ANGLEEPSILONINDEGREE )
                {
                    if( angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE )
                    {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                }
                
                // we are here if it is not a T
                
                // for an X all angles should be close to 90 degree
                if( angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE )
                {
                    crosspoint.type = Crosspoint.EnumAnglePointType.X;
                    continue;
                }
                
                // we are here if its not an T or an X, so it must be a K
                
                crosspoint.type = Crosspoint.EnumAnglePointType.K;
                continue;
                
            }
            else if( crosspoint.adjacentRetinaObjects.size() == 4 )
            {
                // either X or K
                
                final float angleInDegreeBetween01, angleInDegreeBetween02, angleInDegreeBetween03, angleInDegreeBetween12, angleInDegreeBetween13, angleInDegreeBetween23;
                
                angleInDegreeBetween01 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);
                angleInDegreeBetween02 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[2]);
                angleInDegreeBetween03 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[3]);
                angleInDegreeBetween12 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[2]);
                angleInDegreeBetween13 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[3]);
                angleInDegreeBetween23 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[2], tangents[3]);
                
                if(
                    angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween03 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween13 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween23 > 90.0 - ANGLEEPSILONINDEGREE
                )
                {
                    crosspoint.type = Crosspoint.EnumAnglePointType.X;
                }
                else
                {
                    crosspoint.type = Crosspoint.EnumAnglePointType.K;
                }
            }
            else
            {
                Assert.Assert(crosspoint.adjacentRetinaObjects.size() > 4, "");
                
                // can only be a K
                
                crosspoint.type = Crosspoint.EnumAnglePointType.K;
            }
        }
    }
    
    private void storeRetinaObjectWithAssocIntoMap(ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects)
    {
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObjectWithAssoc : arrayOfRetinaObjectWithAssociatedPoints )
        {
            spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.put(iterationRetinaObjectWithAssoc.primitive, iterationRetinaObjectWithAssoc);
        }
    }
    
    /**
     * 
     * stores all intersections into a spatial acceleration structure
     *  
     */
    private void bundleAllIntersections(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, ArrayList<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints)
    {
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
        {
            ArrayList<Intersection> intersections;
            
            intersections = iterationRetinaObject.primitive.getIntersections();
            
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
                    Set<RetinaPrimitive> y = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.keySet();
                    
                    Object[] array = y.toArray();
                    
                    for( int i = 0; i < array.length; i++ )
                    {
                        System.out.println(System.identityHashCode(array[i]));
                    }
                    
                    
                    System.out.println("searched " + System.identityHashCode(iterationIntersection.partners[0].primitive));
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[0].primitive);
                    // we can have intersectionpartners which are not inside
                    // seems to be a bug in clustering or something fishy is going on
                    // TODO< investigate and add here a assert that the assoc can't be null >
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        createdCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                    }
                    
                    
                    
                    
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
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
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[0].primitive);
                    // TODO SAME
                    // relates propably to BUG 0001
                    if( retinaObjectWithAssoc != null )
                    {
                        if( !nearestCrosspointElement.data.doesAdjacentRetinaObjectsContain(retinaObjectWithAssoc) )
                        {
                            nearestCrosspointElement.data.adjacentRetinaObjects.add(new Crosspoint.RetinaObjectWithAssocWithIntersectionType(retinaObjectWithAssoc, iterationIntersection.partners[0].intersectionEndpointType));
                        }
                    }
                    
                    retinaObjectWithAssoc = spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.get(iterationIntersection.partners[1].primitive);
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
        
        createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaObject, networkHandles);
        iterationRetinaObject.workspaceNode = createdPlatonicInstanceNodeForRetinaObject;
        
        // linkage
        createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdPlatonicInstanceNodeForRetinaObject);
        objectNode.outgoingLinks.add(createdForwardLink);

        createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
        createdPlatonicInstanceNodeForRetinaObject.outgoingLinks.add(createdBackwardLink);

        // add all codelet's of it
        codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);

    }
    
    private static PlatonicPrimitiveInstanceNode createPlatonicInstanceNodeForRetinaObject(RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObject, NetworkHandles networkHandles)
    {
        if( retinaObject.primitive.type == RetinaLevel.RetinaPrimitive.EnumType.LINESEGMENT )
        {
            PlatonicPrimitiveInstanceNode createdLineNode;
            
            createdLineNode = new PlatonicPrimitiveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
            createdLineNode.p1 = retinaObject.primitive.line.getAProjected();
            createdLineNode.p2 = retinaObject.primitive.line.getBProjected();
            
            return createdLineNode;
        }

        throw new InternalError();
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
    
    
    private static class RetinaObjectWithAssociatedPointsAndWorkspaceNode
    {
        public RetinaObjectWithAssociatedPointsAndWorkspaceNode(RetinaLevel.RetinaPrimitive primitive)
        {
            this.primitive = primitive;
        }
        
        public RetinaLevel.RetinaPrimitive primitive;
        
        /*
        private Vector2d<Float> getPositionOfEndpoint(int index)
        {
            Assert.Assert(index == 0 || index == 1, "index must be 0 or 1");
            
            if( type == EnumType.LINESEGMENT  )
            {
                return lineDetector.getPositionOfEndpoint(index);
            }
            
            throw new InternalError("");
        }
        */

        
        // TODO< store this in a fast access datastructure for more efficient retrival and comparison >
        // for now we store only the point positions, which is super slow
        public ArrayList<Vector2d<Float>> pointPositions;
        
        
        public Node workspaceNode = null; // null if it is not set

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
    
    
    private static class SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects
    {
        public SpatialAcceleration<Crosspoint> spatialForCrosspoints;
        
        public Map<RetinaPrimitive, RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitveToRetinaObjectWithAssocMap = new IdentityHashMap<>(); 
    }
    
    /**
     * 
     * temporary object to figure out where the intersections are and what type they have
     * 
     */
    public static class Crosspoint
    {
        public static class RetinaObjectWithAssocWithIntersectionType
        {
            public RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssociatedPointsAndWorkspaceNode;
            public Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType;
            
            public RetinaObjectWithAssocWithIntersectionType(RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssociatedPointsAndWorkspaceNode, Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType)
            {
                this.retinaObjectWithAssociatedPointsAndWorkspaceNode = retinaObjectWithAssociatedPointsAndWorkspaceNode;
                this.intersectionPartnerType = intersectionPartnerType;
            }
        }
        
        public ArrayList<RetinaObjectWithAssocWithIntersectionType> adjacentRetinaObjects = new ArrayList<>();
        public Vector2d<Float> position;
        
        public enum EnumAnglePointType
        {
            UNDEFINED,
            K,
            V,
            X,
            T;
            // TODO

            public static EnumAnglePointType fromInteger(int valueAsInt)
            {
                switch( valueAsInt )
                {
                    case 0:
                    return EnumAnglePointType.UNDEFINED;
                    case 1:
                    return EnumAnglePointType.K;
                    case 2:
                    return EnumAnglePointType.V;
                    case 3:
                    return EnumAnglePointType.X;
                    case 4:
                    return EnumAnglePointType.T;
                }
                
                throw new InternalError("");
            }
        }
        
        public EnumAnglePointType type = EnumAnglePointType.UNDEFINED;
        
        public boolean doesAdjacentRetinaObjectsContain(RetinaObjectWithAssociatedPointsAndWorkspaceNode other)
        {
            for( RetinaObjectWithAssocWithIntersectionType adjacentRetinaObject : adjacentRetinaObjects )
            {
                if( adjacentRetinaObject.retinaObjectWithAssociatedPointsAndWorkspaceNode.equals(other) )
                {
                    return true;
                }
            }
            
            return false;
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
