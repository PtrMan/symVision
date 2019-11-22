/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.visualizationTests;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import processing.core.PApplet;
import ptrman.bpsolver.Solver2;
import ptrman.levels.retina.*;
import ptrman.levels.retina.helper.ProcessConnector;

/**
 * encapsulates functionality to draw visualizations for showcase and debugging
 */
public class VisualizationDrawer {
    public final boolean drawVisualizationOfAltitude = true;
    public final boolean drawVisualizationOfEndoSceletons = false; // do we visualize all samples of endo/exo -sceleton
    public final boolean drawVisualizationOfLineDetectors = true;
    public final boolean drawVisualizationOfLineDetectorsEnableAct = true; // do we draw activation of line detectors?
    public final boolean drawVisualizationOfEdgeLineDetectors = true;

    public void drawDetectors(final Solver2 solver, final PApplet applet) {
        if(drawVisualizationOfAltitude) for (final var iSample : solver.connectorSamplesForEndosceleton.workspace) {
            final var color = Math.min((float) iSample.altitude / 20.0f, 1.0f);

            applet.stroke(color * 255.0f);
            applet.rect((float) iSample.position.getOne(), (float) iSample.position.getTwo(), 1, 1);
        }

        if(drawVisualizationOfEndoSceletons) {

            applet.stroke(200.0f, 255.0f, 200.0f);

            for (final var s : solver.connectorSamplesForEndosceleton.workspace)
                if (s.type == ProcessA.Sample.EnumType.ENDOSCELETON) {
                    final var p = s.position;
                    applet.rect(p.getOne(), p.getTwo(), 1, 1);
                }
        }


        if(drawVisualizationOfEdgeLineDetectors) { // draw visualization of line detectors
            for (final var iProcessDEdge : solver.processDEdge)
                for (final var iLineDetector : iProcessDEdge.annealedCandidates) {
                    // iLineDetector.cachedSamplePositions


                    applet.stroke(128.0f, 128, 255);
                    for (final var iLine : iProcessDEdge.splitDetectorIntoLines(iLineDetector)) {
                        final var x0 = iLine.line.a.getDataRef()[0];
                        final var y0 = iLine.line.a.getDataRef()[1];
                        final var x1 = iLine.line.b.getDataRef()[0];
                        final var y1 = iLine.line.b.getDataRef()[1];
                        applet.line((float) x0, (float) y0, (float) x1, (float) y1);
                    }

                    if (false) {
                        applet.stroke(255.0f, 0.0f, 0.0f);
                        for (final var iSample : iLineDetector.samples)
                            applet.rect((float) iSample.position.getOne(), (float) iSample.position.getTwo(), 1, 1);
                    }
                }


            final var here = 5;
        }


        if(drawVisualizationOfLineDetectors) { // draw visualization of line detectors
            for(final var iLineDetector : solver.processD.annealedCandidates) {
                // iLineDetector.cachedSamplePositions

                final var act = drawVisualizationOfLineDetectorsEnableAct ? (float)iLineDetector.calcActivation() : 1.0f;
                applet.stroke(act*255.0f, act*255.0f, act*255.0f);


                for (final var iLine : solver.processD.splitDetectorIntoLines(iLineDetector)) {
                    final var x0 = iLine.line.a.getDataRef()[0];
                    final var y0 = iLine.line.a.getDataRef()[1];
                    final var x1 = iLine.line.b.getDataRef()[0];
                    final var y1 = iLine.line.b.getDataRef()[1];
                    applet.line((float)x0, (float)y0, (float)x1, (float)y1);
                }

                applet.stroke(255.0f, 0.0f, 0.0f);
                for( final var iSample : iLineDetector.samples)
                    applet.rect((float) iSample.position.getOne(), (float) iSample.position.getTwo(), 1, 1);

            }

            final var here = 5;
        }
    }

    public void drawPrimitives(final Solver2 solver, final PApplet applet) {
        // * draw primitives for edges
        for (final var iCntr : solver.connectorDetectorsFromProcessHForEdge)
            for (final var iLinePrimitive : iCntr.workspace) {
                applet.stroke(0.0f, 255.0f, 0.0f);

                final var x0 = iLinePrimitive.line.a.getDataRef()[0];
                final var y0 = iLinePrimitive.line.a.getDataRef()[1];
                final var x1 = iLinePrimitive.line.b.getDataRef()[0];
                final var y1 = iLinePrimitive.line.b.getDataRef()[1];
                applet.line((float) x0, (float) y0, (float) x1, (float) y1);
            }

        // * draw primitives for endoskeleton
        for(final var iLinePrimitive : solver.cntrFinalProcessing.workspace) {
            applet.stroke(255.0f, 255.0f, 255.0f);

            final var x0 = iLinePrimitive.line.a.getDataRef()[0];
            final var y0 = iLinePrimitive.line.a.getDataRef()[1];
            final var x1 = iLinePrimitive.line.b.getDataRef()[0];
            final var y1 = iLinePrimitive.line.b.getDataRef()[1];
            applet.line((float)x0, (float)y0, (float)x1, (float)y1);


            // draw intersections as small triangles
            applet.stroke(255.0f, 0.0f, 0.0f);

            for (final var iIntersection : iLinePrimitive.line.intersections) {
                final var x = (int)iIntersection.intersectionPosition.getDataRef()[0];
                final var y = (int)iIntersection.intersectionPosition.getDataRef()[1];

                applet.line(x,y-1,x-1,y+1);
                applet.line(x,y-1,x+1,y+1);
                applet.line(x-1,y+1,x+1,y+1);
            }
        }

    }
}
