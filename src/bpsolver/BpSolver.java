package bpsolver;

import FargGeneral.Coderack;
import Datastructures.Map2d;
import Datastructures.Vector2d;
import Gui.Interactive;
import RetinaLevel.ProcessA;
import RetinaLevel.ProcessB;
import RetinaLevel.ProcessC;
import RetinaLevel.ProcessD;
import RetinaLevel.ProcessH;
import bpsolver.codelets.LineSegmentLength;
import bpsolver.ltm.Link;
import bpsolver.ltm.LinkCreator;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import bpsolver.nodes.PlatonicPrimitiveNode;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;

public class BpSolver {
    public static void main(String[] args) {
        BpSolver solver = new BpSolver();
        solver.initializeNetwork();
        solver.setupLtmFactoryDefault();
        solver.initializeCodeletLtmLookup();
        
        
        Map2d<Boolean> image = drawToImage();
        
        ProcessA processA = new ProcessA();
        ProcessB processB = new ProcessB();
        ProcessC processC = new ProcessC();
        ProcessD processD = new ProcessD();
        ProcessH processH = new ProcessH();
        
        processA.setWorkingImage(image);
        ArrayList<ProcessA.Sample> samples = processA.sampleImage();
        
        
        processB.process(samples, image);
        processC.process(samples);
        
        ArrayList<ProcessD.LineDetector> lineDetectors = processD.detectLines(samples);
        
        processH.process(lineDetectors);
        
        BufferedImage detectorImage = drawDetectors(lineDetectors, samples);
        
        
        Interactive interactive = new Interactive();
        interactive.canvas.setImage(detectorImage);
        
        
        Node objectNode = RetinaToWorkspaceTranslator.createObjectFromLines(lineDetectors, solver.network, solver.networkHandles, solver.coderack, solver.codeletLtmLookup);
        
        // TODO< process >
    }
    
    /**
     * 
     * stores all factory preset nodes in the ltm (standard node types, linked attributes, etc)
     */
    public void setupLtmFactoryDefault()
    {
        PlatonicPrimitiveNode lineSegmentPrimitiveNode = new PlatonicPrimitiveNode("LineSegment", null);
        networkHandles.lineSegmentPlatonicPrimitiveNode = lineSegmentPrimitiveNode;
        network.nodes.add(lineSegmentPrimitiveNode);
        
        PlatonicPrimitiveNode lineSegmentAttributeNode = new PlatonicPrimitiveNode("LineSegmentLength", "LineSegmentLength");
        networkHandles.lineSegmentFeatureLineLengthPrimitiveNode = lineSegmentAttributeNode;
        network.nodes.add(lineSegmentAttributeNode);
        
        FargGeneral.network.Link link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, lineSegmentAttributeNode);
        lineSegmentPrimitiveNode.outgoingLinks.add(link);
        
        
        PlatonicPrimitiveNode objectPrimitiveNode = new PlatonicPrimitiveNode("Object", null);
        networkHandles.objectPlatonicPrimitiveNode = objectPrimitiveNode;
        network.nodes.add(objectPrimitiveNode);
    }
    
    private void initializeNetwork()
    {
        network.linkCreator = new LinkCreator();
    }
    
    private void initializeCodeletLtmLookup()
    {
        CodeletLtmLookup.RegisterEntry createdRegistryEntry;
        SolverCodelet createdCodelet;
        
        codeletLtmLookup = new CodeletLtmLookup();
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new LineSegmentLength(network, networkHandles);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.5f));
        
        codeletLtmLookup.registry.put("LineSegmentLength", createdRegistryEntry);
    }
    
    // both ltm and workspace
    // the difference is that the nodes of the workspace may all be deleted
    private Network network = new Network();
    private NetworkHandles networkHandles = new NetworkHandles();
    private Coderack coderack = new Coderack();
    private CodeletLtmLookup codeletLtmLookup;
    
    
    
    
    // function in here because we don't know who should have it
    private static Map2d<Boolean> drawToImage()
    {
        BufferedImage off_Image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = off_Image.createGraphics();
        g2.drawLine(10, 10, 50, 20);
        
        g2.drawLine(20, 50, 50, 20);
        
        DataBuffer imageBuffer = off_Image.getData().getDataBuffer();
        
        int bufferI;
        Map2d<Boolean> convertedToMap;
        
        convertedToMap = new Map2d<Boolean>(off_Image.getWidth(), off_Image.getHeight());
        
        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
        {
            boolean convertedPixel;
            
            convertedPixel = imageBuffer.getElem(bufferI) != 0;
            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
        }
        
        return convertedToMap;
    }
    
    private static BufferedImage drawDetectors(ArrayList<ProcessD.LineDetector> lineDetectors, ArrayList<ProcessA.Sample> samples)
    {
        BufferedImage resultImage;
        Graphics2D graphics;
        
        resultImage = new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB);
        
        graphics = resultImage.createGraphics();
        graphics.setColor(Color.RED);
        
        for( ProcessD.LineDetector iterationDetector : lineDetectors )
        {
            Vector2d<Float> aProjectedFloat;
            Vector2d<Float> bProjectedFloat;
            
            aProjectedFloat = iterationDetector.getAProjected();
            bProjectedFloat = iterationDetector.getBProjected();
            
            graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));
        }
        
        /*
        graphics.setColor(Color.GREEN);
        
        for( ProcessA.Sample iterationSample : samples )
        {
            graphics.drawLine(iterationSample.position.x, iterationSample.position.y, iterationSample.position.x, iterationSample.position.y);
        }
        */
        
        return resultImage;
    }
}
