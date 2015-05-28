package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;

/**
 * filters samples by type
 */
public class ProcessSampleFilter implements IProcess {
    public ProcessSampleFilter(final ProcessA.Sample.EnumType filterType) {
        this.filterType = filterType;
    }

    public void preSetupSet(ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> outputSampleConnector) {
        this.inputSampleConnector = inputSampleConnector;
        this.outputSampleConnector = outputSampleConnector;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
    }

    @Override
    public void setup() {
    }

    @Override
    public void preProcessData() {

    }

    @Override
    public void processData() {
        while( inputSampleConnector.getSize() > 0 ) {
            final ProcessA.Sample currentSample = inputSampleConnector.poll();

            if( currentSample.type == filterType ) {
                outputSampleConnector.add(currentSample);
            }
        }
    }

    @Override
    public void postProcessData() {

    }

    private ProcessConnector<ProcessA.Sample> outputSampleConnector;
    private ProcessConnector<ProcessA.Sample> inputSampleConnector;

    private ProcessA.Sample.EnumType filterType;
}
