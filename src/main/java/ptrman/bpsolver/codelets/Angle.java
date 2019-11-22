/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.codelets;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.HelperFunctions;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.AbstractTranslatorStrategy;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.AttributeNode;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.math.Maths;
import ptrman.math.TruncatedFisherYades;
import ptrman.misc.AngleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ptrman.math.ArrayRealVectorHelper.normalize;

/**
 *
 * calculates the angle(s) of a anglePoint
 * should be be called only once!
 */
public class Angle extends SolverCodelet {
    private static final int KPOINTNUMBEROFANGLESUNTILSTOCHASTICCHOICE = 10;
    private static final int KPOINTNUMBEROFCHOSENANGLES = 10; // must be smaller or equal to KPOINTNUMBEROFANGLESUNTILSTOCHASTICCHOICE

    private static class AngleInformation {
        public AngleInformation(final double angle, final int count) {
            this.angle = angle;
            this.count = count;
        }
        
        public int count; // number of the connections from the anglepoint to the (not jet created) attribute node
        public final double angle;
    }
    
    public Angle(final Solver bpSolver) {
        super(bpSolver);
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        return new Angle(bpSolver);
    }

    @Override
    public RunResult run() {
        assert startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() && ((PlatonicPrimitiveInstanceNode)startNode).primitiveNode.equals(getNetworkHandles().anglePointNodePlatonicPrimitiveNode) : "ASSERT: " + "";

        final var anglePointType = getAnglePointType((PlatonicPrimitiveInstanceNode) startNode);
        final var anglePartners = getPartnersOfAnglepoint((PlatonicPrimitiveInstanceNode)startNode);
        final var anglePosition = getAnglePosition((PlatonicPrimitiveInstanceNode)startNode);
        
        // checks
        if( anglePointType == AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.K )
            assert anglePartners.size() >= 3 : "ASSERT: " + "";
        else if( anglePointType == AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.V )
            assert anglePartners.size() == 2 : "ASSERT: " + "";
        else if( anglePointType == AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.X )
            assert anglePartners.size() >= 2 && anglePartners.size() <= 4 : "ASSERT: " + "";
        else // relates to BUG 0001
            // we just return when this case triggers
            // uncomment when the bug is fixed
            //throw new InternalError();
            if( anglePointType == AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.T )
                assert anglePartners.size() == 2 || anglePartners.size() == 3 : "ASSERT: " + "";
        else return new RunResult(false);


        final var angles = calculateAnglesBetweenPartners(anglePointType == AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.K ? EnumIsKPoint.YES : EnumIsKPoint.NO,
                anglePartners,
                anglePosition
        );
        final var angleInformations = bundleAnglesAndCreateAngleInformations(angles);
        
        createNodesAndLinkAngleInformation((PlatonicPrimitiveInstanceNode)startNode, angleInformations);
        createAndLinkAnglePointType((PlatonicPrimitiveInstanceNode)startNode, anglePointType);
        
        return new RunResult(false);
    }
    
