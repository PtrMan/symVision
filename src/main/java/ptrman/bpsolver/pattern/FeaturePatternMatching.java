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
import ptrman.misc.Assert;

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
        public static float distanceToSimilarity(float distance)
        {
            return (float)Math.exp(-distance);
        }
    }

    public interface IMatchingPathRatingStrategy
    {
        float calculate(List<MatchingPathElement> matchingPathElements);
    }

    public static class MultiplyMatchingPathRatingStrategy implements  IMatchingPathRatingStrategy {

        @Override
        public float calculate(List<MatchingPathElement> matchingPathElements) {

            double result = 1.0f;

            for( MatchingPathElement iterationMatchingPathElement : matchingPathElements )
                result *= iterationMatchingPathElement.similarityValue;

            return (float)result;
        }
    }

    private static class IntegrateTDistributionUpperIntegral implements UnivariateFunction {
        public double n;
        
        public double value(double x) {
            return (float)Math.pow((n - 1.0f)/(n - 1.0f + x * x), n/2.0f);
        }
    }
    
    private static class IntegrateTDistributionLowerIntegral implements UnivariateFunction {
        public double n;
        
        public double value(double x) {
            return (float)Math.pow(Math.sin(x), n);
        }
    }
    
    private static class IntegrateEquation8Dot4 implements UnivariateFunction {
        public double value(double x) {
            return Math.pow(Math.E, -0.5*x*x);
        }
    }
    
    /**
     * 
     * f1 and f2 are from the Node of the same type (excluding numerosity)
     */
    public double matchSameTypeNonNumerosity(FeatureStatistics f1, FeatureStatistics f2) {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 ) {
            return matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(f1, f2);
        }
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 ) {
            return matchSameTypeNonNumerosityWithF1Equals1(f1, f2);
        }
        else {
            return matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(f1, f2);
        }
    }
    
    public float matchSameTypeNumerosity(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2) {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 ) {
            return matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(f1, numeriosity1, f2, numeriosity2);
        }
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 ) {
            return matchSameTypeNumerosityWithF1Equals1(f1, numeriosity1, f2, numeriosity2);
        }
        else {
            return matchSameTypeNumeroistyWithF1AndF2NotEqual1(f1, numeriosity1, f2, numeriosity2);
        }
    }
    
    // matches two (PlatonicPrimitiveInstanceNode for now) against each other and returns the matching degree
    // takes the activation/priming inside the LTM into account to do so
    // see foundalis disertation 8.2.3 to see how it is compared
    public float matchAnyNonRecursive(Node nodeA, Node nodeB, NetworkHandles networkHandles) {
        // the type is FeatureNode.featureTypeNode
        // only if two elements are in the array the type is

        // check that the nodes are PlatonicOrimitiveInstanceNode's
        // this can be removed if we need functionality which isn't constrained that way
        // for now its enought
        // if we remove this we need for each node a IS link(and the best would be an accelerated type based access)
        Assert.Assert(nodeA.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        Assert.Assert(nodeB.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        
        // we return zero here because if the type is different it is ill-defined (as defined for now)
        if( !( ((PlatonicPrimitiveInstanceNode)nodeA).primitiveNode.equals(((PlatonicPrimitiveInstanceNode)nodeB).primitiveNode) ) ) {
            return 0.0f;
        }

        Map<Node, ArrayList<FeatureNode>> featureNodesByType = new HashMap<>();
        
        getAllFeatureNodesAndAddToMap(nodeA, featureNodesByType);
        getAllFeatureNodesAndAddToMap(nodeB, featureNodesByType);
        
        return matchAndWeightFeatureNodesByType(featureNodesByType, networkHandles);
    }

    // TODO< document source (chapter of foundalis disertation after the chapter about the nonrecursive version) >
    // TODO< recurse until reach of lower bound >
    // recursive until lower bound
    public List<MatchingPathElement> matchAnyRecursive(Node nodeA, Node nodeB, NetworkHandles networkHandles, List<Link.EnumType> linkWhitelist, int maxDepth) {

        List<MatchingPathElement> resultMatchingPath = new ArrayList<>();

        matchAnyRecursiveInternal(resultMatchingPath, nodeA, nodeB, networkHandles, linkWhitelist, maxDepth, 1);

        return resultMatchingPath;
    }

    // helper for calculate rating with strategy
    public static float calculateRatingWithDefaultStrategy(List<MatchingPathElement> matchingPathElements) {

        IMatchingPathRatingStrategy strategy = new MultiplyMatchingPathRatingStrategy();

        return strategy.calculate(matchingPathElements);
    }


    private void matchAnyRecursiveInternal(List<MatchingPathElement> resultMatchingPath, Node nodeA, Node nodeB, NetworkHandles networkHandles, List<Link.EnumType> linkWhitelist, int maxDepth, int currentDepth) {
        int linkIndexA, linkIndexB;

        if( currentDepth >= maxDepth ) {
            return;
        }

        MatchingPathElement bestMatchingPathElement = new MatchingPathElement();
        bestMatchingPathElement.bestMatchNodeAIndex = -1;
        bestMatchingPathElement.bestMatchNodeBIndex = -1;
        bestMatchingPathElement.similarityValue = 0.0f;

        for(linkIndexA = 0; linkIndexA < nodeA.out.size(); linkIndexA++ ) {
            for(linkIndexB = 0; linkIndexB < nodeB.out.size(); linkIndexB++ ) {

                Link linkA = nodeA.out.get(linkIndexA);
                Link linkB = nodeB.out.get(linkIndexB);

                if( !doesLinkTypeListContainType(linkWhitelist, linkA.type) || !doesLinkTypeListContainType(linkWhitelist, linkB.type) )
                    continue;


                float currentDistance = matchAnyNonRecursive(linkA.target, linkB.target, networkHandles);
                float currentSimilarity = Converter.distanceToSimilarity(currentDistance);

                if( currentSimilarity > bestMatchingPathElement.similarityValue) {
                    bestMatchingPathElement.similarityValue = currentSimilarity;
                    bestMatchingPathElement.bestMatchNodeAIndex = linkIndexA;
                    bestMatchingPathElement.bestMatchNodeBIndex = linkIndexB;
                }
            }
        }

        if( bestMatchingPathElement.similarityValue != 0.0f ) {
            resultMatchingPath.add(bestMatchingPathElement);

            matchAnyRecursiveInternal(resultMatchingPath, nodeA.out.get(bestMatchingPathElement.bestMatchNodeAIndex).target, nodeB.out.get(bestMatchingPathElement.bestMatchNodeBIndex).target, networkHandles, linkWhitelist, maxDepth, currentDepth+1);
        }
    }

    public static class MatchingPathElement {
        public int bestMatchNodeAIndex;
        public int bestMatchNodeBIndex;

        public float similarityValue;
    }

    /** helper for matchAnyNonRecursive
     * 
     * goes through the map and weights it after the activation in LTM
     * non common FeatureNodes (where the count is != 2) are weighted with zero
     * 
     */
    private float matchAndWeightFeatureNodesByType(Map<Node, ArrayList<FeatureNode>> featureNodesByType,  NetworkHandles networkHandles) {

        float weightSum = 0.0f;
        float upperSum = 0.0f;
        
        if( featureNodesByType.keySet().isEmpty() ) {
            return 0.0f;
        }
        
        for(Map.Entry<Node, ArrayList<FeatureNode>> nodeArrayListEntry : featureNodesByType.entrySet()) {
            double featureDistance;

            ArrayList<FeatureNode> featureNodesForType = nodeArrayListEntry.getValue();
            
            if( featureNodesForType.size() == 2 ) {
                featureDistance = matchSameTypeNonNumerosity(featureNodesForType.get(0).statistics, featureNodesForType.get(1).statistics);
            }
            else {
                featureDistance = 0.0;
            }
            
            float weight = (nodeArrayListEntry.getKey().activation + getSystemWeightOfPlatonicFeatureType(nodeArrayListEntry.getKey(), networkHandles)) * 0.5f;
            
            upperSum += (featureDistance * weight);
            weightSum += weight;
        }
        
        return upperSum / weightSum;
    }
    
    // helper for
    private static float getSystemWeightOfPlatonicFeatureType(Node featureType, NetworkHandles networkHandles) {
        if( featureType.equals(networkHandles.lineSegmentFeatureLineLengthPrimitiveNode) ) {
            return HardParameters.FeatureWeights.LINESEGMENTFEATURELINELENGTH;
        }
        return 0.0f;
    }
    
    // helper for matchAnyNonRecursive
    private static void getAllFeatureNodesAndAddToMap(Node node, Map<Node, ArrayList<FeatureNode>> featureNodesByType) {

        for( Link iterationAttributeLink : node.getLinksByType(Link.EnumType.HASATTRIBUTE)) {

            if( iterationAttributeLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() )
                continue;

            FeatureNode targetFeatureNode = (FeatureNode) iterationAttributeLink.target;
            
            if( featureNodesByType.containsKey(targetFeatureNode.featureTypeNode) ) {
                featureNodesByType.get(targetFeatureNode.featureTypeNode).add(targetFeatureNode);
            } else {

                ArrayList<FeatureNode> createdFeatureNodeList = new ArrayList<>(1);
                createdFeatureNodeList.add(targetFeatureNode);
                featureNodesByType.put(targetFeatureNode.featureTypeNode, createdFeatureNodeList);
            }
        }
    }

    // helper
    private static boolean doesLinkTypeListContainType(Iterable<Link.EnumType> list, Link.EnumType searchFor) {
        for( Link.EnumType iterationType : list ) {
            if( iterationType.ordinal() == searchFor.ordinal() ) {
                return true;
            }
        }

        return false;
    }
    
    private float matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2) {

        float l = Math.max(numeriosity1, numeriosity2);
        float s = Math.min(numeriosity1, numeriosity2);

        float z = Math.abs(l - s) / (SIGMAZERO * (float) Math.sqrt(l + s));
        
        return calcNumeriosityD(z);
    }
    
    // maybe this is wrong implemented
    private float matchSameTypeNumerosityWithF1Equals1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2) {

        double insideSqrt = SIGMAZERO * SIGMAZERO * f1.getMean() + Maths.power2(f2.getStandardDeviation()) / Maths.power2(f2.numberOfObservations);
        float z = (float) (Math.abs(f1.getMean() - f2.getMean()) / Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float matchSameTypeNumeroistyWithF1AndF2NotEqual1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2) {

        double insideSqrt = Maths.power2(f1.getStandardDeviation()) / f1.numberOfObservations + Maths.power2(f2.getStandardDeviation()) / f2.numberOfObservations;
        float z = (float) (Math.abs(f1.getMean() - f2.getMean()) / Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float calcNumeriosityD(float z) {
        return (float)(1.0f/(Math.sqrt(2.0*Math.PI)))*(float)integrator.integrate(INTEGRATEMAXEVAL, integrateEquation8Dot4, -z, z);
    }
    
    private static float matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(FeatureStatistics f1, FeatureStatistics f2) {
        return Math.abs(f1.getMean() - f2.getMean()) / f1.primitiveFeatureMax;
    }
    
    private double matchSameTypeNonNumerosityWithF1Equals1(FeatureStatistics f1, FeatureStatistics f2) {
        double n = f2.numberOfObservations;
        double t = ((f2.getMean()-f1.getMean())*Math.sqrt(n))/f2.getStandardDeviation();
        
        return calcStudentTDistribution(n, t);
    }
    
    private double matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(FeatureStatistics f1, FeatureStatistics f2) {
        double s1 = f1.getStandardDeviation();
        double n1 = f1.numberOfObservations;

        double s2 = f2.getStandardDeviation();
        double n2 = f2.numberOfObservations;

        double nDividend = Maths.power2(s1)/n1 + Maths.power2(s2)/n2;
        double nDivisorSum1 = Maths.power2((s1 * s1) / n1) / (n1 - 1.0f);
        double nDivisorSum2 = Maths.power2((s2 * s2) / n2) / (n2 - 1.0f);

        double n = nDividend / (nDivisorSum1 + nDivisorSum2);
        double t = Math.abs(f1.getMean()-f2.getMean())/Math.sqrt(Maths.power2(s1) / n1 + Maths.power2(s2) / n2);
        
        return calcStudentTDistribution(n, t);
    }
    
    private double calcStudentTDistribution(double n, double t) {


        integrateTDistributionUpperIntegral.n = n;
        integrateTDistributionLowerIntegral.n = n;

        float upperIntegral = (float) integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionUpperIntegral, -t, t);
        float lowerIntegral = (float) integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionLowerIntegral, 0, 2.0f * Math.PI);
        
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
