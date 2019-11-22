/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.InternalTests;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.ITranslatorStrategy;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.NearIntersectionStrategy;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.pattern.FeaturePatternMatching;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.retina.SingleLineDetector;

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
    public static void main(final String[] args) {

		final var bpSolver = new Solver();
        bpSolver.setImageSize(new Vector2d<>(128, 128));
        bpSolver.setup();

		final List<RetinaPrimitive> lineDetectors = new ArrayList<>();

        lineDetectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{0.0f, 0.0f}), new ArrayRealVector(new double[]{10.0f, 40.0f}), 0.2)));

		final ITranslatorStrategy retinaToWorkspaceTranslatorStrategy = new NearIntersectionStrategy();

        final var objectNodes = retinaToWorkspaceTranslatorStrategy.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver);

        bpSolver.cycle(500);





        final var MAXDEPTH = 3;

		final var featurePatternMatching = new FeaturePatternMatching();

		final var matchingPathElements = featurePatternMatching.matchAnyRecursive(objectNodes.get(0), objectNodes.get(0), bpSolver.networkHandles, Collections.singletonList(Link.EnumType.CONTAINS), MAXDEPTH);
		final var matchingSimilarityValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);

        if( matchingSimilarityValue > 1.001f || matchingSimilarityValue < 0.999f ) {
            // test failed
            final var testFailed = 1;
        }

    }
}
