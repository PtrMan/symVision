package ptrman.levels.retina;

import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import static ptrman.Datastructures.Vector2d.ConverterHelper.convertIntVectorToFloat;
import static ptrman.Datastructures.Vector2d.FloatHelper.add;
import static ptrman.Datastructures.Vector2d.FloatHelper.getLength;
import static ptrman.Datastructures.Vector2d.FloatHelper.normalize;
import static ptrman.Datastructures.Vector2d.FloatHelper.sub;
import ptrman.bpsolver.HardParameters;
import java.util.ArrayList;
import java.util.Arrays;
import ptrman.misc.Assert;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealVector;

/** curve detection
 *
 * 
 */
public class ProcessG
{
    
    public static class Curve
    {
        public Curve(ArrayList<CurveElement> curveElements)
        {
            this.curveElements = curveElements;
        }
        
        public ArrayList<CurveElement> curveElements;
        
        public Vector2d<Float> getNormalizedTangentAtEndpoint(int index)
        {
            Assert.Assert(index >= 0 && index <= 1, "");
            
            if( index == 0 )
            {
                return curveElements.get(0).calcTangent(0.0f);
            }
            else
            {
                return curveElements.get(curveElements.size()-1).calcTangent(1.0f);
            }
        }
        
        public Intersection.IntersectionPartner.EnumIntersectionEndpointType getIntersectionEndpoint(Vector2d<Float> point)
        {
            // TODO< other strategy for figuring out if the point is *at* the line and not lear the endpoints >
            
            float distToBegin, distToEnd;
            Vector2d<Float> diff;
            
            diff = sub(calcPosition(0.0f), point);
            distToBegin = getLength(diff);
            
            diff = sub(calcPosition(1.0f), point);
            distToEnd = getLength(diff);
            
            if( distToBegin < distToEnd )
            {
                return Intersection.IntersectionPartner.EnumIntersectionEndpointType.BEGIN;
            }
            else
            {
                return Intersection.IntersectionPartner.EnumIntersectionEndpointType.END;
            }
        }
        
