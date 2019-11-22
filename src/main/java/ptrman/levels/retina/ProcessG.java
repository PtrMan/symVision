/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina;

import com.google.common.collect.ImmutableSet;
import org.apache.commons.math3.linear.*;
import ptrman.Datastructures.IMap2d;
import ptrman.bpsolver.HardParameters;
import ptrman.math.ArrayRealVectorHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static ptrman.bpsolver.Helper.isNeightborhoodPixelSet;
import static ptrman.levels.retina.LineDetectorWithMultiplePoints.real;
import static ptrman.math.ArrayRealVectorHelper.*;

/** curve detection
 *
 * 
 */
public class ProcessG {
    public static class Curve {
        public Curve(final List<CurveElement> curveElements) {
            this.curveElements = curveElements;
        }
        
        public final List<CurveElement> curveElements;
        
        public ArrayRealVector getNormalizedTangentAtEndpoint(final int index) {
            assert index >= 0 && index <= 1 : "ASSERT: " + "";

            return index == 0 ? curveElements.get(0).calcTangent(0.0f) : curveElements.get(curveElements.size() - 1).calcTangent(1.0f);
        }
        
        public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(final ArrayRealVector point) {
            // TODO< other strategy for figuring out if the point is *at* the line and not lear the endpoints >

            var diff = calcPosition(0.0f).subtract(point);
            final var distToBegin = diff.getNorm();
            
            diff = calcPosition(1.0f).subtract(point);
            final var distToEnd = diff.getNorm();

            return distToBegin < distToEnd ? Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN : Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
        }
        
        public ArrayRealVector calcPosition(final float t) {

            final var t2 = t * (float) curveElements.size();

            final var rem = t2 % 1.0f;
            final var index = Math.round(t2);
            
            return curveElements.get(index).calcPosition(rem);
        }
    }
    
    
    // test, works
    public static void testPoints() {

        final List<ArrayRealVector> testPoints = new ArrayList<>();
        testPoints.add(new ArrayRealVector(new double[]{1.0f, 5.0f}));
        testPoints.add(new ArrayRealVector(new double[]{1.8f, 4.0f}));
        testPoints.add(new ArrayRealVector(new double[]{2.0f, 7.0f}));

        // works fine
        final var resultCurve = calculatePolynominalsAndReturnCurve(testPoints);
    }
    
    
    public static class CurveElement {
        public CurveElement(final float a0, final float a1, final float a2, final float a3, final float b0, final float b1, final float b2, final float b3) {
            a = new float[]{a0, a1, a2, a3};
            b = new float[]{b0, b1, b2, b3};
        }
        
        // parametric curve parameters
        private final float[] a;
        private final float[] b;
        
        /**
         * 
         * result is normalized
         * 
         * \param t
         * \return 
         */
        public ArrayRealVector calcTangent(final double t) {

            assert t >= 0.0 && t <= 1.0 : "ASSERT: " + "t not in range";

            final var TEPSILON = 0.0001;
            final double t2 = t > 0.5 ? t - TEPSILON : t + TEPSILON;

            final var p1 = calcPosition(t);
            final var p2 = calcPosition(t2);
            final var diff = p1.subtract(p2);
            return normalize(diff);
        }
        
        public ArrayRealVector calcPosition(final double t) {
            assert t >= 0.0 && t <= 1.0 : "ASSERT: " + "t not in range";

            final var x = a[0] + t*a[1] + t*t*a[2] + t*t*t*a[3];
            final var y = b[0] + t*b[1] + t*t*b[2] + t*t*t*b[3];
            
            return new ArrayRealVector(new double[]{x, y});
        }
    }
    
