package ptrman.Gui;

import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.levels.retina.*;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.Parameters;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.ITranslatorStrategy;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.NearIntersectionStrategy;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.bpsolver.pattern.FeaturePatternMatching;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller
{
    public static class RecalculateActionListener implements ActionListener
    {
        
        
        public RecalculateActionListener(BpSolver bpSolver, NodeGraph nodeGraph, IImageDrawer imageDrawer)
        {
            this.bpSolver = bpSolver;
            this.nodeGraph = nodeGraph;
            this.imageDrawer = imageDrawer;
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
            
            BufferedImage javaImage = imageDrawer.drawToJavaImage((float)time, bpSolver);
            Map2d<Boolean> image = drawToImage(javaImage);

            ProcessA processA = new ProcessA();
            ProcessB processB = new ProcessB();
            ProcessC processC = new ProcessC();
            ProcessD processD = new ProcessD();
            ProcessH processH = new ProcessH();
            ProcessE processE = new ProcessE();
            ProcessM processM = new ProcessM();

            processA.setWorkingImage(image);
            List<ProcessA.Sample> samples = processA.sampleImage();


            processB.process(samples, image);
            processC.process(samples);

            List<RetinaPrimitive> lineDetectors = processD.detectLines(samples);
            
            List<Intersection> lineIntersections = new ArrayList<>();
            
            
            
            if( enableProcessH )
            {
                processH.process(lineDetectors);
            }
            
            
            
            if( enableProcessE )
            {
                processE.process(lineDetectors, image);
                
                lineIntersections = getAllLineIntersections(lineDetectors);
            }
            
            List<ProcessM.LineParsing> lineParsings = new ArrayList<>();
            
            if( enableProcessM )
            {
                processM.process(lineDetectors);
                
                lineParsings = processM.getLineParsings();
            }
            
            
            
            ITranslatorStrategy retinaToWorkspaceTranslatorStrategy;
            
            retinaToWorkspaceTranslatorStrategy = new NearIntersectionStrategy();
            
            List<Node> objectNodes = retinaToWorkspaceTranslatorStrategy.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver);
            
            bpSolver.cycle(500);
            
            BufferedImage detectorImage;
            Graphics2D graphics;

            detectorImage = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);
            graphics = detectorImage.createGraphics();
            
            // TODO< remove the list of line intersections >
            
            drawDetectors(graphics, lineDetectors, lineIntersections, samples);
            //drawObjectBaryCenters(graphics, objectNodes, bpSolver.networkHandles);
            //drawLineParsings(graphics, lineParsings);
            

            interactive.leftCanvas.setImage(javaImage);
            interactive.rightCanvas.setImage(detectorImage);
            
            // TODO< convert retina level information to workspace nodes >
            // TODO< process workspace nodes >
            
            nodeGraph.repopulateAfterNodes(objectNodes, bpSolver.networkHandles);

            if( true )
            {
                FeaturePatternMatching featurePatternMatching;

                featurePatternMatching = new FeaturePatternMatching();

                for( Node iterationNode : objectNodes )
                {
                    Node bestPatternNode;
                    float bestPatternSimilarity;

                    bestPatternNode = null;
                    bestPatternSimilarity = 0.0f;

                    for( Node patternNode : bpSolver.patternRootNodes )
                    {
                        List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
                        float matchingDistanceValue;
                        float matchingSimilarityValue;

                        matchingPathElements = featurePatternMatching.matchAnyRecursive(iterationNode, patternNode, bpSolver.networkHandles, Arrays.asList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
                        matchingDistanceValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);
                        matchingSimilarityValue = FeaturePatternMatching.Converter.distanceToSimilarity(matchingDistanceValue);

                        if( matchingSimilarityValue > Parameters.getPatternMatchingMinSimilarity() && matchingSimilarityValue > bestPatternSimilarity )
                        {
                            bestPatternNode = patternNode;
                            bestPatternSimilarity = matchingSimilarityValue;
                        }
                    }

                    if( bestPatternNode != null )
                    {
                        // TODO< incorperate new pattern into old >
                        int debugPoint = 0;
                    }
                    else
                    {
                        bpSolver.patternRootNodes.add(iterationNode);
                    }
                }


            }
            
            time++;
        }
        
        
        
        // function in here because we don't know who should have it
        /**

        
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
        }*/
        
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
        

        private static void drawDetectors(Graphics2D graphics, List<RetinaPrimitive> lineDetectors, List<Intersection> intersections, List<ProcessA.Sample> samples)
        {

            for( RetinaPrimitive iterationRetinaPrimitive : lineDetectors )
            {
                SingleLineDetector iterationDetector;

                Vector2d<Float> aProjectedFloat;
                Vector2d<Float> bProjectedFloat;

                iterationDetector = iterationRetinaPrimitive.line;

                aProjectedFloat = iterationDetector.aFloat;
                bProjectedFloat = iterationDetector.bFloat;
                
                if( iterationDetector.resultOfCombination || false )
                {
                    graphics.setColor(Color.RED);
                }
                else
                {
                    graphics.setColor(Color.BLUE);
                }

                graphics.setStroke(new BasicStroke(2));
                graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));

                // for old code
                // TODO< overwork old code so the stroke is set at the beginning >
                graphics.setStroke(new BasicStroke(1));
            }
            
            for( Intersection iterationIntersection : intersections )
            {
                graphics.setColor(Color.BLUE);
                
                graphics.drawRect(iterationIntersection.intersectionPosition.x-1, iterationIntersection.intersectionPosition.y-1, 3, 3);
            }


            graphics.setColor(Color.GREEN);

            for( ProcessA.Sample iterationSample : samples )
            {
                graphics.drawLine(iterationSample.position.x, iterationSample.position.y, iterationSample.position.x, iterationSample.position.y);
            }


            
        }
        
        // TODO< refactor out >
        private static List<Intersection> getAllLineIntersections(List<RetinaPrimitive> lineDetectors)
        {
            List<Intersection> uniqueIntersections;
            
            uniqueIntersections = new ArrayList<>();
            
            for( RetinaPrimitive currentPrimitive : lineDetectors )
            {
                if( currentPrimitive.type != RetinaPrimitive.EnumType.LINESEGMENT )
                {
                    continue;
                }
                
                findAndAddUniqueIntersections(uniqueIntersections, currentPrimitive.line.intersections);
            }
            
            return uniqueIntersections;
        }
        
        // modifies uniqueIntersections
        private static void findAndAddUniqueIntersections(List<Intersection> uniqueIntersections, List<Intersection> intersections)
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
        
        public Interactive interactive;
        private int time;
        
        private BpSolver bpSolver;
        private final NodeGraph nodeGraph;

        private IImageDrawer imageDrawer;
    }
}
