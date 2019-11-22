package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import ptrman.math.ArrayRealVectorHelper;

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


    public LineDetectorWithMultiplePoints(final double xStep) {
        this.xStep = xStep;
    }

    /** compute sigmoid like activation function as described in
     * https://www.foundalis.com/res/Generalization_of_Hebbian_Learning_and_Categorization.pdf
     */
    public static double calcActivation(double x) {
        x = Math.max(x, 0.0);
        x = Math.min(x, 1.0);

        final var pointAX = 0.15; // from begin of quadratic shape to line segment
        final var pointAY = 0.2;

        final var pointBX = 1.0; // from linear segment to other quadratic shape

        if (x < pointAX) { // are we in the region of the quadratic shape?
            final var relX = x / pointAX;
            return relX*relX * pointAY;
        }
        else //
            if (x < pointBX) { // linear shape
            final var relX = (x - pointAX) / (pointBX-pointAX);
                final var pointBY = 1.0;
                return relX * (pointBY - pointAY) + pointAY;
        }
        else return 1.0; // TODO< implement computation >
    }


    public double calcActivationX() {
        var x = this.x+xDecayDelta; // compute "real" x for activation function
        x = Math.max(x, 0.0);
        x = Math.min(x, 1.0);
        return x;
    }

    public double calcActivation() {
        final var x = calcActivationX();
        return calcActivation(x);
    }

    @Deprecated public ArrayRealVector projectPointOntoLine(final IntIntPair point) {
        return projectPointOntoLine(real(point));
    }

    public static ArrayRealVector real(final IntIntPair point) {
        return new ArrayRealVector(new double[] { point.getOne(), point.getTwo()}, false);
    }

    public ArrayRealVector projectPointOntoLine(final ArrayRealVector point) {
        // call isn't allowed
        //return projectPointOntoLineForSingular(point);
        if (isYAxisSingularity()) throw new RuntimeException("internal error");
        else return projectPointOntoLineForNonsingular(point);
    }

        /*private Vector2d<Float> projectPointOntoLineForSingular(Vector2d<Float> point)
        {
            return new Vector2d<Float>(point.x, horizontalOffset);
        }*/

    public ArrayRealVector projectPointOntoLineForNonsingular(final ArrayRealVector point) {
        final var lineDirection = getNormalizedDirection();
        final var diffFromAToPoint = point.subtract(new ArrayRealVector(new double[]{0.0f, n}));
        final var dotResult = lineDirection.dotProduct(diffFromAToPoint);

        return new ArrayRealVector(new double[]{0.0f, n}).add(getScaled(lineDirection, dotResult));
    }

    private ArrayRealVector getNormalizedDirection() {
        if (isYAxisSingularity()) throw new RuntimeException("internal error");
        else return ArrayRealVectorHelper.normalize(new ArrayRealVector(new double[]{1.0f, m}));
    }

    // TODO< just simply test flag >
    public boolean isYAxisSingularity() {
        return Double.isInfinite(m);
    }

    public double getLength() {
        final var sortedSamplePositions = ProcessD.getSortedSamplePositions(this);

        assert sortedSamplePositions.size() >= 2 : "ASSERT: " + "samples size must be equal or greater than two";

        // it doesn't care if the line is singuar or not, the distance is always the length
        final var lastSamplePosition = sortedSamplePositions.get(sortedSamplePositions.size() - 1);
        final var firstSamplePosition = sortedSamplePositions.get(0);

        return lastSamplePosition.subtract(firstSamplePosition).getNorm();
    }

    public void recalcConf() {
        cachedConf = samples.get(0).conf;
        var idx = 0;
        for(final var iSample : samples)
            if (idx++ > 0)
                cachedConf = NalTv.calcRevConf(cachedConf, iSample.conf);
    }

    // must be called before removal in process-D
    public void cleanup() {
        for(final var iSample : samples)
            iSample.refCount--;
    }
}