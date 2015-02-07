package bpsolver;

import FargGeneral.Coderack;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import bpsolver.nodes.PlatonicPrimitiveNode;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import misc.Assert;

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
            
            public SolverCodelet templateCodelet; // codelet which is cloned and then placed on the coderack with the priority
            public float priority; // priority of the codelet 
        }
        
        public ArrayList<CodeletInformation> codeletInformations = new ArrayList<CodeletInformation>();
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

                if( iterationLink.type != FargGeneral.network.Link.EnumType.HASFEATURE )
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
    
    private static void instantiateAllCodeletsForWorkspaceNode(Node workspaceNode, Coderack coderack, ArrayList<RegisterEntry.CodeletInformation> codeletInformations)
    {
        for( RegisterEntry.CodeletInformation iterationCodeletInformation : codeletInformations )
        {
            SolverCodelet clonedCodelet;
            
            clonedCodelet = iterationCodeletInformation.templateCodelet.cloneObject();
            clonedCodelet.setStartNode(workspaceNode);
            
            coderack.enqueue(clonedCodelet, iterationCodeletInformation.priority);
        }
    }
    
    public AbstractMap<String, RegisterEntry> registry = new HashMap<String, RegisterEntry>();
    
    /*
    // NOTE< should be moved maybe into its own class >
    public class LookupAcceleratorForPlatonicNodeTypes
    {
        public AbstractMap<String, 
    }
    
    public LookupAcceleratorForPlatonicNodeTypes lookupAccelerator = new LookupAcceleratorForPlatonicNodeTypes();
    */
}
