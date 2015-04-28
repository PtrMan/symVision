package bpsolver.codelets;

import Datastructures.Tuple4;
import FargGeneral.Codelet;
import FargGeneral.network.Link;
import FargGeneral.network.Node;
import bpsolver.BpSolver;
import bpsolver.HardParameters;
import bpsolver.SolverCodelet;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.PlatonicPrimitiveNode;
import math.DistinctUtility;
import misc.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import static math.Math.weightFloats;

/**
 *
 * chooses two items and looks if they have a matching anotation, if so, modify the graph so they point at the same (averaged) attribute
 * 
 * see page 147 in the foundalis disertation
 * 
 * * line segments - relinks already existing LineSegmentLength nodes if the length is roughtly equal
 * TODO
 * * angle - relink if they are roughtly equal
 */
public class SearchAndFuseRoughtlyEqualElements extends SolverCodelet {
    // startnode must be a object node
    public SearchAndFuseRoughtlyEqualElements(BpSolver bpSolver)
    {
        super(bpSolver);
    }
    
    @Override
    public Codelet.RunResult run() {
        ArrayList<Integer> distinctEdgeIndices;
        Node nodeA, nodeB;
        HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> commonChildnodesOfNodes;
        
        // choose two random nodes
        
        if( startNode.outgoingLinks.size() < 2 )
        {
            return new RunResult(true);
        }
        // else here
        
        distinctEdgeIndices = DistinctUtility.getTwoDisjunctNumbers(random, startNode.outgoingLinks.size());
        
        nodeA = startNode.outgoingLinks.get(distinctEdgeIndices.get(0)).target;
        nodeB = startNode.outgoingLinks.get(distinctEdgeIndices.get(1)).target;
        
        commonChildnodesOfNodes = getCommonFeatureNodesOfNodes(nodeA, nodeB);
        
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
        PlatonicPrimitiveNode[] typeKeysAsArray = (PlatonicPrimitiveNode[])commonChildnodesOfNodes.keySet().toArray();
        int keyIndex = random.nextInt(typeKeysAsArray.length);
        PlatonicPrimitiveNode chosenKeyForCommonChildnodes = typeKeysAsArray[keyIndex];
        
        Tuple4<FeatureNode, Integer, FeatureNode, Integer> chosenTuple = commonChildnodesOfNodes.get(chosenKeyForCommonChildnodes);
        
        boolean measurementsAreRoughtlyEqual = areMeasurementsRoughtlyEqual(chosenTuple.e0, chosenTuple.e2);
        if( !measurementsAreRoughtlyEqual )
        {
            return new RunResult(true);
        }
        // else here
        
        relinkGraph(startNode, nodeA, nodeB, chosenTuple);
        
        return new RunResult(true);
    }
    
    
    @Override
    public void initialize() {
        random = new Random();
    }

    @Override
    public SolverCodelet cloneObject() {
        throw new NotImplementedException();
    }
    
