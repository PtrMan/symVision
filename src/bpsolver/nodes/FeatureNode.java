package bpsolver.nodes;

import FargGeneral.network.Node;
import bpsolver.FeatureStatistics;

/**
 * a feature is something like lineslope or linelength
 * foundalis dissertation page 143
 */
public class FeatureNode extends Node {
    public static FeatureNode createFloatNode(Node featureTypeNode, float value, int weight)
    {
        return new FeatureNode(featureTypeNode, EnumValueType.FLOAT, value, 0, weight);
    }
    
    public static FeatureNode createIntegerNode(Node featureTypeNode, int value, int weight)
    {
        return new FeatureNode(featureTypeNode, EnumValueType.INT, 0.0f, value, weight);
    }
    
    /**
     * 
     * \param featureTypeNode is the node in ltm which holds the type of the feature
     * \param valueType
     * \param valueFloat
     * \param valueInt 
     */
    private FeatureNode(Node featureTypeNode, EnumValueType valueType, float valueFloat, int valueInt, int weight)
    {
        super(NodeTypes.EnumType.FEATURENODE.ordinal());
        
        this.valueType = valueType;
        this.valueFloat = valueFloat;
        this.valueInt = valueInt;
        
        this.featureTypeNode = featureTypeNode;
        
        this.weight = weight;
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
    
    public void setValueAsFloat(float value)
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
    
    private float valueFloat;
    private int valueInt;
    private EnumValueType valueType;
    
    private int weight; // used for fair fusing of nodes
    
    // TODO< set with constructor >
    public FeatureStatistics statistics;
    
    public enum EnumValueType
    {
        INT,
        FLOAT
    }
}
