/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.codelets;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.HelperFunctions;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.misc.Assert;

/**
 * calculates endpoints of a line segment or a curve
 * must be called only once!
 */
public class EndPoint extends SolverCodelet {
    public EndPoint(Solver bpSolver) {
        super(bpSolver);
    }

    @Override
    public void initialize() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SolverCodelet cloneObject() {
        return new EndPoint(bpSolver);
    }

    @Override
    public RunResult run() {
        Assert.Assert(startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "must be platonic instance node");
        final PlatonicPrimitiveInstanceNode startNodeAsPlatonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)startNode;
        
        final ArrayRealVector[] endPoints = calculateEndpointsOfPlatonicPrimitiveInstanceNode(startNodeAsPlatonicPrimitiveInstanceNode);
        
        for( int endpointI = 0; endpointI < 2; endpointI++ ) {
            final PlatonicPrimitiveInstanceNode createdEndpointInstanceNode = HelperFunctions.createVectorAttributeNode(endPoints[endpointI], getNetworkHandles().endpointPlatonicPrimitiveNode, bpSolver);
            final Link linkToEndpoint = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdEndpointInstanceNode);
            startNode.out(linkToEndpoint);
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
