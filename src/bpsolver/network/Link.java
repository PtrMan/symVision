package bpsolver.network;

public class Link
{
    public float strength; // [0.0, 1.0]
    public Node target;
    
    public Node source; // can be null for unidirectional edges
        
    public Node[] hyperNodes;
    
    public boolean isBidirectional()
    {
        return this.source != null;
    }
}
