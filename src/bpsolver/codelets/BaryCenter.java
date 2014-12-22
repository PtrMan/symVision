package bpsolver.codelets;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import bpsolver.NetworkHandles;
import bpsolver.SolverCodelet;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import java.util.ArrayList;
import misc.Assert;

// NOTE< a problem could be that the endpoint positions of the line segments are handled per line, and not as a whole >
// to fix this, we need to have a reference to the dots/points of a line, and union them
/**
 *
 * calculates the barycenter (center of gravity) of an object
 */
public class BaryCenter extends SolverCodelet
{
    public enum EnumRecalculate
    {
        YES,
        NO
    }
    
    public BaryCenter(Network network, NetworkHandles networkHandles, EnumRecalculate recalculate)
    {
        super(network, networkHandles);
        this.recalculate = recalculate;
    }
    
    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        BaryCenter cloned;
        
        cloned = new BaryCenter(network, networkHandles, recalculate);
        
        return cloned;
    }

    @Override
    public RunResult run()
    {
        Vector2d<Float> calculatedBaryCenter;
        
        if( hasObjectAlreadyABaryCenter() && recalculate == EnumRecalculate.NO )
        {
            return new RunResult(false);
        }
        
        calculatedBaryCenter = calculateBaryCenter();
        
        // now we store the calculated barycenter
        if( hasObjectAlreadyABaryCenter() )
        {
            PlatonicPrimitiveInstanceNode baryCenterInstanceNode;
            
            baryCenterInstanceNode = getBaryCenterNodeOfObject();
            Assert.Assert(baryCenterInstanceNode != null, "");
            
            // update the coordinate nodes
            
            for( Link iterationLink : baryCenterInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE) )
            {
                FeatureNode targetFeatureNode;
                
                if( iterationLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() )
                {
                    continue;
                }
                
                targetFeatureNode = (FeatureNode)iterationLink.target;
                
                if( targetFeatureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) )
                {
                    targetFeatureNode.setValueAsFloat(calculatedBaryCenter.x);
                }
                else if( targetFeatureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) )
                {
                    targetFeatureNode.setValueAsFloat(calculatedBaryCenter.y);
                }
                // else ignore
            }
        }
        else
        {
            // NOTE< we don't add the Nodes to the list here, because this makes managment more easy >
            
            PlatonicPrimitiveInstanceNode createdBaryCenterInstanceNode;
            FeatureNode createdCoordinateXNode, createdCoordinateYNode;
            Link linkToXNode, linkToYNode;
            Link linkToBaryCenter;

            // create barycenter node and link it to the object
            // add the coordinate nodes
            
            createdBaryCenterInstanceNode = new PlatonicPrimitiveInstanceNode(networkHandles.barycenterPlatonicPrimitiveNode);
            linkToBaryCenter = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdBaryCenterInstanceNode);
            startNode.outgoingLinks.add(linkToBaryCenter);
            
            createdCoordinateXNode = FeatureNode.createFloatNode(networkHandles.xCoordinatePlatonicPrimitiveNode, calculatedBaryCenter.x, 1);
            createdCoordinateYNode = FeatureNode.createFloatNode(networkHandles.yCoordinatePlatonicPrimitiveNode, calculatedBaryCenter.y, 1);
            
            linkToXNode = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdCoordinateXNode);
            createdBaryCenterInstanceNode.outgoingLinks.add(linkToXNode);
            
            linkToYNode = network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdCoordinateYNode);
            createdBaryCenterInstanceNode.outgoingLinks.add(linkToYNode);
        }
        
        return new RunResult(recalculate == EnumRecalculate.YES);
    }
    
    private boolean hasObjectAlreadyABaryCenter()
    {
        return getBaryCenterNodeOfObject() != null;
    }
    
    // returns null if the object has no BaryCenter
    private PlatonicPrimitiveInstanceNode getBaryCenterNodeOfObject()
    {
        ArrayList<Link> linksOfObject;
        
        linksOfObject = startNode.getLinksByType(Link.EnumType.HASATTRIBUTE);
        
        for( Link iterationLink : linksOfObject )
        {
            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNodeOfTarget;
            
            if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
            {
                continue;
            }
            // is platonic primitive instance node
            
            platonicPrimitiveInstanceNodeOfTarget = (PlatonicPrimitiveInstanceNode)iterationLink.target;
            
            if( !platonicPrimitiveInstanceNodeOfTarget.primitiveNode.equals(networkHandles.barycenterPlatonicPrimitiveNode) )
            {
                continue;
            }
            
            return platonicPrimitiveInstanceNodeOfTarget;
        }
        
        return null;
    }
    
    private Vector2d<Float> calculateBaryCenter()
    {
        Vector2d<Float> baryCenter;
        float weight = 0.0f;
        ArrayList<Link> linksOfObject;
        
        baryCenter = new Vector2d<Float>(0.0f, 0.0f);
        
        linksOfObject = startNode.getLinksByType(Link.EnumType.HASATTRIBUTE);
        
        for( Link iterationLink : linksOfObject )
        {
            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNodeOfTarget;
            
            if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
            {
                continue;
            }
            // is platonic primitive instance node
            
            platonicPrimitiveInstanceNodeOfTarget = (PlatonicPrimitiveInstanceNode)iterationLink.target;
            
            if( platonicPrimitiveInstanceNodeOfTarget.primitiveNode.equals(networkHandles.lineSegmentPlatonicPrimitiveNode) )
            {
                Vector2d<Float> centerOfLine;
                
                centerOfLine = getScaled(add(platonicPrimitiveInstanceNodeOfTarget.p1, platonicPrimitiveInstanceNodeOfTarget.p2), 0.5f);
                
                if( weight == 0.0f )
                {
                    baryCenter = centerOfLine;
                    weight = 1.0f;
                }
                else
                {
                    Vector2d<Float> tempVector;
                    
                    tempVector = add(baryCenter, getScaled(centerOfLine, 1.0f/weight));
                    tempVector = getScaled(tempVector, 1.0f/(weight+1.0f));
                    
                    weight += 1.0f;
                    baryCenter = tempVector;
                }
            }
            // TODO< points >
            // TODO< curves >
        }
        
        return baryCenter;
    }
    
    private EnumRecalculate recalculate;
}
