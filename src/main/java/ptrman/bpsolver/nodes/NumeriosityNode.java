package ptrman.bpsolver.nodes;

import ptrman.FargGeneral.network.Node;

public class NumeriosityNode extends Node
{
    public NumeriosityNode(Node numerosityTypeNode)
    {
        super(NodeTypes.EnumType.NUMEROSITYNODE.ordinal());
        this.numerosityTypeNode = numerosityTypeNode;
    }
    
    public int numerosity;
    
    public Node numerosityTypeNode; // node in ltm, which is either a platonic primitive node or a node which is a learned type (triangle, etc)
    // is compared by reference (isEqual), because the node can be of any valid type
}
