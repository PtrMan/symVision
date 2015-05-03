package ptrman.bpsolver;

import org.junit.Test;
import ptrman.Datastructures.Vector2d;
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


        createdSample = new ProcessA.Sample(new Vector2d<>(0, 0));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(1, 0));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(2, 0));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        createdSample = new ProcessA.Sample(new Vector2d<>(0, 1));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(1, 1));
        createdSample.altitude = (float)Math.sqrt(2.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(2, 1));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        createdSample = new ProcessA.Sample(new Vector2d<>(0, 2));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(1, 2));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);

        createdSample = new ProcessA.Sample(new Vector2d<>(2, 2));
        createdSample.altitude = (float)Math.sqrt(0.0f);
        samples.add(createdSample);


        ProcessC processC = new ProcessC();

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