        public Vector2d<Float> calcPosition(float t)
        {
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
    public static void testPoints()
    {
        ArrayList<Vector2d<Float>> testPoints;
        
        testPoints = new ArrayList<>();
        testPoints.add(new Vector2d<>(1.0f, 5.0f));
        testPoints.add(new Vector2d<>(1.8f, 4.0f));
        testPoints.add(new Vector2d<>(2.0f, 7.0f));
        
        Curve resultCurve;
        
        // works fine
        resultCurve = calculatePolynominalsAndReturnCurve(testPoints);
    }
    
    
    public static class CurveElement
    {
        public CurveElement(float a0, float a1, float a2, float a3, float b0, float b1, float b2, float b3)
        {
            a = new float[]{a0, a1, a2, a3};
            b = new float[]{b0, b1, b2, b3};
        }
        
        // parametric curve parameters
        private float[] a;
        private float[] b;
        
        /**
         * 
         * result is normalized
         * 
         * \param t
         * \return 
         */
        public Vector2d<Float> calcTangent(float t)
        {
            final Vector2d<Float> p1;
            final Vector2d<Float> p2;
            final Vector2d<Float> diff;
            
            final float t2;
            
            final float TEPSILON = 0.0001f;
            
            Assert.Assert(t >= 0.0f && t <= 1.0f, "t not in range");
            
            if( t > 0.5f )
            {
                t2 = t - TEPSILON;
            }
            else
            {
                t2 = t + TEPSILON;
            }
            
            p1 = calcPosition(t);
            p2 = calcPosition(t2);
            diff = sub(p1, p2);
            return normalize(diff);
        }
        
        public Vector2d<Float> calcPosition(float t)
        {
            float x, y;
            
            Assert.Assert(t >= 0.0f && t <= 1.0f, "t not in range");
            
            x = a[0] + t*a[1] + t*t*a[2] + t*t*t*a[3];
            y = b[0] + t*b[1] + t*t*b[2] + t*t*t*b[3];
            
            return new Vector2d<>(x, y);
        }
    }
    
    public void process(ArrayList<SingleLineDetector> lineDetectors, ArrayList<ProcessM.LineParsing> lineParsings, ArrayList<ProcessA.Sample> samples, Map2d<Boolean> image)
    {
        resultCurves.clear();
        
        rerateLineParsings(lineParsings);
        
        // for each timeslice we get we select n times randomly "the best"(after the rating of the lineparsing) a lineparsing
        // then we try to convert a part or all of it to a curve
        
        // not that we add and remove lineparsings, because it has no advantage to have a lineparsing and a curve which do overlap
        // it adds also new lineparsings because lineparsings can be divided into curves
        
        // for now we do this for *all* curves
        // TODO< select by random >
        
        int currentLineParsingIndex;
        
        for( currentLineParsingIndex = 0; currentLineParsingIndex < lineParsings.size(); currentLineParsingIndex++ )
        {
            ProcessM.LineParsing currentLineParsing;
            
            currentLineParsing = lineParsings.get(currentLineParsingIndex);
            
            
            // try to covert (at least) a part of the lineParsing to a curve
            
            ArrayList<ArrayList<Vector2d<Float>>> protocurves;
            ArrayList<Vector2d<Float>> currentProtocurve;
            
            protocurves = new ArrayList<ArrayList<Vector2d<Float>>>();
            currentProtocurve = null;
            
            for( int pointIndex = 1; pointIndex < currentLineParsing.lineParsing.size()-1; pointIndex++ )
            {
                boolean atLeastOneSampleNotNearAdjacentLines = examineVincityOfSegmentPoint(pointIndex, currentLineParsing, samples);
                
                boolean IsParsingACurve = currentProtocurve != null;
                
                if( IsParsingACurve )
                {
                    if( atLeastOneSampleNotNearAdjacentLines )
                    {
                        // add segment to curve and mark last segment as in a curve
                        currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex-1).getBProjected());
                        
                        currentLineParsing.lineParsing.get(pointIndex-1).markedPartOfCurve = true;
                    }
                    else
                    {
                        // finish lineparsing
                        protocurves.add(currentProtocurve);
                        currentProtocurve = null;
                    }
                }
                else
                {
                    if( atLeastOneSampleNotNearAdjacentLines )
                    {
                        // begin a new lineparsing
                        currentProtocurve = new ArrayList<>();
                        
                        currentProtocurve.add(currentLineParsing.lineParsing.get(pointIndex-1).getBProjected());
                        
                        currentLineParsing.lineParsing.get(pointIndex-1).markedPartOfCurve = true;
                    }
                    else
                    {
                        // do nothing
                    }
                }
            }
            
            boolean IsParsingACurve = currentProtocurve != null;
            
            if( IsParsingACurve )
            {
                protocurves.add(currentProtocurve);
                currentProtocurve = null;
                IsParsingACurve = false;
            }
            
            // convert protocurves to real curves
            
            for( ArrayList<Vector2d<Float>> iterationProtoCurve : protocurves )
            {
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
    private static void recalculateIntersections(ArrayList<SingleLineDetector> lineDetectors, ArrayList<Curve> curves, Map2d<Boolean> image)
    {
        // intersections between curves and lines
        
        for( Curve iterationCurve : curves )
        {
            final float curveBeginM, curveBeginN;
            final float curveEndM, curveEndN;
            CurveElement beginCurveElement, endCurveElement;
            SingleLineDetector tempBeginCurveTangentLine, tempEndCurveTangentLine;
            
            beginCurveElement = iterationCurve.curveElements.get(0);
            endCurveElement = iterationCurve.curveElements.get(iterationCurve.curveElements.size()-1);
            
            // TODO ASK< maybe we have to search the ending with the minimal x position? >
            tempBeginCurveTangentLine = SingleLineDetector.createFromFloatPositions(beginCurveElement.calcPosition(0.0f), add(beginCurveElement.calcPosition(0.0f), beginCurveElement.calcTangent(0.0f)));
            tempEndCurveTangentLine = SingleLineDetector.createFromFloatPositions(endCurveElement.calcPosition(1.0f), add(endCurveElement.calcPosition(1.0f), endCurveElement.calcTangent(1.0f)));
            
            curveBeginM = tempBeginCurveTangentLine.getM();
            curveBeginN = tempBeginCurveTangentLine.getN();
            
            curveEndM = tempEndCurveTangentLine.getM();
            curveEndN = tempEndCurveTangentLine.getN();
            
            for( SingleLineDetector iterationLineDetector : lineDetectors )
            {
                final Vector2d<Float> intersectionPositionBegin, intersectionPositionEnd;
                
                intersectionPositionBegin = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveBeginM, curveBeginN);
                intersectionPositionEnd = SingleLineDetector.intersectLineWithMN(iterationLineDetector, curveEndM, curveEndN);
                
                // examine the intersection positions for inside the image and the neightborhood
                
                if(
                    ProcessE.isPointInsideImage(Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionBegin), image) && 
                    ProcessE.isNeightborhoodPixelSet(Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionBegin), image)
                )
                {
                    Intersection createdIntersection;

                    createdIntersection = new Intersection();
                    createdIntersection.partners[0] = new Intersection.IntersectionPartner(RetinaPrimitive.makeCurve(iterationCurve), iterationCurve.getIntersectionEndpoint(intersectionPositionBegin));
                    createdIntersection.partners[1] = new Intersection.IntersectionPartner(RetinaPrimitive.makeLine(iterationLineDetector), iterationLineDetector.getIntersectionEndpoint(intersectionPositionBegin));
                    createdIntersection.intersectionPosition = Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionBegin);
                    
                    iterationLineDetector.intersections.add(createdIntersection);
                }
                
                if(
                    ProcessE.isPointInsideImage(Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionEnd), image) && 
                    ProcessE.isNeightborhoodPixelSet(Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionEnd), image)
                )
                {
                    Intersection createdIntersection;

                    createdIntersection = new Intersection();
                    createdIntersection.partners[0] = new Intersection.IntersectionPartner(RetinaPrimitive.makeCurve(iterationCurve), iterationCurve.getIntersectionEndpoint(intersectionPositionEnd));
                    createdIntersection.partners[1] = new Intersection.IntersectionPartner(RetinaPrimitive.makeLine(iterationLineDetector), iterationLineDetector.getIntersectionEndpoint(intersectionPositionEnd));
                    createdIntersection.intersectionPosition = Vector2d.ConverterHelper.convertFloatVectorToInt(intersectionPositionEnd);
                    
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

    
    public ArrayList<Curve> getResultCurves()
    {
        return resultCurves;
    }
    
    private static void removeLinedetectorsWhichWereUsedInCurves(ArrayList<SingleLineDetector> lineDetectors)
    {
        int lineDetectorI;
        
        for( lineDetectorI = 0; lineDetectorI < lineDetectors.size(); lineDetectorI++ )
        {
            SingleLineDetector currentLineDetector;
            
            currentLineDetector = lineDetectors.get(lineDetectorI);
            
            if( currentLineDetector.markedPartOfCurve )
            {
                // before we remove it we have to make sure it doesn't get referenced in intersections
                removeLineDetectorFromNeightborIntersections(currentLineDetector);
                
                lineDetectors.remove(lineDetectorI);
                lineDetectorI--;
                continue;
            }
        }
    }
    
    private static void removeLineDetectorFromNeightborIntersections(SingleLineDetector lineDetector)
    {
        for( Intersection iterationIntersection : lineDetector.intersections )
        {
            Assert.Assert(iterationIntersection.partners[0].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.partners[1].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            
            if( iterationIntersection.partners[0].primitive.line.equals(lineDetector) )
            {
                removeIntersectionBetweenLines(iterationIntersection.partners[1].primitive.line, lineDetector);
            }
            else if( iterationIntersection.partners[1].primitive.line.equals(lineDetector) )
            {
                removeIntersectionBetweenLines(iterationIntersection.partners[0].primitive.line, lineDetector);
            }
        }
    }

    private static void removeIntersectionBetweenLines(SingleLineDetector lineA, SingleLineDetector lineB)
    {
        int intersectionI;
        
        for( intersectionI = 0; intersectionI < lineA.intersections.size(); intersectionI++ )
        {
            Intersection iterationIntersection;
            
            iterationIntersection = lineA.intersections.get(intersectionI);
            
            Assert.Assert(iterationIntersection.partners[0].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.partners[1].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");

            if( iterationIntersection.partners[0].primitive.line.equals(lineB) )
            {
                lineA.intersections.remove(intersectionI);
                break;
            }
            else if( iterationIntersection.partners[1].primitive.line.equals(lineB) )
            {
                lineA.intersections.remove(intersectionI);
                break;
            }
        }
        
        
        for( intersectionI = 0; intersectionI < lineB.intersections.size(); intersectionI++ )
        {
            Intersection iterationIntersection;
            
            iterationIntersection = lineB.intersections.get(intersectionI);
            
            Assert.Assert(iterationIntersection.partners[0].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            Assert.Assert(iterationIntersection.partners[1].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "must be line");
            
            if( iterationIntersection.partners[0].primitive.line.equals(lineA) )
            {
                lineB.intersections.remove(intersectionI);
                break;
            }
            else if( iterationIntersection.partners[1].primitive.line.equals(lineA) )
            {
                lineB.intersections.remove(intersectionI);
                break;
            }
        }
    }
    
    private static void rerateLineParsings(ArrayList<ProcessM.LineParsing> lineParsings)
    {
        for( ProcessM.LineParsing iterationLineParsing : lineParsings )
        {
            rerateLineParsing(iterationLineParsing);
        }
    }
    
    private static void rerateLineParsing(ProcessM.LineParsing lineParsing)
    {
        int lineDetectorI;
        
        if( lineParsing.processGRated )
        {
            return;
        }
        
        lineParsing.processGInterestRating = 0.0f;
        
        for( lineDetectorI = 0; lineDetectorI < lineParsing.lineParsing.size()-1; lineDetectorI++ )
        {
            SingleLineDetector iterationLineDetector;
            SingleLineDetector nextLineDetector;
            float length;
            float angleBetweenSegments;
            Vector2d<Float> endToEndDiff;
            float endToEndDistance;
            float endToEndRating;
            
            iterationLineDetector = lineParsing.lineParsing.get(lineDetectorI);
            nextLineDetector = lineParsing.lineParsing.get(lineDetectorI+1);
            
            // length criteria
            
            length = iterationLineDetector.getLength();
            
            // TODO< configurable constant >
            lineParsing.processGInterestRating += (1.0f/length);
            
            // angle criteria
            
            angleBetweenSegments = SingleLineDetector.getAngleBetween(iterationLineDetector, nextLineDetector);
            lineParsing.processGInterestRating += (90.0f-angleBetweenSegments)*HardParameters.ProcessG.RATINGANGLEMULTIPLIER;
            
            // meeting end to end criteria
            
            // we assume that the point a is the first and point b is the last
            
            endToEndDiff = sub(iterationLineDetector.getBProjected(), nextLineDetector.getAProjected());
            endToEndDistance = getLength(endToEndDiff);
            
            endToEndRating = (float)Math.max(0.0f, (HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE - endToEndDistance)/HardParameters.ProcessG.RATINGENDTOENDMAXDISTANCE);
            endToEndRating *= HardParameters.ProcessG.RATINGENDTOENDMULTIPLIER;
            lineParsing.processGInterestRating += endToEndRating;
        }
        
        lineParsing.processGRated = true;
    }
    
    
    
    
    private static boolean examineVincityOfSegmentPoint(int pointIndex, ProcessM.LineParsing lineParsing, ArrayList<ProcessA.Sample> samples)
    {
        Vector2d<Float> centerPoint;
        ArrayList<ProcessA.Sample> endosceletonSamplesInVicinity;
        ArrayList<SingleLineDetector> neightborLinesOfPoint;
        boolean atLeastOneSampleNotNearLine;
        
        centerPoint = lineParsing.lineParsing.get(pointIndex).getBProjected();
        endosceletonSamplesInVicinity = queryEndosceletonPointsInVicinityOf(samples, centerPoint);
        neightborLinesOfPoint = getNeightborLinesOfPoint(pointIndex, lineParsing);
        atLeastOneSampleNotNearLine = !areAllSamplesNearLines(endosceletonSamplesInVicinity, neightborLinesOfPoint, HardParameters.ProcessG.MAXIMALDISTANCEOFENDOSCELETONTOLINE);
        
        return atLeastOneSampleNotNearLine;
    }
    
    private static ArrayList<ProcessA.Sample> queryEndosceletonPointsInVicinityOf(ArrayList<ProcessA.Sample> samples, Vector2d<Float> centerPoint)
    {
        return queryPointsInRadius(samples, centerPoint, HardParameters.ProcessG.VICINITYRADIUS, new ProcessA.Sample.EnumType[]{ProcessA.Sample.EnumType.ENDOSCELETON});
    }
    
    private static ArrayList<ProcessA.Sample> queryPointsInRadius(ArrayList<ProcessA.Sample> samples, Vector2d<Float> centerPoint, float radius, ProcessA.Sample.EnumType[] typeFilterCriteria)
    {
        ArrayList<ProcessA.Sample> samplesInRadius;
        
        samplesInRadius = new ArrayList<>();
        
        // TODO< query the points in the radius with a optimized spartial scheme >
        for( ProcessA.Sample iterationSample : samples )
        {
            Vector2d<Float> diff;
            float distance;
            
            diff = sub(convertIntVectorToFloat(iterationSample.position), centerPoint);
            distance = getLength(diff);
            
            if( distance < radius && Arrays.asList(typeFilterCriteria).contains(iterationSample.type) )
            {
                samplesInRadius.add(iterationSample);
            }
        }
        
        return samplesInRadius;
    }
    
    private static ArrayList<SingleLineDetector> getNeightborLinesOfPoint(int pointIndex, ProcessM.LineParsing lineParsing) {
        ArrayList<SingleLineDetector> resultLines;
        
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
    private static boolean areAllSamplesNearLines(ArrayList<ProcessA.Sample> samples, ArrayList<SingleLineDetector> lines, final float maximalDistance)
    {
        for( ProcessA.Sample iterationSample : samples )
        {
            boolean sampleNearAnyLine;
            Vector2d<Float> iterationSamplePosition;
            
            iterationSamplePosition = convertIntVectorToFloat(iterationSample.position);
            
            sampleNearAnyLine = false;
            
            for( SingleLineDetector iterationLine : lines )
            {
                Vector2d<Float> projectedPointPosition;
                boolean projectedPointInsideLine;
                Vector2d<Float> diff;
                float distanceOfPointToLine;
                
                projectedPointPosition = iterationLine.projectPointOntoLine(iterationSamplePosition);
                projectedPointInsideLine = iterationLine.isXOfPointInLine(projectedPointPosition);
                
                // ignore if it is not on the line
                if( !projectedPointInsideLine )
                {
                    continue;
                }
                
                diff = sub(projectedPointPosition, iterationSamplePosition);
                distanceOfPointToLine = getLength(diff);
                
                if( distanceOfPointToLine < maximalDistance )
                {
                    sampleNearAnyLine = true;
                    continue;
                }
            }
            
            if( !sampleNearAnyLine )
            {
                return false;
            }
        }
        
        return true;
    }

    
    
    
    private static Curve calculatePolynominalsAndReturnCurve(ArrayList<Vector2d<Float>> points)
    {
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
    private static RealVector solveLinearEquationFor2ForPoints(ArrayList<Vector2d<Float>> points, EnumAxis axis)
    {
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
        for( i = 1; i < points.size()-1; i++ )
        {
            matrix.setEntry(i, i-1, 1.0);
            matrix.setEntry(i, i-1+1, 4.0);
            matrix.setEntry(i, i-1+2, 1.0);
        }
        
        // populate constants
        
        constants = new ArrayRealVector(points.size());
        constants.setEntry(0, 0.0);
        constants.setEntry(points.size()-1, 0.0);
        
        for( i = 0; i < points.size()-2; i++ )
        {
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
    private static RealVector calculate_1_i(ArrayList<Vector2d<Float>> points, RealVector a_2_i, EnumAxis axis)
    {
        RealVector result;
        
        result = new ArrayRealVector(a_2_i.getDimension()-1);
        for( int i = 0; i < a_2_i.getDimension()-1; i++ )
        {
            double result_1_i;
            
            result_1_i = getAxisValueForPointOfArray(points, i+1, axis) - getAxisValueForPointOfArray(points, i, axis) - (1.0/3.0)*(2.0*a_2_i.getEntry(i) + a_2_i.getEntry(i+1));
            result.setEntry(i, result_1_i);
        }
        
        return result;
    }
    
    // calculate the (A|B)_3_i after formula (7)
    private static RealVector calculate_2_i(ArrayList<Vector2d<Float>> points, RealVector solved_2_i)
    {
        RealVector result;
        
        result = new ArrayRealVector(solved_2_i.getDimension()-1);
        for( int i = 0; i < solved_2_i.getDimension()-1; i++ )
        {
            double result_3_i = (solved_2_i.getEntry(i+1)-solved_2_i.getEntry(i))*0.3333333333333333333333333;
            result.setEntry(i, result_3_i);
        }
        
        return result;
    }
    
    // "calculate" the (A|B)_0_i after formula (4)
    private static RealVector calculate_0_i(ArrayList<Vector2d<Float>> points, EnumAxis axis)
    {
        RealVector result;
        
        result = new ArrayRealVector(points.size());
        
        for( int i = 0; i < points.size(); i++ )
        {
            double result_0_i;
            result_0_i = getAxisValueForPointOfArray(points, i, axis);
            result.setEntry(i, result_0_i);
        }
        
        return result;
    }
    
    
    private static ArrayList<CurveElement> createCurves(RealVector solvedA_0_i, RealVector solvedA_1_i, RealVector solvedA_2_i, RealVector solvedA_3_i, RealVector solvedB_0_i, RealVector solvedB_1_i, RealVector solvedB_2_i, RealVector solvedB_3_i) {
        ArrayList<CurveElement> resultCurves;
        int numberOfPoints;
        int curveI;
        
        numberOfPoints = solvedA_0_i.getDimension();
        
        resultCurves = new ArrayList<>();
        
        for( curveI = 0; curveI < numberOfPoints-1; curveI++ )
        {
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
    
    
    private static float getAxisValueForPointOfArray(ArrayList<Vector2d<Float>> points, int index, EnumAxis axis)
    {
        return getAxisValueForPoint(points.get(index), axis);
    }
    
    private static float getAxisValueForPoint(Vector2d<Float> point, EnumAxis axis)
    {
        if( axis == EnumAxis.X )
        {
            return point.x;
        }
        else
        {
            return point.y;
        }
    }

    

    
    
    private enum EnumAxis
    {
        X,
        Y
    }
    
    private ArrayList<Curve> resultCurves = new ArrayList<Curve>();
}
