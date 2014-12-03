package bpsolver.codelets;

import bpsolver.Codelet;
import bpsolver.Database;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class Numerosity extends Codelet {
    // startnode must be a object node
    public Numerosity(Database database, Database.Node startNode)
    {
        this.database = database;
        this.startNode = startNode;
    }
    
    @Override
    public RunResult run() {
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
        
        return new RunResult(true);
    }
    
    private static boolean isNodeNumberable(Database.Node node)
    {
        // TODO< do this dynamically >
        return node.type == Database.Node.EnumType.LINE;
    }
    
    private Database.Node startNode;
    private Database database;
    private Random random = new Random();
}
