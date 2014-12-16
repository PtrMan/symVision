package bpsolver;

import FargGeneral.Coderack;
import bpsolver.codelets.LineSegmentLength;
import bpsolver.ltm.LinkCreator;
import FargGeneral.network.Network;
import bpsolver.nodes.PlatonicPrimitiveNode;

public class BpSolver {
    public static void main(String[] args) {
        Parameters.init();
        
        BpSolver solver = new BpSolver();
        solver.initializeNetwork();
        solver.setupLtmFactoryDefault();
        solver.initializeCodeletLtmLookup();
        
        /*
         BufferedImage javaImage = drawToJavaImage();
        Map2d<Boolean> image = drawToImage(javaImage);
        
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
        interactive.leftCanvas.setImage(javaImage);
        interactive.rightCanvas.setImage(detectorImage);
        */
        
        //Node objectNode = RetinaToWorkspaceTranslator.createObjectFromLines(lineDetectors, solver.network, solver.networkHandles, solver.coderack, solver.codeletLtmLookup);
        
        // TODO< process >
    }
    
    /**
     * 
     * stores all factory preset nodes in the ltm (standard node types, linked attributes, etc)
     */
    public void setupLtmFactoryDefault()
    {
        FargGeneral.network.Link link;
        
        networkHandles.lineSegmentPlatonicPrimitiveNode = new PlatonicPrimitiveNode("LineSegment", null);
        network.nodes.add(networkHandles.lineSegmentPlatonicPrimitiveNode);
        
        networkHandles.lineSegmentFeatureLineLengthPrimitiveNode = new PlatonicPrimitiveNode("LineSegmentLength", "LineSegmentLength");
        network.nodes.add(networkHandles.lineSegmentFeatureLineLengthPrimitiveNode);
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.lineSegmentFeatureLineLengthPrimitiveNode);
        networkHandles.lineSegmentFeatureLineLengthPrimitiveNode.outgoingLinks.add(link);
        
        
        networkHandles.barycenterPlatonicPrimitiveNode = new PlatonicPrimitiveNode("barycenter", null);
        network.nodes.add(networkHandles.barycenterPlatonicPrimitiveNode);
        
        networkHandles.xCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("xCoordinate", null);
        network.nodes.add(networkHandles.xCoordinatePlatonicPrimitiveNode);
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        networkHandles.yCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("yCoordinate", null);
        network.nodes.add(networkHandles.yCoordinatePlatonicPrimitiveNode);
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
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
    
    
    
    
}
