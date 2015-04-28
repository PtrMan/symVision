package bpsolver;

import Datastructures.Vector2d;
import FargGeneral.network.Link;
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
    public static PlatonicPrimitiveInstanceNode createVectorAttributeNode(Vector2d<Float> vector, PlatonicPrimitiveNode primitiveNodeType, BpSolver bpSolver)
    {
        PlatonicPrimitiveInstanceNode createdVectorInstanceNode;
        FeatureNode createdXNode;
        FeatureNode createdYNode;
        Link linkToXNode;
        Link linkToYNode;
        
        createdVectorInstanceNode = new PlatonicPrimitiveInstanceNode(primitiveNodeType);
        
        createdXNode = FeatureNode.createFloatNode(bpSolver.networkHandles.xCoordinatePlatonicPrimitiveNode, vector.x, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(bpSolver.networkHandles.xCoordinatePlatonicPrimitiveNode));
        createdYNode = FeatureNode.createFloatNode(bpSolver.networkHandles.yCoordinatePlatonicPrimitiveNode, vector.y, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(bpSolver.networkHandles.yCoordinatePlatonicPrimitiveNode));
        linkToXNode = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdXNode);
        createdVectorInstanceNode.outgoingLinks.add(linkToXNode);
        linkToYNode = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdYNode);
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
