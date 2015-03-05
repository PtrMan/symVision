package bpsolver;

import Datastructures.Vector2d;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
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
    
    public static Vector2d<Float> getVectorFromVectorAttributeNode(NetworkHandles networkHandles, PlatonicPrimitiveInstanceNode node)
    {
        Vector2d<Float> result;
        
        result = new Vector2d<>(0.0f, 0.0f);
        
        for( Link iterationLink : node.getLinksByType(Link.EnumType.HASATTRIBUTE) )
        {
            FeatureNode targetFeatureNode;

            if( iterationLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() )
            {
                continue;
            }

            targetFeatureNode = (FeatureNode)iterationLink.target;

            if( targetFeatureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) )
            {
                result.x = targetFeatureNode.getValueAsFloat();
            }
            else if( targetFeatureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) )
            {
                result.y = targetFeatureNode.getValueAsFloat();
            }
            // else ignore
        }
        
        return result;
    }
}
