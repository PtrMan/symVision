/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.pattern;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.BaseAbstractUnivariateIntegrator;
import org.apache.commons.math3.analysis.integration.SimpsonIntegrator;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.FeatureStatistics;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.math.Maths;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class which encapuslates feature matching for learning/matching in the workspace/ltm
 * 
 * page 214 ff
 */
public class FeaturePatternMatching {
    public enum Converter {
        ;

        /**
         *
         *
         * see Foundalis dissertation page 225   8.2.5 Using difference to compute similarity
         */
        public static float distanceToSimilarity(final float distance)
        {
            return (float)Math.exp(-distance);
        }
    }

    public interface IMatchingPathRatingStrategy
    {
        float calculate(List<MatchingPathElement<Link>> matchingPathElements);
    }

    public static class MultiplyMatchingPathRatingStrategy implements  IMatchingPathRatingStrategy {

        @Override
        public float calculate(final List<MatchingPathElement<Link>> matchingPathElements) {

            final var result = matchingPathElements.stream().mapToDouble(iterationMatchingPathElement -> iterationMatchingPathElement.similarity).reduce(1.0f, (a, b) -> a * b);

            return (float)result;
        }
    }

    private static class IntegrateTDistributionUpperIntegral implements UnivariateFunction {
        public double n;
        
        public double value(final double x) {
            return (float)Math.pow((n - 1.0f)/(n - 1.0f + x * x), n/2.0f);
        }
    }
    
    private static class IntegrateTDistributionLowerIntegral implements UnivariateFunction {
        public double n;
        
        public double value(final double x) {
            return (float)Math.pow(Math.sin(x), n);
        }
    }
    
    private static class IntegrateEquation8Dot4 implements UnivariateFunction {
        public double value(final double x) {
            return Math.pow(Math.E, -0.5*x*x);
        }
    }
    
