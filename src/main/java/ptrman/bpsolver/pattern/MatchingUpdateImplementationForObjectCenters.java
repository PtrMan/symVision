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

import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.NetworkHandles;

import java.util.ArrayList;

import static ptrman.Datastructures.Vector2d.FloatHelper.*;

/**
 * Implementation for grouping based on the barycenter (center) of objects
 * 
 * used for the more standard "custering" with the general clustering/pattern formation/analogy algorithm
 *
 */
public class MatchingUpdateImplementationForObjectCenters implements IMatchingUpdate
{
    /**
     * Pattern which stores the (cluster) center and the weight
     * 
     */
    public static class PatternWithCenterAndMass extends Pattern
    {
        public Vector2d<Float> clusterCenter;
        
        public float getWeight()
        {
            return exemplars.size();
        }
    }
    
    public MatchingUpdateImplementationForObjectCenters(final float absoluteClusteringDistance, final float clusteringThreshold)
    {
        this.absoluteClusteringDistance = absoluteClusteringDistance;
        this.clusteringThreshold = clusteringThreshold;
    }
    
    @Override
    public Pattern updateCore(final Pattern orginal, final Pattern additional, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {

        final boolean value1 = orginal instanceof PatternWithCenterAndMass;
        assert value1 : "ASSERT: " + "";
        final boolean value = additional instanceof PatternWithCenterAndMass;
        assert value : "ASSERT: " + "";

        final var orginalWithCenterAndMass = (PatternWithCenterAndMass) orginal;
        final var additionalWithCenterAndMass = (PatternWithCenterAndMass) additional;
        
        // deep copy exemplars from orginal and additional
        final var createdPattern = new PatternWithCenterAndMass();
        createdPattern.exemplars = new ArrayList<>();
		createdPattern.exemplars.addAll(orginal.exemplars);
		createdPattern.exemplars.addAll(additional.exemplars);

        Vector2d<Float> newCenter = add(getScaled(orginalWithCenterAndMass.clusterCenter, orginalWithCenterAndMass.getWeight()), getScaled(additionalWithCenterAndMass.clusterCenter, additionalWithCenterAndMass.getWeight()));
        newCenter = getScaled(newCenter, 1.0f/(orginalWithCenterAndMass.getWeight()+additionalWithCenterAndMass.getWeight()));
        
        createdPattern.clusterCenter = newCenter;
        
        return createdPattern;
    }

    @Override
    public float match(final Pattern a, final Pattern b, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {

        final boolean value1 = a instanceof PatternWithCenterAndMass;
        assert value1 : "ASSERT: " + "";
        final boolean value = b instanceof PatternWithCenterAndMass;
        assert value : "ASSERT: " + "";

        final var aWithCenterAndMass = (PatternWithCenterAndMass) a;
        final var bWithCenterAndMass = (PatternWithCenterAndMass) b;

        final var diff = sub(aWithCenterAndMass.clusterCenter, bWithCenterAndMass.clusterCenter);
        final var distance = (float) Math.sqrt(diff.x * diff.x + diff.y * diff.y);
        
        if( distance > absoluteClusteringDistance )
            return 0.0f;

        final var absoluteMaximalDistanceForNullRating = absoluteClusteringDistance * (1.0f / clusteringThreshold);
        return 1.0f - (distance / absoluteMaximalDistanceForNullRating);
    }
    
    
    private final float absoluteClusteringDistance;
    private final float clusteringThreshold;
}
