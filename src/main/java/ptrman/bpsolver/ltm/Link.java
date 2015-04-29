package ptrman.bpsolver.ltm;

/**
 * link implementation for ltm
 * 
 */
public class Link extends ptrman.FargGeneral.network.Link
{
    public Link(ptrman.FargGeneral.network.Link.EnumType type)
    {
        super(type);
    }
    
    // from phaeaco disertation page 249
    // links can have their elasticity permanently modified
    // all the more the links shirk and extend rapidly
    public float permanentlyElasticity;
}
