package ptrman.bpsolver.codelets;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.HelperFunctions;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.misc.Assert;

import java.util.List;

import static ptrman.math.ArrayRealVectorHelper.getScaled;

// NOTE< a problem could be that the endpoint positions of the line segments are handled per line, and not as a whole >
// to fix this, we need to have a reference to the dots/points of a line, and union them
/**
 *
 * calculates the barycenter (center of gravity) of an object
 */
public class BaryCenter extends SolverCodelet {
    public enum EnumRecalculate {
        YES,
        NO
    }
    
    public BaryCenter(Solver bpSolver, EnumRecalculate recalculate) {
        super(bpSolver);
        this.recalculate = recalculate;
    }
    
    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        BaryCenter cloned;
        
        cloned = new BaryCenter(bpSolver, recalculate);
        
        return cloned;
    }

    @Override
    public RunResult run() {
        if( hasObjectAlreadyABaryCenter() && recalculate == EnumRecalculate.NO ) {
            return new RunResult(false);
        }
        
        final ArrayRealVector calculatedBaryCenter = calculateBaryCenter();
        
        // now we store the calculated barycenter
        if( hasObjectAlreadyABaryCenter() ) {
            PlatonicPrimitiveInstanceNode baryCenterInstanceNode;
            
            baryCenterInstanceNode = getBaryCenterNodeOfObject();
            Assert.Assert(baryCenterInstanceNode != null, "");
            
            // update the coordinate nodes
            
            for( Link iterationLink : baryCenterInstanceNode.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {
                if( iterationLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() ) {
                    continue;
                }
                
                final FeatureNode targetFeatureNode = (FeatureNode)iterationLink.target;
                
                if( targetFeatureNode.featureTypeNode.equals(getNetworkHandles().xCoordinatePlatonicPrimitiveNode) ) {
                    targetFeatureNode.setValueAsFloat(calculatedBaryCenter.getDataRef()[0]);
                }
                else if( targetFeatureNode.featureTypeNode.equals(getNetworkHandles().yCoordinatePlatonicPrimitiveNode) ) {
                    targetFeatureNode.setValueAsFloat(calculatedBaryCenter.getDataRef()[1]);
                }
                // else ignore
            }
        }
        else {
            // NOTE< we don't add the Nodes to the list here, because this makes managment more easy >
            
            PlatonicPrimitiveInstanceNode createdBaryCenterInstanceNode;
            FeatureNode createdCoordinateXNode, createdCoordinateYNode;
            Link linkToXNode, linkToYNode;
            Link linkToBaryCenter;

            // create barycenter node and link it to the object
            // add the coordinate nodes
            
            createdBaryCenterInstanceNode = HelperFunctions.createVectorAttributeNode(calculatedBaryCenter, getNetworkHandles().barycenterPlatonicPrimitiveNode, bpSolver);
            linkToBaryCenter = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdBaryCenterInstanceNode);
            startNode.outgoingLinks.add(linkToBaryCenter);
            
        }
        
        return new RunResult(recalculate == EnumRecalculate.YES);
    }

    private boolean hasObjectAlreadyABaryCenter() {
        return getBaryCenterNodeOfObject() != null;
    }
    
    // returns null if the object has no BaryCenter
    private PlatonicPrimitiveInstanceNode getBaryCenterNodeOfObject() {
        final List<Link>  linksOfObject = startNode.getLinksByType(Link.EnumType.HASATTRIBUTE);
        for( Link iterationLink : linksOfObject ) {
            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNodeOfTarget;
            
            if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
                continue;
            }
            // is platonic primitive instance node
            
            platonicPrimitiveInstanceNodeOfTarget = (PlatonicPrimitiveInstanceNode)iterationLink.target;
            
            if( !platonicPrimitiveInstanceNodeOfTarget.primitiveNode.equals(getNetworkHandles().barycenterPlatonicPrimitiveNode) ) {
                continue;
            }
            
            return platonicPrimitiveInstanceNodeOfTarget;
        }
        
        return null;
    }
    
    private ArrayRealVector calculateBaryCenter() {
        float weight = 0.0f;

        ArrayRealVector baryCenter = new ArrayRealVector(new double[]{0.0, 0.0});
        
        final List<Link> linksOfObject = startNode.getLinksByType(Link.EnumType.CONTAINS);
        for( Link iterationLink : linksOfObject ) {
            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNodeOfTarget;
            
            if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
                continue;
            }
            // is platonic primitive instance node
            
            platonicPrimitiveInstanceNodeOfTarget = (PlatonicPrimitiveInstanceNode)iterationLink.target;
            
            if( platonicPrimitiveInstanceNodeOfTarget.primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode) ) {
                final ArrayRealVector centerOfLine = getScaled(platonicPrimitiveInstanceNodeOfTarget.p1.add(platonicPrimitiveInstanceNodeOfTarget.p2), 0.5);
                
                if( weight == 0.0f ) {
                    baryCenter = centerOfLine;
                    weight = 1.0f;
                }
                else {
                    ArrayRealVector tempVector = baryCenter.add(getScaled(centerOfLine, 1.0f / weight));
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
