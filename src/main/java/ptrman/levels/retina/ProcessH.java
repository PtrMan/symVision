package ptrman.levels.retina;

import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.bpsolver.HardParameters;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.misc.Assert;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;

import static ptrman.bpsolver.Helper.createMapByObjectIdsFromListOfRetinaPrimitives;
import static ptrman.levels.retina.helper.SpatialDrawer.getPositionsOfCellsOfLineUnbound;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;

/**
 * tries to combine linedetectors
 *
 * combines detectors only of the same objectId
 */
public class ProcessH implements IProcess {

    final float maxFusionsPerCycle = 0.15f; //adjustable

    private ProcessConnector<RetinaPrimitive> resultPrimitiveConnector;
    private ProcessConnector<RetinaPrimitive> inputPrimitiveConnection;

    private Vector2d<Integer> imageSize;

    private final int GRIDSIZE = 8;
    private IMap2d<Boolean> accelerationMap;

    public void set(ProcessConnector<RetinaPrimitive> inputPrimitiveConnection, ProcessConnector<RetinaPrimitive> resultPrimitiveConnector) {
        this.inputPrimitiveConnection = inputPrimitiveConnection;
        this.resultPrimitiveConnector = resultPrimitiveConnector;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {
        Assert.Assert((imageSize.x % GRIDSIZE) == 0, "");
        Assert.Assert((imageSize.y % GRIDSIZE) == 0, "");

        accelerationMap = new Map2d<>(imageSize.x / GRIDSIZE, imageSize.y / GRIDSIZE);

        for( int y = 0; y < accelerationMap.getLength(); y++ ) {
            for( int x = 0; x < accelerationMap.getWidth(); x++ ) {
                accelerationMap.setAt(x, y, false);
            }
        }
    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {
        List<RetinaPrimitive> allInputDetectors = inputPrimitiveConnection.getWorkspace();

        IntObjectHashMap<Deque<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(allInputDetectors);

        objectIdToRetinaPrimitivesMap.forEachKeyValue( (k, v) -> {
            combineOfObjectId(v, k, maxFusionsPerCycle);

            // transfer the detectors into the result
            resultPrimitiveConnector.addAll(v);
        });

    }

    @Override
    public void postProcessData() {

    }

    public void combineOfObjectId(Deque<RetinaPrimitive> workingDetectors, final int objectId, float maxFusionPercent) {
        // called low and high because the index low is always lower than high


        // TODO< this algorithm is simple, it is possible to optimize this >
        // TODO< a possible solution is to flag "deleted" elements in the input array and store the fused Detectors in a second array
        //       then after one iteration these two arrays get merged (without deleted elements), this repeats as long as elements are fused
        //     >
        //for(;;) {
        //boolean terminate;

        //terminate = true;



        int remainingCombinations = (int) Math.ceil( maxFusionPercent * workingDetectors.size() );


        //System.out.println("H: START detectors=" + workingDetectors.size());

        Iterator<RetinaPrimitive> a = workingDetectors.iterator();

        while (a.hasNext()) {
            SingleLineDetector detectorLow = a.next().line;

            //Assert.Assert(workingDetectors.get(iteratorLow).type == RetinaPrimitive.EnumType.LINESEGMENT, "");

            Iterator<RetinaPrimitive> b = workingDetectors.descendingIterator();
            while (b.hasNext()) {
                SingleLineDetector detectorHigh = b.next().line;

                //Assert.Assert(workingDetectors.get(iteratorHigh).type == RetinaPrimitive.EnumType.LINESEGMENT, "");

                if (detectorHigh.serial <= detectorLow.serial) {
                    //exclude reverse order permutations (triangular matrix)
                    break;
                }



                boolean fused = false;

                if( canDetectorsBeFusedOverlap(detectorLow, detectorHigh) ) {
                    SingleLineDetector fusedLineDetector;

                    fusedLineDetector = fuseLineDetectorsOverlap(detectorLow, detectorHigh);

                    addNewLine(workingDetectors, objectId, fusedLineDetector);


                    fused = true;
                }
                else if( canDetectorsBeFusedInside(detectorLow, detectorHigh) ) {
                    SingleLineDetector fusedLineDetector;

                    fusedLineDetector = fuseLineDetectorsInside(detectorLow, detectorHigh);

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

    private void addNewLine(Collection<RetinaPrimitive> workingDetectors, int objectId, SingleLineDetector fusedLineDetector) {

        RetinaPrimitive newLine = RetinaPrimitive.makeLine(fusedLineDetector);
        newLine.objectId = objectId;
        workingDetectors.add(newLine);
    }

    private void drawLineToAccelerationStructure(final SingleLineDetector lineDetector, final boolean value) {
        final Vector2d<Integer> integerAbsolutePositionA = arrayRealVectorToInteger(lineDetector.a, ArrayRealVectorHelper.EnumRoundMode.DOWN);
        final Vector2d<Integer> integerAbsolutePositionB = arrayRealVectorToInteger(lineDetector.b, ArrayRealVectorHelper.EnumRoundMode.DOWN);

        final Vector2d<Integer> integerAccelerationPositionA = new Vector2d<>(integerAbsolutePositionA.x / GRIDSIZE, integerAbsolutePositionA.y / GRIDSIZE);
        final Vector2d<Integer> integerAccelerationPositionB = new Vector2d<>(integerAbsolutePositionB.x / GRIDSIZE, integerAbsolutePositionB.y / GRIDSIZE);

        final List<Vector2d<Integer>> detectorLowPositions = getPositionsOfCellsOfLineUnbound(integerAccelerationPositionA, integerAccelerationPositionB);

        for( final Vector2d<Integer> iterationPosition : detectorLowPositions ) {
            accelerationMap.setAt(iterationPosition.x, iterationPosition.y, value);
        }
    }

    // overlap case
    private static boolean canDetectorsBeFusedOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >

        SingleLineDetector inplaceDetectorA, inplaceDetectorB;

        ArrayRealVector projectedABegin = detectorA.getAProjected();
        ArrayRealVector projectedAEnd = detectorA.getBProjected();
        ArrayRealVector projectedBBegin = detectorB.getAProjected();
        ArrayRealVector projectedBEnd = detectorB.getBProjected();

        inplaceDetectorA = detectorA;
        inplaceDetectorB = detectorB;

        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {
            ArrayRealVector tempBegin, tempEnd;
            SingleLineDetector tempDetector;


            tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            projectedBBegin = tempBegin;

            tempEnd = projectedAEnd;
            projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;


            tempDetector = inplaceDetectorA;
            inplaceDetectorA = inplaceDetectorB;
            inplaceDetectorB = tempDetector;
        }

        if( vectorXBetweenInclusive(projectedABegin, projectedAEnd, projectedBBegin) && vectorXBetweenInclusive(projectedBBegin, projectedBEnd, projectedAEnd) )
        {
        }
        else {
            return false;
        }

        // projecting the points on the other line and measue the distance

        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedBBegin, inplaceDetectorA) ) {
            return false;
        }

        if( !isProjectedPointOntoLineBelowDistanceLimit(projectedAEnd, inplaceDetectorB) ) {
            return false;
        }

        return true;
    }

    // fusing for overlap case
    private static SingleLineDetector fuseLineDetectorsOverlap(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >

        SingleLineDetector fusedLineDetector;

        // we fuse them with taking the lowest begin-x as the begin and the other as the end

        ArrayRealVector projectedABegin = detectorA.getAProjected();
        ArrayRealVector projectedAEnd = detectorA.getBProjected();
        ArrayRealVector projectedBBegin = detectorB.getAProjected();
        ArrayRealVector projectedBEnd = detectorB.getBProjected();

        // we need to sort them after the x of the begin, so ABegin.x is always the lowest
        // TODO BUG FIXME< the variables after the switching are not used >
        if( projectedBBegin.getDataRef()[0] < projectedABegin.getDataRef()[0] ) {
            ArrayRealVector /*tempBegin, */tempEnd;

            //tempBegin = projectedABegin;
            projectedABegin = projectedBBegin;
            //projectedBBegin = tempBegin;

            tempEnd = projectedAEnd;
            //projectedAEnd = projectedBEnd;
            projectedBEnd = tempEnd;
        }

        fusedLineDetector = SingleLineDetector.createFromFloatPositions(projectedABegin, projectedBEnd);
        fusedLineDetector.resultOfCombination = true;
        return fusedLineDetector;
    }

    // inside case
    private static boolean canDetectorsBeFusedInside(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        // TODO< vertical special case >

        // which case?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  ) {
            // detectorB inside detectorA ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorB.a, detectorA) && isProjectedPointOntoLineBelowDistanceLimit(detectorB.b, detectorA)  ) {
                return true;
            }
            else {
                return false;
            }
        }
        else if( vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b) ) {
            // detectorA inside detectorB ?
            if( isProjectedPointOntoLineBelowDistanceLimit(detectorA.a, detectorB) && isProjectedPointOntoLineBelowDistanceLimit(detectorA.b, detectorB)  ) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    // TODO fuseLineDetectorsInside
    private static SingleLineDetector fuseLineDetectorsInside(SingleLineDetector detectorA, SingleLineDetector detectorB) {
        SingleLineDetector fusedLineDetector;

        // TODO< vertical special case >

        // which case?
        if( vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.a) && vectorXBetweenInclusive(detectorA.a, detectorA.b, detectorB.b)  ) {
            // detectorB inside detectorA

            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorA.a, detectorA.b);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
        else {
            Assert.Assert(vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.a) && vectorXBetweenInclusive(detectorB.a, detectorB.b, detectorA.b), "");

            // detectorA inside detectorB

            fusedLineDetector = SingleLineDetector.createFromFloatPositions(detectorB.a, detectorB.b);
            fusedLineDetector.resultOfCombination = true;
            return fusedLineDetector;
        }
    }

