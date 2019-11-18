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
import static ptrman.Datastructures.Vector2d.FloatHelper.sub;
import static ptrman.Datastructures.Vector2d.FloatHelper.add;
import static ptrman.Datastructures.Vector2d.FloatHelper.getScaled;

import ptrman.bpsolver.NetworkHandles;
import java.util.ArrayList;
import ptrman.misc.Assert;

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
    
    public MatchingUpdateImplementationForObjectCenters(float absoluteClusteringDistance, float clusteringThreshold)
    {
        this.absoluteClusteringDistance = absoluteClusteringDistance;
        this.clusteringThreshold = clusteringThreshold;
    }
    
    @Override
    public Pattern updateCore(Pattern orginal, Pattern additional, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        Vector2d<Float> newCenter;
        
        Assert.Assert(orginal instanceof PatternWithCenterAndMass, "");
        Assert.Assert(additional instanceof PatternWithCenterAndMass, "");

        PatternWithCenterAndMass orginalWithCenterAndMass = (PatternWithCenterAndMass) orginal;
        PatternWithCenterAndMass additionalWithCenterAndMass = (PatternWithCenterAndMass) additional;
        
        // deep copy exemplars from orginal and additional
        PatternWithCenterAndMass createdPattern = new PatternWithCenterAndMass();
        createdPattern.exemplars = new ArrayList<>();
		createdPattern.exemplars.addAll(orginal.exemplars);
		createdPattern.exemplars.addAll(additional.exemplars);
        
        newCenter = add(getScaled(orginalWithCenterAndMass.clusterCenter, orginalWithCenterAndMass.getWeight()), getScaled(additionalWithCenterAndMass.clusterCenter, additionalWithCenterAndMass.getWeight()));
        newCenter = getScaled(newCenter, 1.0f/(orginalWithCenterAndMass.getWeight()+additionalWithCenterAndMass.getWeight()));
        
        createdPattern.clusterCenter = newCenter;
        
        return createdPattern;
    }

    @Override
    public float match(Pattern a, Pattern b, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {

        Assert.Assert(a instanceof PatternWithCenterAndMass, "");
        Assert.Assert(b instanceof PatternWithCenterAndMass, "");

        PatternWithCenterAndMass aWithCenterAndMass = (PatternWithCenterAndMass) a;
        PatternWithCenterAndMass bWithCenterAndMass = (PatternWithCenterAndMass) b;

        Vector2d<Float> diff = sub(aWithCenterAndMass.clusterCenter, bWithCenterAndMass.clusterCenter);
        float distance = (float) Math.sqrt(diff.x * diff.x + diff.y * diff.y);
        
        if( distance > absoluteClusteringDistance )
        {
            return 0.0f;
        }

        float absoluteMaximalDistanceForNullRating = absoluteClusteringDistance * (1.0f / clusteringThreshold);
        return 1.0f - (distance / absoluteMaximalDistanceForNullRating);
    }
    
    
    private final float absoluteClusteringDistance;
    private final float clusteringThreshold;
}
