package ptrman.Gui;

import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.*;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 *
 */
public class DebugDrawingHelper {

    public static void drawLineParsings(Graphics2D graphics, ArrayList<ProcessM.LineParsing> lineParsings) {
        for( ProcessM.LineParsing iterationLineParsing : lineParsings ) {
            drawLineParsing(graphics, iterationLineParsing);
        }
    }

    private static void drawLineParsing(Graphics2D graphics, ProcessM.LineParsing lineParsing) {
        graphics.setColor(Color.LIGHT_GRAY);

        for( SingleLineDetector iterationDetector : lineParsing.lineParsing ) {
            Vector2d<Float> aProjectedFloat;
            Vector2d<Float> bProjectedFloat;

            aProjectedFloat = iterationDetector.aFloat;
            bProjectedFloat = iterationDetector.bFloat;

            graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));
        }
    }

    // draws the barycenters if possible
    public static void drawObjectBaryCenters(Graphics2D graphics, ArrayList<Node> objectNodes, NetworkHandles networkHandles) {
        graphics.setColor(Color.GREEN);

        for( Node iterationNode : objectNodes ) {
            for( Link iterationLink : iterationNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {
                PlatonicPrimitiveInstanceNode platonicPrimitiveInstance;

                if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
                    continue;
                }

                platonicPrimitiveInstance = (PlatonicPrimitiveInstanceNode)iterationLink.target;

                if( !platonicPrimitiveInstance.primitiveNode.equals(networkHandles.barycenterPlatonicPrimitiveNode) ) {
                    continue;
                }
                // here if barycenter
                float barycenterX, barycenterY;
                int barycenterXAsInt, barycenterYAsInt;

                barycenterX = 0.0f;
                barycenterY = 0.0f;

                for( Link iterationLink2 : platonicPrimitiveInstance.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {
                    FeatureNode featureNode;

                    if( iterationLink2.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() ) {
                        continue;
                    }

                    featureNode = (FeatureNode)iterationLink2.target;

                    if( featureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) ) {
                        barycenterX = featureNode.getValueAsFloat();
                    }
                    else if( featureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) ) {
                        barycenterY = featureNode.getValueAsFloat();
                    }
                }

                barycenterXAsInt = Math.round(barycenterX);
                barycenterYAsInt = Math.round(barycenterY);

                // draw

                graphics.drawLine(barycenterXAsInt-1, barycenterYAsInt, barycenterXAsInt+1, barycenterYAsInt);
                graphics.drawLine(barycenterXAsInt, barycenterYAsInt-1, barycenterXAsInt, barycenterYAsInt+1);

            }
        }
    }


    public static void drawDetectors(Graphics2D graphics, List<RetinaPrimitive> lineDetectors, List<Intersection> intersections, List<ProcessA.Sample> samples) {

        for( RetinaPrimitive iterationRetinaPrimitive : lineDetectors ) {
            SingleLineDetector iterationDetector;

            Vector2d<Float> aProjectedFloat;
            Vector2d<Float> bProjectedFloat;

            iterationDetector = iterationRetinaPrimitive.line;

            aProjectedFloat = iterationDetector.aFloat;
            bProjectedFloat = iterationDetector.bFloat;

            if( iterationDetector.resultOfCombination || false ) {
                graphics.setColor(Color.RED);
            }
            else {
                graphics.setColor(Color.BLUE);
            }

            graphics.setStroke(new BasicStroke(2));
            graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));

            // for old code
            // TODO< overwork old code so the stroke is set at the beginning >
            graphics.setStroke(new BasicStroke(1));
        }
        /*
        for( Intersection iterationIntersection : intersections ) {
            graphics.setColor(Color.BLUE);

            graphics.drawRect(iterationIntersection.intersectionPosition.x-1, iterationIntersection.intersectionPosition.y-1, 3, 3);
        }
        */

        for( ProcessA.Sample iterationSample : samples ) {
            boolean showAltitude = false;

            if( showAltitude ) {
                float altitudeAsGreen;

                if( !iterationSample.isAltidudeValid()) {
                    altitudeAsGreen = 0.5f;
                }
                else {
                    altitudeAsGreen = 0.5f + (1.0f - 0.5f) * java.lang.Math.min(1.0f, iterationSample.altitude / 10.0f);
                }

                graphics.setColor(new Color(0.0f, altitudeAsGreen, 0.0f));
            }
            else {
                // show if it is a endosceleton point or not

                if( iterationSample.type == ProcessA.Sample.EnumType.ENDOSCELETON ) {
                    graphics.setColor(Color.GREEN);
                }
                else {
                    graphics.setColor(Color.PINK);
                }
            }


            graphics.drawLine(iterationSample.position.x, iterationSample.position.y, iterationSample.position.x, iterationSample.position.y);
        }
    }

    public static void drawRetinaPrimitives(Graphics2D graphics,  List<RetinaPrimitive> retinaPrimitives, List<Intersection> intersections, List<ProcessA.Sample> samples) {

        // TODO< sort out between lines, dots, circles and splines >

        drawDetectors(graphics, retinaPrimitives, intersections, samples);
    }
}
