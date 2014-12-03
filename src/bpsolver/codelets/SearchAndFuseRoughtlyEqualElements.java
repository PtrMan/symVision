package bpsolver.codelets;

import Datastructures.Tuple4;
import bpsolver.Codelet;
import bpsolver.Database;
import bpsolver.Parameters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import math.DistinctUtility;
import static math.Math.weightFloats;

/**
 *
 * chooses two items and looks if they have a matching anotation, if so, modify the graph so they point at the same (averaged) attribute
 * 
 * see page 147 in the disertation
 * 
 * * line segments - relinks already existing LINELENGTH nodes if the length is roughtly equal
 * TODO
 * * angle - relink if they are roughtly equal
 */
public class SearchAndFuseRoughtlyEqualElements extends Codelet {
    // startnode must be a object node
    public SearchAndFuseRoughtlyEqualElements(Database database, Database.Node startNode)
    {
        this.database = database;
        this.startNode = startNode;
    }
    
    @Override
    public Codelet.RunResult run() {
        ArrayList<Integer> distinctEdgeIndices;
        Database.Node nodeA, nodeB;
        HashMap<Database.Node.EnumType, Tuple4<Database.Node, Integer, Database.Node, Integer>> commonChildnodesOfNodes;
        
        // choose two random nodes
        
        if( startNode.outgoingEdges.size() < 2 )
        {
            return new RunResult(true);
        }
        // else here
        
        distinctEdgeIndices = DistinctUtility.getTwoDisjunctNumbers(random, startNode.outgoingEdges.size());
        
        nodeA = startNode.outgoingEdges.get(distinctEdgeIndices.get(0)).destination;
        nodeB = startNode.outgoingEdges.get(distinctEdgeIndices.get(1)).destination;
        
        commonChildnodesOfNodes = getCommonChildnodesOfNodes(nodeA, nodeB);
        
        // if the two nodes don't have at least one common type which is Measurable we are done here
        if( commonChildnodesOfNodes.keySet().size() == 0 )
        {
            return new RunResult(true);
        }
        // else here
        
        
        // * select random key
        // * retrive Tuple
        // * look if the measurements are roughtly equal
        // * relink graph if they are
        Database.Node.EnumType[] typeKeysAsArray = (Database.Node.EnumType[])commonChildnodesOfNodes.keySet().toArray();
        int keyIndex = random.nextInt(typeKeysAsArray.length);
        Database.Node.EnumType chosenKeyForCommonChildnodes = typeKeysAsArray[keyIndex];
        
        Tuple4<Database.Node, Integer, Database.Node, Integer> chosenTuple = commonChildnodesOfNodes.get(chosenKeyForCommonChildnodes);
        
        boolean measurementsAreRoughtlyEqual = areMeasurementsRoughtlyEqual(chosenTuple.e0, chosenTuple.e2);
        if( !measurementsAreRoughtlyEqual )
        {
            return new RunResult(true);
        }
        // else here
        
        relinkGraph(startNode, nodeA, nodeB, chosenTuple);
        
        return new RunResult(true);
    }
    
    /**
     * 
     * \return map with the values, where the indices 1 and 3 are the edge indices which lead to the nodes of that type.
     *         the indices 0 and 2 are the subnodes which are the attributes
     */
    private static HashMap<Database.Node.EnumType, Tuple4<Database.Node, Integer, Database.Node, Integer>> getCommonChildnodesOfNodes(Database.Node nodeA, Database.Node nodeB)
    {
        HashMap<Database.Node.EnumType, Tuple4<Database.Node, Integer, Database.Node, Integer>> resultMap;
        int edgeIndexA, edgeIndexB;
        
        resultMap = new HashMap<>();
        
        if( nodeA.type != nodeB.type )
        {
            return resultMap;
        }
        // else here
        
        for( edgeIndexA = 0; edgeIndexA < nodeA.outgoingEdges.size(); edgeIndexA++ )
        {
            for( edgeIndexB = 0; edgeIndexB < nodeB.outgoingEdges.size(); edgeIndexB++ )
            {
                Database.Node iterationChildNodeOfNodeA, iterationChildNodeOfNodeB;
                
                iterationChildNodeOfNodeA = nodeA.outgoingEdges.get(edgeIndexA).destination;
                iterationChildNodeOfNodeB = nodeB.outgoingEdges.get(edgeIndexB).destination;
                
                if( iterationChildNodeOfNodeA.type != iterationChildNodeOfNodeB.type )
                {
                    continue;
                }
                // else here
                
                if( !hasNodetypeMeasurableAttribute(iterationChildNodeOfNodeA.type) )
                {
                    continue;
                }
                // else here
                
                // the nodes can't be equal because this is nonsensical
                // CONTEXT< eual noes can't be connected indirectly >
                if( iterationChildNodeOfNodeA.equals(iterationChildNodeOfNodeB) )
                {
                    continue;
                }
                // else here
                
                resultMap.put(iterationChildNodeOfNodeA.type, new Tuple4<>(iterationChildNodeOfNodeA, edgeIndexA, iterationChildNodeOfNodeB, edgeIndexB));
            }
        }
        
        return resultMap;
    }
    
