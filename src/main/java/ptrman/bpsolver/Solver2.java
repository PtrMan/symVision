/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver;

import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.bindingNars.NarsBinding;
import ptrman.bindingNars.OpenNarsNarseseConsumer;
import ptrman.levels.retina.*;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.levels.visual.*;

import java.awt.image.BufferedImage;

/**
 * new solver which is incomplete but should be in a working state
 */
public class Solver2 {
    public ProcessFi processFi = new ProcessFi();
    public ProcessD processD;
    public ProcessD[] processDEdge;
    public ProcessConnector<ProcessA.Sample> connectorSamplesForEndosceleton;
    public ProcessConnector<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessD;
    public ProcessConnector<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessH;

    public ProcessConnector<ProcessA.Sample>[] connectorSamplesFromProcessAForEdge;
    public ProcessConnector<RetinaPrimitive>[] connectorDetectorsFromProcessDForEdge;
    public ProcessConnector<RetinaPrimitive>[] connectorDetectorsFromProcessHForEdge;

    // connector for final processing
    public ProcessConnector<RetinaPrimitive> cntrFinalProcessing;

    public NarsBinding narsBinding;

    public int annealingStep = 0;

    // image drawer which is used as the source of the images, must be set to a image drawer before the solver is used!
    public IImageDrawer imageDrawer;

    public IMap2d<Boolean> mapBoolean; // boolean "main" map

    private Vector2d<Integer> imageSize;

    public Solver2() {
        processD = new ProcessD();
        processD.maximalDistanceOfPositions = 5000.0;
        processD.onlyEndoskeleton = true;
        processD.processDLineSamplesForProximity = 2;

        connectorSamplesForEndosceleton = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

        cntrFinalProcessing = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

        processFi.outputSampleConnector = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

        // create NARS-binding
        narsBinding = new NarsBinding(new OpenNarsNarseseConsumer());
    }

    /**
     * must be called before the frame method family
     */
    public void preFrame() {
        annealingStep = 0;


        IMap2d<ColorRgb> mapColor = Map2dImageConverter.convertImageToMap(imageDrawer.apply(null));

        imageSize = new Vector2d<>(mapColor.getWidth(), mapColor.getLength());




        // setup the processing chain

        VisualProcessor.ProcessingChain processingChain = new VisualProcessor.ProcessingChain();

        Dag.Element newDagElement = new Dag.Element(
                new VisualProcessor.ProcessingChain.ChainElementColorFloat(
                        new VisualProcessor.ProcessingChain.ConvertColorRgbToGrayscaleFilter(new ColorRgb(1.0f, 1.0f, 1.0f)),
                        "convertRgbToGrayscale",
                        imageSize
                )
        );
        //newDagElement.childIndices.add(1);

        processingChain.filterChainDag.elements.add(newDagElement);

            /* commented because we don't dither
            newDagElement = new Dag.Element(
                    new VisualProcessor.ProcessingChain.ChainElementFloatBoolean(
                            new VisualProcessor.ProcessingChain.DitheringFilter(),
                            "dither",
                            imageSize
                    )
            );

            processingChain.filterChainDag.elements.add(newDagElement);
             */


        processingChain.filterChain(mapColor);

        IMap2d<Float> mapGrayscale = ((VisualProcessor.ProcessingChain.ApplyChainElement) processingChain.filterChainDag.elements.get(processingChain.filterChainDag.elements.size()-1).content).result; // get from last element in the chain

        int numberOfEdgeDetectorDirections = 8;

        // convolution
        Map2dApplyConvolution[] edgeDetectors = new Map2dApplyConvolution[numberOfEdgeDetectorDirections];
        for(int i=0; i<numberOfEdgeDetectorDirections;i++) {
            edgeDetectors[i] = new Map2dApplyConvolution(Convolution2dHelper.calcGaborKernel(8, (float)i/(float)numberOfEdgeDetectorDirections * 2.0f * (float)Math.PI, 10.0f/64.0f, (float)Math.PI*0.5f, 0.4f));
        }

        IMap2d<Float>[] edges = new IMap2d[numberOfEdgeDetectorDirections];
        for(int i=0; i<numberOfEdgeDetectorDirections;i++) { // detect edges with filters
            edges[i] = edgeDetectors[i].process(mapGrayscale);
        }

        ProcessA[] processAEdge = new ProcessA[numberOfEdgeDetectorDirections];
        processDEdge = new ProcessD[numberOfEdgeDetectorDirections];
        for(int i=0; i<numberOfEdgeDetectorDirections;i++) { // create processors for edges
            processAEdge[i] = new ProcessA();
            processDEdge[i] = new ProcessD();
            processDEdge[i].maximalDistanceOfPositions = 5000.0;
            processDEdge[i].overwriteObjectId = 0; // we want to overwrite the id of the detectors, because some parts of the program still assume object id's and we can't provide it in general case
        }

        connectorDetectorsFromProcessDForEdge = new ProcessConnector[numberOfEdgeDetectorDirections];
        connectorSamplesFromProcessAForEdge = new ProcessConnector[numberOfEdgeDetectorDirections];
        connectorDetectorsFromProcessHForEdge = new ProcessConnector[numberOfEdgeDetectorDirections];
        for(int i=0; i<numberOfEdgeDetectorDirections;i++) { // create connectors for edges
            connectorDetectorsFromProcessHForEdge[i] = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            connectorDetectorsFromProcessDForEdge[i] = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            connectorSamplesFromProcessAForEdge[i] = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
        }

        for(int i=0; i<numberOfEdgeDetectorDirections;i++) {
            // copy image because processA changes the image

            IMap2d<Boolean> mapBoolean = Map2dBinary.threshold(edges[i], 0.01f); // convert from edges[0]

            ProcessConnector<ProcessA.Sample> connectorSamplesFromProcessA = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
            processAEdge[i].set(mapBoolean.copy(), null, connectorSamplesFromProcessAForEdge[i]);

            processAEdge[i].setup(imageSize);

            processDEdge[i].setImageSize(imageSize);
            processDEdge[i].set(connectorSamplesFromProcessAForEdge[i], connectorDetectorsFromProcessDForEdge[i]);

            processAEdge[i].preProcessData();
            processAEdge[i].processData(0.12f);
            processAEdge[i].postProcessData();

            processDEdge[i].preProcessData();
            processDEdge[i].processData(1.0f);
            processDEdge[i].postProcessData();
        }

        mapBoolean = Map2dBinary.threshold(mapGrayscale, 0.1f); // convert from edges[0]

        processFi.workingImage = mapGrayscale;
        processFi.preProcess();
        processFi.process(); // sample image with process-Fi

        ProcessZFacade processZFacade = new ProcessZFacade();

        final int processzNumberOfPixelsToMagnifyThreshold = 8;

        final int processZGridsize = 8;

        connectorSamplesForEndosceleton.out.clear();
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
        processA.setup(imageSize);

        ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessB = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
        processB.set(mapBoolean.copy(), connectorSamplesFromProcessA, conntrSamplesFromProcessB);
        processB.setup(imageSize);


        ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessC0 = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
        ProcessConnector<ProcessA.Sample> conntrSamplesFromProcessC1 = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);
        processC.set(conntrSamplesFromProcessB, conntrSamplesFromProcessC0, conntrSamplesFromProcessC1);
        processC.setup(imageSize);

