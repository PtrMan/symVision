package bpsolver.ltm;

import FargGeneral.network.Link;
import FargGeneral.network.Network;

public class LinkCreator implements Network.ILinkCreator{
    @Override
    public Link createLink(Link.EnumType type) {
        return new bpsolver.ltm.Link(type);
    }
    
}