    private void createNodesAndLinkAngleInformation(final PlatonicPrimitiveInstanceNode anglePointPrimitiveInstanceNode, final Iterable<AngleInformation> angleInformations) {
        for( final var iterationAngle : angleInformations ) {
            final var createdFeatureNode = FeatureNode.createFloatNode(getNetworkHandles().anglePointAngleValuePrimitiveNode, iterationAngle.angle, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().anglePointAngleValuePrimitiveNode));
            
            for(var linkI = 0; linkI < iterationAngle.count; linkI++ ) {
                final var createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASFEATURE, createdFeatureNode);
                
                anglePointPrimitiveInstanceNode.out(createdLink);
            }
        }
    }
    
    private void createAndLinkAnglePointType(final PlatonicPrimitiveInstanceNode anglePointPrimitiveInstanceNode, final AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType anglePointType) {
        final var createAnglePointTypeNode = AttributeNode.createIntegerNode(getNetworkHandles().anglePointFeatureTypePrimitiveNode, anglePointType.ordinal());
        
        final var createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createAnglePointTypeNode);
        anglePointPrimitiveInstanceNode.out(createdLink);
    }
    
    private AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType getAnglePointType(final PlatonicPrimitiveInstanceNode anglePointNode) {
        for( final var iterationLink : anglePointNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

            if( iterationLink.target.type != NodeTypes.EnumType.ATTRIBUTENODE.ordinal() ) continue;

            final var targetAttributeNode = (AttributeNode) iterationLink.target;
            
            if( !targetAttributeNode.attributeTypeNode.equals(getNetworkHandles().anglePointFeatureTypePrimitiveNode) )
                continue;
            
            return AbstractTranslatorStrategy.Crosspoint.EnumAnglePointType.fromInteger(targetAttributeNode.getValueAsInt());
            
        }
        
        throw new InternalError();
    }
    
    private ArrayRealVector getAnglePosition(final PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode) {
        for( final var iterationLink : platonicPrimitiveInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

            if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) continue;

            final var targetNode = (PlatonicPrimitiveInstanceNode) iterationLink.target;
            
            if( !targetNode.primitiveNode.equals(getNetworkHandles().anglePointPositionPlatonicPrimitiveNode) )
                continue;
            
            return HelperFunctions.getVectorFromVectorAttributeNode(getNetworkHandles(), targetNode);
        }
        
        throw new InternalError();
    }
    
    private static List<PlatonicPrimitiveInstanceNode> getPartnersOfAnglepoint(final PlatonicPrimitiveInstanceNode anglePointNode) {
        final List<PlatonicPrimitiveInstanceNode> resultList = new ArrayList<>();
        
        for( final var iterationLink : anglePointNode.getLinksByType(Link.EnumType.ISPARTOF) ) {
            assert iterationLink.target.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";
            resultList.add((PlatonicPrimitiveInstanceNode)iterationLink.target);
        }

        assert resultList.size() > 0 : "ASSERT: " + "";
        return resultList;
    }
    
    private double measureAngleBetweenPartnersAtPosition(final PlatonicPrimitiveInstanceNode a, final PlatonicPrimitiveInstanceNode b, final ArrayRealVector position) {
        final var tangentA = getTangentOfPlatonicPrimitiveInstanceAtPosition(a, position);
        final var tangentB = getTangentOfPlatonicPrimitiveInstanceAtPosition(b, position);
        
        return AngleHelper.getMinimalAngleInDegreeBetweenNormalizedVectors(tangentA, tangentB);
    }
    
    
    private ArrayRealVector getTangentOfPlatonicPrimitiveInstanceAtPosition(final PlatonicPrimitiveInstanceNode platonicPrimitive, final ArrayRealVector position) {
        if( platonicPrimitive.primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode) ) {
            final var diff = platonicPrimitive.p1.subtract(platonicPrimitive.p2);
            return normalize(diff);
        }
        else throw new InternalError("Unexpected type of primitive!");
    }
    
    
    private static List<AngleInformation> bundleAnglesAndCreateAngleInformations(final Iterable<Double> angles) {
        final List<AngleInformation> resultAngleInformation = new ArrayList<>();
        
        for( final double angle : angles ) {
            var similarAngleWasFound = false;
            
            for( final var iterationAngleInformation : resultAngleInformation )
                if (Math.abs(angle - iterationAngleInformation.angle) < ANGLEMAXDIFFERENCE) {
                    iterationAngleInformation.count++;
                    similarAngleWasFound = true;
                    break;
                }
            
            if( !similarAngleWasFound ) {

                final var createdAngleInformation = new AngleInformation(angle, 1);
                resultAngleInformation.add(createdAngleInformation);
            }
        }
        
        return resultAngleInformation;
    }

    private List<Double> calculateAnglesBetweenPartners(final EnumIsKPoint isKpoint, final List<PlatonicPrimitiveInstanceNode> anglePartners, final ArrayRealVector anglePosition) {
        List<Double> angleResult = new ArrayList<>();
        
        final var numberOfCombinations = Maths.faculty(anglePartners.size());

        if( isKpoint == EnumIsKPoint.YES && numberOfCombinations > KPOINTNUMBEROFANGLESUNTILSTOCHASTICCHOICE ) {
            // NOTE PERFORMANCE< the Fisher yades algorithm is maybe too slow, future will tell >
            // NOTE< selection policy could be better, we measure only one angle per partner, could be many >

            final var truncatedFisherYades = new TruncatedFisherYades((anglePartners.size() * anglePartners.size() - anglePartners.size()) / 2, new GeneratorImplementation());
            
            for(var i = 0; i < KPOINTNUMBEROFANGLESUNTILSTOCHASTICCHOICE; i++) {
                final var indices = (Tuple<Integer>)truncatedFisherYades.takeOne(random);
                final int partnerIndexA = indices.left;
                final int partnerIndexB = indices.right;

                assert partnerIndexA >= 0 && partnerIndexA < anglePartners.size() : "ASSERT: " + "Invalid index";
                assert partnerIndexB >= 0 && partnerIndexB < anglePartners.size() : "ASSERT: " + "Invalid index";

                angleResult.add(measureAngleBetweenPartnersAtPosition(anglePartners.get(partnerIndexA), anglePartners.get(partnerIndexB), anglePosition));
            }
        }
        else
            angleResult = IntStream.range(0, anglePartners.size()).flatMap(lower -> IntStream.range(lower + 1, anglePartners.size())).mapToObj(higher -> measureAngleBetweenPartnersAtPosition(anglePartners.get(0), anglePartners.get(1), anglePosition)).collect(Collectors.toList());
        
        return angleResult;
    }
    
    private static class GeneratorImplementation implements TruncatedFisherYades.IGenerator<Tuple<Integer>> {
        public GeneratorImplementation() {
        }
        
        @Override
        public Tuple<Integer> generate(final int index) {
            final var triangleIndices = getIndicesOfTriangle(index);
            
            // y+1 because we take the lower triangle in the matrix of the index combinations
            return new Tuple<>(triangleIndices.left, triangleIndices.right+1);
        }
        
        // calculate the index inside a triangle strip
        // examples
        
        // 0
        // result 0
        
        // 0
        // xx
        // result 0xx
        
        // 0
        // xx
        // 000
        // result 0xx000
        
        // ...
        private static Tuple<Integer> getIndicesOfTriangle(final int index) {
            var yIndex = 0;
            var width = 1;
            var remainingIndex = index;

            while (true) {
                if( remainingIndex < width ) return new Tuple<>(remainingIndex, yIndex);
                
                remainingIndex -= width;
                yIndex++;
                width++;
            }
        }
        
    }

    private static class Tuple<T> {
        public final T left;
        public final T right;
        
        public Tuple(final T left, final T right) {
            this.left = left;
            this.right = right;
        }
    }
    
    private enum EnumIsKPoint {
        NO,
        YES
    }
    
    private final Random random = new Random();
    
    private static final float ANGLEMAXDIFFERENCE = 5.0f;
}
