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

import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;

import java.util.Collection;
import java.util.Collections;

/**
 *
 * 
 */
public class MatchingUpdateImplementationForObjects implements IMatchingUpdate
{

    @Override
    public Pattern updateCore(final Pattern orginal, final Pattern additional, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {

        assert orginal.exemplars.size() == 1 : "ASSERT: " + "size expected to be 1";
        assert orginal.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";
        assert additional.exemplars.size() == 1 : "ASSERT: " + "size expected to be 1";
        assert additional.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";

        // OLD TODO
        // < mark all nodes in additional as not marked >
        // < iterate over all childs of orginal object and find the closest match in additional (which was not marked) >
        // for each closest match mark the node itself which was chosen >

        // match it recursivly and
        // as described in
        // Foundalis dissertation 8.3.3 Pattern updating
        // * match it recursivly
        // * strengthen links

		final var matchingPathElements = featurePatternMatching.matchAnyRecursive(orginal.exemplars.get(0), additional.exemplars.get(0), networkHandles, Collections.singletonList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);

        var currentMatchNodeOrginal = orginal.exemplars.get(0);
        var currentMatchNodeAdditional = additional.exemplars.get(0);

        for(int currentMatchPathElementI = 0; currentMatchPathElementI < matchingPathElements.size()-1; currentMatchPathElementI++ )
        {

			final var currentMatchPathElement = matchingPathElements.get(currentMatchPathElementI);

            currentMatchNodeOrginal = currentMatchPathElement.bestMatchA.target;
            currentMatchNodeAdditional = currentMatchPathElement.bestMatchB.target;
        }

        // strengthen links

		final var currentMatchLastPathElement = matchingPathElements.get(matchingPathElements.size() - 1);

        final var additionalStrength = 0.1f;
        currentMatchLastPathElement.bestMatchA.strength += additionalStrength;
        currentMatchLastPathElement.bestMatchB.strength += additionalStrength;
        // TODO< limit it in some way? >

        // update the statistics
        // TODO< update the statistics of the other FeatureNodes along the way and at the bottom ? >

		final var lastOrginalNode = currentMatchLastPathElement.bestMatchA.target;
		final var lastAdditionalNode = currentMatchLastPathElement.bestMatchB.target;

        // NOTE< should we just build around this case with an if? >
        assert lastOrginalNode.type == NodeTypes.EnumType.FEATURENODE.ordinal() : "ASSERT: " + "lastOrginalNode is not a featurenode as expected";

        final var lastOrginalNodeAsFeatureNode = (FeatureNode) lastOrginalNode;
		final var lastAdditionalNodeAsFeatureNode = (FeatureNode) lastAdditionalNode;

        lastOrginalNodeAsFeatureNode.statistics.addValuesFromStatistics(lastAdditionalNodeAsFeatureNode.statistics);

        // simply return (the recombined) orginal...
        return orginal;
    }

    @Override
    public float match(final Pattern a, final Pattern b, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {

        assert a.exemplars.size() == 1 : "ASSERT: " + "size expected to be 1";
        assert a.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";
        assert b.exemplars.size() == 1 : "ASSERT: " + "size expected to be 1";
        assert b.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";

        final var matchingPathElements = featurePatternMatching.matchAnyRecursive(a.exemplars.get(0), b.exemplars.get(0), networkHandles, Collections.singletonList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
		final var matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

        return matchingSimilarityValue;
    }


    private static Node findClosestMatchForSameTypesForPlatonicInstances(final Node template, final Collection<Node> others, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {

        assert others.size() >= 1 : "ASSERT: " + "";

        Node bestMatchingNode = null;
        var bestMatchingSimilarity = 0.0f;

        for( final var iterationOther : others )
        {

			//Assert.Assert(!iterationOther.marked, "node must not be marked!");
            assert template.type == iterationOther.type : "ASSERT: " + "types must be equal";

            // for now it must be a platonic instance node
            assert template.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "";

            final var matchingValue = featurePatternMatching.matchAnyNonRecursive(template, iterationOther, networkHandles);

			final var matchingPathElements = featurePatternMatching.matchAnyRecursive(template, iterationOther, networkHandles, Collections.singletonList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
			final var matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

            if( matchingSimilarityValue > bestMatchingSimilarity )
            {
                bestMatchingSimilarity = matchingSimilarityValue;
                bestMatchingNode = iterationOther;
            }
        }

        assert bestMatchingNode != null : "ASSERT: " + "";
        return bestMatchingNode;
    }
}
