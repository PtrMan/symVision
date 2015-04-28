package InternalTests;

import Datastructures.Vector2d;
import FargGeneral.network.Link;
import FargGeneral.network.Node;
import RetinaLevel.RetinaPrimitive;
import RetinaLevel.SingleLineDetector;
import bpsolver.BpSolver;
import bpsolver.RetinaToWorkspaceTranslator.ITranslatorStrategy;
import bpsolver.RetinaToWorkspaceTranslator.NearIntersectionStrategy;
import bpsolver.pattern.FeaturePatternMatching;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class TestPatternMatching
{
    /**
     *
     * tests if the pattern matching recognizes the same line as the same object
     *
     */
    public static void main(String[] args)
    {
        List<RetinaPrimitive> lineDetectors;

        BpSolver bpSolver;

        bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<Integer>(100, 100));
        bpSolver.setup();

        lineDetectors = new ArrayList<>();

        lineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new Vector2d<>(0.0f, 0.0f), new Vector2d<>(10.0f, 40.0f))));

        ITranslatorStrategy retinaToWorkspaceTranslatorStrategy;

        retinaToWorkspaceTranslatorStrategy = new NearIntersectionStrategy();

        List<Node> objectNodes = retinaToWorkspaceTranslatorStrategy.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver);

        bpSolver.cycle(500);





        final int MAXDEPTH = 3;

        FeaturePatternMatching featurePatternMatching;

        featurePatternMatching = new FeaturePatternMatching();

                List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
                float matchingSimilarityValue;

                matchingPathElements = featurePatternMatching.matchAnyRecursive(objectNodes.get(0), objectNodes.get(0), bpSolver.networkHandles, Arrays.asList(Link.EnumType.CONTAINS), MAXDEPTH);
                matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

        if( matchingSimilarityValue > 1.001f || matchingSimilarityValue < 0.999f )
        {
            // test failed
            int testFailed = 1;
        }

    }
}
