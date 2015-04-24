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
import bpsolver.HelperFunctions;
import bpsolver.NetworkHandles;
import bpsolver.nodes.AttributeNode;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import misc.AngleHelper;
import misc.Assert;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements basic mechanisms for the translation
 *
 */
public abstract class AbstractTranslatorStrategy implements ITranslatorStrategy
{
    public abstract List<Node> createObjectsFromRetinaPrimitives(ArrayList<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup, Vector2d<Float> imageSize);
    
    protected void storeRetinaObjectWithAssocIntoMap(List<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects)
    {
        for( RetinaObjectWithAssociatedPointsAndWorkspaceNode iterationRetinaObjectWithAssoc : arrayOfRetinaObjectWithAssociatedPoints )
        {
            spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.put(iterationRetinaObjectWithAssoc.primitive, iterationRetinaObjectWithAssoc);
        }
    }
    
    protected static class RetinaObjectWithAssociatedPointsAndWorkspaceNode
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
        public List<Vector2d<Float>> pointPositions;
        
        
        public Node workspaceNode = null; // null if it is not set

    }
    
    protected RetinaObjectWithAssociatedPointsAndWorkspaceNode associatePointsToRetinaPrimitive(RetinaPrimitive primitive)
    {
        RetinaObjectWithAssociatedPointsAndWorkspaceNode resultAssosciation;
        
        Assert.Assert(primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "only implemented for linesegment");
        
        resultAssosciation = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(primitive);
        resultAssosciation.pointPositions = new ArrayList<>();
        resultAssosciation.pointPositions.add(primitive.line.getAProjected());
        resultAssosciation.pointPositions.add(primitive.line.getBProjected());
        
        return resultAssosciation;
    }
    
    /**
     * 
     * figure out the types of the angle points (if its T, K, V, X)
     * 
     */
    protected void calculateAnglePointType(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects)
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
    
    protected void createLinksAndNodesForAnglePoints(SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
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
    
    protected static class SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects
    {
        public SpatialAcceleration<Crosspoint> spatialForCrosspoints;
        
        public Map<RetinaPrimitive, RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitiveToRetinaObjectWithAssocMap = new IdentityHashMap<>();
    }
    
    protected static PlatonicPrimitiveInstanceNode createPlatonicInstanceNodeForRetinaObject(RetinaPrimitive primitive, NetworkHandles networkHandles)
    {
        if( primitive.type == RetinaLevel.RetinaPrimitive.EnumType.LINESEGMENT )
        {
            PlatonicPrimitiveInstanceNode createdLineNode;
            
            createdLineNode = new PlatonicPrimitiveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
            createdLineNode.p1 = primitive.line.getAProjected();
            createdLineNode.p2 = primitive.line.getBProjected();
            
            return createdLineNode;
        }

        throw new InternalError();
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
        
        public List<RetinaObjectWithAssocWithIntersectionType> adjacentRetinaObjects = new ArrayList<>();
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
    
}