    public void process(final ArrayList<SingleLineDetector> lineDetectors, final Iterable<ProcessM.LineParsing> lineParsings, final Iterable<ProcessA.Sample> samples, final IMap2d<Boolean> image) {
        resultCurves.clear();
        
        rerateLineParsings(lineParsings);
        
        // for each timeslice we get we select n times randomly "the best"(after the rating of the lineparsing) a lineparsing
        // then we try to convert a part or all of it to a curve
        
        // not that we add and remove lineparsings, because it has no advantage to have a lineparsing and a curve which do overlap
        // it adds also new lineparsings because lineparsings can be divided into curves
        
        // for now we do this for *all* curves
        // TODO< select by rng >

        for (final var lineParsing : lineParsings) {

            final var currentLineParsing = lineParsing;


            // try to covert (at least) a part of the lineParsing to a curve

            final Collection<List<ArrayRealVector>> protocurves = new ArrayList<>();
            List<ArrayRealVector> currentProtocurve = null;

            for (var pointIndex = 1; pointIndex < currentLineParsing.lineParsing.size() - 1; pointIndex++) {
                final var atLeastOneSampleNotNearAdjacentLines = examineVincityOfSegmentPoint(pointIndex, currentLineParsing, samples);

                final var IsParsingACurve = currentProtocurve != null;

                if (IsParsingACurve) if (atLeastOneSampleNotNearAdjacentLines) {
                    // add segment to curve and mark last segment as in a curve
                    currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex - 1).getBProjected());

                    currentLineParsing.lineParsing.get(pointIndex - 1).markedPartOfCurve = true;
                } else {
                    // finish lineparsing
                    protocurves.add(currentProtocurve);
                    currentProtocurve = null;
                }
                else if (atLeastOneSampleNotNearAdjacentLines) {
                    // begin a new lineparsing
                    currentProtocurve = new ArrayList<>();

                    currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex - 1).getBProjected());