    /**
     * 
     * f1 and f2 are from the Node of the same type (excluding numerosity)
     */
    public double matchSameTypeNonNumerosity(final FeatureStatistics f1, final FeatureStatistics f2) {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 )
            return matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(f1, f2);
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 )
            return matchSameTypeNonNumerosityWithF1Equals1(f1, f2);
        else return matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(f1, f2);
    }
    
    public float matchSameTypeNumerosity(final FeatureStatistics f1, final int numeriosity1, final FeatureStatistics f2, final int numeriosity2) {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 )
            return matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(f1, numeriosity1, f2, numeriosity2);
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 )
            return matchSameTypeNumerosityWithF1Equals1(f1, numeriosity1, f2, numeriosity2);
        else return matchSameTypeNumeroistyWithF1AndF2NotEqual1(f1, numeriosity1, f2, numeriosity2);
    }
    
    // matches two (PlatonicPrimitiveInstanceNode for now) against each other and returns the matching degree
    // takes the activation/priming inside the LTM into account to do so
    // see foundalis disertation 8.2.3 to see how it is compared
    public float matchAnyNonRecursive(final Node nodeA, final Node nodeB, final NetworkHandles networkHandles) {
        // the type is FeatureNode.featureTypeNode
        // only if two elements are in the array the type is

        // check that the nodes are PlatonicOrimitiveInstanceNode's
        // this can be removed if we need functionality which isn't constrained that way
        // for now its enought
        // if we remove this we need for each node a IS link(and the best would be an accelerated type based access)
        assert nodeA.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";
        assert nodeB.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";

        // we return zero here because if the type is different it is ill-defined (as defined for now)
        if( !( ((PlatonicPrimitiveInstanceNode)nodeA).primitiveNode.equals(((PlatonicPrimitiveInstanceNode)nodeB).primitiveNode) ) )
            return 0.0f;

        final Map<Node, ArrayList<FeatureNode>> featureNodesByType = new HashMap<>();
        
        getAllFeatureNodesAndAddToMap(nodeA, featureNodesByType);
        getAllFeatureNodesAndAddToMap(nodeB, featureNodesByType);
        
        return matchAndWeightFeatureNodesByType(featureNodesByType, networkHandles);
    }

    // TODO< document source (chapter of foundalis disertation after the chapter about the nonrecursive version) >
    // TODO< recurse until reach of lower bound >
    // recursive until lower bound
    public List<MatchingPathElement<Link>> matchAnyRecursive(final Node nodeA, final Node nodeB, final NetworkHandles networkHandles, final List<Link.EnumType> linkWhitelist, final int maxDepth) {

        final List<MatchingPathElement<Link>> resultMatchingPath = new ArrayList<>();

        matchAnyRecursiveInternal(resultMatchingPath, nodeA, nodeB, networkHandles, linkWhitelist, maxDepth, 1);

        return resultMatchingPath;
    }

    // helper for calculate rating with strategy
    public static float calculateRatingWithDefaultStrategy(final List<MatchingPathElement<Link>> matchingPathElements) {

        final IMatchingPathRatingStrategy strategy = new MultiplyMatchingPathRatingStrategy();

        return strategy.calculate(matchingPathElements);
    }


    private void matchAnyRecursiveInternal(final List<MatchingPathElement<Link>> resultMatchingPath, final Node nodeA, final Node nodeB, final NetworkHandles networkHandles, final List<Link.EnumType> linkWhitelist, final int maxDepth, final int currentDepth) {


        if( currentDepth >= maxDepth ) return;

        final var bestMatchingPathElement = new MatchingPathElement<Link>();
        bestMatchingPathElement.similarity = 0.0f;

        for (final var linkA : nodeA.out())
            for (final var linkB : nodeB.out()) {

                if (!doesLinkTypeListContainType(linkWhitelist, linkA.type) || !doesLinkTypeListContainType(linkWhitelist, linkB.type))
                    continue;

                final var currentDistance = matchAnyNonRecursive(linkA.target, linkB.target, networkHandles);
                final var currentSimilarity = Converter.distanceToSimilarity(currentDistance);

                if (currentSimilarity > bestMatchingPathElement.similarity) {
                    bestMatchingPathElement.similarity = currentSimilarity;
                    bestMatchingPathElement.bestMatchA = linkA;
                    bestMatchingPathElement.bestMatchB = linkB;
                }

            }


        if( bestMatchingPathElement.similarity != 0.0f ) {
            resultMatchingPath.add(bestMatchingPathElement);

            matchAnyRecursiveInternal(resultMatchingPath, bestMatchingPathElement.bestMatchA.target, bestMatchingPathElement.bestMatchB.target, networkHandles, linkWhitelist, maxDepth, currentDepth+1);
        }
    }

    public static class MatchingPathElement<X> {
        public X bestMatchA;
        public X bestMatchB;
        public float similarity;
    }

    /** helper for matchAnyNonRecursive
     * 
     * goes through the map and weights it after the activation in LTM
     * non common FeatureNodes (where the count is != 2) are weighted with zero
     * 
     */
    private float matchAndWeightFeatureNodesByType(final Map<Node, ArrayList<FeatureNode>> featureNodesByType, final NetworkHandles networkHandles) {

        if( featureNodesByType.keySet().isEmpty() ) return 0.0f;

        var upperSum = 0.0f;
        var weightSum = 0.0f;
        for(final var nodeArrayListEntry : featureNodesByType.entrySet()) {

            final var featureNodesForType = nodeArrayListEntry.getValue();

            final double featureDistance = featureNodesForType.size() == 2 ? matchSameTypeNonNumerosity(featureNodesForType.get(0).statistics, featureNodesForType.get(1).statistics) : 0.0;

            final var weight = (nodeArrayListEntry.getKey().activation + getSystemWeightOfPlatonicFeatureType(nodeArrayListEntry.getKey(), networkHandles)) * 0.5f;
            
            upperSum += (featureDistance * weight);
            weightSum += weight;
        }
        
        return upperSum / weightSum;
    }
    
    // helper for
    private static float getSystemWeightOfPlatonicFeatureType(final Node featureType, final NetworkHandles networkHandles) {
        if( featureType.equals(networkHandles.lineSegmentFeatureLineLengthPrimitiveNode) )
            return HardParameters.FeatureWeights.LINESEGMENTFEATURELINELENGTH;
        return 0.0f;
    }
    
    // helper for matchAnyNonRecursive
    private static void getAllFeatureNodesAndAddToMap(final Node node, final Map<Node, ArrayList<FeatureNode>> featureNodesByType) {

        for( final var iterationAttributeLink : node.getLinksByType(Link.EnumType.HASATTRIBUTE)) {

            if( iterationAttributeLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() )
                continue;

            final var targetFeatureNode = (FeatureNode) iterationAttributeLink.target;
            
            if( featureNodesByType.containsKey(targetFeatureNode.featureTypeNode) )
                featureNodesByType.get(targetFeatureNode.featureTypeNode).add(targetFeatureNode);
            else {

                final var createdFeatureNodeList = new ArrayList<FeatureNode>(1);
                createdFeatureNodeList.add(targetFeatureNode);
                featureNodesByType.put(targetFeatureNode.featureTypeNode, createdFeatureNodeList);
            }
        }
    }

    // helper
    private static boolean doesLinkTypeListContainType(final Iterable<Link.EnumType> list, final Link.EnumType searchFor) {
        for( final var iterationType : list ) if (iterationType.ordinal() == searchFor.ordinal()) return true;

        return false;
    }
    
    private float matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(final FeatureStatistics f1, final int numeriosity1, final FeatureStatistics f2, final int numeriosity2) {

        final float l = Math.max(numeriosity1, numeriosity2);
        final float s = Math.min(numeriosity1, numeriosity2);

        final var z = Math.abs(l - s) / (SIGMAZERO * (float) Math.sqrt(l + s));
        
        return calcNumeriosityD(z);
    }
    
    // maybe this is wrong implemented
    private float matchSameTypeNumerosityWithF1Equals1(final FeatureStatistics f1, final int numeriosity1, final FeatureStatistics f2, final int numeriosity2) {

        final var insideSqrt = SIGMAZERO * SIGMAZERO * f1.getMean() + Maths.power2(f2.getStandardDeviation()) / Maths.power2(f2.numberOfObservations);
        final var z = (float) (Math.abs(f1.getMean() - f2.getMean()) / Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float matchSameTypeNumeroistyWithF1AndF2NotEqual1(final FeatureStatistics f1, final int numeriosity1, final FeatureStatistics f2, final int numeriosity2) {

        final var insideSqrt = Maths.power2(f1.getStandardDeviation()) / f1.numberOfObservations + Maths.power2(f2.getStandardDeviation()) / f2.numberOfObservations;
        final var z = (float) (Math.abs(f1.getMean() - f2.getMean()) / Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float calcNumeriosityD(final float z) {
        return (float)(1.0f/(Math.sqrt(2.0*Math.PI)))*(float)integrator.integrate(INTEGRATEMAXEVAL, integrateEquation8Dot4, -z, z);
    }
    
    private static float matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(final FeatureStatistics f1, final FeatureStatistics f2) {
        return Math.abs(f1.getMean() - f2.getMean()) / f1.primitiveFeatureMax;
    }
    
    private double matchSameTypeNonNumerosityWithF1Equals1(final FeatureStatistics f1, final FeatureStatistics f2) {
        final double n = f2.numberOfObservations;
        final var t = ((f2.getMean()-f1.getMean())*Math.sqrt(n))/f2.getStandardDeviation();
        
        return calcStudentTDistribution(n, t);
    }
    
    private double matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(final FeatureStatistics f1, final FeatureStatistics f2) {
        final double s1 = f1.getStandardDeviation();
        final double n1 = f1.numberOfObservations;

        final double s2 = f2.getStandardDeviation();
        final double n2 = f2.numberOfObservations;

        final var nDividend = Maths.power2(s1)/n1 + Maths.power2(s2)/n2;
        final var nDivisorSum1 = Maths.power2((s1 * s1) / n1) / (n1 - 1.0f);
        final var nDivisorSum2 = Maths.power2((s2 * s2) / n2) / (n2 - 1.0f);

        final var n = nDividend / (nDivisorSum1 + nDivisorSum2);
        final var t = Math.abs(f1.getMean()-f2.getMean())/Math.sqrt(Maths.power2(s1) / n1 + Maths.power2(s2) / n2);
        
        return calcStudentTDistribution(n, t);
    }
    
    private double calcStudentTDistribution(final double n, final double t) {


        integrateTDistributionUpperIntegral.n = n;
        integrateTDistributionLowerIntegral.n = n;

        final var upperIntegral = (float) integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionUpperIntegral, -t, t);
        final var lowerIntegral = (float) integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionLowerIntegral, 0, 2.0f * Math.PI);
        
        return (upperIntegral)/((float)Math.sqrt(n-1.0f)*lowerIntegral);
    }

    // this integrator just for getting started
    public final BaseAbstractUnivariateIntegrator integrator = new SimpsonIntegrator();
    private final IntegrateTDistributionUpperIntegral integrateTDistributionUpperIntegral = new IntegrateTDistributionUpperIntegral();
    private final IntegrateTDistributionLowerIntegral integrateTDistributionLowerIntegral = new IntegrateTDistributionLowerIntegral();
    private final UnivariateFunction integrateEquation8Dot4 = new IntegrateEquation8Dot4();
    
    private final static float SIGMAZERO = 1.0f; // used in the comparison of numerosity nodes
    
    private final static int INTEGRATEMAXEVAL = 16;
}
