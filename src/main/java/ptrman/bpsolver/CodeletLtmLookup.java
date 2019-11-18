/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver;

import ptrman.FargGeneral.Coderack;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import ptrman.misc.Assert;

/**
 *
 * for a given PlatonicPrimitiveNode (in ltm) it looks up the PlatonicPrimiveNodes which are the features, and looks if there are codelets registered
 * if so, it places the codelets in the coderack with the assigned priority
 */
public class CodeletLtmLookup
{
    public static class RegisterEntry
    {
        public static class CodeletInformation
        {
            public CodeletInformation(SolverCodelet templateCodelet, float priority)
            {
                this.templateCodelet = templateCodelet;
                this.priority = priority;
            }
            
            public final SolverCodelet templateCodelet; // codelet which is cloned and then placed on the coderack with the priority
            public final float priority; // priority of the codelet
        }
        
        public final ArrayList<CodeletInformation> codeletInformations = new ArrayList<>();
    }
    
    public void lookupAndPutCodeletsAtCoderackForPrimitiveNode(Node node, Coderack coderack, Network ltm, NetworkHandles networkHandles)
    {
        PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode;
        PlatonicPrimitiveNode currentLtmNodeForPrimitiveNode;
        
        // lookup ltmNodeForNode
        Assert.Assert(node.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal(), "Must be a PLATONICPRIMITIVEINSTANCENODE node");
        
        platonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)node;
        currentLtmNodeForPrimitiveNode = platonicPrimitiveInstanceNode.primitiveNode;
        

        
        for(;;)
        {
            boolean continueIsaRelationshipWalk;
            
            // we search for all "HAS" nodes and instantiate the codelets
            
            for( Link iterationLink : currentLtmNodeForPrimitiveNode.outgoingLinks )
            {
                PlatonicPrimitiveNode currentAttributePrimitiveNode;
                RegisterEntry registerEntry;

                if( iterationLink.type != ptrman.FargGeneral.network.Link.EnumType.HASFEATURE )
                {
                    continue;
                }

                if( iterationLink.target.type != NodeTypes.EnumType.PLATONICPRIMITIVENODE.ordinal() )
                {
                    continue;
                }
                // we are here if the link is HAS and the type of the linked node is PLATONICPRIMITIVENODE

                currentAttributePrimitiveNode = (PlatonicPrimitiveNode)iterationLink.target;
                
                // try to lookup the codelet
                if( currentAttributePrimitiveNode.codeletKey == null )
                {
                    continue;
                }

                registerEntry = registry.get(currentAttributePrimitiveNode.codeletKey);

                instantiateAllCodeletsForWorkspaceNode(node, coderack, registerEntry.codeletInformations);
            }
            
            continueIsaRelationshipWalk = false;
            
            // search for a ISA node
            for( Link iterationLink : currentLtmNodeForPrimitiveNode.outgoingLinks )
            {
                if( iterationLink.type == Link.EnumType.ISA )
                {
                    Assert.Assert(iterationLink.target.type == NodeTypes.EnumType.PLATONICPRIMITIVENODE.ordinal(), "must be a PlatonicPrimitiveNode");
                    currentLtmNodeForPrimitiveNode = (PlatonicPrimitiveNode)iterationLink.target;

                    continueIsaRelationshipWalk = true;
                    break;
                }
            }
            
            if( !continueIsaRelationshipWalk )
            {
                return;
            }
        }
    }
    
    private static void instantiateAllCodeletsForWorkspaceNode(Node workspaceNode, Coderack coderack, Iterable<RegisterEntry.CodeletInformation> codeletInformations)
    {
        for( RegisterEntry.CodeletInformation iterationCodeletInformation : codeletInformations )
        {
            SolverCodelet clonedCodelet;
            
            clonedCodelet = iterationCodeletInformation.templateCodelet.cloneObject();
            clonedCodelet.setStartNode(workspaceNode);
            
            coderack.enqueue(clonedCodelet, iterationCodeletInformation.priority);
        }
    }
    
    public final AbstractMap<String, RegisterEntry> registry = new HashMap<>();
    
    /*
    // NOTE< should be moved maybe into its own class >
    public class LookupAcceleratorForPlatonicNodeTypes
    {
        public AbstractMap<String, 
    }
    
    public LookupAcceleratorForPlatonicNodeTypes lookupAccelerator = new LookupAcceleratorForPlatonicNodeTypes();
    */
}
