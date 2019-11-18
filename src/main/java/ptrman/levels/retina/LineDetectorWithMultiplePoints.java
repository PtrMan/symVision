package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.list.primitive.IntList;
import ptrman.bpsolver.Parameters;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.NalTv;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.getScaled;

public class LineDetectorWithMultiplePoints {
    public List<ProcessA.Sample> samples = new ArrayList<>(); // actual samples which are "included" in the line

    public IntList integratedSampleIndices;

    // variable for the line drawing in the acceleration structure
    //public ArrayRealVector spatialAccelerationLineDirection; // can be null
    //public double spatialAccelerationLineLength; // can be null
    //public Vector2d<Integer> spatialAccelerationCenterPosition;

    public double cachedConf = 0.0; // cached confidence of this line detector

    public double m, n;

    public double mse = 0.0f;

    public boolean isLocked = false; // has the detector received enough activation so it stays?

    public int commonObjectId = -1;

    public boolean doesContainSampleIndex(int index) {
        return integratedSampleIndices.contains(index);
    }

    public double getActivation() {
        return
            samples.size() * 0.1 +
            cachedConf * 1.8 +
            (Parameters.getProcessdMaxMse() - mse) * Parameters.getProcessdLockingActivationScale();
    }

    public boolean isCommonObjectIdValid() {
        return commonObjectId != -1;
    }

    public ArrayRealVector projectPointOntoLine(ArrayRealVector point) {
        if (isYAxisSingularity()) {
            // call isn't allowed
            throw new RuntimeException("internal error");
            //return projectPointOntoLineForSingular(point);
        } else {
            return projectPointOntoLineForNonsingular(point);
        }
    }

        /*private Vector2d<Float> projectPointOntoLineForSingular(Vector2d<Float> point)
        {
            return new Vector2d<Float>(point.x, horizontalOffset);
        }*/

    public ArrayRealVector projectPointOntoLineForNonsingular(ArrayRealVector point) {
        ArrayRealVector lineDirection = getNormalizedDirection();
        ArrayRealVector diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, n}));
        double dotResult = lineDirection.dotProduct(diffFromAToPoint);

        return new ArrayRealVector(new double[]{0.0f, n}).add(getScaled(lineDirection, dotResult));
    }

    private ArrayRealVector getNormalizedDirection() {
        if (isYAxisSingularity()) {
            throw new RuntimeException("internal error");
        } else {
            return ArrayRealVectorHelper.normalize(new ArrayRealVector(new double[]{1.0f, m}));
        }
    }

    // TODO< just simply test flag >
    public boolean isYAxisSingularity() {
        return Double.isInfinite(m);
    }

    public double getHorizontalOffset(List<ProcessA.Sample> samples) {
        Assert.Assert(isYAxisSingularity(), "");

        int sampleIndex = integratedSampleIndices.get(0);
        return samples.get(sampleIndex).position.getDataRef()[0];
    }

    public double getLength() {
        List<ArrayRealVector> sortedSamplePositions = ProcessD.getSortedSamplePositions(this);

        Assert.Assert(sortedSamplePositions.size() >= 2, "samples size must be equal or greater than two");

        // it doesn't care if the line is singuar or not, the distance is always the length
        final ArrayRealVector lastSamplePosition = sortedSamplePositions.get(sortedSamplePositions.size() - 1);
        final ArrayRealVector firstSamplePosition = sortedSamplePositions.get(0);

        return lastSamplePosition.subtract(firstSamplePosition).getNorm();
    }

    public void recalcConf() {
        cachedConf = samples.get(0).conf;
        int idx = 0;
        for(ProcessA.Sample iSample : samples) {
            if (idx++ > 0)
                cachedConf = NalTv.calcRevConf(cachedConf, iSample.conf);
        }
    }


    // must be called before removal in process-D
    public void cleanup() {
        for(ProcessA.Sample iSample : samples)
            iSample.refCount--;
    }

}