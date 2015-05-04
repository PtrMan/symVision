package ptrman.bpsolver.nodes;

import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.FeatureStatistics;

/**
 * a feature is something like lineslope or linelength
 * foundalis dissertation page 143
 */
public class FeatureNode extends Node {
    public static FeatureNode createFloatNode(Node featureTypeNode, double value, int weight, float primitiveFeatureMax)
    {
        return new FeatureNode(featureTypeNode, EnumValueType.FLOAT, value, 0, weight, primitiveFeatureMax);
    }
    
    public static FeatureNode createIntegerNode(Node featureTypeNode, int value, int weight, float primitiveFeatureMax)
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
    private FeatureNode(Node featureTypeNode, EnumValueType valueType, double valueFloat, int valueInt, int weight, float primitiveFeatureMax)
    {
        super(NodeTypes.EnumType.FEATURENODE.ordinal());
        
        this.valueType = valueType;
        this.valueFloat = valueFloat;
        this.valueInt = valueInt;
        
        this.featureTypeNode = featureTypeNode;
        
        this.weight = weight;

        if( valueType == EnumValueType.FLOAT )
        {
            this.statistics.addValue((float)valueFloat);
        }
        else
        {
            this.statistics.addValue(valueInt);
        }

        this.statistics.primitiveFeatureMax = primitiveFeatureMax;
    }
    
    public double getValueAsFloat()
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
    
    public void setValueAsFloat(double value)
    {
        valueType = EnumValueType.FLOAT;
        valueFloat = value;
    }
    
    public void setValueAsInt(int value)
    {
        valueType = EnumValueType.INT;
        valueInt = value;
    }
    
    public int getWeight()
    {
        return weight;
    }
    
    public Node featureTypeNode; // node in ltm, which is either a platonic primitive node (with the type of a feature) or a node which is a learned type (triangle, etc)
    // is compared by reference (isEqual), because the node can be of any valid type
    
    private double valueFloat;
    private int valueInt;
    private EnumValueType valueType;
    
    private int weight; // used for fair fusing of nodes

    public FeatureStatistics statistics = new FeatureStatistics();

    public enum EnumValueType
    {
        INT,
        FLOAT
    }
}
