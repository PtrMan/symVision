/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.nodes;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.FargGeneral.network.Node;
import ptrman.levels.retina.ProcessG;

/**
 * is a instance of a platonic primitive, for example a line or a curve
 * 
 */
public class PlatonicPrimitiveInstanceNode extends Node
{
    public PlatonicPrimitiveInstanceNode(PlatonicPrimitiveNode primitiveNode)
    {
        super(NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal());
        
        this.primitiveNode = primitiveNode;
    }
    
    public PlatonicPrimitiveNode primitiveNode;
    
    // used to store data
    // linesegment : uses p1 and p2
    // point       : uses p1
    public ArrayRealVector p1;
    public ArrayRealVector p2;
    
    public ProcessG.Curve curve; // in case of a curve its the curve, can be null if it is not a curve
}
