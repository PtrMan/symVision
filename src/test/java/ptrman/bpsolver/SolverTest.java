package ptrman.bpsolver;

import org.junit.Ignore;
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

   @Ignore
   @Test
    public void testAnglePointV() {
        final var bpSolver = new Solver();
        bpSolver.setImageSize(new Vector2d<>(100, 100));
        
        

        final var javaImage = drawToJavaImage(bpSolver);
        final var image = drawToImage(javaImage);

        final var nodes = getNodesFromImage(image, bpSolver);
        
        for( final var iterationNode : nodes ) {

            final var doesHaveAtLeastOneVAnglePoint = doesNodeHaveAtLeastOneVAnglePoint(iterationNode, bpSolver.networkHandles);
            if( doesHaveAtLeastOneVAnglePoint ) {
                // pass
                final var DEBUG0 = 0;
            }
        }
        
        // fail
        // TODO
        
        final var x = 0;
        
        // TODO< check for at least one V anglepoint >
    }
    
    private static boolean doesNodeHaveAtLeastOneVAnglePoint(final Node node, final NetworkHandles networkHandles) {

        final var nodeHeap = new ArrayList<Node>();
        nodeHeap.add(node);

        final var doneList = new ArrayList<Node>();
        while (true) {

            if( nodeHeap.size() == 0) return false;

            final var currentNode = nodeHeap.get(0);
            nodeHeap.remove(0);
            
            if( doneList.contains(currentNode) ) continue;
            
            doneList.add(currentNode);
            
            for( final var iterationLink : currentNode.out()) nodeHeap.add(iterationLink.target);
            
            if( currentNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {

                final var currentNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode) currentNode;

                // test if it is a V
                if( currentNodeAsPlatonicPrimitiveInstanceNode.primitiveNode.equals(networkHandles.anglePointNodePlatonicPrimitiveNode) )
                    for (final var iterationLink : currentNodeAsPlatonicPrimitiveInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE)) {

                        if (!(iterationLink.target.type == NodeTypes.EnumType.ATTRIBUTENODE.ordinal())) continue;

                        final var targetAttributeNode = (AttributeNode) iterationLink.target;

                        if (!targetAttributeNode.attributeTypeNode.equals(networkHandles.anglePointFeatureTypePrimitiveNode))
                            continue;
                        // if here -> is a anglePointFeatureTypeNode

                        final var anglePointTypeAttributeNode = targetAttributeNode;

                        final var anglePointType = anglePointTypeAttributeNode.getValueAsInt();
                        if (anglePointType == PointProximityStrategy.Crosspoint.EnumAnglePointType.V.ordinal())
                            return true;
                    }
            }
        }
    }
    
    private static Map2d<Boolean> drawToImage(final BufferedImage javaImage) {
        final var imageBuffer = javaImage.getData().getDataBuffer();

        final var convertedToMap = new Map2d<Boolean>(javaImage.getWidth(), javaImage.getHeight());

        for(int bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ ) {

            final var convertedPixel = imageBuffer.getElem(bufferI) != 0;
            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
        }

        return convertedToMap;
    }
    
    private static BufferedImage drawToJavaImage(final Solver bpSolver) {
        final var off_Image = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

        final var g2 = off_Image.createGraphics();
        g2.setColor(Color.BLACK);

        g2.drawLine(10, 10, 15, 30);
        g2.drawLine(20, 10, 15, 30);
        
        return off_Image;
    }
    
    private static List<Node> getNodesFromImage(final Map2d<Boolean> image, final Solver bpSolver) {
        // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
        bpSolver.coderack.flush();
        
        final var processA = new ProcessA();
        final var processB = new ProcessB();
        // ProcessC processC = new ProcessC(null); TODO< overwork >
        final var processD = new ProcessD();
        final var processH = new ProcessH();
        final var processE = new ProcessE();
        final var processM = new ProcessM();

        final IMap2d<Integer> dummyObjectIdMap = new Map2d<>(image.getWidth(), image.getLength());
        for(var y = 0; y < dummyObjectIdMap.getLength(); y++ )
            for (var x = 0; x < dummyObjectIdMap.getWidth(); x++) dummyObjectIdMap.setAt(x, y, 0);

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
    private static List<Intersection> getAllLineIntersections(final List<RetinaPrimitive> primitives) {
        final List<Intersection> uniqueIntersections = new ArrayList<>();

        for( final var currentDetector : primitives )
            findAndAddUniqueIntersections(uniqueIntersections, currentDetector.line.intersections);

        return uniqueIntersections;
    }


    // modifies uniqueIntersections
    private static void findAndAddUniqueIntersections(final List<Intersection> uniqueIntersections, final List<Intersection> intersections) {
        for( final var currentOuterIntersection : intersections ) {

            final var found = uniqueIntersections.stream().anyMatch(currentUnqiueIntersection -> currentUnqiueIntersection.equals(currentOuterIntersection));

            if( !found ) uniqueIntersections.add(currentOuterIntersection);
        }
    }
}
