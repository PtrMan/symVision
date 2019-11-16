package ptrman.FargGeneral.network;

//import com.gs.collections.impl.list.mutable.FastList;

import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Collection;
import java.util.List;

/**
 *
 * slipnet
 */
public class Network
{
    public interface IDepthToDecay
    {
        float translateDepthToDecayrate(int depth);
    }
    
    // used to create links with the right type
    public interface ILinkCreator
    {
        Link createLink(Link.EnumType type, Node target);
    }
    
    public Collection<Node> nodes = new FastList<>();
    public IDepthToDecay depthToDecay;
    
    public void spreadActivation()
    {
        resetActivationDelta();
        spreadActivationInternal();
        addActivationDelta();
        limitActivation();
    }
    
    public void letAllNodesDecay()
    {
        for( Node iterationNode : nodes )
        {
            float decayRate;
            
            decayRate = depthToDecay.translateDepthToDecayrate(iterationNode.conceptualDepth);
            iterationNode.activiation *= (1.0f - decayRate);
        }
    }
    
    private void resetActivationDelta()
    {
        for( Node iterationNode : nodes )
        {
            iterationNode.resetActivationDelta();
        }
    }
    
    private void spreadActivationInternal()
    {
        for( Node iterationNode : nodes )
        {
            float sourceNodeActivation;
            
            sourceNodeActivation = iterationNode.activiation;
            
            for( Link iterationLink : iterationNode.outgoingLinks )
            {
                iterationLink.target.activationDelta += (iterationLink.strength * sourceNodeActivation);
            }
        }
    }
    
    private void addActivationDelta()
    {
        for( Node iterationNode : nodes )
        {
            iterationNode.addActivationDelta();
        }
    }
    
    private void limitActivation()
    {
        for( Node iterationNode : nodes )
        {
            iterationNode.activiation = Math.min(iterationNode.activiation, 1.0f);
        }
    }
    
    public ILinkCreator linkCreator;
}
