package ptrman.bpsolver.codelets;

import ptrman.Datastructures.Tuple4;
import ptrman.FargGeneral.Codelet;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;
import ptrman.math.DistinctUtility;
import ptrman.misc.Assert;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static ptrman.math.Maths.weightDoubles;

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
        List<Integer> distinctEdgeIndices;
        Node nodeA, nodeB;
        HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> commonChildnodesOfNodes;
        
        // choose two random nodes
        
        if( startNode.outgoingLinks.size() < 2 ) {
            return new RunResult(true);
        }
        // else here
        
        distinctEdgeIndices = DistinctUtility.getTwoDisjunctNumbers(random, startNode.outgoingLinks.size());
        
        nodeA = startNode.outgoingLinks.get(distinctEdgeIndices.get(0)).target;
        nodeB = startNode.outgoingLinks.get(distinctEdgeIndices.get(1)).target;
        
        commonChildnodesOfNodes = getCommonFeatureNodesOfNodes(nodeA, nodeB);
        
        // if the two nodes don't have at least one common type which is Measurable we are done here
        if( commonChildnodesOfNodes.keySet().size() == 0 ) {
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
        if( !measurementsAreRoughtlyEqual ) {
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
    private HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> getCommonFeatureNodesOfNodes(Node nodeA, Node nodeB) {
        HashMap<Node, Tuple4<FeatureNode, Integer, FeatureNode, Integer>> resultMap;
        int linkIndexA, linkIndexB;
        
        resultMap = new HashMap<>();
        
        if( nodeA.type != nodeB.type) {
            return resultMap;
        }
        if( nodeA.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
            return resultMap;
        }
        // else here
        
        for( linkIndexA = 0; linkIndexA < nodeA.outgoingLinks.size(); linkIndexA++ ) {
            for( linkIndexB = 0; linkIndexB < nodeB.outgoingLinks.size(); linkIndexB++ ) {
                Node iterationChildNodeOfNodeA, iterationChildNodeOfNodeB;
                FeatureNode iterationChildNodeOfNodeAAsFeatureNode, iterationChildNodeOfNodeBAsFeatureNode;
                
                iterationChildNodeOfNodeA = nodeA.outgoingLinks.get(linkIndexA).target;
                iterationChildNodeOfNodeB = nodeB.outgoingLinks.get(linkIndexB).target;
                
                if( iterationChildNodeOfNodeA.type != iterationChildNodeOfNodeB.type ) {
                    continue;
                }
                if( !isFeatureNode(iterationChildNodeOfNodeA) ) {
                    continue;
                }
                // else here
                
                iterationChildNodeOfNodeAAsFeatureNode = (FeatureNode)iterationChildNodeOfNodeA;
                iterationChildNodeOfNodeBAsFeatureNode = (FeatureNode)iterationChildNodeOfNodeB;
                
                if( iterationChildNodeOfNodeAAsFeatureNode.featureTypeNode.equals(iterationChildNodeOfNodeBAsFeatureNode.featureTypeNode) ) {
                    continue;
                }
                // else here
                
                // the nodes can't be equal because this is nonsensical
                // CONTEXT< equal nodes can't be connected indirectly >
                if( iterationChildNodeOfNodeA.equals(iterationChildNodeOfNodeB) ) {
                    continue;
                }
                // else here
                
                resultMap.put(iterationChildNodeOfNodeAAsFeatureNode.featureTypeNode, new Tuple4<>(iterationChildNodeOfNodeAAsFeatureNode, linkIndexA, iterationChildNodeOfNodeBAsFeatureNode, linkIndexB));
            }
        }
        
        return resultMap;
    }
    
    private boolean isFeatureNode(Node node) {
        return node.type == NodeTypes.EnumType.FEATURENODE.ordinal();
    }
    
    private boolean areMeasurementsRoughtlyEqual(Node nodeA, Node nodeB) {
        FeatureNode nodeAAsFeatureNode, nodeBAsFeatureNode;
        
        Assert.Assert(nodeA.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");
        Assert.Assert(nodeB.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");
        
        nodeAAsFeatureNode = (FeatureNode)nodeA;
        nodeBAsFeatureNode = (FeatureNode)nodeB;
        
        Assert.Assert(nodeAAsFeatureNode.featureTypeNode.equals(nodeBAsFeatureNode), "types are not the same");
        
        if( nodeAAsFeatureNode.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode) ) {
            return areLengthsRoughtlyEqual(nodeAAsFeatureNode, nodeBAsFeatureNode);
        }
        else {
            throw new RuntimeException("Internal Error!");
        }
    }
    
    private boolean areLengthsRoughtlyEqual(FeatureNode nodeA, FeatureNode nodeB) {
        Assert.Assert(nodeA.featureTypeNode.equals(nodeA), "types are not the same");
        Assert.Assert(nodeA.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode), "not a LINESEGMENTLENGTH feature node!");
        
        double lengthMin = Math.min(nodeA.getValueAsFloat(), nodeB.getValueAsFloat());
        double lengthMax = Math.max(nodeA.getValueAsFloat(), nodeB.getValueAsFloat());
        
        double ratio = lengthMin/lengthMax;
        
        return ratio > HardParameters.RELATIVELINELENGTHTOBECONSIDEREDEQUAL;
    }
    
    private void relinkGraph(Node parentNode, Node nodeA, Node nodeB, Tuple4<FeatureNode, Integer, FeatureNode, Integer> relinkInfoTuple) {
        int linkIndexOfNodeA, linkIndexOfNodeB;
        Node combinedNode;

        linkIndexOfNodeA = relinkInfoTuple.e1;
        linkIndexOfNodeB = relinkInfoTuple.e3;
        
        combinedNode = combineFeatureNodes(relinkInfoTuple.e0, relinkInfoTuple.e2);
        
        nodeA.outgoingLinks.set(linkIndexOfNodeA, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
        nodeB.outgoingLinks.set(linkIndexOfNodeB, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
    }
    
    private FeatureNode combineFeatureNodes(FeatureNode nodeA, FeatureNode nodeB) {
        Assert.Assert(nodeA.featureTypeNode.equals(nodeB.featureTypeNode), "assert failed");
        
        if( nodeA.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode) ) {
            final double valueA = nodeA.getValueAsFloat();
            final int weightAsIntA = nodeA.getWeight();
            final double valueB = nodeA.getValueAsFloat();
            final int weightAsIntB = nodeA.getWeight();
            
            final double weightedValue = weightDouble(valueA, weightAsIntA, valueB, weightAsIntB);
            final int weightSum = weightAsIntA + weightAsIntB;
            
            return FeatureNode.createFloatNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode, weightedValue, weightSum, bpSolver.platonicPrimitiveDatabase.getMaxValueByPrimitiveNode(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode));
        }
        else {
            throw new RuntimeException("Internal Error!");
        }
    }
    
    private static double weightDouble(final double valueA, final int weightAAsInt, final double valueB, final int weightBAsInt) {
        return weightDoubles(valueA, (double) weightAAsInt, valueB, (double) weightBAsInt);
    }
    
    private Random random;
}
