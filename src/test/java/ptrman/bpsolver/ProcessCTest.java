package ptrman.bpsolver;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Test;
import ptrman.levels.retina.ProcessA;
import ptrman.levels.retina.ProcessC;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ProcessCTest {
    @Test
    public void testProcessC() {
        List<ProcessA.Sample> samples = new ArrayList<>();

        ProcessA.Sample createdSample;


        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{0, 0}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{1, 0}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{2, 0}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{0, 1}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{1, 1}));
        createdSample.altitude = (float)Math.sqrt(2.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{2, 1}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{0, 2}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{1, 2}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{2, 2}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        ProcessC processC = new ProcessC(null);

        processC.process(samples);

        for( int i = 0; i < samples.size(); i++ ) {
            if( i == 4 ) {
                Assert.Assert(samples.get(i).type == ProcessA.Sample.EnumType.ENDOSCELETON, "must be endosceleton");
            }
            else {
                Assert.Assert(samples.get(i).type == ProcessA.Sample.EnumType.EXOSCELETON, "must be exosceleton");
            }
        }


    }
}
