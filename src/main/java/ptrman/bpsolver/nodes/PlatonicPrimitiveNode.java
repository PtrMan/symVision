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

import ptrman.FargGeneral.network.Node;

/**
 *
 * Node in the ltm for storing a type of a primitive (such as line segment, point, curve etc.) or a attribute of it (line length, etc)
 * see Foundalis dissertation chapter 7.1
 * see Foundalis dissertation page 141 for an explaination of the types
 */
public class PlatonicPrimitiveNode extends Node
{
    public PlatonicPrimitiveNode(String platonicType, String codeletKey)
    {
        super(NodeTypes.EnumType.PLATONICPRIMITIVENODE.ordinal());
        
        this.platonicType = platonicType;
        this.codeletKey = codeletKey;
    }
    
    public String platonicType; // human readable text of type, is not used for comparision
    public String codeletKey; // key of the codelet to put on the coderack and run to calculate the feature of a node, can be null if there is no feature for it
    
    public boolean isAbstract = false; // abstract nodes are used for common properties/connections in the LTM, in LTM and STM can't appear nodes of abstract PlantonicPrimitiveNOde
}
