package bpsolver.nodes;

import FargGeneral.network.Node;

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
