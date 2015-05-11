package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;

import java.util.Queue;

/**
 * filters samples by type
 */
public class ProcessSampleFilter implements IProcess {
    public ProcessSampleFilter(final ProcessA.Sample.EnumType filterType) {
        this.filterType = filterType;
    }

    public void preSetupSet(Queue<ProcessA.Sample> inputSampleQueue, Queue<ProcessA.Sample> outputSampleQueue) {
        this.inputSampleQueue = inputSampleQueue;
        this.outputSampleQueue = outputSampleQueue;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
    }

    @Override
    public void setup() {
    }

    @Override
    public void processData() {
        final int numberOfSamplesInInputQueue = inputSampleQueue.size();

        for( int i = 0; i < numberOfSamplesInInputQueue; i++ ) {
            final ProcessA.Sample currentSample = inputSampleQueue.poll();

            if( currentSample.type == filterType ) {
                outputSampleQueue.add(currentSample);
            }
        }
    }

    private Queue<ProcessA.Sample> inputSampleQueue;
    private Queue<ProcessA.Sample> outputSampleQueue;

    private ProcessA.Sample.EnumType filterType;
}
