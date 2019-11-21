package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import ptrman.bpsolver.Parameters;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.NalTv;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.getScaled;

public class LineDetectorWithMultiplePoints {
    public List<ProcessA.Sample> samples = new ArrayList<>(); // actual samples which are "included" in the line

    public double cachedConf = 0.0; // cached confidence of this line detector

    public double m, n;

    public double mse = 0.0f;

    public boolean isHardened = false; // has the detector received enough activation so it got hardened?

    public int commonObjectId = -1;

    public double x = 0.0; // used to compute the activation function
    public double xStep = 0.0; // current stepsize of x
    public double xDecayDelta = 0.0; // how much is added or subtracted from x when computing the activation function - used for decay


    public LineDetectorWithMultiplePoints(double xStep) {
        this.xStep = xStep;
    }

    /** compute sigmoid like activation function as described in
     * https://www.foundalis.com/res/Generalization_of_Hebbian_Learning_and_Categorization.pdf
     */
    public static double calcActivation(double x) {
        x = Math.max(x, 0.0);
        x = Math.min(x, 1.0);

        double pointAX = 0.15; // from begin of quadratic shape to line segment
        double pointAY = 0.2;

        double pointBX = 1.0; // from linear segment to other quadratic shape
        double pointBY = 1.0;

        if (x < pointAX) { // are we in the region of the quadratic shape?
            double relX = x / pointAX;
            return relX*relX * pointAY;
        }
        else if (x < pointBX) { // linear shape
            double relX = (x - pointAX) / (pointBX-pointAX);
            return relX * (pointBY - pointAY) + pointAY;
        }
        else { //
            return 1.0; // TODO< implement computation >
        }
    }


    public double calcActivationX() {
        double x = this.x+xDecayDelta; // compute "real" x for activation function
        x = Math.max(x, 0.0);
        x = Math.min(x, 1.0);
        return x;
    }

    public double calcActivation() {
        double x = calcActivationX();
        return calcActivation(x);
    }

    @Deprecated public ArrayRealVector projectPointOntoLine(IntIntPair point) {
        return projectPointOntoLine(real(point));
    }

    public static ArrayRealVector real(IntIntPair point) {
        return new ArrayRealVector(new double[] { point.getOne(), point.getTwo()}, false);
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