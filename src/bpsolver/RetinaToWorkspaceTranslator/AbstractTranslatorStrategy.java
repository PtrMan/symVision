package bpsolver.RetinaToWorkspaceTranslator;

import Datastructures.SpatialAcceleration;
import Datastructures.Vector2d;
import FargGeneral.Coderack;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.RetinaPrimitive;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import misc.Assert;

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
            spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitveToRetinaObjectWithAssocMap.put(iterationRetinaObjectWithAssoc.primitive, iterationRetinaObjectWithAssoc);
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
    
    protected static class SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects
    {
        public SpatialAcceleration<Crosspoint> spatialForCrosspoints;
        
        public Map<RetinaPrimitive, RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitveToRetinaObjectWithAssocMap = new IdentityHashMap<>(); 
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
