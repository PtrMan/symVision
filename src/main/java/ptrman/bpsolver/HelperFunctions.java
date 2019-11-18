/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Link;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;

/**
 *
 * 
 */
public enum HelperFunctions {
	;

	public static PlatonicPrimitiveInstanceNode createVectorAttributeNode(ArrayRealVector vector, PlatonicPrimitiveNode primitiveNodeType, Solver bpSolver) {
        PlatonicPrimitiveInstanceNode createdVectorInstanceNode = new PlatonicPrimitiveInstanceNode(primitiveNodeType);
        
        final FeatureNode createdXNode = FeatureNode.createFloatNode(bpSolver.networkHandles.xCoordinatePlatonicPrimitiveNode, vector.getDataRef()[0], 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(bpSolver.networkHandles.xCoordinatePlatonicPrimitiveNode));
        final FeatureNode createdYNode = FeatureNode.createFloatNode(bpSolver.networkHandles.yCoordinatePlatonicPrimitiveNode, vector.getDataRef()[1], 1, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(bpSolver.networkHandles.yCoordinatePlatonicPrimitiveNode));
        final Link linkToXNode = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdXNode);
        createdVectorInstanceNode.out.add(linkToXNode);
        final Link linkToYNode = bpSolver.network.linkCreator.createLink(Link.EnumType.HASATTRIBUTE, createdYNode);
        createdVectorInstanceNode.out.add(linkToYNode);
        
        return createdVectorInstanceNode;
    }
    
    public static ArrayRealVector getVectorFromVectorAttributeNode(final NetworkHandles networkHandles, final PlatonicPrimitiveInstanceNode node) {
        ArrayRealVector result = new ArrayRealVector(new double[]{0.0f, 0.0f});
        
        for( Link iterationLink : node.getLinksByType(Link.EnumType.HASATTRIBUTE) ) {

			if( iterationLink.target.type != NodeTypes.EnumType.FEATURENODE.ordinal() ) {
                continue;
            }

			FeatureNode targetFeatureNode = (FeatureNode) iterationLink.target;

            if( targetFeatureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) ) {
                result.getDataRef()[0] = targetFeatureNode.getValueAsFloat();
            }
            else if( targetFeatureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) ) {
                result.getDataRef()[0] = targetFeatureNode.getValueAsFloat();
            }
            // else ignore
        }
        
        return result;
    }
}
