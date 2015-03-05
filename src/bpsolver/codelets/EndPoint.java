package bpsolver.codelets;

import bpsolver.HelperFunctions;
import Datastructures.Vector2d;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import bpsolver.NetworkHandles;
import bpsolver.SolverCodelet;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import misc.Assert;

/**
 * calculates endpoints of a line segment or a curve
 * must be called only once!
 */
public class EndPoint extends SolverCodelet
{
    public EndPoint(Network network, NetworkHandles networkHandles)
    {
        super(network, networkHandles);
    }

    @Override
    public void initialize()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        EndPoint cloned;
        
        cloned = new EndPoint(network, networkHandles);
        
        return cloned;
    }

    @Override
    public RunResult run()
    {
        PlatonicPrimitiveInstanceNode startNodeAsPlatonicPrimitiveInstanceNode;
        Vector2d<Float> endPoints[];
        int endpointI;
        
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "must be platonic instance node");
        startNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)startNode;
        
        endPoints = calculateEndpointsOfPlatonicPrimitiveInstanceNode(startNodeAsPlatonicPrimitiveInstanceNode);
        
        for( endpointI = 0; endpointI < 2; endpointI++ )
        {
            Link linkToEndpoint;
            
            PlatonicPrimitiveInstanceNode createdEndpointInstanceNode = HelperFunctions.createVectorAttributeNode(endPoints[endpointI], networkHandles.endpointPlatonicPrimitiveNode, network, networkHandles);
            linkToEndpoint = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdEndpointInstanceNode);
            startNode.outgoingLinks.add(linkToEndpoint);
        }
        
        return new RunResult(false);
    }

    private Vector2d<Float>[] calculateEndpointsOfPlatonicPrimitiveInstanceNode(PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode)
    {
        Vector2d<Float>[] resultPoints;
        
        resultPoints = new Vector2d[2];
        
        if( platonicPrimitiveInstanceNode.primitiveNode.equals(networkHandles.lineSegmentPlatonicPrimitiveNode) )
        {
            resultPoints[0] = platonicPrimitiveInstanceNode.p1;
            resultPoints[1] = platonicPrimitiveInstanceNode.p2;
        }
        else if( false /* TODO curve */ )
        {
            // TODO
        }
        else
        {
            Assert.Assert(false, "unreachable");
        }
        
        return resultPoints;
    }
    
}
