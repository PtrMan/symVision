package FargGeneral.network;

import FargGeneral.network.Link;
import java.util.ArrayList;

public abstract class Node
{
    public int type; // int because we don't know what types there can be in the specific impl., in the iml its a enum which gets casted to int
    
    public float activiation; // [0.0, 1.0)
    public float activationDelta;
    
    public int conceptualDepth; // control decayrate
    
    public ArrayList<Link> outgoingLinks = new ArrayList<Link>();
    //public ArrayList<Link> incommingLinks = new ArrayList<Link>(); // only bidirection links are in here
    
    public Node(int type)
    {
        this.type = type;
    }
    
    public void resetActivationDelta()
    {
        activationDelta = 0.0f;
    }
    
    public void addActivationDelta()
    {
        activiation += activationDelta;
    }
}
