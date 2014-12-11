package bpsolver.nodes;

import FargGeneral.network.Node;

/**
 * a feature is something like lineslope or linelength
 * foundalis dissertation page 143
 */
public class FeatureNode extends Node {
    /**
     * 
     * \param featureTypeNode is the node in ltm which holds the type of the feature
     * \param valueType
     * \param valueFloat
     * \param valueInt 
     */
    public FeatureNode(Node featureTypeNode, EnumValueType valueType, float valueFloat, int valueInt)
    {
        super(NodeTypes.EnumType.FEATURENODE.ordinal());
        
        this.valueType = valueType;
        this.valueFloat = valueFloat;
        this.valueInt = valueInt;
        
        this.featureTypeNode = featureTypeNode;
    }
    
    public float getValueAsFloat()
    {
        if( valueType != EnumValueType.FLOAT )
        {
            throw new RuntimeException("Non float queried!");
        }
        
        return valueFloat;
    }
    
    public int getValueAsInt()
    {
        if( valueType != EnumValueType.INT )
        {
            throw new RuntimeException("Non int queried!");
        }
        
        return valueInt;
    }
    
    public Node featureTypeNode; // node in ltm, which is either a platonic primitive node (with the type of a feature) or a node which is a learned type (triangle, etc)
    // is compared by reference (isEqual), because the node can be of any valid type
    
    private float valueFloat;
    private int valueInt;
    private EnumValueType valueType;
    
    public enum EnumValueType
    {
        INT,
        FLOAT
    }
}
