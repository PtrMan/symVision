package bpsolver;

import bpsolver.RetinaToWorkspaceTranslator.PointProximityStrategy;
import Datastructures.Map2d;
import Datastructures.Vector2d;
import FargGeneral.network.Link;
import FargGeneral.network.Node;
import RetinaLevel.Intersection;
import RetinaLevel.ProcessA;
import RetinaLevel.ProcessB;
import RetinaLevel.ProcessC;
import RetinaLevel.ProcessD;
import RetinaLevel.ProcessE;
import RetinaLevel.ProcessH;
import RetinaLevel.ProcessM;
import RetinaLevel.RetinaPrimitive;
import RetinaLevel.SingleLineDetector;
import bpsolver.nodes.AttributeNode;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import org.junit.Test;
import static org.junit.Assert.*;

public class BpSolverTest
{
    public BpSolverTest()
    {
        Parameters.init();
    }

    @Test
    public void testAnglePointV()
    {
        BpSolver bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<>(100, 100));
        
        

        BufferedImage javaImage = drawToJavaImage(bpSolver);
        Map2d<Boolean> image = drawToImage(javaImage);

        ArrayList<Node> nodes = getNodesFromImage(image, bpSolver);
        
        for( Node iterationNode : nodes )
        {
            boolean doesHaveAtLeastOneVAnglePoint;
            
            doesHaveAtLeastOneVAnglePoint = doesNodeHaveAtLeastOneVAnglePoint(iterationNode, bpSolver.networkHandles);
            if( doesHaveAtLeastOneVAnglePoint )
            {
                // pass
                int DEBUG0 = 0;
            }
        }
        
        // fail
        // TODO
        
        int x = 0;
        
        // TODO< check for at least one V anglepoint >
    }
    
    private static boolean doesNodeHaveAtLeastOneVAnglePoint(Node node, NetworkHandles networkHandles)
    {
        ArrayList<Node> nodeHeap;
        ArrayList<Node> doneList;
        
        doneList = new ArrayList<>();
        nodeHeap = new ArrayList<>();
        nodeHeap.add(node);
        
        for(;;)
        {
            Node currentNode;
            
            if( nodeHeap.size() == 0)
            {
                return false;
            }
            
            currentNode = nodeHeap.get(0);
            nodeHeap.remove(0);
            
            if( doneList.contains(currentNode) )
            {
                continue;
            }
            
            doneList.add(currentNode);
            
            for( Link iterationLink : currentNode.outgoingLinks )
            {
                nodeHeap.add(iterationLink.target);
            }
            
            if( currentNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
            {
                PlatonicPrimitiveInstanceNode currentNodeAsPlatonicPrimitiveInstanceNode;
                
                currentNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)currentNode;
                
                if( currentNodeAsPlatonicPrimitiveInstanceNode.primitiveNode.equals(networkHandles.anglePointNodePlatonicPrimitiveNode) )
                {
                    // test if it is a V
                    for( Link iterationLink : currentNodeAsPlatonicPrimitiveInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE) )
                    {
                        AttributeNode anglePointTypeAttributeNode;
                        AttributeNode targetAttributeNode;
                        int anglePointType;
                        
                        if( !(iterationLink.target.type == NodeTypes.EnumType.ATTRIBUTENODE.ordinal()) )
                        {
                            continue;
                        }
                        
                        targetAttributeNode = (AttributeNode)iterationLink.target;
                        
                        if( !targetAttributeNode.attributeTypeNode.equals(networkHandles.anglePointFeatureTypePrimitiveNode) )
                        {
                            continue;
                        }
                        // if here -> is a anglePointFeatureTypeNode
                        
                        anglePointTypeAttributeNode = targetAttributeNode;
                        
                        anglePointType = anglePointTypeAttributeNode.getValueAsInt();
                        if( anglePointType == PointProximityStrategy.Crosspoint.EnumAnglePointType.V.ordinal() )
                        {
                            return true;
                        }
                    }
                }
            }
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
    
    private BufferedImage drawToJavaImage(BpSolver bpSolver)
    {
        BufferedImage off_Image = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(Color.BLACK);

        g2.drawLine(10, 10, 15, 30);
        g2.drawLine(20, 10, 15, 30);
        
        return off_Image;
    }
    
    private ArrayList<Node> getNodesFromImage(Map2d<Boolean> image, BpSolver bpSolver)
    {
        // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
        bpSolver.coderack.flush();
        
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
        
        ArrayList<RetinaPrimitive> lineDetectors = processD.detectLines(samples);
        
        ArrayList<Intersection> lineIntersections = new ArrayList<>();
        
        
        
        processH.process(lineDetectors);
        
        
        
        
        processE.process(lineDetectors, image);
        
        lineIntersections = getAllLineIntersections(lineDetectors);
        
        
        ArrayList<ProcessM.LineParsing> lineParsings = new ArrayList<>();
        
        processM.process(lineDetectors);
        
        lineParsings = processM.getLineParsings();
        
        
        
        
        PointProximityStrategy retinaToWorkspaceTranslator;
        
        retinaToWorkspaceTranslator = new PointProximityStrategy();
        
        ArrayList<Node> objectNodes = retinaToWorkspaceTranslator.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver.network, bpSolver.networkHandles, bpSolver.coderack, bpSolver.codeletLtmLookup, bpSolver.getImageSizeAsFloat());
        
        bpSolver.cycle(500);
        
        return objectNodes;
    }
    
    // TODO< refactor out >
    private static ArrayList<Intersection> getAllLineIntersections(ArrayList<RetinaPrimitive> primitives)
    {
        ArrayList<Intersection> uniqueIntersections;

        uniqueIntersections = new ArrayList<>();

        for( RetinaPrimitive currentDetector : primitives )
        {
            findAndAddUniqueIntersections(uniqueIntersections, currentDetector.line.intersections);
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
    
}
