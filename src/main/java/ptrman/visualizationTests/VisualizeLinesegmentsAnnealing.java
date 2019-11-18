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
import java.util.Random;

//import ptrman.bindingNars.NarsBinding;
//import ptrman.bindingNars.OpenNarsNarseseConsumer;

// visualize line-segments of endosceleton
public class VisualizeLinesegmentsAnnealing extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;


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

    public VisualizeLinesegmentsAnnealing() {
        processD = new ProcessD();
        processD.maximalDistanceOfPositions = 500.0;
        processD.onlyEndoskeleton = true;

        connectorSamplesForEndosceleton = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

//        { // create NARS-binding
//            narsBinding = new NarsBinding(new OpenNarsNarseseConsumer());
//        }
    }

    final ProcessD processD;
    ProcessConnector<ProcessA.Sample> connectorSamplesForEndosceleton;
    ProcessConnector<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessD;

//    public NarsBinding narsBinding;

    int frameCounter = 0;

    static int animationFrameNumber = 0;

    static int annealingStep = 0;

    public void draw(){
        background(64);

        animationFrameNumber = (frameCounter / (5*30));

        if ((frameCounter % (5*30)) == 0 ) {
            chosenImage = new Random().nextInt(3);

            InputDrawer imageDrawer = new InputDrawer();


            // TODO< pull image from source >
            // for now imageDrawer does this
            BufferedImage image = imageDrawer.apply(null);

            Vector2d<Integer> imageSize = new Vector2d<>(image.getWidth(), image.getHeight());


            IMap2d<ColorRgb> mapColor = TestClustering.translateFromImageToMap(image);





            // setup the processing chain

            VisualProcessor.ProcessingChain processingChain = new VisualProcessor.ProcessingChain();

            Dag.Element newDagElement = new Dag.Element(
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

            IMap2d<Boolean> mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement) processingChain.filterChainDag.elements.get(1).content).result;



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

            IMap2d<Integer> notMagnifiedOutputObjectIdsMapDebug = processZFacade.getNotMagnifiedOutputObjectIds();

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

            connectorDetectorsEndosceletonFromProcessD = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

            processD.setImageSize(imageSize);
            connectorSamplesForEndosceleton = conntrSamplesFromProcessC0;
            processD.set(connectorSamplesForEndosceleton, connectorDetectorsEndosceletonFromProcessD);

            processA.preProcessData();
            processA.processData(0.03f);
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

            annealingStep = 0;
        }
        else if( (frameCounter % 5) == 0 ) {
            // do annealing step of process D

            processD.sampleNew();
            processD.tryWiden();
            processD.sortByActivationAndThrowAway();

            if (annealingStep >= 20) { // remove only in later phases
                processD.removeCandidatesBelowActivation(1.1);
            }

            if (annealingStep == 30-1-1) {// is last step?
                // then emit narsese to narsese consumer

                processD.commitLineDetectors(); // split line detectors into "real" primitives

//                narsBinding.emitRetinaPrimitives(connectorDetectorsEndosceletonFromProcessD.workspace); // emit all collected primitives from process D
            }

            annealingStep++;
        }

        frameCounter++;

        { // draw processed image in the background
            PImage pimg = ImageConverter.convBufferedImageToPImage((new InputDrawer()).apply(null));
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
                rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
            }
        }

        if(drawVisualizationOfEndoSceletons) {

            stroke(200.0f, 255.0f, 200.0f);

            for (ProcessA.Sample s : connectorSamplesForEndosceleton.workspace) {
                if (s.type == ProcessA.Sample.EnumType.ENDOSCELETON) {
                    IntIntPair p = s.position;
                    rect(p.getOne(), p.getTwo(), 1, 1);
                }
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
