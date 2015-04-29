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
 * calculates the length of a LineSegment
 */
public class LineSegmentLength extends SolverCodelet
{
    public LineSegmentLength(BpSolver bpSolver)
    {
        super(bpSolver);
    }
    
    @Override
    public RunResult run()
    {
        PlatonicPrimitiveInstanceNode thisLine;
        FeatureNode createdLineSegmentLength;
        float lineSegmentLength;
        Link createdLink;
        
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "startNode node type is wrong!");
        Assert.Assert(((PlatonicPrimitiveInstanceNode)startNode).primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode), "startNode is not a line!");
        
        thisLine = (PlatonicPrimitiveInstanceNode)startNode;
        
        lineSegmentLength = Vector2d.FloatHelper.getLength(Vector2d.FloatHelper.sub(thisLine.p1, thisLine.p2));
        
        createdLineSegmentLength = FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode, lineSegmentLength, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode));
        
        createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdLineSegmentLength);
        thisLine.outgoingLinks.add(createdLink);
        
        return new RunResult(false);
    }
    
    @Override
    public void initialize()
    {
        // no work here
    }
    
    @Override
    public SolverCodelet cloneObject()
    {
        LineSegmentLength cloned;
        
        cloned = new LineSegmentLength(bpSolver);
        
        return cloned;
    }
}
