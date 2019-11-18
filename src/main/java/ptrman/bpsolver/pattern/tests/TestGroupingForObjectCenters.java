/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.pattern.tests;

import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.nodes.NumeriosityNode;
import ptrman.bpsolver.pattern.Grouping;
import ptrman.bpsolver.pattern.IMatchingUpdate;
import ptrman.bpsolver.pattern.MatchingUpdateImplementationForObjectCenters;
import ptrman.bpsolver.pattern.Pattern;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * 
 */
public enum TestGroupingForObjectCenters
{
    ;

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

        Collection<Pattern> exemplars = new ArrayList<>();

        IMatchingUpdate matchingUpdateImplementation = new MatchingUpdateImplementationForObjectCenters(10.0f, 0.6f);
        
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
