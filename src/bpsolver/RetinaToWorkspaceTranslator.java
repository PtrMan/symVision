package bpsolver;

import FargGeneral.Coderack;
import RetinaLevel.ProcessD;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;

public class RetinaToWorkspaceTranslator
{
    /**
     * 
     * \param lines
     * \param network
     * \return the node which is the object node 
     */
    public static Node createObjectFromLines(ArrayList<ProcessD.LineDetector> lines, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
    {
        Node objectNode;
        
        objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
        network.nodes.add(objectNode);
        
        for( ProcessD.LineDetector iterationLine : lines )
        {
            PlatonicPrimitiveInstanceNode createdLineNode;
            Link createdForwardLink, createdBackwardLink;
            
            createdLineNode = new PlatonicPrimitiveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
            createdLineNode.p1 = iterationLine.getAProjected();
            createdLineNode.p2 = iterationLine.getBProjected();
            network.nodes.add(createdLineNode);
            
            // linkage
            createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdLineNode);
            objectNode.outgoingLinks.add(createdForwardLink);
            
            createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
            createdLineNode.outgoingLinks.add(createdBackwardLink);
            
            // add all codelet's of it
            codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdLineNode, coderack, network);
        }
        
        return objectNode;
    }
}
