package bpsolver;

import FargGeneral.Coderack;
import bpsolver.codelets.LineSegmentLength;
import bpsolver.ltm.LinkCreator;
import FargGeneral.network.Network;
import bpsolver.codelets.BaryCenter;
import bpsolver.codelets.LineSegmentSlope;
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
        
        
        networkHandles.objectPlatonicPrimitiveNode = new PlatonicPrimitiveNode("Object", null);
        networkHandles.lineStructureAbstractPrimitiveNode = new PlatonicPrimitiveNode("lineStructure", null);
        networkHandles.lineSegmentPlatonicPrimitiveNode = new PlatonicPrimitiveNode("LineSegment", null);
        
        networkHandles.bayPlatonicPrimitiveNode = new PlatonicPrimitiveNode("bay", null /* TODO "Bay" */);
        networkHandles.endpointPlatonicPrimitiveNode = new PlatonicPrimitiveNode("endpoint", "EndPoint");
        networkHandles.barycenterPlatonicPrimitiveNode = new PlatonicPrimitiveNode("barycenter", "BaryCenter");
        networkHandles.lineSegmentFeatureLineLengthPrimitiveNode = new PlatonicPrimitiveNode("LineSegmentLength", "LineSegmentLength");
        networkHandles.lineSegmentFeatureLineSlopePrimitiveNode = new PlatonicPrimitiveNode("LineSegmentSlope", "LineSegmentSlope");
        
        networkHandles.xCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("xCoordinate", null);
        networkHandles.yCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("yCoordinate", null);
        
        networkHandles.lineStructureAbstractPrimitiveNode.isAbstract = true;
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.endpointPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.bayPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        
        
        network.nodes.add(networkHandles.lineSegmentPlatonicPrimitiveNode);
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.ISA, networkHandles.lineStructureAbstractPrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.lineSegmentFeatureLineLengthPrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.lineSegmentFeatureLineSlopePrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.ISA, networkHandles.objectPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        
        // TODO< imagination of circle, center, tangent lines, etc >
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.endpointPlatonicPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.endpointPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.bayPlatonicPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.bayPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(FargGeneral.network.Link.EnumType.HAS, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
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
        createdCodelet = new LineSegmentSlope(network, networkHandles);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.5f));
        
        codeletLtmLookup.registry.put("LineSegmentSlope", createdRegistryEntry);
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new BaryCenter(network, networkHandles, BaryCenter.EnumRecalculate.NO);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.1f));
        
        codeletLtmLookup.registry.put("BaryCenter", createdRegistryEntry);
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new bpsolver.codelets.EndPoint(network, networkHandles);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.2f));
        
        codeletLtmLookup.registry.put("EndPoint", createdRegistryEntry);
        
    }
    
    // both ltm and workspace
    // the difference is that the nodes of the workspace may all be deleted
    public Network network = new Network();
    public NetworkHandles networkHandles = new NetworkHandles();
    public Coderack coderack = new Coderack();
    public CodeletLtmLookup codeletLtmLookup;
    
    
    
    
}
