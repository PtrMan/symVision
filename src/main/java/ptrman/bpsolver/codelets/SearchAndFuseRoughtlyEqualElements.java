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

import com.google.common.collect.Lists;
import ptrman.Datastructures.Tuple4;
import ptrman.FargGeneral.Codelet;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.HardParameters;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.SolverCodelet;
import ptrman.bpsolver.nodes.FeatureNode;
import ptrman.bpsolver.nodes.NodeTypes;
import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;
import ptrman.math.DistinctUtility;
import ptrman.misc.Assert;

import java.util.*;

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
    public SearchAndFuseRoughtlyEqualElements(Solver bpSolver)
    {
        super(bpSolver);
    }

    @Override
    public Codelet.RunResult run() {

        // choose two random nodes

        List<Link> s = Lists.newArrayList(startNode.out());

        if( s.size() < 2 ) {
            return new RunResult(true);
        }
        // else here

        List<Integer> distinctEdgeIndices = DistinctUtility.getTwoDisjunctNumbers(random, s.size());

        Node nodeA = s.get(distinctEdgeIndices.get(0)).target;
        Node nodeB = s.get(distinctEdgeIndices.get(1)).target;

        HashMap<Node, Tuple4<FeatureNode, Link, FeatureNode, Link>> commonChildnodesOfNodes = getCommonFeatureNodesOfNodes(nodeA, nodeB);

        // if the two nodes don't have at least one common type which is Measurable we are done here
        if( commonChildnodesOfNodes.isEmpty() ) {
            return new RunResult(true);
        }
        // else here


        // * select random key
        // * retrieve Tuple
        // * look if the measurements are roughtly equal
        // * relink graph if they are
        PlatonicPrimitiveNode[] typeKeysAsArray = (PlatonicPrimitiveNode[])commonChildnodesOfNodes.keySet().toArray();
        int keyIndex = random.nextInt(typeKeysAsArray.length);
        PlatonicPrimitiveNode chosenKeyForCommonChildnodes = typeKeysAsArray[keyIndex];

        Tuple4<FeatureNode, Link, FeatureNode, Link> chosenTuple = commonChildnodesOfNodes.get(chosenKeyForCommonChildnodes);

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
        throw new RuntimeException("not implemented");
    }

    /**
     *
     * \return map with the values, where the indices 1 and 3 are the edge indices which lead to the nodes of that type.
     *         the indices 0 and 2 are the subnodes which are the features
     *         the key is the (common) type of the feature, by default points at the featureTypeNode of both nodes
     */
    private static HashMap<Node, Tuple4<FeatureNode, Link, FeatureNode, Link>> getCommonFeatureNodesOfNodes(Node nodeA, Node nodeB) {


        HashMap<Node, Tuple4<FeatureNode, Link, FeatureNode, Link>> resultMap = new HashMap<>();

        if( nodeA.type != nodeB.type) {
            return resultMap;
        }
        if( nodeA.type != NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() ) {
            return resultMap;
        }
        // else here

        for (Link linkA : nodeA.out()) {
            for (Link linkB : nodeB.out()) {
                Node iterationChildNodeOfNodeA = linkA.target;
                Node iterationChildNodeOfNodeB = linkB.target;

                if( iterationChildNodeOfNodeA.type != iterationChildNodeOfNodeB.type ) {
                    continue;
                }
                if( !isFeatureNode(iterationChildNodeOfNodeA) ) {
                    continue;
                }
                // else here

                FeatureNode iterationChildNodeOfNodeAAsFeatureNode = (FeatureNode) iterationChildNodeOfNodeA;
                FeatureNode iterationChildNodeOfNodeBAsFeatureNode = (FeatureNode) iterationChildNodeOfNodeB;

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

                resultMap.put(iterationChildNodeOfNodeAAsFeatureNode.featureTypeNode, new Tuple4<>(iterationChildNodeOfNodeAAsFeatureNode, linkA, iterationChildNodeOfNodeBAsFeatureNode, linkB));

            }
        }

        return resultMap;
    }

    private static boolean isFeatureNode(Node node) {
        return node.type == NodeTypes.EnumType.FEATURENODE.ordinal();
    }

    private boolean areMeasurementsRoughtlyEqual(Node nodeA, Node nodeB) {

        Assert.Assert(nodeA.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");
        Assert.Assert(nodeB.type == NodeTypes.EnumType.FEATURENODE.ordinal(), "");

        FeatureNode nodeAAsFeatureNode = (FeatureNode) nodeA;
        FeatureNode nodeBAsFeatureNode = (FeatureNode) nodeB;

        Assert.Assert(nodeAAsFeatureNode.featureTypeNode.equals(nodeBAsFeatureNode), "types are not the same");

        if( nodeAAsFeatureNode.featureTypeNode.equals(getNetworkHandles().lineSegmentFeatureLineLengthPrimitiveNode) ) {
            return areLengthsRoughtlyEqual(nodeAAsFeatureNode, nodeBAsFeatureNode);
        } else {
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

    private void relinkGraph(Node parentNode, Node nodeA, Node nodeB, Tuple4<FeatureNode, Link, FeatureNode, Link> relinkInfoTuple) {

//        int linkIndexOfNodeA = relinkInfoTuple.e1;
//        int linkIndexOfNodeB = relinkInfoTuple.e3;

        Node combinedNode = combineFeatureNodes(relinkInfoTuple.e0, relinkInfoTuple.e2);

        throw new UnsupportedOperationException("TODO");
//        nodeA.out.set(linkIndexOfNodeA, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
//        nodeB.out.set(linkIndexOfNodeB, getNetwork().linkCreator.createLink(Link.EnumType.HASATTRIBUTE, combinedNode));
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
        return weightDoubles(valueA, weightAAsInt, valueB, weightBAsInt);
    }

    private Random random;
}
