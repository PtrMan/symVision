package Gui;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import static Datastructures.Vector2d.FloatHelper.sub;
import FargGeneral.Coderack;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.ProcessA;
import RetinaLevel.ProcessB;
import RetinaLevel.ProcessC;
import RetinaLevel.ProcessD;
import RetinaLevel.ProcessE;
import RetinaLevel.ProcessH;
import RetinaLevel.ProcessM;
import RetinaLevel.SingleLineDetector;
import bpsolver.BpSolver;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;
import bpsolver.Parameters;
import bpsolver.RetinaToWorkspaceTranslator;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import javax.swing.Timer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Controller
{
    private static class RecalculateActionListener implements ActionListener
    {
        
        
        public RecalculateActionListener(BpSolver bpSolver, NodeGraph nodeGraph)
        {
            this.bpSolver = bpSolver;
            this.nodeGraph = nodeGraph;
        }
        
        @Override
        public void actionPerformed(ActionEvent ae) {
            recalculate();
        }
        
        private void recalculate()
        {
            final boolean enableProcessH = true;
            final boolean enableProcessE = true;
            final boolean enableProcessM = true;
            
            // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
            bpSolver.coderack.flush();
            
            BufferedImage javaImage = drawToJavaImage((float)time * 0.1f);
            Map2d<Boolean> image = drawToImage(javaImage);

            ProcessA processA = new ProcessA();
            ProcessB processB = new ProcessB();
            ProcessC processC = new ProcessC();
            ProcessD processD = new ProcessD();
            ProcessH processH = new ProcessH();
            ProcessE processE = new ProcessE();
            ProcessM processM = new ProcessM();

            processA.setWorkingImage(image);
            ArrayList<ProcessA.Sample> samples = processA.sampleImage();


            processB.process(samples, image);
            processC.process(samples);

            ArrayList<SingleLineDetector> lineDetectors = processD.detectLines(samples);
            
            ArrayList<Intersection> lineIntersections = new ArrayList<>();
            
            
            
            if( enableProcessH )
            {
                processH.process(lineDetectors);
            }
            
            
            
            if( enableProcessE )
            {
                processE.process(lineDetectors, image);
                
                lineIntersections = getAllLineIntersections(lineDetectors);
            }
            
            ArrayList<ProcessM.LineParsing> lineParsings = new ArrayList<>();
            
            if( enableProcessM )
            {
                processM.process(lineDetectors);
                
                lineParsings = processM.getLineParsings();
            }
            
            
            
            
            
            ArrayList<Node> objectNodes = RetinaToWorkspaceTranslator.createObjectFromLines(lineDetectors, bpSolver.network, bpSolver.networkHandles, bpSolver.coderack, bpSolver.codeletLtmLookup);
            
            bpSolver.cycle(500);
            
            BufferedImage detectorImage;
            Graphics2D graphics;

            detectorImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
            graphics = detectorImage.createGraphics();
            
            // TODO< remove the list of line intersections >
            
            //drawDetectors(graphics, lineDetectors, lineIntersections, samples);
            //drawObjectBaryCenters(graphics, objectNodes, bpSolver.networkHandles);
            drawLineParsings(graphics, lineParsings);
            

            interactive.leftCanvas.setImage(javaImage);
            interactive.rightCanvas.setImage(detectorImage);
            
            // TODO< convert retina level information to workspace nodes >
            // TODO< process workspace nodes >
            
            nodeGraph.repopulateAfterNodes(objectNodes);
            
            time++;
        }
        
        
        
        // function in here because we don't know who should have it
        private static BufferedImage drawToJavaImage(float time)
        {
            BufferedImage off_Image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = off_Image.createGraphics();
            g2.setColor(Color.BLACK);

            //g2.drawLine(10, 10, 30, 20);

            //g2.drawLine(20, 30, 30, 20);
            
            ///drawTestTriangle(g2, new Vector2d<>(20.0f, 60.0f), 10.0f, time, (3.0f / (float)Math.sqrt(3)));
            
            //drawTestTriangle(g2, new Vector2d<>(60.0f, 60.0f), 10.0f, time * 0.114f, 0.5f*(3.0f / (float)Math.sqrt(3)));
            
            g2.drawOval(5, 5, 80, 80);
            
            return off_Image;
        }
        
        private static void drawTestTriangle(Graphics2D graphics, Vector2d<Float> position, float radius, float angle, float relativeSegmentWidth)
        {
            drawTrianglePart(graphics, position, radius, angle                                      , radius * relativeSegmentWidth);
            drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (1.0f/3.0f)), radius * relativeSegmentWidth);
            //test drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (2.0f/3.0f)), radius * relativeSegmentWidth);
        }
        
        private static void drawTrianglePart(Graphics2D graphics, Vector2d<Float> center, float radius, float angle, float segmentWidth)
        {
            Vector2d<Float> tangent, scaledTangent;
            Vector2d<Float> normal, scaledNormal;
            Vector2d<Float> pointA, pointB;
            Vector2d<Integer> pointAInt, pointBInt;
            
            normal = new Vector2d<Float>((float)Math.sin(angle), (float)Math.cos(angle));
            tangent = new Vector2d<Float>(normal.y, -normal.x);
            
            scaledNormal = getScaled(normal, radius);
            scaledTangent = getScaled(tangent, segmentWidth);
            
            pointA = add(center, add(scaledNormal, scaledTangent));
            pointB = add(center, sub(scaledNormal, scaledTangent));
            
            pointAInt = new Vector2d<>(Math.round(pointA.x), Math.round(pointA.y));
            pointBInt = new Vector2d<>(Math.round(pointB.x), Math.round(pointB.y));
            
            graphics.drawLine(pointAInt.x, pointAInt.y, pointBInt.x, pointBInt.y);
        }
        
        private static void drawLineParsings(Graphics2D graphics, ArrayList<ProcessM.LineParsing> lineParsings)
        {
            for( ProcessM.LineParsing iterationLineParsing : lineParsings )
            {
                drawLineParsing(graphics, iterationLineParsing);
            }
        }
        
        private static void drawLineParsing(Graphics2D graphics, ProcessM.LineParsing lineParsing)
        {
            graphics.setColor(Color.LIGHT_GRAY);
            
            for( SingleLineDetector iterationDetector : lineParsing.lineParsing )
            {
                Vector2d<Float> aProjectedFloat;
                Vector2d<Float> bProjectedFloat;

                aProjectedFloat = iterationDetector.aFloat;
                bProjectedFloat = iterationDetector.bFloat;
                
                graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));
            }
        }
        
        private static Map2d<Boolean> drawToImage(BufferedImage javaImage)
        {
            DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

            int bufferI;
            Map2d<Boolean> convertedToMap;

            convertedToMap = new Map2d<Boolean>(javaImage.getWidth(), javaImage.getHeight());

            for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
            {
                boolean convertedPixel;

                convertedPixel = imageBuffer.getElem(bufferI) != 0;
                convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
            }

            return convertedToMap;
        }
        
        // draws the barycenters if possible
        private static void drawObjectBaryCenters(Graphics2D graphics, ArrayList<Node> objectNodes, NetworkHandles networkHandles)
        {
            graphics.setColor(Color.GREEN);
            
            for( Node iterationNode : objectNodes )
            {
                for( Link iterationLink : iterationNode.getLinksByType(Link.EnumType.HASATTRIBUTE) )
                {
                    PlatonicPrimitiveInstanceNode platonicPrimitiveInstance;
                    
                    if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
                    {
                        continue;
                    }
                    
                    platonicPrimitiveInstance = (PlatonicPrimitiveInstanceNode)iterationLink.target;
                    
                    if( !platonicPrimitiveInstance.primitiveNode.equals(networkHandles.barycenterPlatonicPrimitiveNode) )
                    {
                        continue;
                    }
                    // here if barycenter
                    float barycenterX, barycenterY;
                    int barycenterXAsInt, barycenterYAsInt;
                    
                    barycenterX = 0.0f;
                    barycenterY = 0.0f;
                    
                    for( Link iterationLink2 : platonicPrimitiveInstance.getLinksByType(Link.EnumType.HASATTRIBUTE) )
                    {
                        FeatureNode featureNode;
                        
                        if( iterationLink2.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() )
                        {
                            continue;
                        }
                        
                        featureNode = (FeatureNode)iterationLink2.target;
                        
                        if( featureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) )
                        {
                            barycenterX = featureNode.getValueAsFloat();
                        }
                        else if( featureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) )
                        {
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
        

        private static void drawDetectors(Graphics2D graphics, ArrayList<SingleLineDetector> lineDetectors, ArrayList<Intersection> intersections, ArrayList<ProcessA.Sample> samples)
        {
            for( SingleLineDetector iterationDetector : lineDetectors )
            {
                Vector2d<Float> aProjectedFloat;
                Vector2d<Float> bProjectedFloat;

                aProjectedFloat = iterationDetector.aFloat;
                bProjectedFloat = iterationDetector.bFloat;
                
                if( iterationDetector.resultOfCombination )
                {
                    graphics.setColor(Color.ORANGE);
                }
                else
                {
                    graphics.setColor(Color.RED);
                }
                
                graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));
            }
            
            for( Intersection iterationIntersection : intersections )
            {
                graphics.setColor(Color.BLUE);
                
                graphics.drawRect(iterationIntersection.intersectionPosition.x-1, iterationIntersection.intersectionPosition.y-1, 3, 3);
            }

            /*
            graphics.setColor(Color.GREEN);

            for( ProcessA.Sample iterationSample : samples )
            {
                graphics.drawLine(iterationSample.position.x, iterationSample.position.y, iterationSample.position.x, iterationSample.position.y);
            }
            */

            
        }
        
        // TODO< refactor out >
        private static ArrayList<Intersection> getAllLineIntersections(ArrayList<SingleLineDetector> lineDetectors)
        {
            ArrayList<Intersection> uniqueIntersections;
            
            uniqueIntersections = new ArrayList<>();
            
            for( SingleLineDetector currentDetector : lineDetectors )
            {
                findAndAddUniqueIntersections(uniqueIntersections, currentDetector.intersections);
            }
            
            return uniqueIntersections;
        }
        
        // modifies uniqueIntersections
        private static void findAndAddUniqueIntersections(ArrayList<Intersection> uniqueIntersections, ArrayList<Intersection> intersections)
        {
            for( Intersection currentOuterIntersection : intersections )
            {
                boolean found;
                
                found = false;
                
                for( Intersection currentUnqiueIntersection : uniqueIntersections )
                {
                    if( currentUnqiueIntersection.equals(currentOuterIntersection) )
                    {
                        found = true;
                        break;
                    }
                }
                
                if( !found )
                {
                    uniqueIntersections.add(currentOuterIntersection);
                }
            }
            
            
        }
        
        private Interactive interactive;
        private int time;
        
        private BpSolver bpSolver;
        private final NodeGraph nodeGraph;
    }
    
    public static void main(String[] args) {
        BpSolver bpSolver = new BpSolver();
        
        Parameters.init();
        
        
        GraphWindow graphWindow = new GraphWindow();
        
        RecalculateActionListener recalculate = new RecalculateActionListener(bpSolver, graphWindow.getNodeGraph());
        recalculate.interactive = new Interactive();
        
        TuningWindow tuningWindow = new TuningWindow();
        
        Timer timer = new Timer(2000, recalculate);
        timer.setInitialDelay(0);
        timer.start(); 
        
    }
}