    /**
     * 
     * \return map with the values, where the indices 1 and 3 are the edge indices which lead to the nodes of that type.
     *         the indices 0 and 2 are the subnodes which are the features
     *         the key is the (common) type of the feature, by default points at the featureTypeNode of both nodes
     */
    private HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> getCommonFeatureNodesOfNodes(Node nodeA, Node nodeB)
    {
        HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> resultMap;
        int linkIndexA, linkIndexB;
        
        resultMap = new HashMap<>();
        
        if( nodeA.type != nodeB.type)
        {
            return resultMap;
        }
        if( nodeA.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
        {
            return resultMap;
        }
        // else here
        
        for( linkIndexA = 0; linkIndexA < nodeA.outgoingLinks.size(); linkIndexA++ )
        {
            for( linkIndexB = 0; linkIndexB < nodeB.outgoingLinks.size(); linkIndexB++ )
            {
                Node iterationChildNodeOfNodeA, iterationChildNodeOfNodeB;
                FeatureNode iterationChildNodeOfNodeAAsFeatureNode, iterationChildNodeOfNodeBAsFeatureNode;
                
                iterationChildNodeOfNodeA = nodeA.outgoingLinks.get(linkIndexA).target;
                iterationChildNodeOfNodeB = nodeB.outgoingLinks.get(linkIndexB).target;
                
                if( iterationChildNodeOfNodeA.type != iterationChildNodeOfNodeB.type )
                {
                    continue;
                }
                if( !isFeatureNode(iterationChildNodeOfNodeA) )
                {
                    continue;
                }
                // else here
                
                iterationChildNodeOfNodeAAsFeatureNode = (FeatureNode)iterationChildNodeOfNodeA;
                iterationChildNodeOfNodeBAsFeatureNode = (FeatureNode)iterationChildNodeOfNodeB;
                
                if( iterationChildNodeOfNodeAAsFeatureNode.featureTypeNode.equals(iterationChildNodeOfNodeBAsFeatureNode.featureTypeNode) )
                {
                    continue;
                }
                // else here
                
                // the nodes can't be equal because this is nonsensical
                // CONTEXT< equal nodes can't be connected indirectly >
                if( iterationChildNodeOfNodeA.equals(iterationChildNodeOfNodeB) )
                {
                    continue;
                }
                // else here
                
                resultMap.put(iterationChildNodeOfNodeAAsFeatureNode.featureTypeNode, new Tuple4<>(iterationChildNodeOfNodeAAsFeatureNode, linkIndexA, iterationChildNodeOfNodeBAsFeatureNode, linkIndexB));
            }
        }
        
        return resultMap;
    }
    
    private boolean isFeatureNode(Node node)
    {
        return node.type == NodeTypes.EnumType.FEATURENODE.ordinal();
    }
    
    private boolean areMeasurementsRoughtlyEqual(Node nodeA, Node nodeB)
    {
        FeatureNode nodeAAsFeatureNode, nodeBAsFeatureNode;
        
        Assert.Assert(nodeA.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");
        Assert.Assert(nodeB.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");
        
        nodeAAsFeatureNode = (FeatureNode)nodeA;
        nodeBAsFeatureNode = (FeatureNode)nodeB;
        
        Assert.Assert(nodeAAsFeatureNode.featureTypeNode.equals(nodeBAsFeatureNode), "types are not the same");
        
        if( nodeAAsFeatureNode.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode) )
        {
            return areLengthsRoughtlyEqual(nodeAAsFeatureNode, nodeBAsFeatureNode);
        }
        else
        {
            throw new RuntimeException("Internal Error!");
        }
    }
    
    private boolean areLengthsRoughtlyEqual(FeatureNode nodeA, FeatureNode nodeB)
    {
        float lengthMin, lengthMax;
        float ratio;
        
        Assert.Assert(nodeA.featureTypeNode.equals(nodeA), "types are not the same");
        Assert.Assert(nodeA.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode), "not a LINESEGMENTLENGTH feature node!");
        
        lengthMin = Math.min(nodeA.getValueAsFloat(), nodeB.getValueAsFloat());
        lengthMax = Math.max(nodeA.getValueAsFloat(), nodeB.getValueAsFloat());
        
        ratio = lengthMin/lengthMax;
        
        return ratio > HardParameters.RELATIVELINELENGTHTOBECONSIDEREDEQUAL;
    }
    
    private void relinkGraph(Node parentNode, Node nodeA, Node nodeB, Tuple4<FeatureNode, Integer, FeatureNode, Integer> relinkInfoTuple)
    {
        int linkIndexOfNodeA, linkIndexOfNodeB;
        Node combinedNode;

        linkIndexOfNodeA = relinkInfoTuple.e1;
        linkIndexOfNodeB = relinkInfoTuple.e3;
        
        combinedNode = combineFeatureNodes(relinkInfoTuple.e0, relinkInfoTuple.e2);
        
        nodeA.outgoingLinks.set(linkIndexOfNodeA, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
        nodeB.outgoingLinks.set(linkIndexOfNodeB, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
    }
    
    private FeatureNode combineFeatureNodes(FeatureNode nodeA, FeatureNode nodeB)
    {
        float valueA, valueB;
        int weightAsIntA, weightAsIntB;
        float weightedValue;
        int weightSum;
        
        Assert.Assert(nodeA.featureTypeNode.equals(nodeB.featureTypeNode), "assert failed");
        
        if( nodeA.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode) )
        {
            valueA = nodeA.getValueAsFloat();
            weightAsIntA = nodeA.getWeight();
            valueB = nodeA.getValueAsFloat();
            weightAsIntB = nodeA.getWeight();
            
            weightedValue = weightFloat(valueA, weightAsIntA, valueB, weightAsIntB);
            weightSum = weightAsIntA + weightAsIntB;
            
            return FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode, weightedValue, weightSum, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode));
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
    
    private Random random;
}
