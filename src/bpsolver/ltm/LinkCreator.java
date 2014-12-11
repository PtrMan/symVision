package bpsolver.ltm;

import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;

public class LinkCreator implements Network.ILinkCreator{
    @Override
    public Link createLink(Link.EnumType type, Node target) {
        bpsolver.ltm.Link createdLink;
        
        createdLink = new bpsolver.ltm.Link(type);
        createdLink.target = target;
        
        return createdLink;
    }
    
}
