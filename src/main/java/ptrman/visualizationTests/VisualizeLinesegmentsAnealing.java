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

import java.awt.*;
import java.awt.image.BufferedImage;

// visualize linesegments of endosceleton
public class VisualizeLinesegmentsAnealing extends PApplet {

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

            //g2.drawRect(2, 2, 100, 100);
            g2.fillRect(10, 10, 70, 20);

            return off_Image;
        }

        public void settings() {
            size(500, 500);
        }
    }

    public VisualizeLinesegmentsAnealing() {
        processD = new ProcessDAnnealing();
        processD.maximalDistanceOfPositions = 500.0;

        connectorSamplesForEndosceleton = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
    }

    ProcessDAnnealing processD;
    ProcessConnector<ProcessA.Sample> connectorSamplesForEndosceleton;
    ProcessConnector<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessD;

    int frameCounter = 0;

    public void draw(){
        background(64);

        if (frameCounter == 0) {
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

            processA.setImageSize(imageSize);
            processA.setup();

            // copy image because processA changes the image
            ProcessConnector<ProcessA.Sample> connectorSamplesFromProcessA = connectorSamplesForEndosceleton;
            processA.set(mapBoolean.copy(), processZFacade.getNotMagnifiedOutputObjectIds(), connectorSamplesFromProcessA);

            processD.setImageSize(imageSize);
            processD.set(connectorSamplesForEndosceleton, connectorDetectorsEndosceletonFromProcessD);

            processA.preProcessData();
            processA.processData(1.0f);

            processD.preProcessData();
            processD.processData(1.0f);
            processD.postProcessData();
        }
        else if( (frameCounter % 60) == 0 ) {
            // do anealing step of process D

            processD.sampleNew();
            processD.sortByActivationAndThrowAway();
        }

        frameCounter++;

        {
            for(LineDetectorWithMultiplePoints iLineDetector : processD.anealedCandidates) {
                // iLineDetector.cachedSamplePositions

                color(1.0f, 1.0f, 1.0f);
                for (RetinaPrimitive iLine : ProcessDAnnealing.splitDetectorIntoLines(iLineDetector)) {
                    double x0 = iLine.line.a.getDataRef()[0];
                    double y0 = iLine.line.a.getDataRef()[1];
                    double x1 = iLine.line.b.getDataRef()[0];
                    double y1 = iLine.line.b.getDataRef()[1];
                    line((float)x0, (float)y0, (float)x1, (float)y1);
                }

                color(1.0f, 0.0f, 0.0f);
                rect(0, 0, 1, 1);
            }

            int here = 5;
        }


        ellipse(mouseX, mouseY, 20, 20);
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "ptrman.visualizationTests.VisualizeLinesegmentsAnealing" };
        PApplet.main(appletArgs);
    }
}
