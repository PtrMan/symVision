package ptrman.bpsolver.nodes;

import ptrman.FargGeneral.network.Node;

/**
 *
 * used for types of primitives or other labels
 */
public class AttributeNode extends Node
{
    
    public enum EnumValueType
    {
        INT,
        FLOAT
    }
    
    public static AttributeNode createFloatNode(Node attributeTypeNode, float value)
    {
        return new AttributeNode(attributeTypeNode, EnumValueType.FLOAT, value, 0);
    }
    
    public static AttributeNode createIntegerNode(Node attributeTypeNode, int value)
    {
        return new AttributeNode(attributeTypeNode, EnumValueType.INT, 0.0f, value);
    }
    
    /**
     * 
     * \param attributeTypeNode is the node in ltm which holds the type of the attribute
     * \param valueType
     * \param valueFloat
     * \param valueInt 
     */
    private AttributeNode(Node attributeTypeNode, EnumValueType valueType, float valueFloat, int valueInt)
    {
        super(NodeTypes.EnumType.ATTRIBUTENODE.ordinal());
        
        this.valueType = valueType;
        this.valueFloat = valueFloat;
        this.valueInt = valueInt;
        
        this.attributeTypeNode = attributeTypeNode;
    }
    
    public Node attributeTypeNode;
    
    
    public float getValueAsFloat()
    {
        if( valueType != AttributeNode.EnumValueType.FLOAT )
        {
            throw new RuntimeException("Non float queried!");
        }
        
        return valueFloat;
    }
    
    public int getValueAsInt()
    {
        if( valueType != AttributeNode.EnumValueType.INT )
        {
            throw new RuntimeException("Non int queried!");
        }
        
        return valueInt;
    }
    
    private float valueFloat;
    private int valueInt;
    private AttributeNode.EnumValueType valueType;
}
