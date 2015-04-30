package ptrman.bpsolver.pattern;

import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.misc.Assert;

import java.util.Arrays;
import java.util.List;

/**
 *
 * 
 */
public class MatchingUpdateImplementationForObjects implements IMatchingUpdate
{

    @Override
    public Pattern updateCore(Pattern orginal, Pattern additional, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        final float additionalStrength = 0.1f;

        List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
        int currentMatchPathElementI;

        Assert.Assert(orginal.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(orginal.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        Assert.Assert(additional.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(additional.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");

        // OLD TODO
        // < mark all nodes in additional as not marked >
        // < iterate over all childs of orginal object and find the closest match in additional (which was not marked) >
        // for each closest match mark the node itself which was chosen >

        // match it recursivly and
        // as described in
        // Foundalis dissertation 8.3.3 Pattern updating
        // * match it recursivly
        // * strengthen links

        matchingPathElements = featurePatternMatching.matchAnyRecursive(orginal.exemplars.get(0), additional.exemplars.get(0), networkHandles, Arrays.asList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);

        Node currentMatchNodeOrginal, currentMatchNodeAdditional;

        currentMatchNodeOrginal = orginal.exemplars.get(0);
        currentMatchNodeAdditional = additional.exemplars.get(0);

        for( currentMatchPathElementI = 0; currentMatchPathElementI < matchingPathElements.size()-1; currentMatchPathElementI++ )
        {
            FeaturePatternMatching.MatchingPathElement currentMatchPathElement;

            currentMatchPathElement = matchingPathElements.get(currentMatchPathElementI);

            currentMatchNodeOrginal = currentMatchNodeOrginal.outgoingLinks.get(currentMatchPathElement.bestMatchNodeAIndex).target;
            currentMatchNodeAdditional = currentMatchNodeAdditional.outgoingLinks.get(currentMatchPathElement.bestMatchNodeBIndex).target;
        }

        // strengthen links

        FeaturePatternMatching.MatchingPathElement currentMatchLastPathElement;

        currentMatchLastPathElement = matchingPathElements.get(matchingPathElements.size()-1);

        currentMatchNodeOrginal.outgoingLinks.get(currentMatchLastPathElement.bestMatchNodeAIndex).strength += additionalStrength;
        currentMatchNodeAdditional.outgoingLinks.get(currentMatchLastPathElement.bestMatchNodeBIndex).strength += additionalStrength;
        // TODO< limit it in some way? >

        // update the statistics
        Node lastOrginalNode;
        Node lastAdditionalNode;
        FeatureNode lastOrginalNodeAsFeatureNode;
        FeatureNode lastAdditionalNodeAsFeatureNode;

        lastOrginalNode = orginal.exemplars.get(0).outgoingLinks.get(currentMatchLastPathElement.bestMatchNodeAIndex).target;
        lastAdditionalNode = additional.exemplars.get(0).outgoingLinks.get(currentMatchLastPathElement.bestMatchNodeBIndex).target;

        // NOTE< should we just build around this case with an if? >
        Assert.Assert(lastOrginalNode.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "lastOrginalNode is not a featurenode as expected");

        lastOrginalNodeAsFeatureNode = (FeatureNode)lastOrginalNode;
        lastAdditionalNodeAsFeatureNode = (FeatureNode)lastAdditionalNode;

        lastOrginalNodeAsFeatureNode.statistics.addValuesFromStatistics(lastAdditionalNodeAsFeatureNode.statistics);

        // simply return (the recombined) orginal...
        return orginal;
    }

    @Override
    public float match(Pattern a, Pattern b, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
        float matchingSimilarityValue;

        Assert.Assert(a.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(a.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        Assert.Assert(b.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(b.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");

        matchingPathElements = featurePatternMatching.matchAnyRecursive(a.exemplars.get(0), b.exemplars.get(0), networkHandles, Arrays.asList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
        matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

        return matchingSimilarityValue;
    }


    private static Node findClosestMatchForSameTypesForPlatonicInstances(Node template, List<Node> others, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        Node bestMatchingNode;
        float bestMatchingSimilarity;
        
        Assert.Assert(others.size() >= 1, "");

        bestMatchingNode = null;
        bestMatchingSimilarity = 0.0f;

        for( Node iterationOther : others )
        {
            List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
            float matchingSimilarityValue;

            //Assert.Assert(!iterationOther.marked, "node must not be marked!");
            Assert.Assert(template.type == iterationOther.type, "types must be equal");
            
            // for now it must be a platonic instance node
            Assert.Assert(template.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
            
            float matchingValue = featurePatternMatching.matchAnyNonRecursive(template, iterationOther, networkHandles);

            matchingPathElements = featurePatternMatching.matchAnyRecursive(template, iterationOther, networkHandles, Arrays.asList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
            matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

            if( matchingSimilarityValue > bestMatchingSimilarity )
            {
                bestMatchingSimilarity = matchingSimilarityValue;
                bestMatchingNode = iterationOther;
            }
        }

        Assert.Assert(bestMatchingNode != null, "");
        return bestMatchingNode;
    }
}