    private static boolean hasNodetypeMeasurableAttribute(Database.Node.EnumType type)
    {
        return type == Database.Node.EnumType.LENGTH;
    }
    
    private static boolean areMeasurementsRoughtlyEqual(Database.Node nodeA, Database.Node nodeB)
    {
        // assert
        if( nodeA.type != nodeB.type )
        {
            throw new RuntimeException("assert failed");
        }
        
        if( nodeA.type == Database.Node.EnumType.LENGTH )
        {
            return areLengthsRoughtlyEqual(nodeA, nodeB);
        }
        else
        {
            throw new RuntimeException("Internal Error!");
        }
    }
    
    private static boolean areLengthsRoughtlyEqual(Database.Node nodeA, Database.Node nodeB)
    {
        Database.LengthNode lengthNodeA, lengthNodeB;
        float lengthMin, lengthMax;
        float ratio;
        
        // assert
        if( nodeA.type != nodeB.type )
        {
            throw new RuntimeException("assert failed");
        }
        
        // assert
        if( nodeA.type != Database.Node.EnumType.LENGTH )
        {
            throw new RuntimeException("assert failed");
        }
        
        lengthNodeA = (Database.LengthNode)nodeA;
        lengthNodeB = (Database.LengthNode)nodeB;
        
        lengthMin = Math.min(lengthNodeA.length, lengthNodeB.length);
        lengthMax = Math.max(lengthNodeA.length, lengthNodeB.length);
        
        ratio = lengthMin/lengthMax;
        
        return ratio > Parameters.RELATIVELINELENGTHTOBECONSIDEREDEQUAL;
    }
    
    private static void relinkGraph(Database.Node parentNode, Database.Node nodeA, Database.Node nodeB, Tuple4<Database.Node, Integer, Database.Node, Integer> relinkInfoTuple)
    {
        int edgeIndexOfNodeA, edgeIndexOfNodeB;
        Database.Node combinedNode;

        edgeIndexOfNodeA = relinkInfoTuple.e1;
        edgeIndexOfNodeB = relinkInfoTuple.e3;
        
        combinedNode = combineMeasurementNodes(relinkInfoTuple.e0, relinkInfoTuple.e2);
        
        nodeA.outgoingEdges.set(edgeIndexOfNodeA, new Database.Edge(combinedNode));
        nodeB.outgoingEdges.set(edgeIndexOfNodeB, new Database.Edge(combinedNode));
    }
    
    private static Database.Node combineMeasurementNodes(Database.Node nodeA, Database.Node nodeB)
    {
        float valueA, valueB;
        int weightAsIntA, weightAsIntB;
        float weightedValue;
        int weightSum;
        
        // assert
        if( nodeA.type != nodeB.type )
        {
            throw new RuntimeException("assert failed");
        }
        
        if( nodeA.type == Database.Node.EnumType.LENGTH )
        {
            Database.LengthNode lengthNodeA, lengthNodeB;
            
            lengthNodeA = (Database.LengthNode)nodeA;
            lengthNodeB = (Database.LengthNode)nodeB;
            
            valueA = lengthNodeA.length;
            weightAsIntA = lengthNodeA.getWeight();
            valueB = lengthNodeB.length;
            weightAsIntB = lengthNodeB.getWeight();
            
            weightedValue = weightFloat(valueA, weightAsIntA, valueB, weightAsIntB);
            weightSum = weightAsIntA + weightAsIntB;
            
            return new Database.LengthNode(weightedValue, weightSum);
        }
        else
        {
            throw new RuntimeException("Internal Error!");
        }
    }
    
    private static float weightFloat(float valueA, int weightAAsInt, float valueB, int weightBAsInt)
    {
        return weightFloats(valueA, (float)weightAAsInt, valueB, (float)weightBAsInt);
    }
    
    private Database.Node startNode;
    private Database database;
    private Random random = new Random();
}
