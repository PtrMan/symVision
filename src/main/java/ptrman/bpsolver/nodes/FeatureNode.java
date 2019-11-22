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
import ptrman.bpsolver.FeatureStatistics;

/**
 * a feature is something like lineslope or linelength
 * foundalis dissertation page 143
 */
public class FeatureNode extends Node {
    public static FeatureNode createFloatNode(final Node featureTypeNode, final double value, final int weight, final float primitiveFeatureMax)
    {
        return new FeatureNode(featureTypeNode, EnumValueType.FLOAT, value, 0, weight, primitiveFeatureMax);
    }
    
    public static FeatureNode createIntegerNode(final Node featureTypeNode, final int value, final int weight, final float primitiveFeatureMax)
    {
        return new FeatureNode(featureTypeNode, EnumValueType.INT, 0.0f, value, weight, primitiveFeatureMax);
    }
    
    /**
     * 
     * \param featureTypeNode is the node in ltm which holds the type of the feature
     * \param valueType
     * \param valueFloat
     * \param valueInt 
     */
    private FeatureNode(final Node featureTypeNode, final EnumValueType valueType, final double valueFloat, final int valueInt, final int weight, final float primitiveFeatureMax)
    {
        super(NodeTypes.EnumType.FEATURENODE.ordinal());
        
        this.valueType = valueType;
        this.valueFloat = valueFloat;
        this.valueInt = valueInt;
        
        this.featureTypeNode = featureTypeNode;
        
        this.weight = weight;

        if( valueType == EnumValueType.FLOAT )
            this.statistics.addValue((float) valueFloat);
        else
            this.statistics.addValue(valueInt);

        this.statistics.primitiveFeatureMax = primitiveFeatureMax;
    }
    
    public double getValueAsFloat()
    {
        assert valueType == EnumValueType.FLOAT : "Non float queried!";
        
        return valueFloat;
    }
    
    public int getValueAsInt()
    {
        assert valueType == EnumValueType.INT : "Non int queried!";
        
        return valueInt;
    }
    
    public void setValueAsFloat(final double value)
    {
        valueType = EnumValueType.FLOAT;
        valueFloat = value;
    }
    
    public void setValueAsInt(final int value)
    {
        valueType = EnumValueType.INT;
        valueInt = value;
    }
    
    public int getWeight()
    {
        return weight;
    }
    
    public final Node featureTypeNode; // node in ltm, which is either a platonic primitive node (with the type of a feature) or a node which is a learned type (triangle, etc)
    // is compared by reference (isEqual), because the node can be of any valid type
    
    private double valueFloat;
    private int valueInt;
    private EnumValueType valueType;
    
    private final int weight; // used for fair fusing of nodes

    public final FeatureStatistics statistics = new FeatureStatistics();

    public enum EnumValueType
    {
        INT,
        FLOAT
    }
}
