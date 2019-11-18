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
    
    public final Node attributeTypeNode;
    
    
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
    
    private final float valueFloat;
    private final int valueInt;
    private final AttributeNode.EnumValueType valueType;
}
