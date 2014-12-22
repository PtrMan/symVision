package bpsolver;

import FargGeneral.Coderack;
import bpsolver.codelets.LineSegmentLength;
import bpsolver.ltm.LinkCreator;
import FargGeneral.network.Network;
import bpsolver.codelets.BaryCenter;
import bpsolver.nodes.PlatonicPrimitiveNode;

public class BpSolver {
    public static void main(String[] args)
    {
        Parameters.init();
        
        BpSolver solver = new BpSolver();
    }
    
    public BpSolver()
    {
        initializeNetwork();
        setupLtmFactoryDefault();
        initializeCodeletLtmLookup();
    }
    
    public void cycle(int cycleCount)
    {
        coderack.cycle(cycleCount);
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
        
        
        networkHandles.barycenterPlatonicPrimitiveNode = new PlatonicPrimitiveNode("barycenter", "BaryCenter");
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
        
        // a object has a barycenter
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.barycenterPlatonicPrimitiveNode);
        networkHandles.objectPlatonicPrimitiveNode.outgoingLinks.add(link);
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
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new BaryCenter(network, networkHandles, BaryCenter.EnumRecalculate.NO);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.1f));
        
        codeletLtmLookup.registry.put("BaryCenter", createdRegistryEntry);
    }
    
    // both ltm and workspace
    // the difference is that the nodes of the workspace may all be deleted
    public Network network = new Network();
    public NetworkHandles networkHandles = new NetworkHandles();
    public Coderack coderack = new Coderack();
    public CodeletLtmLookup codeletLtmLookup;
    
    
    
    
}
