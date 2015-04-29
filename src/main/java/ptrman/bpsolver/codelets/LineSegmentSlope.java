package ptrman.bpsolver.codelets;

import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.misc.Assert;

/**
 *
 *
 */
public class LineSegmentSlope extends SolverCodelet
{
    public LineSegmentSlope(BpSolver bpSolver)
    {
        super(bpSolver);
    }
    
    @Override
    public void initialize()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject()
    {
        return new LineSegmentSlope(bpSolver);
    }

    @Override
    public RunResult run()
    {
        PlatonicPrimitiveInstanceNode thisLine;
        FeatureNode createdLineSlope;
        float lineSegmentSlope;
        Link createdLink;
        Vector2d<Float> diff;
        
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "startNode node type is wrong!");
        Assert.Assert(((PlatonicPrimitiveInstanceNode)startNode).primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode), "startNode is not a line!");
        
        thisLine = (PlatonicPrimitiveInstanceNode)startNode;
        
        diff = Vector2d.FloatHelper.sub(thisLine.p1, thisLine.p2);
        
        if( diff.x == 0.0f )
        {
            lineSegmentSlope = Float.POSITIVE_INFINITY;
        }
        else
        {
            lineSegmentSlope = diff.y / diff.x;
        }
        
        createdLineSlope = FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode, lineSegmentSlope, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode));
        
        createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdLineSlope);
        thisLine.outgoingLinks.add(createdLink);
        
        return new RunResult(false);
    }
}
