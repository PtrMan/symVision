package ptrman.bpsolver.pattern;

import ptrman.Datastructures.Vector2d;
import static ptrman.Datastructures.Vector2d.FloatHelper.sub;
import static ptrman.Datastructures.Vector2d.FloatHelper.add;
import static ptrman.Datastructures.Vector2d.FloatHelper.getScaled;
import ptrman.FargGeneral.network.Node;
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
        PatternWithCenterAndMass createdPattern;
        PatternWithCenterAndMass orginalWithCenterAndMass;
        PatternWithCenterAndMass additionalWithCenterAndMass;
        Vector2d<Float> newCenter;
        
        Assert.Assert(orginal instanceof PatternWithCenterAndMass, "");
        Assert.Assert(additional instanceof PatternWithCenterAndMass, "");
        
        orginalWithCenterAndMass = (PatternWithCenterAndMass)orginal;
        additionalWithCenterAndMass = (PatternWithCenterAndMass)additional;
        
        // deep copy exemplars from orginal and additional
        createdPattern = new PatternWithCenterAndMass();
        createdPattern.exemplars = new ArrayList<>();
        for( Node node : orginal.exemplars )
        {
            createdPattern.exemplars.add(node);
        }
        for( Node node : additional.exemplars )
        {
            createdPattern.exemplars.add(node);
        }
        
        newCenter = add(getScaled(orginalWithCenterAndMass.clusterCenter, orginalWithCenterAndMass.getWeight()), getScaled(additionalWithCenterAndMass.clusterCenter, additionalWithCenterAndMass.getWeight()));
        newCenter = getScaled(newCenter, 1.0f/(orginalWithCenterAndMass.getWeight()+additionalWithCenterAndMass.getWeight()));
        
        createdPattern.clusterCenter = newCenter;
        
        return createdPattern;
    }

    @Override
    public float match(Pattern a, Pattern b, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        PatternWithCenterAndMass aWithCenterAndMass;
        PatternWithCenterAndMass bWithCenterAndMass;
        Vector2d<Float> diff;
        float distance;
        float absoluteMaximalDistanceForNullRating;
        
        Assert.Assert(a instanceof PatternWithCenterAndMass, "");
        Assert.Assert(b instanceof PatternWithCenterAndMass, "");
        
        aWithCenterAndMass = (PatternWithCenterAndMass)a;
        bWithCenterAndMass = (PatternWithCenterAndMass)b;
        
        diff = sub(aWithCenterAndMass.clusterCenter, bWithCenterAndMass.clusterCenter);
        distance = (float)Math.sqrt(diff.x*diff.x + diff.y*diff.y);
        
        if( distance > absoluteClusteringDistance )
        {
            return 0.0f;
        }
        
        absoluteMaximalDistanceForNullRating = absoluteClusteringDistance*(1.0f/clusteringThreshold);
        return 1.0f - (distance / absoluteMaximalDistanceForNullRating);
    }
    
    
    private float absoluteClusteringDistance;
    private float clusteringThreshold;
}
