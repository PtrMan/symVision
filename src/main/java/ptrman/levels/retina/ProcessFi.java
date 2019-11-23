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

import ptrman.Datastructures.IMap2d;
import ptrman.levels.retina.helper.ProcessConnector;

import java.util.Random;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 * process to compute the filling/gradient/texture etc.
 */
public class ProcessFi {
    public Random rng = new Random();

    public IMap2d<Boolean> workingImage;
    public ProcessConnector<TexPoint> outputSampleConnector;

    public int numberOfSamples = 1000;

    public void preProcess() {
        outputSampleConnector.workspace.clear();
    }

    public void process() {
        // TODO< better sampling strategy which is less random, based on grid, etc >

        for(int iSample=0;iSample<numberOfSamples;iSample++) {
            int posX = rng.nextInt(workingImage.getWidth());
            int posY = rng.nextInt(workingImage.getLength());

            if (workingImage.readAt(posX, posY)) {
                TexPoint tp = new TexPoint(pair(posX, posY), "f");
                outputSampleConnector.workspace.add(tp);
            }
        }
    }
}
