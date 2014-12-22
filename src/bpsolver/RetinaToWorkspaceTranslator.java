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
    public static ArrayList<Node> createObjectFromLines(ArrayList<ProcessD.SingleLineDetector> lines, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
    {
        ArrayList<Node> resultNodes;
        
        // for now we store each linesegment as a slingle object
        // todo< integrate other processes which use angle connections to build objects >
        
        resultNodes = new ArrayList<>();
        
        for( ProcessD.SingleLineDetector iterationLine : lines )
        {
            PlatonicPrimitiveInstanceNode createdLineNode;
            Link createdForwardLink, createdBackwardLink;
            Node objectNode;
            
            
            objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            network.nodes.add(objectNode);
            
            // add all codelet's of the object
            codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(objectNode, coderack, network);
        
            
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
            
            resultNodes.add(objectNode);
        }
        
        return resultNodes;
    }
}
