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

import org.apache.commons.math3.linear.*;
import ptrman.Datastructures.Map2d;
import ptrman.bpsolver.HardParameters;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ptrman.bpsolver.Helper.isNeightborhoodPixelSet;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;
import static ptrman.math.ArrayRealVectorHelper.normalize;

/** curve detection
 *
 * 
 */
public class ProcessG {
    public static class Curve {
        public Curve(List<CurveElement> curveElements) {
            this.curveElements = curveElements;
        }
        
        public final List<CurveElement> curveElements;
        
        public ArrayRealVector getNormalizedTangentAtEndpoint(int index) {
            Assert.Assert(index >= 0 && index <= 1, "");
            
            if( index == 0 ) {
                return curveElements.get(0).calcTangent(0.0f);
            }
            else {
                return curveElements.get(curveElements.size()-1).calcTangent(1.0f);
            }
        }
        
        public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(ArrayRealVector point) {
            // TODO< other strategy for figuring out if the point is *at* the line and not lear the endpoints >
            
            ArrayRealVector diff;
            
            diff = calcPosition(0.0f).subtract(point);
            double distToBegin = diff.getNorm();
            
            diff = calcPosition(1.0f).subtract(point);
            double distToEnd = diff.getNorm();
            
            if( distToBegin < distToEnd ) {
                return Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN;
            }
            else {
                return Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
            }
        }
        
        public ArrayRealVector calcPosition(float t) {
            float t2;
            float rem;
            int index;
            
            t2 = t * (float)curveElements.size();
            
            rem = t2 % 1.0f;
            index = Math.round(t2);
            
            return curveElements.get(index).calcPosition(rem);
        }
    }
    
    
    // test, works
    public static void testPoints() {
        List<ArrayRealVector> testPoints;
        
        testPoints = new ArrayList<>();
        testPoints.add(new ArrayRealVector(new double[]{1.0f, 5.0f}));
        testPoints.add(new ArrayRealVector(new double[]{1.8f, 4.0f}));
        testPoints.add(new ArrayRealVector(new double[]{2.0f, 7.0f}));
        
        Curve resultCurve;
        
        // works fine
        resultCurve = calculatePolynominalsAndReturnCurve(testPoints);
    }
    
    
    public static class CurveElement {
        public CurveElement(float a0, float a1, float a2, float a3, float b0, float b1, float b2, float b3) {
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
        public ArrayRealVector calcTangent(double t) {
            final double t2;
            
            final double TEPSILON = 0.0001;
            
            Assert.Assert(t >= 0.0 && t <= 1.0, "t not in range");
            
            if( t > 0.5 ) {
                t2 = t - TEPSILON;
            }
            else {
                t2 = t + TEPSILON;
            }

            ArrayRealVector p1 = calcPosition(t);
            ArrayRealVector p2 = calcPosition(t2);
            ArrayRealVector diff = p1.subtract(p2);
            return normalize(diff);
        }
        
        public ArrayRealVector calcPosition(double t) {
            Assert.Assert(t >= 0.0 && t <= 1.0, "t not in range");
            
            double x = a[0] + t*a[1] + t*t*a[2] + t*t*t*a[3];
            double y = b[0] + t*b[1] + t*t*b[2] + t*t*t*b[3];
            
            return new ArrayRealVector(new double[]{x, y});
        }
    }
    
