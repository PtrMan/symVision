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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.block.procedure.primitive.IntObjectProcedure;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.levels.retina.helper.ProcessConnector;

import java.util.Collection;
import java.util.Deque;

import static ptrman.bpsolver.Helper.createMapByObjectIdsFromListOfRetinaPrimitives;

/**
 * tries to combine line-detectors
 *
 * combines detectors only of the same objectId
 */
public class ProcessH implements IProcess {

    public final float maxFusionsPerCycle = 0.15f; //adjustable

    public ProcessConnector<RetinaPrimitive> resultPrimitiveConnector;
    public ProcessConnector<RetinaPrimitive> inputPrimitiveConnection;

    public Vector2d<Integer> imageSize;

    public final int GRIDSIZE = 8;
    public IMap2d<Boolean> accelerationMap;

    public void set(final ProcessConnector<RetinaPrimitive> inputPrimitiveConnection, final ProcessConnector<RetinaPrimitive> resultPrimitiveConnector) {
        this.inputPrimitiveConnection = inputPrimitiveConnection;
        this.resultPrimitiveConnector = resultPrimitiveConnector;
    }

    @Override
    public void setImageSize(final Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
        assert (imageSize.x % GRIDSIZE) == 0 : "ASSERT: " + "";
        assert (imageSize.y % GRIDSIZE) == 0 : "ASSERT: " + "";

        accelerationMap = new Map2d<>(imageSize.x / GRIDSIZE, imageSize.y / GRIDSIZE);

        for(var y = 0; y < accelerationMap.getLength(); y++ )
            for (var x = 0; x < accelerationMap.getWidth(); x++) accelerationMap.setAt(x, y, false);
    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {
        final var allInputDetectors = inputPrimitiveConnection.getWorkspace();

        final var objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(allInputDetectors);

        objectIdToRetinaPrimitivesMap.forEachKeyValue((IntObjectProcedure<Deque<RetinaPrimitive>>) (k, v) -> {
            combineOfObjectId(v, k, maxFusionsPerCycle);

            // transfer the detectors into the result
            resultPrimitiveConnector.addAll(v);
        });

    }

    @Override
    public void postProcessData() {

    }

    public static void combineOfObjectId(final Deque<RetinaPrimitive> workingDetectors, final int objectId, final float maxFusionPercent) {
        // called low and high because the index low is always lower than high


        // TODO< this algorithm is simple, it is possible to optimize this >
        // TODO< a possible solution is to flag "deleted" elements in the input array and store the fused Detectors in a second array
        //       then after one iteration these two arrays get merged (without deleted elements), this repeats as long as elements are fused
        //     >
        //for(;;) {
        //boolean terminate;

        //terminate = true;


        var remainingCombinations = (int) Math.ceil( maxFusionPercent * workingDetectors.size() );


        //System.out.println("H: START detectors=" + workingDetectors.size());

        final var a = workingDetectors.iterator();

        while (a.hasNext()) {
            final var detectorLow = a.next().line;

            //Assert.Assert(workingDetectors.get(iteratorLow).type == RetinaPrimitive.EnumType.LINESEGMENT, "");

            final var b = workingDetectors.descendingIterator();
            while (b.hasNext()) {
                final var detectorHigh = b.next().line;

                //Assert.Assert(workingDetectors.get(iteratorHigh).type == RetinaPrimitive.EnumType.LINESEGMENT, "");

                //exclude reverse order permutations (triangular matrix)
                if (detectorHigh.serial <= detectorLow.serial) break;


                var fused = false;

                if( canDetectorsBeFusedOverlap(detectorLow, detectorHigh) ) {

                    final var fusedLineDetector = fuseLineDetectorsOverlap(detectorLow, detectorHigh);
                    addNewLine(workingDetectors, objectId, fusedLineDetector);

                    fused = true;
                }
                else if( canDetectorsBeFusedInside(detectorLow, detectorHigh) ) {

                    final var fusedLineDetector = fuseLineDetectorsInside(detectorLow, detectorHigh);
                    addNewLine(workingDetectors, objectId, fusedLineDetector);

                    fused = true;
                }

                if (fused) {
                    remainingCombinations--;

                    b.remove();
                    a.remove();


                    if (remainingCombinations == 0)
                        return;
                    else
                        break;


                }
            }
        }
    }

    private static void addNewLine(final Collection<RetinaPrimitive> workingDetectors, final int objectId, final SingleLineDetector fusedLineDetector) {

        final var newLine = RetinaPrimitive.makeLine(fusedLineDetector);
        newLine.objectId = objectId;
        workingDetectors.add(newLine);
    }

//    private void drawLineToAccelerationStructure(final SingleLineDetector lineDetector, final boolean value) {
//        final Vector2d<Integer> integerAbsolutePositionA = arrayRealVectorToInteger(lineDetector.a, ArrayRealVectorHelper.EnumRoundMode.DOWN);
//        final Vector2d<Integer> integerAbsolutePositionB = arrayRealVectorToInteger(lineDetector.b, ArrayRealVectorHelper.EnumRoundMode.DOWN);
//
//        final Vector2d<Integer> integerAccelerationPositionA = new Vector2d<>(integerAbsolutePositionA.x / GRIDSIZE, integerAbsolutePositionA.y / GRIDSIZE);
//        final Vector2d<Integer> integerAccelerationPositionB = new Vector2d<>(integerAbsolutePositionB.x / GRIDSIZE, integerAbsolutePositionB.y / GRIDSIZE);
//
//        for( final IntIntPair iterationPosition : getPositionsOfCellsOfLineUnbound(integerAccelerationPositionA, integerAccelerationPositionB)) {
//            accelerationMap.setAt(iterationPosition.getOne(), iterationPosition.getTwo(), value);
//        }
//    }

    // overlap case
    private static boolean canDetectorsBeFusedOverlap(final SingleLineDetector detectorA, final SingleLineDetector detectorB) {
        // TODO< vertical special case >

        var projectedABegin = detectorA.getAProjected();
        var projectedAEnd = detectorA.getBProjected();
        var projectedBBegin = detectorB.getAProjected();
        var projectedBEnd = detectorB.getBProjected();

        var inplaceDetectorA = detectorA;
        var inplaceDetectorB = detectorB;

        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {


            final var tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            projectedBBegin = tempBegin;

            final var tempEnd = projectedAEnd;
            projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;


            final var tempDetector = inplaceDetectorA;
            inplaceDetectorA = inplaceDetectorB;
            inplaceDetectorB = tempDetector;
        }

        if( vectorXBetweenInclusive(projectedABegin, projectedAEnd, projectedBBegin) && vectorXBetweenInclusive(projectedBBegin, projectedBEnd, projectedAEnd) )
        {
        }
        else return false;

        // projecting the points on the other line and measue the distance

        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedBBegin, inplaceDetectorA) ) return false;

