package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by me on 7/16/15.
 */
public class SingleLineDetectorTest  {


    // tests
    @Test
    public void unittestProjectPoint() {
        SingleLineDetector testLine;
        ArrayRealVector point;
        ArrayRealVector projectedPoint;

        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{1.0f, 2.0f}), new ArrayRealVector(new double[]{2.0f, 3.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f}, false);

        projectedPoint = testLine.projectPointOntoLine(point);

        if( projectedPoint.getEntry(0) < 1.0f + 0.01f && projectedPoint.getEntry(0) > 1.0f - 0.01f && projectedPoint.getEntry(1) < 2.0f + 0.01f && projectedPoint.getEntry(1) > 2.0f - 0.01f ) {
            // all fine
        }
        else {
            //throw new RuntimeException("Unittest failed (1)");
            assertTrue(false);
        }


        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{1.0f, 2.0f}), new ArrayRealVector(new double[]{2.0f, 2.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f});

        projectedPoint = testLine.projectPointOntoLine(point);

        if( projectedPoint.getEntry(0) < 2.0f + 0.01f && projectedPoint.getEntry(0) > 2.0f - 0.01f && projectedPoint.getEntry(1) < 2.0f + 0.01f && projectedPoint.getEntry(1) > 2.0f - 0.01f ) {
            // all fine
        }
        else {
            //throw new RuntimeException("Unittest failed (1)");
            assertTrue(false);
        }

        testLine = SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{0.0f, 1.0f}), new ArrayRealVector(new double[]{1.0f, 0.0f}));
        point = new ArrayRealVector(new double[]{2.0f, 1.0f});

        projectedPoint = testLine.projectPointOntoLine(point);
    }

}