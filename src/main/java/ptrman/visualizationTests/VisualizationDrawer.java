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
    public boolean drawVisualizationOfAltitude = true;
    public boolean drawVisualizationOfEndoSceletons = false; // do we visualize all samples of endo/exo -sceleton
    public boolean drawVisualizationOfLineDetectors = false;
    public boolean drawVisualizationOfLineDetectorsEnableAct = true; // do we draw activation of line detectors?
    public boolean drawVisualizationOfEdgeLineDetectors = false;
    public boolean drawVisualizationOfTex = true; // draw visualization of texture

    public void drawDetectors(Solver2 solver, PApplet applet) {
        if(drawVisualizationOfAltitude) {
            for (ProcessA.Sample iSample : solver.connectorSamplesForEndosceleton.out) {
                float color = Math.min((float)iSample.altitude / 20.0f, 1.0f);

                applet.stroke(color*255.0f);
                applet.rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
            }
        }

        if(drawVisualizationOfEndoSceletons) {

            applet.stroke(200.0f, 255.0f, 200.0f);

            for (ProcessA.Sample s : solver.connectorSamplesForEndosceleton.out) {
                if (s.type == ProcessA.Sample.EnumType.ENDOSCELETON) {
                    IntIntPair p = s.position;
                    applet.rect(p.getOne(), p.getTwo(), 1, 1);
                }
            }
        }


        if(drawVisualizationOfEdgeLineDetectors) { // draw visualization of line detectors
            for (ProcessD iProcessDEdge : solver.processDEdge) {
                for(LineDetectorWithMultiplePoints iLineDetector : iProcessDEdge.annealedCandidates) {
                    // iLineDetector.cachedSamplePositions


                    applet.stroke(128.0f, 128, 255);
                    for (RetinaPrimitive iLine : iProcessDEdge.splitDetectorIntoLines(iLineDetector)) {
                        double x0 = iLine.line.a.getDataRef()[0];
                        double y0 = iLine.line.a.getDataRef()[1];
                        double x1 = iLine.line.b.getDataRef()[0];
                        double y1 = iLine.line.b.getDataRef()[1];
                        applet.line((float)x0, (float)y0, (float)x1, (float)y1);
                    }

                    if (true) {
                        applet.stroke(255.0f, 0.0f, 0.0f);
                        for( ProcessA.Sample iSample : iLineDetector.samples) {
                            applet.rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                        }
                    }
                }
            }


            int here = 5;
        }


        if(drawVisualizationOfLineDetectors) { // draw visualization of line detectors
            for(LineDetectorWithMultiplePoints iLineDetector : solver.processD.annealedCandidates) {
                // iLineDetector.cachedSamplePositions

                float act = drawVisualizationOfLineDetectorsEnableAct ? (float)iLineDetector.calcActivation() : 1.0f;
                applet.stroke(act*255.0f, act*255.0f, act*255.0f);


                for (RetinaPrimitive iLine : solver.processD.splitDetectorIntoLines(iLineDetector)) {
                    double x0 = iLine.line.a.getDataRef()[0];
                    double y0 = iLine.line.a.getDataRef()[1];
                    double x1 = iLine.line.b.getDataRef()[0];
                    double y1 = iLine.line.b.getDataRef()[1];
                    applet.line((float)x0, (float)y0, (float)x1, (float)y1);
                }

                applet.stroke(255.0f, 0.0f, 0.0f);
                for( ProcessA.Sample iSample : iLineDetector.samples) {

                    applet.rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                }

            }

            int here = 5;
        }

        if (drawVisualizationOfTex) { // visualize texture points
            for(TexPoint iTex : solver.processFi.outputSampleConnector.out) {
                applet.stroke(255.0f, 0.0f, 255.0f);
                applet.rect(iTex.x, iTex.y, 1, 1);
            }
        }
    }

    public void drawPrimitives(Solver2 solver, PApplet applet) {
        // * draw primitives for edges

        for (ProcessConnector<RetinaPrimitive> iCntr : solver.connectorDetectorsFromProcessHForEdge) {
            for(RetinaPrimitive iLinePrimitive : iCntr.out) {
                applet.stroke(0.0f, 255.0f, 0.0f);

                double[] aa = iLinePrimitive.line.a.getDataRef();
                double x0 = aa[0];
                double y0 = aa[1];
                double[] bb = iLinePrimitive.line.b.getDataRef();
                double x1 = bb[0];
                double y1 = bb[1];
                applet.line((float)x0, (float)y0, (float)x1, (float)y1);
            }
        }


        { // iterate over line detectors of processD for edges
            for (ProcessD iProcessDEdge : solver.processDEdge) {
                for(LineDetectorWithMultiplePoints iLineDetector : iProcessDEdge.annealedCandidates) {
                    // iLineDetector.cachedSamplePositions

                    float act = drawVisualizationOfLineDetectorsEnableAct ? (float)iLineDetector.calcActivation() : 1.0f;
                    applet.stroke(act*255.0f, act*255.0f, act*255.0f);


                    for (RetinaPrimitive iLine : solver.processD.splitDetectorIntoLines(iLineDetector)) {
                        double x0 = iLine.line.a.getDataRef()[0];
                        double y0 = iLine.line.a.getDataRef()[1];
                        double x1 = iLine.line.b.getDataRef()[0];
                        double y1 = iLine.line.b.getDataRef()[1];
                        applet.line((float)x0, (float)y0, (float)x1, (float)y1);
                    }

                    applet.stroke(255.0f, 0.0f, 0.0f);
                    for( ProcessA.Sample iSample : iLineDetector.samples) {

                        applet.rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                    }

                }
            }

        }

        // * draw primitives for endoskeleton
        for(RetinaPrimitive iLinePrimitive : solver.cntrFinalProcessing.out) {
            applet.stroke(255.0f, 255.0f, 255.0f);

            double x0 = iLinePrimitive.line.a.getDataRef()[0];
            double y0 = iLinePrimitive.line.a.getDataRef()[1];
            double x1 = iLinePrimitive.line.b.getDataRef()[0];
            double y1 = iLinePrimitive.line.b.getDataRef()[1];
            applet.line((float)x0, (float)y0, (float)x1, (float)y1);


            // draw intersections as small triangles
            applet.stroke(255.0f, 0.0f, 0.0f);

            for (Intersection iIntersection : iLinePrimitive.line.intersections) {
                int x = (int)iIntersection.intersectionPosition.getDataRef()[0];
                int y = (int)iIntersection.intersectionPosition.getDataRef()[1];

                applet.line(x,y-1,x-1,y+1);
                applet.line(x,y-1,x+1,y+1);
                applet.line(x-1,y+1,x+1,y+1);
            }
        }

    }
}
