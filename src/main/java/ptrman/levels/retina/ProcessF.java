package ptrman.levels.retina;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Sends traces in imagespace for samples which are deeper than a threshold
 *
 * * works only on rasterized input
 */
public class ProcessF {
    private static class Ray {
        public Ray(ArrayRealVector position, ArrayRealVector direction) {
            this.position = position;
            this.direction = direction;
        }

        public void advance() {
            position.add(direction);
        }

        public ArrayRealVector position;
        public ArrayRealVector direction;
        public boolean isActive = true;
    }


    public ProcessF(Queue<ProcessA.Sample> inputSampleQueue, IMap2d<Boolean> map) {
        this.inputSampleQueue = inputSampleQueue;
        this.map = map;
    }

    public void loop() {
        ProcessA.Sample currentSample = inputSampleQueue.poll();
        ArrayRealVector[] borderPositions = processSample(currentSample);
        // TODO
    }

    private ArrayRealVector[] processSample(ProcessA.Sample sample) {
        List<Ray> activeRays = new ArrayList<>();
        List<Ray> inativeRays = new ArrayList<>();
        ArrayRealVector[] resultPositions = new ArrayRealVector[COUNTOFRAYDIRECTIONS];

        for( ArrayRealVector currentRayDirection : RAYDIRECTIONS ) {
            activeRays.add(new Ray(sample.position, currentRayDirection));
        }

        for(;;) {
            if( activeRays.isEmpty() ) {
                break;
            }

            for( Ray iterationRay : activeRays ) {
                iterationRay.advance();
            }

            for( Ray iterationRay : activeRays ) {
                if( !readMapAtFloat(iterationRay.position) ) {
                    iterationRay.isActive = false;
                }
            }

            for( Ray iterationRay : activeRays ) {
                if( !iterationRay.isActive ) {
                    inativeRays.add(iterationRay);
                }
            }

            activeRays.removeIf(current -> !current.isActive);
        }

        for( int i = 0; i < COUNTOFRAYDIRECTIONS; i++ ) {
            resultPositions[i] = inativeRays.get(i).position;
        }

        return resultPositions;

    }

    private boolean readMapAtFloat(ArrayRealVector position) {
        // NOTE use default rounding
        int x = (int)position.getDataRef()[0];
        int y = (int)position.getDataRef()[1];

        if( !map.inBounds(new Vector2d<>(x, y)) ) {
            return false;
        }

        return map.readAt(x, y);
    }

    private static List<ProcessA.Sample> createSamplesWithPositions(ArrayRealVector[] positions) {
        List<ProcessA.Sample> samples = new ArrayList<>();

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

            result[currentDivision] = new ArrayRealVector(new double[]{x, y});
        }

        return result;
    }

    private Queue<ProcessA.Sample> inputSampleQueue;
    private final IMap2d<Boolean> map;

    private static final int COUNTOFRAYDIRECTIONS = 16;
    private static final ArrayRealVector[] RAYDIRECTIONS = calculateRayDirections(COUNTOFRAYDIRECTIONS);
}
