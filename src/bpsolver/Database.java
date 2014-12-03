package bpsolver;

import java.util.ArrayList;

/**
 *
 * graph based database
 */
public class Database {
    public static abstract class Node
    {
        public ArrayList<Edge> outgoingEdges = new ArrayList<Edge>();
        public ArrayList<Edge> incommingEdges = new ArrayList<Edge>();
        
        public enum EnumType
        {
            LINE,
            OBJECT,
            BOX,
            NUMEROSITY,
            LENGTH
        }
        
        public EnumType type;
        
        public Node(EnumType type)
        {
            this.type = type;
        }
    }
    
    public static class NumerosityNode extends Node
    {
        public NumerosityNode(Node.EnumType numerosityType)
        {
            super(Node.EnumType.NUMEROSITY);
            this.numerosityType = numerosityType;
        }
        
        public Node.EnumType numerosityType;
        public int numerosity;
    }
    
    public static class LengthNode extends Node
    {
        public LengthNode(float length, int weight)
        {
            super(Node.EnumType.LENGTH);
            this.length = length;
            weight = weight;
        }
        
        public float getWeightAsFloat()
        {
            return (float)weight;
        }
        
        public int getWeight()
        {
            return weight;
        }
        
        public float length;
        private int weight; // how many times were the lengths combined
    }
    
    public static class Edge
    {
        public Edge(Node destination)
        {
            this.destination = destination;
        }
        
        public boolean isBidirectional()
        {
            return this.source != null;
        }
        
        public Node destination;
        public Node source; // can be null for unidirectional edges
        
        public Node[] hyperNodes;
        
        // is the edge a acceleration edge (for a faster more direct access)
        public boolean metaAcceleration;
        
        
    }
    
    public ArrayList<Node> nodes = new ArrayList<Node>();
}
