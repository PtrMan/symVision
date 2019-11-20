/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Gui;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.*;

import java.awt.*;
import java.util.List;

/**
 *
 */
public enum DebugDrawingHelper {
    ;

    public abstract static class DrawingEntity {
        public enum EnumDrawType {
            SAMPLES,
            RETINA_PRIMITIVE,
            CUSTOM
        }

        public DrawingEntity(final int dataSourceIndex, final EnumDrawType drawType) {
            this.dataSourceIndex = dataSourceIndex;
            this.drawType = drawType;
        }

        public int getDataSourceIndex() {
            return dataSourceIndex;
        }

        public EnumDrawType getDrawType() {
            return drawType;
        }

        public abstract void drawSamples(Graphics2D graphics, final Iterable<ProcessA.Sample> samples);
        public abstract void drawRetinaPrimitives(Graphics2D graphics2D, final RetinaPrimitive... primitives);
        public abstract void drawCustom(Graphics2D graphics2D);

        private final EnumDrawType drawType;
        private final int dataSourceIndex;
    }

    public static final class SampleDrawingEntity extends DrawingEntity {
        public SampleDrawingEntity(final int dataSourceIndex, final boolean showAltitude, final double maxAltitude) {
            super(dataSourceIndex, EnumDrawType.SAMPLES);
            this.showAltitude = showAltitude;
            this.maxAltitude = maxAltitude;
        }

        @Override
        public void drawSamples(Graphics2D graphics, final Iterable<ProcessA.Sample> samples) {
            for( ProcessA.Sample iterationSample : samples ) {
                if( showAltitude ) {
                    graphics.setColor(new Color(0.0f, (float)
                        (iterationSample.isAltitudeValid() ? 0.5 + (1.0 - 0.5) * Math.min(1.0, iterationSample.altitude / maxAltitude) : 0.5),
                        0.0f));
                }
                else {
                    // show if it is a endosceleton point or not
                    graphics.setColor(iterationSample.type == ProcessA.Sample.EnumType.ENDOSCELETON ? Color.GREEN : Color.RED);
                }


                graphics.drawLine(iterationSample.position.getOne(), iterationSample.position.getTwo(), iterationSample.position.getOne(), iterationSample.position.getTwo());
            }
        }

        @Override
        public void drawRetinaPrimitives(Graphics2D graphics2D, RetinaPrimitive[] primitives) {
            throw new RuntimeException("Not Implemented!");
        }

        @Override
        public void drawCustom(Graphics2D graphics2D) {
            throw new RuntimeException("Not Implemented!");
        }

        private final boolean showAltitude;
        private final double maxAltitude;

    }

    public static final class RetinaPrimitiveDrawingEntity extends DrawingEntity {
        public RetinaPrimitiveDrawingEntity(final int dataSourceIndex) {
            super(dataSourceIndex, EnumDrawType.RETINA_PRIMITIVE);
        }

        @Override
        public void drawSamples(Graphics2D graphics, Iterable<ProcessA.Sample> samples) {
            throw new RuntimeException("Not Implemented!");
        }

        @Override
        public void drawRetinaPrimitives(Graphics2D graphics2D, RetinaPrimitive[] primitives) {
            for( RetinaPrimitive p : primitives ) {

                SingleLineDetector iterationDetector = p.line;

                double[] aa = iterationDetector.a.getDataRef(), bb = iterationDetector.b.getDataRef();

                graphics2D.setColor(iterationDetector.resultOfCombination ? Color.RED : Color.BLUE);
                graphics2D.setStroke(new BasicStroke(2));
                graphics2D.drawLine(Math.round((float) aa[0]), Math.round((float) aa[1]), Math.round((float) bb[0]), Math.round((float) bb[1]));
            }

            // for old code
            // TODO< overwork old code so the stroke is set at the beginning >
            graphics2D.setStroke(new BasicStroke(1));
        }

        @Override
        public void drawCustom(Graphics2D graphics2D) {
            throw new RuntimeException("Not Implemented!");
        }
    }

