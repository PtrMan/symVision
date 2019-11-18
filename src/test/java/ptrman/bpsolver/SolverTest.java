package ptrman.bpsolver;

import org.junit.Test;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.PointProximityStrategy;
import ptrman.bpsolver.nodes.AttributeNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import java.util.List;

public class SolverTest {
    public SolverTest() {
        Parameters.init();
    }

    @Test
    public void testAnglePointV() {
        Solver bpSolver = new Solver();
        bpSolver.setImageSize(new Vector2d<>(100, 100));
        
        

        BufferedImage javaImage = drawToJavaImage(bpSolver);
        Map2d<Boolean> image = drawToImage(javaImage);

        List<Node> nodes = getNodesFromImage(image, bpSolver);
        
        for( Node iterationNode : nodes ) {

            boolean doesHaveAtLeastOneVAnglePoint = doesNodeHaveAtLeastOneVAnglePoint(iterationNode, bpSolver.networkHandles);
            if( doesHaveAtLeastOneVAnglePoint ) {
                // pass
                int DEBUG0 = 0;
            }
        }
        
        // fail
        // TODO
        
        int x = 0;
        
        // TODO< check for at least one V anglepoint >
    }
    
    private static boolean doesNodeHaveAtLeastOneVAnglePoint(Node node, NetworkHandles networkHandles) {

        ArrayList<Node> doneList = new ArrayList<>();
        ArrayList<Node> nodeHeap = new ArrayList<>();
        nodeHeap.add(node);
        
        for(;;) {

            if( nodeHeap.size() == 0) {
                return false;
            }

            Node currentNode = nodeHeap.get(0);
            nodeHeap.remove(0);
            
            if( doneList.contains(currentNode) ) {
                continue;
            }
            
            doneList.add(currentNode);
            
            for( Link iterationLink : currentNode.outgoingLinks ) {
                nodeHeap.add(iterationLink.target);
            }
            
            if( currentNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {

                PlatonicPrimitiveInstanceNode currentNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode) currentNode;
                
                if( currentNodeAsPlatonicPrimitiveInstanceNode.primitiveNode.equals(networkHandles.anglePointNodePlatonicPrimitiveNode) ) {
                    // test if it is a V
                    for( Link iterationLink : currentNodeAsPlatonicPrimitiveInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

                        if( !(iterationLink.target.type == NodeTypes.EnumType.ATTRIBUTENODE.ordinal()) ) {
                            continue;
                        }

                        AttributeNode targetAttributeNode = (AttributeNode) iterationLink.target;
                        
                        if( !targetAttributeNode.attributeTypeNode.equals(networkHandles.anglePointFeatureTypePrimitiveNode) ) {
                            continue;
                        }
                        // if here -> is a anglePointFeatureTypeNode

                        AttributeNode anglePointTypeAttributeNode = targetAttributeNode;

                        int anglePointType = anglePointTypeAttributeNode.getValueAsInt();
                        if( anglePointType == PointProximityStrategy.Crosspoint.EnumAnglePointType.V.ordinal() ) {
                            return true;
                        }
                    }
                }
            }
        }
    }
    
    private static Map2d<Boolean> drawToImage(BufferedImage javaImage) {
        DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

        int bufferI;

        Map2d<Boolean> convertedToMap = new Map2d<>(javaImage.getWidth(), javaImage.getHeight());

        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ ) {

            boolean convertedPixel = imageBuffer.getElem(bufferI) != 0;
            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
        }

        return convertedToMap;
    }
    
    private static BufferedImage drawToJavaImage(Solver bpSolver) {
        BufferedImage off_Image = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.setColor(Color.BLACK);

        g2.drawLine(10, 10, 15, 30);
        g2.drawLine(20, 10, 15, 30);
        
        return off_Image;
    }
    
    private static List<Node> getNodesFromImage(Map2d<Boolean> image, Solver bpSolver) {
        // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
        bpSolver.coderack.flush();
        
        ProcessA processA = new ProcessA();
        ProcessB processB = new ProcessB();
        // ProcessC processC = new ProcessC(null); TODO< overwork >
        ProcessD processD = new ProcessD();
        ProcessH processH = new ProcessH();
        ProcessE processE = new ProcessE();
        ProcessM processM = new ProcessM();

        IMap2d<Integer> dummyObjectIdMap = new Map2d<>(image.getWidth(), image.getLength());
        for( int y = 0; y < dummyObjectIdMap.getLength(); y++ ) {
            for( int x = 0; x < dummyObjectIdMap.getWidth(); x++ ) {
                dummyObjectIdMap.setAt(x, y, 0);
            }
        }

        throw new RuntimeException("Not implemented!");

        /*

        processA.set(image, dummyObjectIdMap);
        List<ProcessA.Sample> samples = processA.sampleImage();
        
        
        processB.process(samples, image);


        //processC.process(samples); TODO
        
        List<RetinaPrimitive> lineDetectors = null; // TODO processD.detectLines(samples);
        
        List<Intersection> lineIntersections = new ArrayList<>();


        Assert.Assert(false, "TODO modernize");
        //processH.process(lineDetectors);
        
        
        
        
        processE.process(lineDetectors, image);
        
        lineIntersections = getAllLineIntersections(lineDetectors);
        
        
        List<ProcessM.LineParsing> lineParsings = new ArrayList<>();
        
        processM.process(lineDetectors);
        
        lineParsings = processM.getLineParsings();
        
        
        
        
        PointProximityStrategy retinaToWorkspaceTranslator;
        
        retinaToWorkspaceTranslator = new PointProximityStrategy();
        List<Node> objectNodes = retinaToWorkspaceTranslator.createObjectsFromRetinaPrimitives(lineDetectors, bpSolver);

        bpSolver.cycle(500);
        
        return objectNodes;

        */
    }
    
    // TODO< refactor out >
    private static List<Intersection> getAllLineIntersections(List<RetinaPrimitive> primitives) {
        List<Intersection> uniqueIntersections = new ArrayList<>();

        for( RetinaPrimitive currentDetector : primitives ) {
            findAndAddUniqueIntersections(uniqueIntersections, currentDetector.line.intersections);
        }

        return uniqueIntersections;
    }


    // modifies uniqueIntersections
    private static void findAndAddUniqueIntersections(List<Intersection> uniqueIntersections, List<Intersection> intersections) {
        for( Intersection currentOuterIntersection : intersections ) {

            boolean found = false;

            for( Intersection currentUnqiueIntersection : uniqueIntersections ) {
                if( currentUnqiueIntersection.equals(currentOuterIntersection) ) {
                    found = true;
                    break;
                }
            }

            if( !found ) {
                uniqueIntersections.add(currentOuterIntersection);
            }
        }
    }
}