                    currentLineParsing.lineParsing.get(pointIndex - 1).markedPartOfCurve = true;
                } else {
                    // do nothing
                }
            }

            var IsParsingACurve = currentProtocurve != null;

            if (IsParsingACurve) {
                protocurves.add(currentProtocurve);
                currentProtocurve = null;
                IsParsingACurve = false;
            }

            // convert protocurves to real curves

            for (final var iterationProtoCurve : protocurves) {

                final var createdCurve = calculatePolynominalsAndReturnCurve(iterationProtoCurve);
                resultCurves.add(createdCurve);
            }
        }
        
        // remove segments which are part of curves
        // we don't touch the lineparsings here, because they are not visible from the cognitive layer
        removeLinedetectorsWhichWereUsedInCurves(lineDetectors);
        
        // calculate intersections of curves with lines and curves
        // ASK< does this belong into process E or another process? >
        recalculateIntersections(lineDetectors, resultCurves, image);
    }
    
    // calculates only intersections of tangents
    // TODO< other intersections >
    private static void recalculateIntersections(final Iterable<SingleLineDetector> lineDetectors, final Iterable<Curve> curves, final IMap2d<Boolean> image) {
        // intersections between curves and lines
        
        for( final var iterationCurve : curves ) {

            final var beginCurveElement = iterationCurve.curveElements.get(0);
            final var endCurveElement = iterationCurve.curveElements.get(iterationCurve.curveElements.size() - 1);
            
            // TODO ASK< maybe we have to search the ending with the minimal x position? >
            // TODO< compute confidence >
            final var tempBeginCurveTangentLine = SingleLineDetector.createFromFloatPositions(beginCurveElement.calcPosition(0.0f), beginCurveElement.calcPosition(0.0f).add(beginCurveElement.calcTangent(0.0f)), 0.0);
            final var tempEndCurveTangentLine = SingleLineDetector.createFromFloatPositions(endCurveElement.calcPosition(1.0f), endCurveElement.calcPosition(1.0f).add(endCurveElement.calcTangent(1.0f)), 0.0);
            
            final var curveBeginM = tempBeginCurveTangentLine.getM();
            final var curveBeginN = tempBeginCurveTangentLine.getN();

            final var curveEndM = tempEndCurveTangentLine.getM();
            final var curveEndN = tempEndCurveTangentLine.getN();
            
            for( final var iterationLineDetector : lineDetectors ) {
                final var intersectionPositionBegin = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveBeginM, curveBeginN);
                final var intersectionPositionEnd = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveEndM, curveEndN);
                
                // examine the intersection positions for inside the image and the neightborhood
                
                if(
                    image.inBounds(arrayRealVectorToInteger(intersectionPositionBegin, ArrayRealVectorHelper.EnumRoundMode.DOWN)) &&
                    isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPositionBegin, ArrayRealVectorHelper.EnumRoundMode.DOWN), image)
                ) {

                    final var createdIntersection = new Intersection(intersectionPositionBegin,
                        new Intersection.IntersectionPartner(RetinaPrimitive.makeCurve(iterationCurve), iterationCurve.getIntersectionEndpoint(intersectionPositionBegin)),
                        new Intersection.IntersectionPartner(RetinaPrimitive.makeLine(iterationLineDetector), iterationLineDetector.getIntersectionEndpoint(intersectionPositionBegin))
                    );

                    iterationLineDetector.intersections.add(createdIntersection);
                }
                
                if(
                        image.inBounds(arrayRealVectorToInteger(intersectionPositionEnd, ArrayRealVectorHelper.EnumRoundMode.DOWN)) &&
                                isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPositionEnd, ArrayRealVectorHelper.EnumRoundMode.DOWN), image)
                ) {

                    final var createdIntersection = new Intersection(intersectionPositionEnd,
                        new Intersection.IntersectionPartner(RetinaPrimitive.makeCurve(iterationCurve), iterationCurve.getIntersectionEndpoint(intersectionPositionEnd)),
                        new Intersection.IntersectionPartner(RetinaPrimitive.makeLine(iterationLineDetector), iterationLineDetector.getIntersectionEndpoint(intersectionPositionEnd))
                    );

                    iterationLineDetector.intersections.add(createdIntersection);
                }
                
                // TODO LOW line curve middle intersection
                // (we need to segmentate the curve as many small lines and do a intersection test 
            }
        }
        
        // intersections between curves and curves
        
        // begin/end
        // TODO HIGH
        
        // middle section(s)
        // TODO MEDIUM
    }

    
    public List<Curve> getResultCurves() {
        return resultCurves;
    }
    
    private static void removeLinedetectorsWhichWereUsedInCurves(final ArrayList<SingleLineDetector> lineDetectors) {
        for(var lineDetectorI = 0; lineDetectorI < lineDetectors.size(); lineDetectorI++ ) {

            final var currentLineDetector = lineDetectors.get(lineDetectorI);
            
            if( currentLineDetector.markedPartOfCurve ) {
                // before we remove it we have to make sure it doesn't get referenced in intersections
                removeLineDetectorFromNeightborIntersections(currentLineDetector);
                
                lineDetectors.remove(lineDetectorI);
                lineDetectorI--;
                continue;
            }
        }
    }
    
    private static void removeLineDetectorFromNeightborIntersections(final SingleLineDetector lineDetector) {
        for( final var iterationIntersection : lineDetector.intersections ) {
            assert iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";
            assert iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";

            if( iterationIntersection.p0.primitive.line.equals(lineDetector) )
                removeIntersectionBetweenLines(iterationIntersection.p1.primitive.line, lineDetector);
            else if( iterationIntersection.p1.primitive.line.equals(lineDetector) )
                removeIntersectionBetweenLines(iterationIntersection.p0.primitive.line, lineDetector);
        }
    }

    private static void removeIntersectionBetweenLines(final SingleLineDetector lineA, final SingleLineDetector lineB) {
        for(var intersectionI = 0; intersectionI < lineA.intersections.size(); intersectionI++ ) {

            final var iterationIntersection = lineA.intersections.get(intersectionI);

            assert iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";
            assert iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";

            if( iterationIntersection.p0.primitive.line.equals(lineB) ) {
                lineA.intersections.remove(intersectionI);
                break;
            }
            else if( iterationIntersection.p1.primitive.line.equals(lineB) ) {
                lineA.intersections.remove(intersectionI);
                break;
            }
        }
        
        
        for(var intersectionI = 0; intersectionI < lineB.intersections.size(); intersectionI++ ) {

            final var iterationIntersection = lineB.intersections.get(intersectionI);

            assert iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";
            assert iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT : "ASSERT: " + "must be line";

            if( iterationIntersection.p0.primitive.line.equals(lineA) ) {
                lineB.intersections.remove(intersectionI);
                break;
            }
            else if( iterationIntersection.p1.primitive.line.equals(lineA) ) {
                lineB.intersections.remove(intersectionI);
                break;
            }
        }
    }
    
    private static void rerateLineParsings(final Iterable<ProcessM.LineParsing> lineParsings) {
        for( final var iterationLineParsing : lineParsings ) rerateLineParsing(iterationLineParsing);
    }
    
    private static void rerateLineParsing(final ProcessM.LineParsing lineParsing) {

        if( lineParsing.processGRated ) return;
        
        lineParsing.processGInterestRating = 0.0f;
        
        for(int lineDetectorI = 0; lineDetectorI < lineParsing.lineParsing.size()-1; lineDetectorI++ ) {

            final var iterationLineDetector = lineParsing.lineParsing.get(lineDetectorI);
            final var nextLineDetector = lineParsing.lineParsing.get(lineDetectorI + 1);
            
            // length criteria
            
            final var length = iterationLineDetector.getLength();
            
            // TODO< configurable constant >
            lineParsing.processGInterestRating += (1.0f/length);
            
            // angle criteria
            
            final var angleBetweenSegments = SingleLineDetector.getAngleBetween(iterationLineDetector, nextLineDetector);
            lineParsing.processGInterestRating += (90.0f-angleBetweenSegments)*HardParameters.ProcessG.RATINGANGLEMULTIPLIER;
            
            // meeting end to end criteria
            
            // we assume that the point a is the first and point b is the last

            final var endToEndDistance = iterationLineDetector.b.getDistance(nextLineDetector.a);

            float endToEndRating = (float) Math.max(0.0f, (HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE - endToEndDistance) / HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE);
            endToEndRating *= HardParameters.ProcessG.RATINGENDTOENDMULTIPLIER;
            lineParsing.processGInterestRating += endToEndRating;
        }
        
        lineParsing.processGRated = true;
    }
    
    
    
    
    private static boolean examineVincityOfSegmentPoint(final int pointIndex, final ProcessM.LineParsing lineParsing, final Iterable<ProcessA.Sample> samples) {
        final var centerPoint = lineParsing.lineParsing.get(pointIndex).getBProjected();
        final var endosceletonSamplesInVicinity = queryEndosceletonPointsInVicinityOf(samples, centerPoint);
        final var neightborLinesOfPoint = getNeightborLinesOfPoint(pointIndex, lineParsing);
        final var atLeastOneSampleNotNearLine = !areAllSamplesNearLines(endosceletonSamplesInVicinity, neightborLinesOfPoint, HardParameters.ProcessG.MAXIMALDISTANCEOFENDOSCELETONTOLINE);
        
        return atLeastOneSampleNotNearLine;
    }
    
    private static List<ProcessA.Sample> queryEndosceletonPointsInVicinityOf(final Iterable<ProcessA.Sample> samples, final ArrayRealVector centerPoint) {
        return queryPointsInRadius(samples, centerPoint, HardParameters.ProcessG.VICINITYRADIUS, new ProcessA.Sample.EnumType[]{ProcessA.Sample.EnumType.ENDOSCELETON});
    }
    
    private static List<ProcessA.Sample> queryPointsInRadius(final Iterable<ProcessA.Sample> samples, final ArrayRealVector centerPoint, final double radius, final ProcessA.Sample.EnumType[] typeFilterCriteria) {

        final Set<ProcessA.Sample.EnumType> filterCriteria = ImmutableSet.copyOf(typeFilterCriteria);

        final List<ProcessA.Sample> samplesInRadius = new ArrayList<>();

        // TODO< query the points in the radius with a optimized spartial scheme >
        for( final var iterationSample : samples ) {
            final var distance = distance(iterationSample.position, centerPoint);

            if( distance < radius && filterCriteria.contains(iterationSample.type))
                samplesInRadius.add(iterationSample);

        }
        
        return samplesInRadius;
    }
    
    private static List<SingleLineDetector> getNeightborLinesOfPoint(final int pointIndex, final ProcessM.LineParsing lineParsing) {

        final List<SingleLineDetector> resultLines = new ArrayList<>();
        
        resultLines.add(lineParsing.lineParsing.get(pointIndex-1));
        resultLines.add(lineParsing.lineParsing.get(pointIndex));
        
        return resultLines;
    }
    
    /**
     * checks if all samples are near at least one of the lines
     * if this is not the case for one sample, it returns false
     *  
     */
    private static boolean areAllSamplesNearLines(final Iterable<ProcessA.Sample> samples, final Iterable<SingleLineDetector> lines, final double maximalDistance) {
        for( final var iterationSample : samples ) {
            final var iterationSamplePosition = real(iterationSample.position);

            var sampleNearAnyLine = false;


            final var tmp = new ArrayRealVector(2);
            for( final var iterationLine : lines ) {
                final var projectedPointPosition = iterationLine.projectPointOntoLine(iterationSamplePosition, tmp);
                final var projectedPointInsideLine = iterationLine.isXOfPointInLine(projectedPointPosition);
                
                // ignore if it is not on the line
                if( !projectedPointInsideLine ) continue;

                final var distanceOfPointToLine = projectedPointPosition.getDistance(iterationSamplePosition);
                
                if( distanceOfPointToLine < maximalDistance ) {
                    sampleNearAnyLine = true;
                    continue;
                }
            }
            
            if( !sampleNearAnyLine ) return false;
        }
        
        return true;
    }

    
    
    
    private static Curve calculatePolynominalsAndReturnCurve(final List<ArrayRealVector> points) {

        final var solvedA_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.X);
        final var solvedA_1_i = ProcessG.calculate_1_i(points, solvedA_2_i, EnumAxis.X);
        final var solvedA_3_i = ProcessG.calculate_2_i(points, solvedA_2_i);
        final var solvedA_0_i = ProcessG.calculate_0_i(points, EnumAxis.X);

        final var solvedB_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.Y);
        final var solvedB_1_i = ProcessG.calculate_1_i(points, solvedB_2_i, EnumAxis.Y);
        final var solvedB_3_i = ProcessG.calculate_2_i(points, solvedB_2_i);
        final var solvedB_0_i = ProcessG.calculate_0_i(points, EnumAxis.Y);
        
        return new Curve(createCurves(solvedA_0_i, solvedA_1_i, solvedA_2_i, solvedA_3_i, solvedB_0_i, solvedB_1_i, solvedB_2_i, solvedB_3_i));
    }
    
    // builds a linear equation for the a|b_2,i values and returns the coefficients
    // NOTE< points must be for sure sorted by x axis? >
    private static RealVector solveLinearEquationFor2ForPoints(final List<ArrayRealVector> points, final EnumAxis axis) {

        // math libary usage see http://commons.apache.org/proper/commons-math/userguide/linear.html

        final var matrix = new Array2DRowRealMatrix(points.size(), points.size());
        
        // populate matrix
        
        //  top and bottom
        matrix.setEntry(0, 0, 1.0);
        matrix.setEntry(points.size()-1, points.size()-1, 1.0);
        
        // middle
        int i;
        for(i = 1; i < points.size()-1; i++ ) {
            matrix.setEntry(i, i-1, 1.0);
            matrix.setEntry(i, i-1+1, 4.0);
            matrix.setEntry(i, i-1+2, 1.0);
        }
        
        // populate constants

        final RealVector constants = new ArrayRealVector(points.size());
        constants.setEntry(0, 0.0);
        constants.setEntry(points.size()-1, 0.0);
        
        for( i = 0; i < points.size()-2; i++ ) {

            final var value = 3.0 * getAxisValueForPointOfArray(points, i, axis) - 6.0 * getAxisValueForPointOfArray(points, i + 1, axis) + 3.0 * getAxisValueForPointOfArray(points, i + 2, axis);
            constants.setEntry(i+1, value);
        }
        
        
        // solve system
        final var solver = new LUDecomposition(matrix).getSolver();
        final var solution = solver.solve(constants);
        
        return solution;
    }
    
    // calculates the (A|B)_1_i after Formula (9a) (foundalis dissertation page 422)
    // note that the result vector is one shorter than the input vector
    private static RealVector calculate_1_i(final List<ArrayRealVector> points, final RealVector a_2_i, final EnumAxis axis) {

        final RealVector result = new ArrayRealVector(a_2_i.getDimension() - 1);
        for(var i = 0; i < a_2_i.getDimension()-1; i++ ) {

            final var result_1_i = getAxisValueForPointOfArray(points, i + 1, axis) - getAxisValueForPointOfArray(points, i, axis) - (1.0 / 3.0) * (2.0 * a_2_i.getEntry(i) + a_2_i.getEntry(i + 1));
            result.setEntry(i, result_1_i);
        }
        
        return result;
    }
    
    // calculate the (A|B)_3_i after formula (7)
    private static RealVector calculate_2_i(final List<ArrayRealVector> points, final RealVector solved_2_i) {

        final RealVector result = new ArrayRealVector(solved_2_i.getDimension() - 1);
        for(var i = 0; i < solved_2_i.getDimension()-1; i++ ) {
            final var result_3_i = (solved_2_i.getEntry(i+1)-solved_2_i.getEntry(i))*0.3333333333333333333333333;
            result.setEntry(i, result_3_i);
        }
        
        return result;
    }
    
    // "calculate" the (A|B)_0_i after formula (4)
    private static RealVector calculate_0_i(final List<ArrayRealVector> points, final EnumAxis axis) {

        final RealVector result = new ArrayRealVector(points.size());
        
        for(var i = 0; i < points.size(); i++ ) {
            final var result_0_i = getAxisValueForPointOfArray(points, i, axis);
            result.setEntry(i, result_0_i);
        }
        
        return result;
    }
    
    
    private static List<CurveElement> createCurves(final RealVector solvedA_0_i, final RealVector solvedA_1_i, final RealVector solvedA_2_i, final RealVector solvedA_3_i, final RealVector solvedB_0_i, final RealVector solvedB_1_i, final RealVector solvedB_2_i, final RealVector solvedB_3_i) {

        final var numberOfPoints = solvedA_0_i.getDimension();

        final List<CurveElement> resultCurves = new ArrayList<>();
        
        for(int curveI = 0; curveI < numberOfPoints-1; curveI++ )
            resultCurves.add(new CurveElement(
                            (float) solvedA_0_i.getEntry(curveI),
                            (float) solvedA_1_i.getEntry(curveI),
                            (float) solvedA_2_i.getEntry(curveI),
                            (float) solvedA_3_i.getEntry(curveI),

                            (float) solvedB_0_i.getEntry(curveI),
                            (float) solvedB_1_i.getEntry(curveI),
                            (float) solvedB_2_i.getEntry(curveI),
                            (float) solvedB_3_i.getEntry(curveI)
                    )
            );
        
        return resultCurves;
    }
    
    
    private static double getAxisValueForPointOfArray(final List<ArrayRealVector> points, final int index, final EnumAxis axis) {
        return getAxisValueForPoint(points.get(index), axis);
    }
    
    private static double getAxisValueForPoint(final ArrayRealVector point, final EnumAxis axis) {
        return axis == EnumAxis.X ? point.getDataRef()[0] : point.getDataRef()[1];
    }

    private enum EnumAxis {
        X,
        Y
    }
    
    private final List<Curve> resultCurves = new ArrayList<>();
}