    public void process(ArrayList<SingleLineDetector> lineDetectors, ArrayList<ProcessM.LineParsing> lineParsings, ArrayList<ProcessA.Sample> samples, Map2d<Boolean> image) {
        resultCurves.clear();
        
        rerateLineParsings(lineParsings);
        
        // for each timeslice we get we select n times randomly "the best"(after the rating of the lineparsing) a lineparsing
        // then we try to convert a part or all of it to a curve
        
        // not that we add and remove lineparsings, because it has no advantage to have a lineparsing and a curve which do overlap
        // it adds also new lineparsings because lineparsings can be divided into curves
        
        // for now we do this for *all* curves
        // TODO< select by random >

        for (ProcessM.LineParsing lineParsing : lineParsings) {
            ProcessM.LineParsing currentLineParsing;

            currentLineParsing = lineParsing;


            // try to covert (at least) a part of the lineParsing to a curve

            List<List<ArrayRealVector>> protocurves;
            List<ArrayRealVector> currentProtocurve;

            protocurves = new ArrayList<>();
            currentProtocurve = null;

            for (int pointIndex = 1; pointIndex < currentLineParsing.lineParsing.size() - 1; pointIndex++) {
                boolean atLeastOneSampleNotNearAdjacentLines = examineVincityOfSegmentPoint(pointIndex, currentLineParsing, samples);

                boolean IsParsingACurve = currentProtocurve != null;

                if (IsParsingACurve) {
                    if (atLeastOneSampleNotNearAdjacentLines) {
                        // add segment to curve and mark last segment as in a curve
                        currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex - 1).getBProjected());

                        currentLineParsing.lineParsing.get(pointIndex - 1).markedPartOfCurve = true;
                    } else {
                        // finish lineparsing
                        protocurves.add(currentProtocurve);
                        currentProtocurve = null;
                    }
                } else {
                    if (atLeastOneSampleNotNearAdjacentLines) {
                        // begin a new lineparsing
                        currentProtocurve = new ArrayList<>();

                        currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex - 1).getBProjected());

                        currentLineParsing.lineParsing.get(pointIndex - 1).markedPartOfCurve = true;
                    } else {
                        // do nothing
                    }
                }
            }

            boolean IsParsingACurve = currentProtocurve != null;

            if (IsParsingACurve) {
                protocurves.add(currentProtocurve);
                currentProtocurve = null;
                IsParsingACurve = false;
            }

            // convert protocurves to real curves

            for (List<ArrayRealVector> iterationProtoCurve : protocurves) {
                Curve createdCurve;

                createdCurve = calculatePolynominalsAndReturnCurve(iterationProtoCurve);
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
    private static void recalculateIntersections(List<SingleLineDetector> lineDetectors, List<Curve> curves, Map2d<Boolean> image) {
        // intersections between curves and lines
        
        for( Curve iterationCurve : curves ) {
            CurveElement beginCurveElement, endCurveElement;
            SingleLineDetector tempBeginCurveTangentLine, tempEndCurveTangentLine;
            
            beginCurveElement = iterationCurve.curveElements.get(0);
            endCurveElement = iterationCurve.curveElements.get(iterationCurve.curveElements.size()-1);
            
            // TODO ASK< maybe we have to search the ending with the minimal x position? >
            tempBeginCurveTangentLine = SingleLineDetector.createFromFloatPositions(beginCurveElement.calcPosition(0.0f), beginCurveElement.calcPosition(0.0f).add(beginCurveElement.calcTangent(0.0f)));
            tempEndCurveTangentLine = SingleLineDetector.createFromFloatPositions(endCurveElement.calcPosition(1.0f), endCurveElement.calcPosition(1.0f).add(endCurveElement.calcTangent(1.0f)));
            
            final double curveBeginM = tempBeginCurveTangentLine.getM();
            final double curveBeginN = tempBeginCurveTangentLine.getN();

            final double curveEndM = tempEndCurveTangentLine.getM();
            final double curveEndN = tempEndCurveTangentLine.getN();
            
            for( SingleLineDetector iterationLineDetector : lineDetectors ) {
                final ArrayRealVector intersectionPositionBegin = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveBeginM, curveBeginN);
                final ArrayRealVector intersectionPositionEnd = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveEndM, curveEndN);
                
                // examine the intersection positions for inside the image and the neightborhood
                
                if(
                    image.inBounds(arrayRealVectorToInteger(intersectionPositionBegin, ArrayRealVectorHelper.EnumRoundMode.DOWN)) &&
                    isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPositionBegin, ArrayRealVectorHelper.EnumRoundMode.DOWN), image)
                ) {
                    Intersection createdIntersection;

                    createdIntersection = new Intersection(intersectionPositionBegin,
                            new Intersection.IntersectionPartner(RetinaPrimitive.makeCurve(iterationCurve), iterationCurve.getIntersectionEndpoint(intersectionPositionBegin)),
                            new Intersection.IntersectionPartner(RetinaPrimitive.makeLine(iterationLineDetector), iterationLineDetector.getIntersectionEndpoint(intersectionPositionBegin))
                            );

                    iterationLineDetector.intersections.add(createdIntersection);
                }
                
                if(
                        image.inBounds(arrayRealVectorToInteger(intersectionPositionEnd, ArrayRealVectorHelper.EnumRoundMode.DOWN)) &&
                                isNeightborhoodPixelSet(arrayRealVectorToInteger(intersectionPositionEnd, ArrayRealVectorHelper.EnumRoundMode.DOWN), image)
                ) {
                    Intersection createdIntersection;

                    createdIntersection = new Intersection(intersectionPositionEnd,
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
    
    private static void removeLinedetectorsWhichWereUsedInCurves(ArrayList<SingleLineDetector> lineDetectors) {
        for( int lineDetectorI = 0; lineDetectorI < lineDetectors.size(); lineDetectorI++ ) {
            SingleLineDetector currentLineDetector;
            
            currentLineDetector = lineDetectors.get(lineDetectorI);
            
            if( currentLineDetector.markedPartOfCurve ) {
                // before we remove it we have to make sure it doesn't get referenced in intersections
                removeLineDetectorFromNeightborIntersections(currentLineDetector);
                
                lineDetectors.remove(lineDetectorI);
                lineDetectorI--;
                continue;
            }
        }
    }
    
    private static void removeLineDetectorFromNeightborIntersections(SingleLineDetector lineDetector) {
        for( Intersection iterationIntersection : lineDetector.intersections ) {
            Assert.Assert(iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            
            if( iterationIntersection.p0.primitive.line.equals(lineDetector) ) {
                removeIntersectionBetweenLines(iterationIntersection.p1.primitive.line, lineDetector);
            }
            else if( iterationIntersection.p1.primitive.line.equals(lineDetector) ) {
                removeIntersectionBetweenLines(iterationIntersection.p0.primitive.line, lineDetector);
            }
        }
    }

    private static void removeIntersectionBetweenLines(SingleLineDetector lineA, SingleLineDetector lineB) {
        for( int intersectionI = 0; intersectionI < lineA.intersections.size(); intersectionI++ ) {
            Intersection iterationIntersection;
            
            iterationIntersection = lineA.intersections.get(intersectionI);
            
            Assert.Assert(iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");

            if( iterationIntersection.p0.primitive.line.equals(lineB) ) {
                lineA.intersections.remove(intersectionI);
                break;
            }
            else if( iterationIntersection.p1.primitive.line.equals(lineB) ) {
                lineA.intersections.remove(intersectionI);
                break;
            }
        }
        
        
        for( int intersectionI = 0; intersectionI < lineB.intersections.size(); intersectionI++ ) {
            Intersection iterationIntersection;
            
            iterationIntersection = lineB.intersections.get(intersectionI);
            
            Assert.Assert(iterationIntersection.p0.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.p1.primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            
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
    
    private static void rerateLineParsings(ArrayList<ProcessM.LineParsing> lineParsings) {
        for( ProcessM.LineParsing iterationLineParsing : lineParsings ) {
            rerateLineParsing(iterationLineParsing);
        }
    }
    
    private static void rerateLineParsing(ProcessM.LineParsing lineParsing) {
        int lineDetectorI;
        
        if( lineParsing.processGRated ) {
            return;
        }
        
        lineParsing.processGInterestRating = 0.0f;
        
        for( lineDetectorI = 0; lineDetectorI < lineParsing.lineParsing.size()-1; lineDetectorI++ ) {
            SingleLineDetector iterationLineDetector;
            SingleLineDetector nextLineDetector;
            float endToEndRating;
            
            iterationLineDetector = lineParsing.lineParsing.get(lineDetectorI);
            nextLineDetector = lineParsing.lineParsing.get(lineDetectorI+1);
            
            // length criteria
            
            double length = iterationLineDetector.getLength();
            
            // TODO< configurable constant >
            lineParsing.processGInterestRating += (1.0f/length);
            
            // angle criteria
            
            double angleBetweenSegments = SingleLineDetector.getAngleBetween(iterationLineDetector, nextLineDetector);
            lineParsing.processGInterestRating += (90.0f-angleBetweenSegments)*HardParameters.ProcessG.RATINGANGLEMULTIPLIER;
            
            // meeting end to end criteria
            
            // we assume that the point a is the first and point b is the last

            double endToEndDistance = iterationLineDetector.b.getDistance(nextLineDetector.a);
            
            endToEndRating = (float)Math.max(0.0f, (HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE - endToEndDistance)/HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE);
            endToEndRating *= HardParameters.ProcessG.RATINGENDTOENDMULTIPLIER;
            lineParsing.processGInterestRating += endToEndRating;
        }
        
        lineParsing.processGRated = true;
    }
    
    
    
    
    private static boolean examineVincityOfSegmentPoint(int pointIndex, ProcessM.LineParsing lineParsing, List<ProcessA.Sample> samples) {
        ArrayRealVector centerPoint = lineParsing.lineParsing.get(pointIndex).getBProjected();
        List<ProcessA.Sample> endosceletonSamplesInVicinity = queryEndosceletonPointsInVicinityOf(samples, centerPoint);
        List<SingleLineDetector> neightborLinesOfPoint = getNeightborLinesOfPoint(pointIndex, lineParsing);
        boolean atLeastOneSampleNotNearLine = !areAllSamplesNearLines(endosceletonSamplesInVicinity, neightborLinesOfPoint, HardParameters.ProcessG.MAXIMALDISTANCEOFENDOSCELETONTOLINE);
        
        return atLeastOneSampleNotNearLine;
    }
    
    private static List<ProcessA.Sample> queryEndosceletonPointsInVicinityOf(List<ProcessA.Sample> samples, ArrayRealVector centerPoint) {
        return queryPointsInRadius(samples, centerPoint, HardParameters.ProcessG.VICINITYRADIUS, new ProcessA.Sample.EnumType[]{ProcessA.Sample.EnumType.ENDOSCELETON});
    }
    
    private static List<ProcessA.Sample> queryPointsInRadius(List<ProcessA.Sample> samples, ArrayRealVector centerPoint, double radius, ProcessA.Sample.EnumType[] typeFilterCriteria) {
        List<ProcessA.Sample> samplesInRadius;
        
        samplesInRadius = new ArrayList<>();
        
        // TODO< query the points in the radius with a optimized spartial scheme >
        for( ProcessA.Sample iterationSample : samples ) {
            double distance = iterationSample.position.getDistance(centerPoint);

            if( distance < radius && Arrays.asList(typeFilterCriteria).contains(iterationSample.type) ) {
                samplesInRadius.add(iterationSample);
            }
        }
        
        return samplesInRadius;
    }
    
    private static List<SingleLineDetector> getNeightborLinesOfPoint(int pointIndex, ProcessM.LineParsing lineParsing) {
        List<SingleLineDetector> resultLines;
        
        resultLines = new ArrayList<>();
        
        resultLines.add(lineParsing.lineParsing.get(pointIndex-1));
        resultLines.add(lineParsing.lineParsing.get(pointIndex));
        
        return resultLines;
    }
    
    /**
     * checks if all samples are near at least one of the lines
     * if this is not the case for one sample, it returns false
     *  
     */
    private static boolean areAllSamplesNearLines(List<ProcessA.Sample> samples, List<SingleLineDetector> lines, final double maximalDistance) {
        for( ProcessA.Sample iterationSample : samples ) {
            ArrayRealVector iterationSamplePosition = iterationSample.position;

            boolean sampleNearAnyLine = false;


            ArrayRealVector tmp = new ArrayRealVector(2);
            for( SingleLineDetector iterationLine : lines ) {
                ArrayRealVector projectedPointPosition = iterationLine.projectPointOntoLine(iterationSamplePosition, tmp);
                boolean projectedPointInsideLine = iterationLine.isXOfPointInLine(projectedPointPosition);
                
                // ignore if it is not on the line
                if( !projectedPointInsideLine ) {
                    continue;
                }

                double distanceOfPointToLine = projectedPointPosition.getDistance(iterationSamplePosition);
                
                if( distanceOfPointToLine < maximalDistance ) {
                    sampleNearAnyLine = true;
                    continue;
                }
            }
            
            if( !sampleNearAnyLine ) {
                return false;
            }
        }
        
        return true;
    }

    
    
    
    private static Curve calculatePolynominalsAndReturnCurve(List<ArrayRealVector> points) {
        RealVector solvedA_2_i;
        RealVector solvedA_1_i;
        RealVector solvedA_3_i;
        RealVector solvedA_0_i;
        
        RealVector solvedB_2_i;
        RealVector solvedB_1_i;
        RealVector solvedB_3_i;
        RealVector solvedB_0_i;
        
        solvedA_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.X);
        solvedA_1_i = ProcessG.calculate_1_i(points, solvedA_2_i, EnumAxis.X);
        solvedA_3_i = ProcessG.calculate_2_i(points, solvedA_2_i);
        solvedA_0_i = ProcessG.calculate_0_i(points, EnumAxis.X);
        
        solvedB_2_i = ProcessG.solveLinearEquationFor2ForPoints(points, EnumAxis.Y);
        solvedB_1_i = ProcessG.calculate_1_i(points, solvedB_2_i, EnumAxis.Y);
        solvedB_3_i = ProcessG.calculate_2_i(points, solvedB_2_i);
        solvedB_0_i = ProcessG.calculate_0_i(points, EnumAxis.Y);
        
        return new Curve(createCurves(solvedA_0_i, solvedA_1_i, solvedA_2_i, solvedA_3_i, solvedB_0_i, solvedB_1_i, solvedB_2_i, solvedB_3_i));
    }
    
    // builds a linear equation for the a|b_2,i values and returns the coefficients
    // NOTE< points must be for sure sorted by x axis? >
    private static RealVector solveLinearEquationFor2ForPoints(List<ArrayRealVector> points, EnumAxis axis) {
        Array2DRowRealMatrix matrix;
        RealVector constants;
        int i;
        
        // math libary usage see http://commons.apache.org/proper/commons-math/userguide/linear.html
        
        matrix = new Array2DRowRealMatrix(points.size(), points.size());
        
        // populate matrix
        
        //  top and bottom
        matrix.setEntry(0, 0, 1.0);
        matrix.setEntry(points.size()-1, points.size()-1, 1.0);
        
        // middle
        for( i = 1; i < points.size()-1; i++ ) {
            matrix.setEntry(i, i-1, 1.0);
            matrix.setEntry(i, i-1+1, 4.0);
            matrix.setEntry(i, i-1+2, 1.0);
        }
        
        // populate constants
        
        constants = new ArrayRealVector(points.size());
        constants.setEntry(0, 0.0);
        constants.setEntry(points.size()-1, 0.0);
        
        for( i = 0; i < points.size()-2; i++ ) {
            double value;
            
            value = 3.0*getAxisValueForPointOfArray(points, i, axis) - 6.0*getAxisValueForPointOfArray(points, i+1, axis) + 3.0*getAxisValueForPointOfArray(points, i+2, axis);
            constants.setEntry(i+1, value);
        }
        
        
        // solve system
        DecompositionSolver solver = new LUDecomposition(matrix).getSolver();
        RealVector solution = solver.solve(constants);
        
        return solution;
    }
    
    // calculates the (A|B)_1_i after Formula (9a) (foundalis dissertation page 422)
    // note that the result vector is one shorter than the input vector
    private static RealVector calculate_1_i(List<ArrayRealVector> points, RealVector a_2_i, EnumAxis axis) {
        RealVector result;
        
        result = new ArrayRealVector(a_2_i.getDimension()-1);
        for( int i = 0; i < a_2_i.getDimension()-1; i++ ) {
            double result_1_i;
            
            result_1_i = getAxisValueForPointOfArray(points, i+1, axis) - getAxisValueForPointOfArray(points, i, axis) - (1.0/3.0)*(2.0*a_2_i.getEntry(i) + a_2_i.getEntry(i+1));
            result.setEntry(i, result_1_i);
        }
        
        return result;
    }
    
    // calculate the (A|B)_3_i after formula (7)
    private static RealVector calculate_2_i(List<ArrayRealVector> points, RealVector solved_2_i) {
        RealVector result;
        
        result = new ArrayRealVector(solved_2_i.getDimension()-1);
        for( int i = 0; i < solved_2_i.getDimension()-1; i++ ) {
            double result_3_i = (solved_2_i.getEntry(i+1)-solved_2_i.getEntry(i))*0.3333333333333333333333333;
            result.setEntry(i, result_3_i);
        }
        
        return result;
    }
    
    // "calculate" the (A|B)_0_i after formula (4)
    private static RealVector calculate_0_i(List<ArrayRealVector> points, EnumAxis axis) {
        RealVector result;
        
        result = new ArrayRealVector(points.size());
        
        for( int i = 0; i < points.size(); i++ ) {
            double result_0_i;
            result_0_i = getAxisValueForPointOfArray(points, i, axis);
            result.setEntry(i, result_0_i);
        }
        
        return result;
    }
    
    
    private static List<CurveElement> createCurves(RealVector solvedA_0_i, RealVector solvedA_1_i, RealVector solvedA_2_i, RealVector solvedA_3_i, RealVector solvedB_0_i, RealVector solvedB_1_i, RealVector solvedB_2_i, RealVector solvedB_3_i) {
        List<CurveElement> resultCurves;
        int numberOfPoints;
        int curveI;
        
        numberOfPoints = solvedA_0_i.getDimension();
        
        resultCurves = new ArrayList<>();
        
        for( curveI = 0; curveI < numberOfPoints-1; curveI++ ) {
            resultCurves.add(new CurveElement(
                    (float)solvedA_0_i.getEntry(curveI),
                    (float)solvedA_1_i.getEntry(curveI),
                    (float)solvedA_2_i.getEntry(curveI),
                    (float)solvedA_3_i.getEntry(curveI),
                    
                    (float)solvedB_0_i.getEntry(curveI),
                    (float)solvedB_1_i.getEntry(curveI),
                    (float)solvedB_2_i.getEntry(curveI),
                    (float)solvedB_3_i.getEntry(curveI)
                )
            );
        }
        
        return resultCurves;
    }
    
    
    private static double getAxisValueForPointOfArray(List<ArrayRealVector> points, int index, EnumAxis axis) {
        return getAxisValueForPoint(points.get(index), axis);
    }
    
    private static double getAxisValueForPoint(ArrayRealVector point, EnumAxis axis) {
        if( axis == EnumAxis.X ) {
            return point.getDataRef()[0];
        }
        else {
            return point.getDataRef()[1];
        }
    }

    private enum EnumAxis {
        X,
        Y
    }
    
    private final List<Curve> resultCurves = new ArrayList<>();
}
