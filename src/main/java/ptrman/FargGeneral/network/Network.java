/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.FargGeneral.network;

//import com.gs.collections.impl.list.mutable.FastList;

import org.eclipse.collections.impl.list.mutable.FastList;

import java.util.Collection;

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
    public interface ILinkCreator {
        Link createLink(Link.EnumType type, Node target);
    }
    public ILinkCreator linkCreator;
    
    public final Collection<Node> nodes = new FastList<>();
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
        for( final var iterationNode : nodes )
        {

            final var decayRate = depthToDecay.translateDepthToDecayrate(iterationNode.conceptualDepth);
            iterationNode.activation *= (1.0f - decayRate);
        }
    }
    
    private void resetActivationDelta()
    {
        for( final var iterationNode : nodes )
            iterationNode.resetActivationDelta();
    }
    
    private void spreadActivationInternal()
    {
        for( final var iterationNode : nodes ) {

            final var sourceNodeActivation = iterationNode.activation;
            
            for( final var iterationLink : iterationNode.out())
                iterationLink.target.activationDelta += (iterationLink.strength * sourceNodeActivation);
        }
    }
    
    private void addActivationDelta() {
        for( final var n : nodes )
            n.addActivationDelta();
    }
    
    private void limitActivation()
    {
        for( final var n : nodes )
            n.activation = Math.min(n.activation, 1.0f);
    }
    

}
