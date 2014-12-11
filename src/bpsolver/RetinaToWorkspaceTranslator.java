package bpsolver;

import FargGeneral.Coderack;
import RetinaLevel.ProcessD;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import bpsolver.nodes.PlatonicPrimitveInstanceNode;
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
        
        objectNode = new PlatonicPrimitveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
        network.nodes.add(objectNode);
        
        for( ProcessD.LineDetector iterationLine : lines )
        {
            PlatonicPrimitveInstanceNode createdLineNode;
            Link createdForwardLink, createdBackwardLink;
            
            createdLineNode = new PlatonicPrimitveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
            createdLineNode.p1 = iterationLine.getAProjected();
            createdLineNode.p2 = iterationLine.getBProjected();
            network.nodes.add(createdLineNode);
            
            // linkage
            createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS);
            createdForwardLink.target = createdLineNode;
            objectNode.outgoingLinks.add(createdForwardLink);
            
            createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF);
            createdBackwardLink.target = objectNode;
            createdLineNode.outgoingLinks.add(createdBackwardLink);
            
            // add all codelet's of it
            codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdLineNode, coderack, network);
        }
        
        return objectNode;
    }
}
