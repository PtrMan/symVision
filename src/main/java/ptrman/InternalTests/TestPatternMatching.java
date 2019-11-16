package ptrman.InternalTests;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.retina.SingleLineDetector;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.ITranslatorStrategy;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.NearIntersectionStrategy;
import ptrman.bpsolver.pattern.FeaturePatternMatching;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 */
public enum TestPatternMatching {
	;

	/**
     *
     * tests if the pattern matching recognizes the same line as the same object
     *
     */
    public static void main(String[] args) {
        List<RetinaPrimitive> lineDetectors;

        BpSolver bpSolver;

        bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<>(100, 100));
        bpSolver.setup();

        lineDetectors = new ArrayList<>();

        lineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{0.0f, 0.0f}), new ArrayRealVector(new double[]{10.0f, 40.0f}))));

        ITranslatorStrategy retinaToWorkspaceTranslatorStrategy;

        retinaToWorkspaceTranslatorStrategy = new NearIntersectionStrategy();

        List<Node> objectNodes = retinaToWorkspaceTranslatorStrategy.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver);

        bpSolver.cycle(500);





        final int MAXDEPTH = 3;

        FeaturePatternMatching featurePatternMatching;

        featurePatternMatching = new FeaturePatternMatching();

                List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
                float matchingSimilarityValue;

                matchingPathElements = featurePatternMatching.matchAnyRecursive(objectNodes.get(0), objectNodes.get(0), bpSolver.networkHandles, Collections.singletonList(Link.EnumType.CONTAINS), MAXDEPTH);
                matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

        if( matchingSimilarityValue > 1.001f || matchingSimilarityValue < 0.999f ) {
            // test failed
            int testFailed = 1;
        }

    }
}
