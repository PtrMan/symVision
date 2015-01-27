package bpsolver.codelets;

import Datastructures.Vector2d;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import bpsolver.NetworkHandles;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import bpsolver.nodes.PlatonicPrimitiveNode;

/**
 *
 * 
 */
public class HelperFunctions
{
    public static PlatonicPrimitiveInstanceNode createVectorAttributeNode(Vector2d<Float> vector, PlatonicPrimitiveNode primitiveNodeType, Network network, NetworkHandles networkHandles)
    {
        PlatonicPrimitiveInstanceNode createdVectorInstanceNode;
        FeatureNode createdXNode;
        FeatureNode createdYNode;
        Link linkToXNode;
        Link linkToYNode;
        
        createdVectorInstanceNode = new PlatonicPrimitiveInstanceNode(primitiveNodeType);
        
        createdXNode = FeatureNode.createFloatNode(networkHandles.xCoordinatePlatonicPrimitiveNode, vector.x, 1);
        createdYNode = FeatureNode.createFloatNode(networkHandles.yCoordinatePlatonicPrimitiveNode, vector.y, 1);
        linkToXNode = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdXNode);
        createdVectorInstanceNode.outgoingLinks.add(linkToXNode);
        linkToYNode = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdYNode);
        createdVectorInstanceNode.outgoingLinks.add(linkToYNode);
        
        return createdVectorInstanceNode;
    }
}