        connectorDetectorsEndosceletonFromProcessD = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);

        connectorDetectorsEndosceletonFromProcessH = ProcessConnector.createWithDefaultQueues(ProcessConnector.EnumMode.WORKSPACE);


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
    }

    /**
     * does one processing step for the processing of the frame
     */
    public void frameStep() {
        // do annealing step of process D

        processD.step();

        for (ProcessD d : processDEdge) {
            d.step();
        }

        annealingStep++;
    }

    /**
     * must be called to "finilize" the processing of a frame
     */
    public void postFrame() {
        // * emit narsese to narsese consumer

        processD.commitLineDetectors(); // split line detectors into "real" primitives

        for (ProcessD iD : processDEdge) {
            iD.commitLineDetectors();
        }

        // * process-H for edges
        for (int i = 0, processDEdgeLength = processDEdge.length; i < processDEdgeLength; i++) {
            //ProcessD iD = processDEdge[i];
            ProcessH processH = new ProcessH();
            processH.setImageSize(imageSize);
            processH.set(connectorDetectorsFromProcessDForEdge[i], connectorDetectorsFromProcessHForEdge[i]);
            processH.setup();

            processH.preProcessData();
            processH.processData();
            processH.postProcessData();
        }

        // * process-H and process-E

        ProcessH processH = new ProcessH();
        processH.setImageSize(imageSize);
        processH.set(connectorDetectorsEndosceletonFromProcessD, connectorDetectorsEndosceletonFromProcessH);
        processH.setup();

        processH.preProcessData();
        processH.processData();
        processH.postProcessData();

        cntrFinalProcessing = connectorDetectorsEndosceletonFromProcessH; // connect the connector for final processing to output from process-H

        // intersect line primitives
        ProcessE.process(cntrFinalProcessing.out, mapBoolean);

        narsBinding.emitRetinaPrimitives(cntrFinalProcessing.out); // emit all collected primitives from process D
    }
}
