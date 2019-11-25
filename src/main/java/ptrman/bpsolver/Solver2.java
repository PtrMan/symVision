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
import ptrman.levels.visual.*;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Random;

/**
 * new solver which is incomplete but should be in a working state
 */
@Deprecated public class Solver2 {
    public ProcessFi processFi = new ProcessFi();
    public ProcessD processD;
    public ProcessD[] processDEdge;
    public Queue<ProcessA.Sample> connectorSamplesForEndosceleton;
    public Queue<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessD;
    public Queue<RetinaPrimitive> connectorDetectorsEndosceletonFromProcessH;

    public Queue<ProcessA.Sample>[] connectorSamplesFromProcessAForEdge;
    public Queue<RetinaPrimitive>[] connectorDetectorsFromProcessDForEdge;
    public Queue<RetinaPrimitive>[] connectorDetectorsFromProcessHForEdge;

    // connector for final processing
    public Queue<RetinaPrimitive> cntrFinalProcessing;

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

        connectorSamplesForEndosceleton = (Queue<ProcessA.Sample>) new ArrayDeque();

        cntrFinalProcessing = (Queue<RetinaPrimitive>) new ArrayDeque();

        processFi.outputSampleConnector = (Queue<TexPoint>) new ArrayDeque();

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

        connectorDetectorsFromProcessDForEdge = new Queue[numberOfEdgeDetectorDirections];
        connectorSamplesFromProcessAForEdge = new Queue[numberOfEdgeDetectorDirections];
        connectorDetectorsFromProcessHForEdge = new Queue[numberOfEdgeDetectorDirections];
        for(int i=0; i<numberOfEdgeDetectorDirections;i++) { // create connectors for edges
            connectorDetectorsFromProcessHForEdge[i] = (Queue<RetinaPrimitive>) new ArrayDeque();
            connectorDetectorsFromProcessDForEdge[i] = (Queue<RetinaPrimitive>) new ArrayDeque();
            connectorSamplesFromProcessAForEdge[i] = (Queue<ProcessA.Sample>) new ArrayDeque();
        }

        for(int i=0; i<numberOfEdgeDetectorDirections;i++) {
            // copy image because processA changes the image

            IMap2d<Boolean> mapBoolean = Map2dBinary.threshold(edges[i], 0.01f); // convert from edges[0]

            Queue<ProcessA.Sample> connectorSamplesFromProcessA = (Queue<ProcessA.Sample>) new ArrayDeque();
            processAEdge[i].set(mapBoolean.copy(), null, connectorSamplesFromProcessAForEdge[i]);

            processAEdge[i].setup(imageSize);

            processDEdge[i].setImageSize(imageSize);
            processDEdge[i].set(connectorSamplesFromProcessAForEdge[i], connectorDetectorsFromProcessDForEdge[i]);

            processAEdge[i].processData(new Random(), 0.12f);

            processDEdge[i].processData(1.0f);

        }

        mapBoolean = Map2dBinary.threshold(mapGrayscale, 0.1f); // convert from edges[0]

        processFi.workingImage = mapGrayscale;
        processFi.process(); // sample image with process-Fi

        ProcessZFacade processZFacade = new ProcessZFacade();

        final int processzNumberOfPixelsToMagnifyThreshold = 8;

        final int processZGridsize = 8;

        connectorSamplesForEndosceleton.clear();
        processD.annealedCandidates.clear(); // TODO< cleanup in process with method >

        processZFacade.setImageSize(imageSize);
        processZFacade.preSetupSet(processZGridsize, processzNumberOfPixelsToMagnifyThreshold);
        processZFacade.setup();

        processZFacade.set(mapBoolean); // image doesn't need to be copied

        processZFacade.processData();

        IMap2d<Integer> notMagnifiedOutputObjectIdsMapDebug = processZFacade.getNotMagnifiedOutputObjectIds();

        ProcessA processA = new ProcessA();
        ProcessB processB = new ProcessB();
        ProcessC processC = new ProcessC();

        // copy image because processA changes the image
        Queue<ProcessA.Sample> connectorSamplesFromProcessA = (Queue<ProcessA.Sample>) new ArrayDeque();
        processA.set(mapBoolean.copy(), processZFacade.getNotMagnifiedOutputObjectIds(), connectorSamplesFromProcessA);
        processA.setup(imageSize);

        Queue<ProcessA.Sample> conntrSamplesFromProcessB = (Queue<ProcessA.Sample>) new ArrayDeque();
        processB.set(mapBoolean.copy(), connectorSamplesFromProcessA, conntrSamplesFromProcessB);
        processB.setup(imageSize);


        Queue<ProcessA.Sample> conntrSamplesFromProcessC0 = (Queue<ProcessA.Sample>) new ArrayDeque();
        Queue<ProcessA.Sample> conntrSamplesFromProcessC1 = (Queue<ProcessA.Sample>) new ArrayDeque();
        processC.set(conntrSamplesFromProcessB, conntrSamplesFromProcessC0, conntrSamplesFromProcessC1);
        processC.setup(imageSize);

        connectorDetectorsEndosceletonFromProcessD = (Queue<RetinaPrimitive>) new ArrayDeque();

        connectorDetectorsEndosceletonFromProcessH = (Queue<RetinaPrimitive>) new ArrayDeque();


        processD.setImageSize(imageSize);
        connectorSamplesForEndosceleton = conntrSamplesFromProcessC0;
        processD.set(connectorSamplesForEndosceleton, connectorDetectorsEndosceletonFromProcessD);

        processA.processData(new Random(), 0.03f);

        processB.processData();

        processC.processData();

        processD.processData(1.0f);

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

            processH.processData();
        }

        // * process-H and process-E

        ProcessH processH = new ProcessH();
        processH.setImageSize(imageSize);
        processH.set(connectorDetectorsEndosceletonFromProcessD, connectorDetectorsEndosceletonFromProcessH);
        processH.setup();

        processH.processData();

        cntrFinalProcessing = connectorDetectorsEndosceletonFromProcessH; // connect the connector for final processing to output from process-H

        // intersect line primitives
        ProcessE.process(cntrFinalProcessing, mapBoolean);

        narsBinding.emitRetinaPrimitives(cntrFinalProcessing); // emit all collected primitives from process D
    }
}
