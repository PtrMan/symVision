package ptrman.bpsolver.ltm;

import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;

public class LinkCreator implements Network.ILinkCreator{
    @Override
    public Link createLink(Link.EnumType type, Node target) {
        ptrman.bpsolver.ltm.Link createdLink;
        
        createdLink = new ptrman.bpsolver.ltm.Link(type);
        createdLink.target = target;
        
        return createdLink;
    }
    
}
