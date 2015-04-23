package bpsolver.pattern;

import FargGeneral.network.Node;
import bpsolver.NetworkHandles;
import bpsolver.nodes.NodeTypes;
import java.util.ArrayList;
import misc.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 *
 * 
 */
public class MatchingUpdateImplementationForObjects implements IMatchingUpdate
{

    @Override
    public Pattern updateCore(Pattern orginal, Pattern additional, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching) {
        
        Assert.Assert(orginal.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(orginal.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        Assert.Assert(additional.exemplars.size() == 1, "size expected to be 1");
        Assert.Assert(additional.exemplars.get(0).type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
        
        // TODO< mark all nodes in additional as not marked >
        
        // TODO< iterate over all childs of orginal object and find the closest match in additional (which was not marked) >
        // for each closest match mark the node itself which was chosen >
        
        throw new NotImplementedException();
    }

    @Override
    public float match(Pattern a, Pattern b, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        // TODO
        throw new NotImplementedException();
    }


    private static Node findClosestMatchForSameTypesForPlatonicInstances(Node template, ArrayList<Node> others, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching)
    {
        Node bestMatchingNode;
        
        Assert.Assert(others.size() >= 1, "");
        
        for( Node iterationOther : others )
        {
            //Assert.Assert(!iterationOther.marked, "node must not be marked!");
            Assert.Assert(template.type == iterationOther.type, "types must be equal");
            
            // for now it must be a platonic instance node
            Assert.Assert(template.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "");
            
            float matchingValue = featurePatternMatching.matchAnyNonRecursive(template, iterationOther, networkHandles);
            
            // TODO< update best match if necessary >
            
            throw new NotImplementedException();
        }
    }
}