    /**
     * checks if the value.x is between min.x and max.x
     *
     */
    private static boolean vectorXBetween(ArrayRealVector min, ArrayRealVector max, ArrayRealVector value) {
        final double[] vData = value.getDataRef();
        return vData[0] > min.getDataRef()[0] && vData[0] < max.getDataRef()[0];
    }

    private static boolean vectorXBetweenInclusive(ArrayRealVector min, ArrayRealVector max, ArrayRealVector value) {
        final double[] vData = value.getDataRef();
        return vData[0] >= min.getDataRef()[0] && vData[0] <= max.getDataRef()[0];
    }

    private static boolean isProjectedPointOntoLineBelowDistanceLimit(ArrayRealVector point, SingleLineDetector line) {
        ArrayRealVector projectedPoint = line.projectPointOntoLine(point);
        double distanceBetweenProjectedAndPoint = projectedPoint.getDistance(point);

        //System.out.println("line A (" + line.aFloat.x.toString() + "," + line.aFloat.y.toString() + ") B (" + line.bFloat.x.toString() + "," + line.bFloat.y.toString() + ")");
        //System.out.println("point (" + point.x.toString() + "," + point.y.toString() + ")");
        //System.out.println("projectedpoint (" + projectedPoint.x.toString() + "," + projectedPoint.y.toString() + ")");

        return distanceBetweenProjectedAndPoint < HardParameters.ProcessH.MAXDISTANCEFORCANDIDATEPOINT;
    }
}
