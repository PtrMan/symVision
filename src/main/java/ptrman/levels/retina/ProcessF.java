package ptrman.levels.retina;

import com.gs.collections.impl.list.mutable.FastList;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;

import java.util.List;

/**
 * Sends traces in imagespace for samples which are deeper than a threshold
 *
 * * works only on rasterized input
 */
public class ProcessF implements IProcess {
    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
    }

    public void preSetup(ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> outputSampleConnector) {
        this.inputSampleConnector = inputSampleConnector;
        this.outputSampleConnector = outputSampleConnector;
    }

    @Override
    public void setup() {

    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {
        for(;;) {
            if( inputSampleConnector.getSize() == 0 ) {
                break;
            }

            final ProcessA.Sample currentSample = inputSampleConnector.poll();
            final ArrayRealVector[] borderPositions = processSample(currentSample);

            List<ProcessA.Sample> borderSamples = createSamplesWithPositions(borderPositions);

            for( final ProcessA.Sample iterationSample : borderSamples ) {
                outputSampleConnector.add(iterationSample);
            }
        }
    }

    @Override
    public void postProcessData() {

    }

    public void set(IMap2d<Boolean> map) {
        this.map = map;
    }

    private ArrayRealVector[] processSample(ProcessA.Sample sample) {

        //List<Ray> inativeRays = new ArrayList<>();
        ArrayRealVector[] resultPositions = new ArrayRealVector[COUNTOFRAYDIRECTIONS];

        List<Ray> activeRays = new FastList<>(RAYDIRECTIONS.length);
        for( ArrayRealVector currentRayDirection : RAYDIRECTIONS ) {
            activeRays.add(new Ray(sample.position, currentRayDirection));
        }

        int remaining = activeRays.size();

        for(;;) {


            for( Ray iterationRay : activeRays ) {
                if (iterationRay.isActive) {
                    iterationRay.advance();
                }
            }

            for( Ray iterationRay : activeRays ) {
                if (iterationRay.isActive) {
                    if( !readMapAtFloat(iterationRay.position) ) {
                        iterationRay.isActive = false;
                        remaining--;
                    }
                }
            }

            if( remaining == 0 ) //activeRays.isEmpty() ) {
                break;
            //}

            /*
            for( Ray iterationRay : activeRays ) {
                if( !iterationRay.isActive ) {
                    inativeRays.add(iterationRay);
                }
            }
            */

            //activeRays.removeIf(current -> !current.isActive);

        }

        for( int i = 0; i < COUNTOFRAYDIRECTIONS; i++ ) {
            resultPositions[i] = activeRays.get(i).position;
        }

        return resultPositions;

    }

    private boolean readMapAtFloat(ArrayRealVector position) {
        // NOTE use default rounding
        final double[] dr = position.getDataRef();
        int x = (int) dr[0];
        int y = (int) dr[1];

        if( !map.inBounds(x, y) ) {
            return false;
        }

        return map.readAt(x, y);
    }

    private static List<ProcessA.Sample> createSamplesWithPositions(ArrayRealVector[] positions) {
        List<ProcessA.Sample> samples = new FastList<>(positions.length);

        for( ArrayRealVector position : positions ) {
            samples.add(new ProcessA.Sample(position));
        }

        return samples;
    }

    private static ArrayRealVector[] calculateRayDirections(int divisions) {
        ArrayRealVector[] result = new ArrayRealVector[divisions];

        for( int currentDivision = 0; currentDivision < divisions; currentDivision++ ) {
            double relativeDivision = (double)currentDivision / (double)divisions;
            double radiants = relativeDivision * 2.0 * java.lang.Math.PI;

            double x = java.lang.Math.sin(radiants);
            double y = java.lang.Math.cos(radiants);

            result[currentDivision] = new ArrayRealVector(new double[]{x, y}, false);
        }

        return result;
    }

    private static class Ray {
        public Ray(ArrayRealVector position, ArrayRealVector direction) {
            this.position = position;
            this.direction = direction;
        }

        public void advance() {
            position = position.add(direction);

        }

        public ArrayRealVector position;
        public ArrayRealVector direction;
        public boolean isActive = true;
    }


    private IMap2d<Boolean> map;

    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<ProcessA.Sample> outputSampleConnector;

    private static final int COUNTOFRAYDIRECTIONS = 16;
    private static final ArrayRealVector[] RAYDIRECTIONS = calculateRayDirections(COUNTOFRAYDIRECTIONS);
}
