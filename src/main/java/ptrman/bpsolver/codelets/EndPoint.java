package ptrman.bpsolver.codelets;

import org.apache.commons.math3.linear.ArrayRealVector;
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
public class EndPoint extends SolverCodelet {
    public EndPoint(BpSolver bpSolver) {
        super(bpSolver);
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        EndPoint cloned;
        
        cloned = new EndPoint(bpSolver);
        
        return cloned;
    }

    @Override
    public RunResult run() {
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "must be platonic instance node");
        final PlatonicPrimitiveInstanceNode startNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)startNode;
        
        final ArrayRealVector[] endPoints = calculateEndpointsOfPlatonicPrimitiveInstanceNode(startNodeAsPlatonicPrimitiveInstanceNode);
        
        for( int endpointI = 0; endpointI < 2; endpointI++ ) {
            final PlatonicPrimitiveInstanceNode createdEndpointInstanceNode = HelperFunctions.createVectorAttributeNode(endPoints[endpointI], getNetworkHandles().endpointPlatonicPrimitiveNode, bpSolver);
            final Link linkToEndpoint = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdEndpointInstanceNode);
            startNode.outgoingLinks.add(linkToEndpoint);
        }
        
        return new RunResult(false);
    }

    private ArrayRealVector[] calculateEndpointsOfPlatonicPrimitiveInstanceNode(final PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode) {
        ArrayRealVector[] resultPoints = new ArrayRealVector[2];
        
        if( platonicPrimitiveInstanceNode.primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode) ) {
            resultPoints[0] = platonicPrimitiveInstanceNode.p1;
            resultPoints[1] = platonicPrimitiveInstanceNode.p2;
        }
        else if( false /* TODO curve */ ) {
            // TODO
        }
        else {
            Assert.Assert(false, "unreachable");
        }
        
        return resultPoints;
    }
    
}
