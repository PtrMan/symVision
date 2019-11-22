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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.eclipse.collections.impl.list.mutable.FastList;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;

import java.util.List;

import static ptrman.levels.retina.LineDetectorWithMultiplePoints.real;

/**
 * Sends traces in imagespace for samples which are deeper than a threshold
 *
 * * works only on rasterized input
 */
public class ProcessF implements IProcess {
    public IMap2d<Boolean> map;

    public ProcessConnector<ProcessA.Sample> inputSampleConnector;
    public ProcessConnector<ProcessA.Sample> outputSampleConnector;

    public static final int COUNTOFRAYDIRECTIONS = 16;
    public static final ArrayRealVector[] RAYDIRECTIONS;

    @Override
    public void setImageSize(final Vector2d<Integer> imageSize) {
    }

    public void preSetup(final ProcessConnector<ProcessA.Sample> inputSampleConnector, final ProcessConnector<ProcessA.Sample> outputSampleConnector) {
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
        var borderPositions = new ArrayRealVector[COUNTOFRAYDIRECTIONS];
        final List<Ray> activeRays = new FastList<>(RAYDIRECTIONS.length);

        while (inputSampleConnector.getSize() > 0)
            for (final var borderPos : (borderPositions = processSample(inputSampleConnector.poll(), borderPositions, activeRays)))
                outputSampleConnector.add(new ProcessA.Sample(borderPos));
    }

    @Override
    public void postProcessData() {

    }

    public void set(final IMap2d<Boolean> map) {
        this.map = map;
    }

    private ArrayRealVector[] processSample(final ProcessA.Sample sample, final ArrayRealVector[] resultPositions, final List<Ray> active) {

        active.clear();
        for( final var currentRayDirection : RAYDIRECTIONS )
            active.add(new Ray(sample.position, currentRayDirection));

        var remaining = COUNTOFRAYDIRECTIONS;

        while (remaining > 0) for (final var r : active)
            if (r.isActive) {
                r.advance();
                if (!readMapAtFloat(r.position)) {
                    r.isActive = false;
                    remaining--;
                }
            }

        for(var i = 0; i < COUNTOFRAYDIRECTIONS; i++ ) resultPositions[i] = active.get(i).position;

        return resultPositions;

    }

    private boolean readMapAtFloat(final ArrayRealVector position) {
        final var dr = position.getDataRef();

        //int x = (int) dr[0], y = (int) dr[1]; //default rounding
        final var x = (int)Math.round(dr[0]);
        final var y = (int)Math.round(dr[1]);

        return map.inBounds(x, y) && map.readAt(x, y);
    }

//    private static List<ProcessA.Sample> createSamplesWithPositions(ArrayRealVector[] positions) {
//        List<ProcessA.Sample> samples = new FastList<>(positions.length);
//
//        for( ArrayRealVector position : positions ) {
//            samples.add(new ProcessA.Sample(position));
//        }
//
//        return samples;
//    }


    private static class Ray {

        public Ray(final IntIntPair position, final ArrayRealVector direction) {
            this(real(position), direction);
        }

        public Ray(final ArrayRealVector position, final ArrayRealVector direction) {
            this.position = position;
            this.direction = direction;
        }

        public void advance() {
            position = position.add(direction);

        }

        public ArrayRealVector position;
        public final ArrayRealVector direction;
        public boolean isActive = true;
    }

    static {
        final var divisions = COUNTOFRAYDIRECTIONS;

        final var result = new ArrayRealVector[divisions];

        for(var currentDivision = 0; currentDivision < divisions; currentDivision++ ) {
            final var relativeDivision = (double)currentDivision / (double)divisions;
            final var radiants = relativeDivision * 2.0 * java.lang.Math.PI;

            final var x = java.lang.Math.sin(radiants);
            final var y = java.lang.Math.cos(radiants);

            result[currentDivision] = new ArrayRealVector(new double[]{x, y}, false);
        }

        RAYDIRECTIONS = result;
    }
}
