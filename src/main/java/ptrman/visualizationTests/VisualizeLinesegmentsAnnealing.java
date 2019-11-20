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
import processing.core.PImage;
import ptrman.Gui.IImageDrawer;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.Solver2;
import ptrman.levels.retina.*;
import ptrman.misc.ImageConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/** visualize line-segments of endosceleton
 *
 */
public class VisualizeLinesegmentsAnnealing extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;

    public Solver2 solver2 = new Solver2();


    public static class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage apply(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(Color.BLACK);

            g2.drawRect(0, 0, off_Image.getWidth(), off_Image.getHeight());

            g2.setColor(Color.WHITE);

            if (chosenImage == 0) {
                // draw big boxes
                if(false) {
                    g2.fillRect(10, 10, 70, 20);

                    g2.fillRect(10, 50, 70, 20);
                }

                if(true) {// draw "A"
                    int endpointADeltaX = (int)(Math.cos(animationFrameNumber * 0.1) * 10);
                    int endpointADeltaY = (int)(Math.sin(animationFrameNumber * 0.1) * 10);


                    g2.setStroke(new BasicStroke(12));
                    g2.drawLine(10+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                    g2.drawLine(90+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                    g2.drawLine(30, 40, 70, 40);
                }
            }
            else if(chosenImage == 1){
                // draw star
                g2.setFont(new Font("TimesRoman", Font.PLAIN, 230));
                g2.drawString("*", 20, 170);
            }
            else if(chosenImage == 2){
                // text
                g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
                g2.drawString("/en-", 2, 100);
            }


            return off_Image;
        }
    }

    static int chosenImage = 0; // chosen image




    int frameCounter = 0;

    static int animationFrameNumber = 0;


    public void draw(){
        background(64);


        int framesPerStep = 10; // how many frames do we visualize one step?

        int steps = 50; // how many steps are done?

        animationFrameNumber = (frameCounter / (framesPerStep*steps));


        if ((frameCounter % (framesPerStep*steps)) == 0 ) {
            chosenImage = new Random().nextInt(3);


            solver2.imageDrawer = new VisualizeLinesegmentsAnnealing.InputDrawer();

            solver2.preFrame(); // do all processing and setup before the actual processing of the frame

        }
        else if( (frameCounter % framesPerStep) == 0 ) {
            solver2.frameStep(); // step of a frame

            if (solver2.annealingStep == steps-1-1) {// is last step?
                solver2.postFrame(); // finish off frame processing
            }
        }

        frameCounter++;

        { // draw processed image in the background
            PImage pimg = ImageConverter.convBufferedImageToPImage((new InputDrawer()).apply(null));
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }

        boolean drawVisualizationOfAltitude = false;
        boolean drawVisualizationOfEndoSceletons = false; // do we visualize all samples of endo/exo -sceleton
        boolean drawVisualizationOfLineDetectors = true;
        boolean drawVisualizationOfLineDetectorsEnableAct = true; // do we draw activation of line detectors?
        boolean drawVisualizationOfEdgeLineDetectors = false;


        if(drawVisualizationOfAltitude) {
            for (ProcessA.Sample iSample : solver2.connectorSamplesForEndosceleton.workspace) {
                float color = Math.min((float)iSample.altitude / 20.0f, 1.0f);

                stroke(color*255.0f);
                rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
            }
        }

        if(drawVisualizationOfEndoSceletons) {

            stroke(200.0f, 255.0f, 200.0f);

            for (ProcessA.Sample s : solver2.connectorSamplesForEndosceleton.workspace) {
                if (s.type == ProcessA.Sample.EnumType.ENDOSCELETON) {
                    IntIntPair p = s.position;
                    rect(p.getOne(), p.getTwo(), 1, 1);
                }
            }
        }


        if(drawVisualizationOfEdgeLineDetectors) { // draw visualization of line detectors
            for (ProcessD iProcessDEdge : solver2.processDEdge) {
                for(LineDetectorWithMultiplePoints iLineDetector : iProcessDEdge.annealedCandidates) {
                    // iLineDetector.cachedSamplePositions


                    stroke(128.0f, 128, 255);
                    for (RetinaPrimitive iLine : ProcessD.splitDetectorIntoLines(iLineDetector)) {
                        double x0 = iLine.line.a.getDataRef()[0];
                        double y0 = iLine.line.a.getDataRef()[1];
                        double x1 = iLine.line.b.getDataRef()[0];
                        double y1 = iLine.line.b.getDataRef()[1];
                        line((float)x0, (float)y0, (float)x1, (float)y1);
                    }

                    if (false) {
                        stroke(255.0f, 0.0f, 0.0f);
                        for( ProcessA.Sample iSample : iLineDetector.samples) {
                            rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                        }
                    }
                }
            }


            int here = 5;
        }


        if(drawVisualizationOfLineDetectors) { // draw visualization of line detectors
            for(LineDetectorWithMultiplePoints iLineDetector : solver2.processD.annealedCandidates) {
                // iLineDetector.cachedSamplePositions

                float act = drawVisualizationOfLineDetectorsEnableAct ? (float)iLineDetector.calcActivation() : 1.0f;
                stroke(act*255.0f, act*255.0f, act*255.0f);


                for (RetinaPrimitive iLine : ProcessD.splitDetectorIntoLines(iLineDetector)) {
                    double x0 = iLine.line.a.getDataRef()[0];
                    double y0 = iLine.line.a.getDataRef()[1];
                    double x1 = iLine.line.b.getDataRef()[0];
                    double y1 = iLine.line.b.getDataRef()[1];
                    line((float)x0, (float)y0, (float)x1, (float)y1);
                }

                stroke(255.0f, 0.0f, 0.0f);
                for( ProcessA.Sample iSample : iLineDetector.samples) {

                    rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                }

            }

            int here = 5;
        }


        // mouse cursor
        ellipse(mouseX, mouseY, 4, 4);
    }

    @Override
    public void settings() {
        size(200, 200);
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "ptrman.visualizationTests.VisualizeLinesegmentsAnnealing" };
        PApplet.main(appletArgs);
    }
}
