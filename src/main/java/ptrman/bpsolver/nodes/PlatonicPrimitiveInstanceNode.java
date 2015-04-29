package ptrman.bpsolver.nodes;

import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.network.Node;
import ptrman.RetinaLevel.ProcessG;

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
    public Vector2d<Float> p1;
    public Vector2d<Float> p2;
    
    public ProcessG.Curve curve; // in case of a curve its the curve, can be null if it is not a curve
}
