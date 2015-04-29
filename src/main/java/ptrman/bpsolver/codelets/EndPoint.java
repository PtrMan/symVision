package ptrman.bpsolver.codelets;

import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.HelperFunctions;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.misc.Assert;

/**
 * calculates endpoints of a line segment or a curve
 * must be called only once!
 */
public class EndPoint extends SolverCodelet
{
    public EndPoint(BpSolver bpSolver)
    {
        super(bpSolver);
    }

    @Override
    public void initialize()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        EndPoint cloned;
        
        cloned = new EndPoint(bpSolver);
        
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
            
            PlatonicPrimitiveInstanceNode createdEndpointInstanceNode = HelperFunctions.createVectorAttributeNode(endPoints[endpointI], getNetworkHandles().endpointPlatonicPrimitiveNode, bpSolver);
            linkToEndpoint = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdEndpointInstanceNode);
            startNode.outgoingLinks.add(linkToEndpoint);
        }
        
        return new RunResult(false);
    }

    private Vector2d<Float>[] calculateEndpointsOfPlatonicPrimitiveInstanceNode(PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode)
    {
        Vector2d<Float>[] resultPoints;
        
        resultPoints = new Vector2d[2];
        
        if( platonicPrimitiveInstanceNode.primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode) )
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
