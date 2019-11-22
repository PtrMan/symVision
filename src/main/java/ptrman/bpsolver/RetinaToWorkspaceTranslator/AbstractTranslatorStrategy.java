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
import ptrman.bpsolver.HelperFunctions;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.nodes.AttributeNode;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.Intersection;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.AngleHelper;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Implements basic mechanisms for the translation
 *
 */
public abstract class AbstractTranslatorStrategy implements ITranslatorStrategy {
    public abstract List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, Solver bpSolver);
    
    protected static void storeRetinaObjectWithAssocIntoMap(final Iterable<RetinaObjectWithAssociatedPointsAndWorkspaceNode> arrayOfRetinaObjectWithAssociatedPoints, final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects) {
        for( final var iterationRetinaObjectWithAssoc : arrayOfRetinaObjectWithAssociatedPoints )
            spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.primitiveToRetinaObjectWithAssocMap.put(iterationRetinaObjectWithAssoc.primitive, iterationRetinaObjectWithAssoc);
    }
    
    protected static class RetinaObjectWithAssociatedPointsAndWorkspaceNode {
        public RetinaObjectWithAssociatedPointsAndWorkspaceNode(final ptrman.levels.retina.RetinaPrimitive primitive) {
            this.primitive = primitive;
        }
        
        public final ptrman.levels.retina.RetinaPrimitive primitive;
        
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
        public List<ArrayRealVector> pointPositions;
        
        
        public Node workspaceNode = null; // null if it is not set

    }
    
    protected static RetinaObjectWithAssociatedPointsAndWorkspaceNode associatePointsToRetinaPrimitive(final RetinaPrimitive primitive) {

        assert primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "only implemented for linesegment";

        final var resultAssosciation = new RetinaObjectWithAssociatedPointsAndWorkspaceNode(primitive);
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
    protected void calculateAnglePointType(final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects) {
        for( final var currentElement : spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getContentOfAllCells() ) {
            final var ANGLEEPSILONINDEGREE = 5.0;

            final var crosspoint = currentElement.data;

            final var tangents = crosspoint.adjacentRetinaObjects.stream().map(retinaObjectWithAssocWithIntersectionType -> retinaObjectWithAssocWithIntersectionType.retinaObjectWithAssociatedPointsAndWorkspaceNode.primitive.getNormalizedTangentForIntersectionTypeAndT(retinaObjectWithAssocWithIntersectionType.intersectionPartnerType, 0.0f)).toArray(ArrayRealVector[]::new);
            // TODO< pass in T from the intersectioninfo >

            // HACK TODO< after bugremoval uncomment this assert
            // relates propably to BUG 0001
            //Assert.Assert(crosspoint.adjacentRetinaObjects.size() >= 2, "");
            
            if( crosspoint.adjacentRetinaObjects.size() < 2 ) {
                // we land here when a angle is invalid
                // relates propably to BUG 0001
                
                // we just do nothing
            }
            else // its either T, V, or X with two partners
                // its a X
                if( crosspoint.adjacentRetinaObjects.size() == 2 )
                    if (crosspoint.adjacentRetinaObjects.get(0).intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.MIDDLE || crosspoint.adjacentRetinaObjects.get(1).intersectionPartnerType == Intersection.IntersectionPartner.EnumIntersectionEndpointType.MIDDLE)
                        crosspoint.type = Crosspoint.EnumAnglePointType.X;
                    else {
                        // its either V or T

                        final var angleInDegree = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);

                        crosspoint.type = angleInDegree < 45.0 ?
                                Crosspoint.EnumAnglePointType.T : Crosspoint.EnumAnglePointType.V;
                    }
            else if( crosspoint.adjacentRetinaObjects.size() == 3 ) {
                // its either T (with three partners), X, or K
                
                final var angleInDegreeBetween01 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);
                final var angleInDegreeBetween02 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[2]);
                final var angleInDegreeBetween12 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[2]);
                
                // check for T
                // (one angle must be close to 0, the others must be close to 90)
                if( angleInDegreeBetween01 < ANGLEEPSILONINDEGREE ) {
                    if( angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE ) {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                }
                else if( angleInDegreeBetween12 < ANGLEEPSILONINDEGREE ) {
                    if( angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE ) {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                }
                else if( angleInDegreeBetween02 < ANGLEEPSILONINDEGREE )
                    if (angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE) {
                        crosspoint.type = Crosspoint.EnumAnglePointType.T;
                        continue;
                    }
                
                // we are here if it is not a T
                
                // for an X all angles should be close to 90 degree
                if( angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE && angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE ) {
                    crosspoint.type = Crosspoint.EnumAnglePointType.X;
                    continue;
                }
                
                // we are here if its not an T or an X, so it must be a K
                
                crosspoint.type = Crosspoint.EnumAnglePointType.K;
                continue;
                
            }
            else if( crosspoint.adjacentRetinaObjects.size() == 4 ) {
                // either X or K

                final var angleInDegreeBetween01 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[1]);
                final var angleInDegreeBetween02 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[2]);
                final var angleInDegreeBetween03 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[0], tangents[3]);
                final var angleInDegreeBetween12 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[2]);
                final var angleInDegreeBetween13 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[1], tangents[3]);
                final var angleInDegreeBetween23 = AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangents[2], tangents[3]);

                crosspoint.type = angleInDegreeBetween01 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween02 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween03 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween12 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween13 > 90.0 - ANGLEEPSILONINDEGREE &&
                    angleInDegreeBetween23 > 90.0 - ANGLEEPSILONINDEGREE ?
                        Crosspoint.EnumAnglePointType.X : Crosspoint.EnumAnglePointType.K;
            }
            else {
                    assert crosspoint.adjacentRetinaObjects.size() > 4 : "ASSERT: " + "";

                    // can only be a K
                
                crosspoint.type = Crosspoint.EnumAnglePointType.K;
            }
        }
    }
    
    protected static void createLinksAndNodesForAnglePoints(final SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects spatialAccelerationForCrosspointsWithMappingOfRetinaObjects, final Solver bpSolver) {
        for( final var currentElement : spatialAccelerationForCrosspointsWithMappingOfRetinaObjects.spatialForCrosspoints.getContentOfAllCells() ) {

            final var crosspoint = currentElement.data;

            final var createdAnglePointNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.anglePointNodePlatonicPrimitiveNode);
            // add codelets
            bpSolver.codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdAnglePointNode, bpSolver.coderack, bpSolver.network, bpSolver.networkHandles);
            
            // linkage
            for(  final var iterationRetinaObjectWithAssoc : crosspoint.adjacentRetinaObjects ) {

                final var workspaceNode = iterationRetinaObjectWithAssoc.retinaObjectWithAssociatedPointsAndWorkspaceNode.workspaceNode;

                final var createdForwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, workspaceNode);
                createdAnglePointNode.out(createdForwardLink);

                final var createdBackwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.HASNODE, createdAnglePointNode);
                workspaceNode.out(createdBackwardLink);
            }


            final var createdAnglePointAttributeNode = AttributeNode.createIntegerNode(bpSolver.networkHandles.anglePointFeatureTypePrimitiveNode, crosspoint.type.ordinal());
            final var createdFeatureTypeNodeLink = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdAnglePointAttributeNode);
            createdAnglePointNode.out(createdFeatureTypeNodeLink);

            final var createdAnglePointPosition = HelperFunctions.createVectorAttributeNode(crosspoint.position, bpSolver.networkHandles.anglePointPositionPlatonicPrimitiveNode, bpSolver);
            final var createdPositionLink = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdAnglePointPosition);
            createdAnglePointNode.out(createdPositionLink);
        }
    }
    
    protected static class SpatialAccelerationForCrosspointsWithMappingOfRetinaObjects {
        public SpatialAcceleration<Crosspoint> spatialForCrosspoints;
        
        public Map<RetinaPrimitive, RetinaObjectWithAssociatedPointsAndWorkspaceNode> primitiveToRetinaObjectWithAssocMap = new IdentityHashMap<>();
    }
    
    protected static PlatonicPrimitiveInstanceNode createPlatonicInstanceNodeForRetinaObject(final RetinaPrimitive primitive, final NetworkHandles networkHandles) {
        if( primitive.type == ptrman.levels.retina.RetinaPrimitive.EnumType.LINESEGMENT ) {

            final var createdLineNode = new PlatonicPrimitiveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
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
    public static class Crosspoint {
        public static class RetinaObjectWithAssocWithIntersectionType {
            public final RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssociatedPointsAndWorkspaceNode;
            public final Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType;
            
            public RetinaObjectWithAssocWithIntersectionType(final RetinaObjectWithAssociatedPointsAndWorkspaceNode retinaObjectWithAssociatedPointsAndWorkspaceNode, final Intersection.IntersectionPartner.EnumIntersectionEndpointType intersectionPartnerType) {
                this.retinaObjectWithAssociatedPointsAndWorkspaceNode = retinaObjectWithAssociatedPointsAndWorkspaceNode;
                this.intersectionPartnerType = intersectionPartnerType;
            }
        }
        
        public final List<RetinaObjectWithAssocWithIntersectionType> adjacentRetinaObjects = new ArrayList<>();
        public ArrayRealVector position;
        
        public enum EnumAnglePointType {
            UNDEFINED,
            K,
            V,
            X,
            T;
            // TODO

            public static EnumAnglePointType fromInteger(final int valueAsInt)
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
        
        public boolean doesAdjacentRetinaObjectsContain(final RetinaObjectWithAssociatedPointsAndWorkspaceNode other) {

            return adjacentRetinaObjects.stream().anyMatch(adjacentRetinaObject -> adjacentRetinaObject.retinaObjectWithAssociatedPointsAndWorkspaceNode.equals(other));
        }
    }
    
}
