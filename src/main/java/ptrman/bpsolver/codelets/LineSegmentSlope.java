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

import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;

/**
 *
 *
 */
public class LineSegmentSlope extends SolverCodelet {
    public LineSegmentSlope(final Solver bpSolver) {
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

        assert startNode.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() : "ASSERT: " + "startNode node type is wrong!";
        assert ((PlatonicPrimitiveInstanceNode)startNode).primitiveNode.equals(getNetworkHandles().lineSegmentPlatonicPrimitiveNode) : "ASSERT: " + "startNode is not a line!";

        final var thisLine = (PlatonicPrimitiveInstanceNode)startNode;
        
        final var diff = thisLine.p1.subtract(thisLine.p2);

        final var lineSegmentSlope = diff.getDataRef()[0] == 0.0f ? Float.POSITIVE_INFINITY : diff.getDataRef()[1] / diff.getDataRef()[0];

        final var createdLineSlope = FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode, lineSegmentSlope, 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineSlopePrimitiveNode));
        
        final var createdLink = getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdLineSlope);
        thisLine.out(createdLink);
        
        return new RunResult(false);
    }
}