    public static void drawLineParsings(Graphics2D graphics, Iterable<ProcessM.LineParsing> lineParsings) {
        for( ProcessM.LineParsing iterationLineParsing : lineParsings ) {
            drawLineParsing(graphics, iterationLineParsing);
        }
    }

    private static void drawLineParsing(Graphics2D graphics, ProcessM.LineParsing lineParsing) {
        graphics.setColor(Color.LIGHT_GRAY);

        for( SingleLineDetector iterationDetector : lineParsing.lineParsing ) {
            ArrayRealVector aProjectedFloat = iterationDetector.a;
            ArrayRealVector bProjectedFloat = iterationDetector.b;

            graphics.drawLine((int)aProjectedFloat.getDataRef()[0], (int)aProjectedFloat.getDataRef()[1], (int)bProjectedFloat.getDataRef()[0], (int)bProjectedFloat.getDataRef()[1]);
        }
    }

    // draws the barycenters if possible
    public static void drawObjectBaryCenters(Graphics2D graphics, Iterable<Node> objectNodes, NetworkHandles networkHandles) {
        graphics.setColor(Color.GREEN);

        for( Node iterationNode : objectNodes ) {
            for( Link iterationLink : iterationNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

                if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
                    continue;
                }

                PlatonicPrimitiveInstanceNode platonicPrimitiveInstance = (PlatonicPrimitiveInstanceNode) iterationLink.target;

                if( !platonicPrimitiveInstance.primitiveNode.equals(networkHandles.barycenterPlatonicPrimitiveNode) ) {
                    continue;
                }
                // here if barycenter

                double barycenterX = 0.0f;
                double barycenterY = 0.0f;

                for( Link iterationLink2 : platonicPrimitiveInstance.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

                    if( iterationLink2.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() ) {
                        continue;
                    }

                    FeatureNode featureNode = (FeatureNode) iterationLink2.target;

                    if( featureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) ) {
                        barycenterX = featureNode.getValueAsFloat();
                    }
                    else if( featureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) ) {
                        barycenterY = featureNode.getValueAsFloat();
                    }
                }

                int barycenterXAsInt = Math.round((float)barycenterX);
                int barycenterYAsInt = Math.round((float)barycenterY);

                // draw

                graphics.drawLine(barycenterXAsInt-1, barycenterYAsInt, barycenterXAsInt+1, barycenterYAsInt);
                graphics.drawLine(barycenterXAsInt, barycenterYAsInt-1, barycenterXAsInt, barycenterYAsInt+1);

            }
        }
    }


    //TODO use forEach visitors intead of creating lists or concatenating iterators,
    // just execute one big draw sequence
    public static void drawDetectors(Graphics2D graphics,
                                     List<RetinaPrimitive> retinaPrimitivesArray,
                                     Iterable<Intersection> intersections,
                                     Iterable<ProcessA.Sample> samplesArray, Iterable<DrawingEntity> drawingEntities) {

        for( Intersection iterationIntersection : intersections ) {
            graphics.setColor(Color.BLUE);

            double[] ip = iterationIntersection.intersectionPosition.getDataRef();
            graphics.drawRect((int)ip[0]-1, (int)ip[1]-1, 3, 3);
        }


        for( DrawingEntity iterationDrawingEntity : drawingEntities ) {
            switch( iterationDrawingEntity.getDrawType() ) {
                case SAMPLES:
                //iterationDrawingEntity.drawSamples(graphics, samplesArray.get(iterationDrawingEntity.getDataSourceIndex()));
                    iterationDrawingEntity.drawSamples(graphics, samplesArray);
                break;

                case RETINA_PRIMITIVE:
                iterationDrawingEntity.drawRetinaPrimitives(graphics,
                        retinaPrimitivesArray.get(iterationDrawingEntity.getDataSourceIndex())
                    );
                break;

                case CUSTOM:
                iterationDrawingEntity.drawCustom(graphics);
                break;
            }
        }
    }
}
