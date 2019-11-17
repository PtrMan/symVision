/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
