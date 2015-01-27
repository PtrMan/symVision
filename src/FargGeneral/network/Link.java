package FargGeneral.network;

public class Link
{
    public Link(EnumType type)
    {
        this.type = type;
    }
    
    public float strength; // [0.0, 1.0]
    public Node target;
    
    //public Node source; // can be null for unidirectional edges
        
    public Node[] hyperNodes;
    
    /*public boolean isBidirectional()
    {
        return this.source != null;
    }*/
    
    public EnumType type;
    
    public enum EnumType
    {
        ISPARTOF,
        CONTAINS,
        HAS, // used for (platonic) features of platonic primitives in ltm
        HASATTRIBUTE,
        ISA // "is a"
    }
}
