package ptrman.bpsolver.codelets;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.misc.Assert;

/**
 *
 *
 */
public class LineSegmentSlope extends SolverCodelet {
    public LineSegmentSlope(Solver bpSolver) {
        super(bpSolver);
    }
    
    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        return new LineSegmentSlope(bpSolver);
    }

    @Override
    public RunResult run() {
        double lineSegmentSlope;
        
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "startNode node type is wrong!");
        Assert.Assert(((PlatonicPrimitiveInstanceNode)startNode).primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode), "startNode is not a line!");

        PlatonicPrimitiveInstanceNode thisLine = (PlatonicPrimitiveInstanceNode)startNode;
        
        ArrayRealVector diff = thisLine.p1.subtract(thisLine.p2);
        
        if( diff.getDataRef()[0] == 0.0f ) {
            lineSegmentSlope = Float.POSITIVE_INFINITY;
        }
        else {
            lineSegmentSlope = diff.getDataRef()[1] / diff.getDataRef()[0];
        }

        FeatureNode createdLineSlope = FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode, lineSegmentSlope, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode));
        
        Link createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdLineSlope);
        thisLine.outgoingLinks.add(createdLink);
        
        return new RunResult(false);
    }
}
