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

import processing.core.PApplet;
import processing.core.PImage;
import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Showcases.TestClustering;
import ptrman.bpsolver.Solver;
import ptrman.levels.retina.*;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;
import ptrman.misc.ImageConverter;

import java.awt.*;
import java.awt.image.BufferedImage;

// visualize line-segments of endosceleton
public class VisualizeLinesegmentsAnnealing extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;


    public class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(Color.BLACK);

            g2.drawRect(0, 0, off_Image.getWidth(), off_Image.getHeight());

            g2.setColor(Color.WHITE);


            // draw big boxes
            if(false) {
                g2.fillRect(10, 10, 70, 20);

                g2.fillRect(10, 50, 70, 20);
            }

            if(true) {// draw "A"
                int endpointADeltaX = (int)(Math.cos(animationFrameNumber * 0.1) * 10);
                int endpointADeltaY = (int)(Math.sin(animationFrameNumber * 0.1) * 10);


                g2.setStroke(new BasicStroke(6));
                g2.drawLine(10+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                g2.drawLine(90+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                g2.drawLine(30, 40, 70, 40);
            }


            return off_Image;
        }
    }

    public VisualizeLinesegmentsAnnealing() {
        processD = new ProcessD();
        processD.maximalDistanceOfPositions = 500.0;
        processD.onlyEndoskeleton = true;

        connectorSamplesForEndosceleton = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
    }

    ProcessD processD;
    ProcessConnector<ProcessA.Sample> connectorSamplesForEndosceleton;
    ProcessConnector<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessD;

    int frameCounter = 0;

    static int animationFrameNumber = 0;

    public void draw(){
        background(64);

        animationFrameNumber = (frameCounter / (5*10));

        if ((frameCounter % (5*10)) == 0 ) {
            InputDrawer imageDrawer = new InputDrawer();

            BufferedImage image;
            IMap2d<Boolean> mapBoolean;
            IMap2d<ColorRgb> mapColor;



            // TODO< pull image from source >
            // for now imageDrawer does this
            image = imageDrawer.drawToJavaImage(null);

            Vector2d<Integer> imageSize = new Vector2d<>(image.getWidth(), image.getHeight());



            mapColor = TestClustering.translateFromImageToMap(image);





            // setup the processing chain

            VisualProcessor.ProcessingChain processingChain = new VisualProcessor.ProcessingChain();

            Dag.Element newDagElement;

            newDagElement = new Dag.Element(
                    new VisualProcessor.ProcessingChain.ChainElementColorFloat(
                            new VisualProcessor.ProcessingChain.ConvertColorRgbToGrayscaleFilter(new ColorRgb(1.0f, 1.0f, 1.0f)),
                            "convertRgbToGrayscale",
                            imageSize
                    )
            );
            newDagElement.childIndices.add(1);

            processingChain.filterChainDag.elements.add(newDagElement);


            newDagElement = new Dag.Element(
                    new VisualProcessor.ProcessingChain.ChainElementFloatBoolean(
                            new VisualProcessor.ProcessingChain.DitheringFilter(),
                            "dither",
                            imageSize
                    )
            );

            processingChain.filterChainDag.elements.add(newDagElement);



            processingChain.filterChain(mapColor);

            mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement)processingChain.filterChainDag.elements.get(1).content).result;



            ProcessZFacade processZFacade = new ProcessZFacade();

            final int processzNumberOfPixelsToMagnifyThreshold = 8;

            final int processZGridsize = 8;

            connectorSamplesForEndosceleton.workspace.clear();
            processD.annealedCandidates.clear(); // TODO< cleanup in process with method >

            processZFacade.setImageSize(imageSize);
            processZFacade.preSetupSet(processZGridsize, processzNumberOfPixelsToMagnifyThreshold);
            processZFacade.setup();

            processZFacade.set(mapBoolean); // image doesn't need to be copied

            processZFacade.preProcessData();
            processZFacade.processData();
            processZFacade.postProcessData();

            IMap2d<Integer> notMagnifiedOutputObjectIdsMapDebug;

            notMagnifiedOutputObjectIdsMapDebug = processZFacade.getNotMagnifiedOutputObjectIds();

            ProcessA processA = new ProcessA();
            ProcessB processB = new ProcessB();
            ProcessC processC = new ProcessC();

            // copy image because processA changes the image
            ProcessConnector<ProcessA.Sample> connectorSamplesFromProcessA = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            processA.set(mapBoolean.copy(), processZFacade.getNotMagnifiedOutputObjectIds(), connectorSamplesFromProcessA);
            processA.setImageSize(imageSize);
            processA.setup();

            ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessB = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            processB.set(mapBoolean.copy(), connectorSamplesFromProcessA, conntrSamplesFromProcessB);
            processB.setImageSize(imageSize);
            processB.setup();


            ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessC0 = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessC1 = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            processC.set(conntrSamplesFromProcessB, conntrSamplesFromProcessC0, conntrSamplesFromProcessC1);
            processC.setImageSize(imageSize);
            processC.setup();

            processD.setImageSize(imageSize);
            connectorSamplesForEndosceleton = conntrSamplesFromProcessC0;
            processD.set(connectorSamplesForEndosceleton, connectorDetectorsEndosceletonFromProcessD);

            processA.preProcessData();
            processA.processData(0.01f);
            processA.postProcessData();

            processB.preProcessData();
            processB.processData();
            processB.postProcessData();

            processC.preProcessData();
            processC.processData();
            processC.postProcessData();

            processD.preProcessData();
            processD.processData(1.0f);
            processD.postProcessData();
        }
        else if( (frameCounter % 5) == 0 ) {
            // do annealing step of process D

            processD.sampleNew();
            processD.tryWiden();
            processD.sortByActivationAndThrowAway();
        }

        frameCounter++;

        { // draw processed image in the background
            IImageDrawer imgDrawer = new InputDrawer();
            BufferedImage img = imgDrawer.drawToJavaImage(null);
            PImage pimg = ImageConverter.convBufferedImageToPImage(img);
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }

        boolean drawVisualizationOfAltitude = true;
        boolean drawVisualizationOfEndoSceletons = true; // do we visualize all samples of endo/exo -sceleton
        boolean drawVisualizationOfLineDetectors = true;


        if(drawVisualizationOfAltitude) {
            for (ProcessA.Sample iSample : connectorSamplesForEndosceleton.workspace) {
                float color = Math.min((float)iSample.altitude / 20.0f, 1.0f);

                stroke(color*255.0f);
                rect((float)iSample.position.getDataRef()[0], (float)iSample.position.getDataRef()[1], 1, 1);
            }
        }

        if(drawVisualizationOfEndoSceletons) {
            for (ProcessA.Sample iSample : connectorSamplesForEndosceleton.workspace) {
                if (iSample.type != ProcessA.Sample.EnumType.ENDOSCELETON) {
                    continue;
                }
                stroke(200.0f, 255.0f, 200.0f);

                rect((float)iSample.position.getDataRef()[0], (float)iSample.position.getDataRef()[1], 1, 1);
            }
        }

        if(drawVisualizationOfLineDetectors) { // draw visualization of line detectors
            for(LineDetectorWithMultiplePoints iLineDetector : processD.annealedCandidates) {
                // iLineDetector.cachedSamplePositions

                stroke(255.0f, 255.0f, 255.0f);
                for (RetinaPrimitive iLine : ProcessD.splitDetectorIntoLines(iLineDetector)) {
                    double x0 = iLine.line.a.getDataRef()[0];
                    double y0 = iLine.line.a.getDataRef()[1];
                    double x1 = iLine.line.b.getDataRef()[0];
                    double y1 = iLine.line.b.getDataRef()[1];
                    line((float)x0, (float)y0, (float)x1, (float)y1);
                }

                stroke(255.0f, 0.0f, 0.0f);
                for( ProcessA.Sample iSample : iLineDetector.samples) {

                    rect((float)iSample.position.getDataRef()[0], (float)iSample.position.getDataRef()[1], 1, 1);
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
