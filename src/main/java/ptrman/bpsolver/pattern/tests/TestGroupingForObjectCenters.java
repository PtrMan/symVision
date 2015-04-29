package ptrman.bpsolver.pattern.tests;

import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.nodes.NumeriosityNode;
import ptrman.bpsolver.pattern.Grouping;
import ptrman.bpsolver.pattern.MatchingUpdateImplementationForObjectCenters;
import ptrman.bpsolver.pattern.Pattern;
import java.util.ArrayList;

/**
 *
 * 
 */
public class TestGroupingForObjectCenters
{
    public static void main(String[] args)
    {
        test();
    }
    
    public static void test()
    {
        testClustering1();
    }
    
    private static void testClustering1()
    {
        // for testing we use the numerosity node as content to tell the elements apart
        
        ArrayList<Pattern> exemplars;
        MatchingUpdateImplementationForObjectCenters matchingUpdateImplementation;
        
        exemplars = new ArrayList<>();
        
        matchingUpdateImplementation = new MatchingUpdateImplementationForObjectCenters(10.0f, 0.6f);
        
        MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass patternA = new MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass();
        patternA.clusterCenter = new Vector2d<>(5.0f, 0.0f);
        patternA.exemplars = new ArrayList<>();
        patternA.exemplars.add(new NumeriosityNode(null));
        ((NumeriosityNode)patternA.exemplars.get(0)).numerosity = 0;
        exemplars.add(patternA);
        
        MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass patternB = new MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass();
        patternB.clusterCenter = new Vector2d<>(6.0f, 0.0f);
        patternB.exemplars = new ArrayList<>();
        patternB.exemplars.add(new NumeriosityNode(null));
        ((NumeriosityNode)patternB.exemplars.get(0)).numerosity = 1;
        exemplars.add(patternB);
        
        // pattern C is external
        MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass patternC = new MatchingUpdateImplementationForObjectCenters.PatternWithCenterAndMass();
        patternC.clusterCenter = new Vector2d<>(17.0f, 0.0f);
        patternC.exemplars = new ArrayList<>();
        patternC.exemplars.add(new NumeriosityNode(null));
        ((NumeriosityNode)patternC.exemplars.get(0)).numerosity = 2;
        exemplars.add(patternC);
        
        Grouping.group(exemplars, 0.7f, null, matchingUpdateImplementation, null);
    }
}
