/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.codelets;

import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.NodeTypes;

import java.util.Random;

public class Numerosity extends SolverCodelet {
    public Numerosity(Solver bpSolver)
    {
        super(bpSolver);
    }
    
    @Override
    public RunResult run() {
        // TODO< recode >
        /*
        
        HashMap<Database.Node.EnumType, Database.NumerosityNode> existingNumerosityNodes;
        ArrayList<Database.Node> numberableNodes;
        
        // choose random non special edge
        //  for that we sort out the nonspecial edges and search at the same time for a special edge which connects to an numerosity node
        
        numberableNodes = new ArrayList<>();
        existingNumerosityNodes = new HashMap<>();
        
        for( Database.Edge iterationEdge : startNode.outgoingEdges )
        {
            if( iterationEdge.metaAcceleration )
            {
                if( iterationEdge.destination.type == Database.Node.EnumType.NUMEROSITY )
                {
                    Database.NumerosityNode destinationAsNumerosityNode;
                    
                    destinationAsNumerosityNode = (Database.NumerosityNode)iterationEdge.destination;
                    
                    existingNumerosityNodes.put(destinationAsNumerosityNode.numerosityType, destinationAsNumerosityNode);
                }
            }
            else
            {
                if( isNodeNumberable(iterationEdge.destination) )
                {
                    numberableNodes.add(iterationEdge.destination);
                }
            }
        }
        
        // pick random numberable node
        // if it has no numerosity attached increment it, else create a new one and link it into the graph and into the Node
        if( numberableNodes.isEmpty() )
        {
            return new RunResult(true);
        }
        
        int numberableIndex;
        
        numberableIndex = random.nextInt(numberableNodes.size()-1);
        Database.Node currentNumberableNode = numberableNodes.get(numberableIndex);
        
        if( existingNumerosityNodes.containsKey(currentNumberableNode.type) )
        {
            Database.NumerosityNode currentNumerosityNode;
            
            currentNumerosityNode = existingNumerosityNodes.get(currentNumberableNode.type);
            currentNumerosityNode.numerosity++;
            
            currentNumberableNode.outgoingEdges.add(new Database.Edge(currentNumerosityNode));
        }
        else
        {
            Database.NumerosityNode createNumerosityNode;
            Database.Edge createdEdgeFromStartNode;
            Database.Edge createdEdgeFromCurrentNode;
            
            // create new numerosity node and link
            
            createNumerosityNode = new Database.NumerosityNode(currentNumberableNode.type);
            
            createdEdgeFromStartNode = new Database.Edge(createNumerosityNode);
            createdEdgeFromStartNode.metaAcceleration = true;
            startNode.outgoingEdges.add(createdEdgeFromStartNode);
            
            createdEdgeFromCurrentNode = new Database.Edge(createNumerosityNode);
            currentNumberableNode.outgoingEdges.add(createdEdgeFromCurrentNode);
            
            database.nodes.add(createNumerosityNode);
        }
        */
        
        return new RunResult(true);
    }
    
    
    @Override
    public void initialize()
    {
        random = new Random();
    }
    
    
    @Override
    public SolverCodelet cloneObject() {
        throw new RuntimeException(); // not implemented
    }
    
    
    private static boolean isNodeNumberable(Node node)
    {
        return node.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal();
    }
    
    private Random random;
    

}
