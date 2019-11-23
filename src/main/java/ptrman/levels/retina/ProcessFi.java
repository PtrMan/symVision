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

    public IMap2d<Float> workingImage;
    public ProcessConnector<TexPoint> outputSampleConnector;

    public int numberOfSamples = 1000;

    public double thresholdFilled = 0.5; // threshold for pixel to register as filled
    public double thresholdGradient = 0.05; // threshold for pixel to register as gradient

    public void preProcess() {
        outputSampleConnector.workspace.clear();
    }

    public void process() {
        // TODO< better sampling strategy which is less random, based on grid, etc >

        for(int iSample=0;iSample<numberOfSamples;iSample++) {
            int posX = 1+rng.nextInt(workingImage.getWidth()-2);
            int posY = 1+rng.nextInt(workingImage.getLength()-2);

            float v = workingImage.readAt(posX, posY);
            if (v > thresholdFilled) {
                { // filling particle
                    TexPoint tp = new TexPoint(pair(posX, posY), "f"); // filled
                    tp.value = v;
                    outputSampleConnector.workspace.add(tp);
                }

                { // gradient particle if gradient is present
                    float vx = workingImage.readAt(posX+1, posY);
                    float vy = workingImage.readAt(posX, posY+1);
                    if (Math.abs(v-vx) > thresholdGradient || Math.abs(v-vy) > thresholdGradient) { // is gradient present?
                        TexPoint tp = new TexPoint(pair(posX, posY), "g"); // gradient
                        tp.gradientX = v-vx;
                        tp.gradientY = v-vy;
                        outputSampleConnector.workspace.add(tp);
                    }
                }
            }
        }
    }
}
