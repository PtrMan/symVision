package ptrman.bpsolver.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import org.junit.Ignore;
import org.junit.Test;
import ptrman.levels.retina.ProcessA;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ProcessCTest {
    @Test @Ignore
    public void testProcessC() {


        var createdSample = new ProcessA.Sample(new ArrayRealVector(new double[]{0, 0}));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        final List<ProcessA.Sample> samples = new ArrayList<>();
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

        assert false : "ASSERT: " + "TODO< overwork >";
        //ProcessC processC = new ProcessC(null);

        // TODO
        // processC.process(samples);

        for(var i = 0; i < samples.size(); i++ )
            if (i == 4)
                assert samples.get(i).type == ProcessA.Sample.EnumType.ENDOSCELETON : "ASSERT: " + "must be endosceleton";
            else assert samples.get(i).type == ProcessA.Sample.EnumType.EXOSCELETON : "ASSERT: " + "must be exosceleton";


    }
}
