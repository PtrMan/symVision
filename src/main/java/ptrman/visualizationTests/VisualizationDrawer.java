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

import org.apache.commons.math3.linear.ArrayRealVector;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import processing.core.PApplet;
import ptrman.Datastructures.Bb;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.bpsolver.Solver2;
import ptrman.levels.retina.*;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.math.Tv;
import ptrman.misc.Classifier;
import ptrman.misc.TvClassifier;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * encapsulates functionality to draw visualizations for showcase and debugging
 */
public class VisualizationDrawer {
    public boolean drawVisualizationOfAltitude = true;
    public boolean drawVisualizationOfEndoSceletons = false; // do we visualize all samples of endo/exo -sceleton
    public boolean drawVisualizationOfLineDetectors = false;
    public boolean drawVisualizationOfLineDetectorsEnableAct = false; // do we draw activation of line detectors?
    public boolean drawVisualizationOfEdgeLineDetectors = false;
    public boolean drawVisualizationOfTex = true; // draw visualization of texture

    public boolean drawVisEdgeLine0 = false; // draw visualization of edge line detectors?
    public boolean drawVisProcessHEdge = false; // draw visualization of edges from process H?

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

    // used to transfer narsese relationships
    public static List<String> relN = new ArrayList<>();

    public void drawPrimitives(Solver2 solver, PApplet applet, TvClassifier classifier) {
        // * draw primitives for edges

        if (drawVisProcessHEdge) {
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
        }



        { // iterate over line detectors of processD for edges

            ArrayList<Bb> allBbs  = new ArrayList<>(); // BB's of all edge filters

            for (ProcessD iProcessDEdge : solver.processDEdge) {
                for(LineDetectorWithMultiplePoints iLineDetector : iProcessDEdge.annealedCandidates) {
                    // iLineDetector.cachedSamplePositions

                    float act = drawVisualizationOfLineDetectorsEnableAct ? (float)iLineDetector.calcActivation() : 1.0f;
                    applet.stroke(act*255.0f, act*255.0f, act*255.0f);

                    if(drawVisEdgeLine0) {
                        for (RetinaPrimitive iLine : solver.processD.splitDetectorIntoLines(iLineDetector)) {
                            double x0 = iLine.line.a.getDataRef()[0];
                            double y0 = iLine.line.a.getDataRef()[1];
                            double x1 = iLine.line.b.getDataRef()[0];
                            double y1 = iLine.line.b.getDataRef()[1];
                            applet.line((float)x0, (float)y0, (float)x1, (float)y1);
                        }
                    }


                    applet.stroke(255.0f, 0.0f, 0.0f);
                    for( ProcessA.Sample iSample : iLineDetector.samples) {

                        applet.rect((float)iSample.position.getOne(), (float)iSample.position.getTwo(), 1, 1);
                    }
                }



                // bounding box logic
                // we try to group points to bounding boxes for each edge detector direction
                ArrayList<Bb> bbs  = new ArrayList<>();

                {
                    for(int idx=0;idx<solver.connectorSamplesFromProcessAForEdge.length;idx++) {
                        for(ProcessA.Sample iSample : solver.connectorSamplesFromProcessAForEdge[idx].out) {
                            boolean found = false; // found bb to add to?

                            for(Bb iBb : bbs) {
                                if (Bb.inRange(iBb, iSample.position.getOne(), iSample.position.getTwo(), 4.0)) { // in at boundary f bb or inside?
                                    iBb.add(iSample.position.getOne(), iSample.position.getTwo());
                                    found = true;
                                }
                            }

                            if (!found) {
                                Bb bb = new Bb();
                                bb.add(iSample.position.getOne(), iSample.position.getTwo());
                                bbs.add(bb);
                            }
                        }
                    }
                }

                // visualize BB's
                if(false) {
                    applet.stroke(255.0f, 255.0f, 0.0f);
                    applet.fill(0, 1.0f);
                    for(Bb iBB : bbs) {
                        applet.rect((int)iBB.minx, (int) iBB.miny, (int)(iBB.maxx-iBB.minx), (int)(iBB.maxy-iBB.miny));
                    }
                }

                // transfer bbs to all
                allBbs.addAll(bbs);
            }



            // merge BB's
            for(int idx0=0; idx0 < allBbs.size();) {
                for(int idx1=idx0+1; idx1 < allBbs.size(); idx1++) {
                    if (Bb.checkOverlap(allBbs.get(idx0), allBbs.get(idx1))) {
                        allBbs.set(idx0, Bb.merge(allBbs.get(idx0), allBbs.get(idx1)));
                        allBbs.remove(idx1);

                        // restart
                        idx0 = -1;

                        break;
                    }
                }
                idx0++;
            }

            // visualize merged BB's
            applet.stroke(0.0f, 255.0f, 0.0f);
            applet.fill(0, 1.0f);
            for(Bb iBB : allBbs) {
                applet.rect((int)iBB.minx, (int) iBB.miny, (int)(iBB.maxx-iBB.minx), (int)(iBB.maxy-iBB.miny));
            }

            List<Classification> classifications = new ArrayList<>();

            if (classifier != null) { // is classification enabled?
                ////////////////////////
                // PREPARATION FOR CLASIFIERS

                // prepare maps as targets to draw "quantized" line detectors
                IMap2d<Boolean>[] edgeMaps = new IMap2d[solver.processDEdge.length];
                for(int idx=0;idx<solver.processDEdge.length;idx++) {
                    edgeMaps[idx] = new Map2d<>(solver.mapBoolean.getWidth(), solver.mapBoolean.getLength());
                    for(int ix=0;ix<edgeMaps[idx].getWidth();ix++) {
                        for(int iy=0;iy<edgeMaps[idx].getLength();iy++) {
                            edgeMaps[idx].setAt(ix,iy,false);
                        }
                    }
                }


                // draw "quantized" line detectors as dots to get them back to a image representation
                for(int idx=0;idx<solver.connectorSamplesFromProcessAForEdge.length;idx++) {
                    IMap2d<Boolean> selmap = edgeMaps[idx];

                    for(ProcessA.Sample iSample : solver.connectorSamplesFromProcessAForEdge[idx].out) {
                        int x = iSample.position.getOne();
                        int y = iSample.position.getTwo();
                        selmap.setAt(x, y, true);
                        selmap.setAt(x+1, y, true);
                        selmap.setAt(x, y+1, true);
                        selmap.setAt(x+1, y+1, true);
                    }
                }

                ////////////////////////
                // ACTUAL CLASSIFICATION

                boolean verbose = false;

                if(verbose) System.out.println("FRAME");

                for(Bb iBB : allBbs) {
                    float centerX = (float)(iBB.maxx+iBB.minx)/2.0f;
                    float centerY = (float)(iBB.maxy+iBB.miny)/2.0f;

                    long t0 = System.nanoTime();

                    ClassificationWithSimilarity[] clasfnWSims = new ClassificationWithSimilarity[2];

                    {
                        Tv[] stimulus = cropEdgeMapsAndConcatToVecAt(edgeMaps, (int)centerX, (int)centerY, 32, 32); // extract cropped image
                        classifier.classify(stimulus, false);
                        int bestCenterX = (int)centerX;
                        int bestCenterY = (int)centerY;
                        float bestSimilarity = classifier.bestCategorySimilarity;

                        // fill with center classification
                        clasfnWSims[0] = new ClassificationWithSimilarity(bestCenterX, bestCenterY, classifier.lastClassfnMsg, bestSimilarity);
                        clasfnWSims[1] = new ClassificationWithSimilarity(bestCenterX, bestCenterY, classifier.lastClassfnMsg, bestSimilarity);
                    }

                    int boundaryMinX = (int)iBB.minx;
                    //boundaryMinX += 8;
                    boundaryMinX = Math.min(boundaryMinX, (int)iBB.maxx);

                    int boundaryMaxX = (int)iBB.maxx;
                    //boundaryMaxX += 8;
                    boundaryMaxX = Math.max(boundaryMaxX, (int)iBB.minx);



                    for(int iiy=(int)iBB.miny;iiy<iBB.maxy;iiy+=2) {
                        for(int iix=boundaryMinX;iix<boundaryMaxX;iix+=2) {
                            int thisCenterX = (int)iix;
                            int thisCenterY = (int)iiy;

                            Tv[] stimulus2 = cropEdgeMapsAndConcatToVecAt(edgeMaps, (int)thisCenterX, (int)thisCenterY, 32, 32); // extract cropped image
                            classifier.classify(stimulus2, false);
                            float thisSim = classifier.bestCategorySimilarity;
                            if (thisSim > clasfnWSims[1].sim) {
                                // FIFO
                                clasfnWSims[0] = clasfnWSims[1];
                                clasfnWSims[1] = new ClassificationWithSimilarity(thisCenterX, thisCenterY, classifier.lastClassfnMsg, thisSim);
                            }
                        }
                    }

                    /* other older algorithm
                    int convRange = 3; // range of convolution - in + - dimension
                    for(int dx=-convRange;dx<convRange;dx++) {
                        for(int dy=-convRange;dy<convRange;dy++) {
                            int thisCenterX = (int)centerX+dx;
                            int thisCenterY = (int)centerY+dy;

                            ArrayRealVector stimulus2 = cropEdgeMapsAndConcatToVecAt(edgeMaps, (int)thisCenterX, (int)thisCenterY, 32, 32); // extract cropped image
                            classifier.classify(stimulus2, false);
                            float thisSim = classifier.bestCategorySimilarity;
                            if (thisSim > bestSimilarity) {
                                bestSimilarity = thisSim;
                                bestCenterX = thisCenterX;
                                bestCenterY = thisCenterY;
                            }
                        }
                    }

                     */


                    // * extract cropped image



                    for(ClassificationWithSimilarity iClasfnWSim : clasfnWSims) {
                        // DEBUG CLASSIFICATION RECT
                        applet.stroke(255.0f, 255.0f, 255.0f);
                        applet.fill(0, 1.0f);
                        applet.rect( (int)iClasfnWSim.posX-32/2, (int)iClasfnWSim.posY-32/2, 32, 32);

                        // * classify

                        Tv[] stimulus = cropEdgeMapsAndConcatToVecAt(edgeMaps, (int)iClasfnWSim.posX, (int)iClasfnWSim.posY, 32, 32); // extract cropped image
                        long categoryId = classifier.classify(stimulus, true);
                        float thisClassificationSim = classifier.bestCategorySimilarity;

                        long dt = System.nanoTime() - t0;
                        double timeInMs = dt / 1000000.0;
                        System.out.println("classifier time= "+timeInMs+" ms");

                        classifications.add(new Classification(iClasfnWSim.posX, iClasfnWSim.posY, categoryId)); // store classification for later processing


                        // * draw classification (for debugging)
                        applet.fill(255);
                        applet.text("c="+categoryId, iClasfnWSim.posX-32/2, iClasfnWSim.posY-32/2);

                        //commented because doesn't work
                        //if (iClasfnWSim.msg.equals("NOCLASS")) {
                        //    break; // don't add multiple if it is novel
                        //}
                    }

                }

                if(verbose) System.out.println("FRAME END");
            }


            // TODO< group by class and send relations by element with lowest count of class! >

            Map<Long, ArrayList<Classification>> classificationsByCategory = new HashMap<>();
            for(Classification iClasfcn: classifications) {
                if(classificationsByCategory.containsKey(iClasfcn.category)) {
                    classificationsByCategory.get(iClasfcn.category).add(iClasfcn);
                }
                else {
                    ArrayList<Classification> arr = new ArrayList<>();
                    arr.add(iClasfcn);
                    classificationsByCategory.put(iClasfcn.category, arr);
                }
            }

            // find classification with lowest number of items
            ArrayList<Classification> classificationCandidatesWithLowestMemberCount = null;
            for(Map.Entry<Long, ArrayList<Classification>> i : classificationsByCategory.entrySet()) {
                if (classificationCandidatesWithLowestMemberCount == null) { // is first one?
                    classificationCandidatesWithLowestMemberCount = i.getValue();
                }
                else if(i.getValue().size() < classificationCandidatesWithLowestMemberCount.size()) {
                    classificationCandidatesWithLowestMemberCount = i.getValue();
                }
            }


            // send to NAR

            //< it seems like NAR can get overwhelmed, so we don't send every time
            if ((solver.t % 2) == 0) {
                relN.clear();

                HashMap<String, Boolean> relByNarsese = new HashMap<>(); // we want to reduce the amount of spam to NARS by omitting redudant events (all happens in the same frame anyways)

                // build relations between classifications with low count with classifications of high count
                if (classificationCandidatesWithLowestMemberCount != null) {
                    for(Classification iClassfcnWithLowestCount : classificationCandidatesWithLowestMemberCount) {
                        for(Classification iClasfcnOther: classifications) {
                            if (iClassfcnWithLowestCount != iClasfcnOther) { // must be different objects
                                if (iClassfcnWithLowestCount.category != iClasfcnOther.category) { // we are only interested in different classifications!
                                    String relY;
                                    String relX;
                                    { // compute relationship term
                                        relY = "c";
                                        relX = "c";
                                        double diffX = iClassfcnWithLowestCount.posX-iClasfcnOther.posX;
                                        double diffY = iClassfcnWithLowestCount.posY-iClasfcnOther.posY;

                                        if (diffY < -15.0) {
                                            relY = "b"; // below
                                        }
                                        if (diffY > 15.0) {
                                            relY = "a"; // above
                                        }

                                        if (diffX < -15.0) {
                                            relX = "b"; // below
                                        }
                                        if (diffX > 15.0) {
                                            relX = "a"; // above
                                        }
                                    }

                                    // scalable way// String n = "< ( {"+(relY)+"} * < ( {"+iClassfcnWithLowestCount.category+"} * {"+iClasfcnOther.category+"} ) --> h > ) --> relY >. :|:";

                                    // not scalable way, will xplode for more complicated scenes
                                    //String n = "< {( {"+(relY)+"} * {"+iClassfcnWithLowestCount.category+"D"+iClasfcnOther.category+"} )} --> relY >. :|:";
                                    String n = relY+"Q"+iClassfcnWithLowestCount.category+"D"+iClasfcnOther.category+ "QrelY. :|:";
                                    relByNarsese.put(n, true); // store in set

                                    n = "< {( {"+(relX)+"} * {"+iClassfcnWithLowestCount.category+"D"+iClasfcnOther.category+"} )} --> relX >. :|:";
                                    //relByNarsese.put(n, true); // store in set
                                }
                            }
                        }
                    }
                }

                for(String iN : relByNarsese.keySet()) {
                    relN.add(iN);
                }




                // HACK< sort BB's by x axis >
                Collections.sort(allBbs, (a, b) -> (a.minx == b.minx) ? 0 : ((a.minx > b.minx) ? 1 : -1));

                // HACK< we should send it normally over NarsBinding >

                // build relations to center
                if (allBbs.size() >= 2) {
                    Bb centerBb = allBbs.get(0);
                    for(int idx=1;idx<allBbs.size();idx++) {
                        Bb otherBb = allBbs.get(idx);

                        if (otherBb.minx < 20.0) {
                            continue; // HACK for pong, ignore it because we else get BS relations
                        }

                        String relY = "c";
                        double diffY = centerBb.miny-otherBb.miny;
                        double diffX = centerBb.minx-otherBb.minx;

                        if (diffY < -15.0) {
                            relY = "b"; // below
                        }
                        if (diffY > 15.0) {
                            relY = "a"; // above
                        }

                        break; // we only care about first relation
                    }
                }
            }

        }

        // * draw primitives for endoskeleton
        if(true) {
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

    // helper
    // reads crops from edge maps and concatenate it all to a single vector for classification
    /* commented because not used anymore
    static ArrayRealVector cropEdgeMapsAndConcatToVecAt(IMap2d<Boolean>[] edgeMaps, int centerX, int centerY, int width, int height) {
        ArrayRealVector dest = new ArrayRealVector(edgeMaps.length*width*height);
        int destIdx = 0; // index in dest

        for(IMap2d<Boolean> iEdgeMap : edgeMaps) {
            for(int ix=centerX-width/2;ix<centerX+width/2;ix++) {
                for(int iy=centerY-height/2;iy<centerY+height/2;iy++) {
                    if (iEdgeMap.inBounds(ix, iy)) {
                        boolean v = iEdgeMap.readAt(ix, iy);
                        dest.setEntry(destIdx, v ? 1 : 0);
                    }
                    destIdx++;
                }
            }
        }

        return dest;
    }
     */
    static Tv[] cropEdgeMapsAndConcatToVecAt(IMap2d<Boolean>[] edgeMaps, int centerX, int centerY, int width, int height) {
        Tv[] dest = new Tv[2*edgeMaps.length*width*height];
        for(int i=0;i<dest.length/2;i++) {
            dest[i*2] = new Tv(0.0f, 0.0001f);
            dest[i*2+1] = new Tv(1.0f, 0.0001f);
        }

        int destIdx = 0; // index in dest

        for(IMap2d<Boolean> iEdgeMap : edgeMaps) {
            for(int ix=centerX-width/2;ix<centerX+width/2;ix++) {
                for(int iy=centerY-height/2;iy<centerY+height/2;iy++) {
                    if (iEdgeMap.inBounds(ix, iy)) {
                        boolean v = iEdgeMap.readAt(ix, iy);
                        if(v) {
                            dest[destIdx*2  ] = new Tv(1.0f, 0.02f);
                            dest[destIdx*2+1] = new Tv(0.0f, 0.02f);
                        }
                    }
                    destIdx++;
                }
            }
        }

        return dest;
    }


    // helper
    static class Classification {
        public long category;
        public int posX;
        public int posY;
        public Classification(int posX, int posY, long category) {
            this.posX = posX;
            this.posY = posY;
            this.category = category;
        }
    }

    static class ClassificationWithSimilarity {
        public String msg;
        public int posX;
        public int posY;
        public double sim; // similarity
        public ClassificationWithSimilarity(int posX, int posY, String msg, double sim) {
            this.posX = posX;
            this.posY = posY;
            this.msg = msg;
            this.sim = sim;
        }
    }
}