        return isProjectedPointOntoLineBelowDistanceLimit(projectedAEnd, inplaceDetectorB);
    }

    // fusing for overlap case
    private static SingleLineDetector fuseLineDetectorsOverlap(final SingleLineDetector detectorA, final SingleLineDetector detectorB) {
        // TODO< vertical special case >

        // we fuse them with taking the lowest begin-x as the begin and the other as the end

        var projectedABegin = detectorA.getAProjected();
        final var projectedAEnd = detectorA.getBProjected();
        final var projectedBBegin = detectorB.getAProjected();
        var projectedBEnd = detectorB.getBProjected();

        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        // TODO BUG FIXME< the variables after the switching are not used >
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {

            //tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            //projectedBBegin = tempBegin;

            final var /*tempBegin, */tempEnd = projectedAEnd;
            //projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;
        }


      
        final var conf = NalTv.calcRevConf(detectorA.conf, detectorB.conf);
        final var fusedLineDetector = SingleLineDetector.createFromFloatPositions(projectedABegin, projectedBEnd, conf);

        fusedLineDetector.resultOfCombination = true;
        return fusedLineDetector;
    }

    // inside case
    private static boolean canDetectorsBeFusedInside(final SingleLineDetector detectorA, final SingleLineDetector detectorB) {
        // TODO< vertical special case >

        // which case?
        // detectorB inside detectorA ?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  )
            return isProjectedPointOntoLineBelowDistanceLimit(detectorB.a, detectorA) && isProjectedPointOntoLineBelowDistanceLimit(detectorB.b, detectorA);
        else // detectorA inside detectorB ?
            if( vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b) )
                return isProjectedPointOntoLineBelowDistanceLimit(detectorA.a, detectorB) && isProjectedPointOntoLineBelowDistanceLimit(detectorA.b, detectorB);
        else return false;
    }

    // TODO fuseLineDetectorsInside
    private static SingleLineDetector fuseLineDetectorsInside(final SingleLineDetector detectorA, final SingleLineDetector detectorB) {
        final SingleLineDetector fusedLineDetector;

        final var conf = NalTv.calcRevConf(detectorA.conf, detectorB.conf);


        // TODO< vertical special case >

        // which case?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  ) {
            // detectorB inside detectorA

            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorA.a, detectorA.b, conf);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
        else {
            assert vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b) : "ASSERT: " + "";

            // detectorA inside detectorB

            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorB.a, detectorB.b, conf);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
    }

    /**
     * checks if the value.x is between min.x and max.x
     *
     */
    private static boolean vectorXBetween(final ArrayRealVector min, final ArrayRealVector max, final ArrayRealVector value) {
        final var vData = value.getDataRef();
        return vData[0] > min.getDataRef()[0] && vData[0] < max.getDataRef()[0];
    }

    private static boolean vectorXBetweenInclusive(final ArrayRealVector min, final ArrayRealVector max, final ArrayRealVector value) {
        final var vData = value.getDataRef();
        return vData[0] >= min.getDataRef()[0] && vData[0] <= max.getDataRef()[0];
    }

    private static boolean isProjectedPointOntoLineBelowDistanceLimit(final ArrayRealVector point, final SingleLineDetector line) {
        final var projectedPoint = line.projectPointOntoLine(point);
        final var distanceBetweenProjectedAndPoint = projectedPoint.getDistance(point);

        return distanceBetweenProjectedAndPoint < HardParameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
}
