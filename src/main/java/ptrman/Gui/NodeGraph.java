/**
 * Copyright 2019 The SymVision authors
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Gui;

import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.NetworkHandles;
import ptrman.bpsolver.nodes.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class NodeGraph {
	private final mxGraph graph = new mxGraph();
	private final mxGraphComponent graphComponent;

	public NodeGraph() {
		graphComponent = new mxGraphComponent(graph);

		//graph.setHtmlLabels(true);
	}

	private static void populateEdgesBetweenNodes(VertexList vv) {
		for (VertexWithNode v : vv.verticesWithNode) {
			for (Link l : v.node.outgoingLinks) {
				v.outgoingVerticesWithNode.add(
					vv.getVertexWithNodeByNode(l.target)
                );
			}
		}
	}

	private static Link getLinkCorespondingToTargetNode(Node s, Node t) {
		for (Link l : s.outgoingLinks)
			if (l.target.equals(t))
				return l;

		throw new RuntimeException("internal error");
	}

	public mxGraphComponent getGraph() {
		return graphComponent;
	}

	public void repopulateAfterNodes(List<Node> nodes, NetworkHandles networkHandles) {
		clear();
		populateAfterNodes(nodes, networkHandles);
	}

	public void clear() {
		graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
	}

	private void populateAfterNodes(List<Node> nodes, NetworkHandles networkHandles) {

        graph.getModel().beginUpdate();

        VertexList verticesWithNode = convertNodesToVertexWithNodeRecursivly(nodes, networkHandles);
		populateEdgesBetweenNodes(verticesWithNode);
		insertLinksBetweenVertexNodes(verticesWithNode);

		graph.getModel().endUpdate();

		//graphComponent = new mxGraphComponent(graph);


		mxIGraphLayout layout = new mxFastOrganicLayout(graph);

		layout.execute(graph.getDefaultParent());

        /*
        // layout using morphing
        graph.getModel().beginUpdate();
        try {
            layout.execute(graph.getDefaultParent());
        } finally {
            mxMorphing morph = new mxMorphing(graphComponent, 20, 1.2, 20);

            morph.addListener(mxEvent.DONE, new mxIEventListener() {

                @Override
                public void invoke(Object arg0, mxEventObject arg1) {
                    graph.getModel().endUpdate();
                    // fitViewport();
                }
            });

            morph.startAnimation();
        }
        */
	}

	private void insertLinksBetweenVertexNodes(VertexList vertexList) {

        Object parent = graph.getDefaultParent();

		for (VertexWithNode iterationVertex : vertexList.verticesWithNode) {
			for (VertexWithNode outgoingVertex : iterationVertex.outgoingVerticesWithNode) {
				// TODO< label edgetype >

                Link outgoingLinkForOutgoingVertex = getLinkCorespondingToTargetNode(iterationVertex.node, outgoingVertex.node);

				graph.insertEdge(parent, null, outgoingLinkForOutgoingVertex.type.toString(), iterationVertex.vertexForNode, outgoingVertex.vertexForNode);
			}
		}
	}

	private VertexList convertNodesToVertexWithNodeRecursivly(List<Node> nodes, NetworkHandles networkHandles) {
        //Object graphParent;

        VertexList resultVertexList = new VertexList();

        Deque<Node> remainingNodes = new ArrayDeque<>();

		if (nodes != null) {
			remainingNodes.addAll(nodes);
		}

		for (; ; ) {

            if (remainingNodes.size() == 0)
				break;

            Node currentNode = remainingNodes.removeFirst();

			// we don't need to create/inert it if it is already in there
			if (resultVertexList.existsVertexWthNodeByNode(currentNode))
				continue;


			// add it
			resultVertexList.verticesWithNode.add(VertexWithNode.createFromNodeAndVertex(currentNode, createVertexForNode(currentNode, networkHandles)));

			// look for connected childnodes
			for (Link iterationLink : currentNode.outgoingLinks) {
				// TODO< hyperlinks >
				// optimization
				if (!resultVertexList.existsVertexWthNodeByNode(iterationLink.target))
    				remainingNodes.add(iterationLink.target);
			}
		}

		return resultVertexList;
	}

	private Object createVertexForNode(Node node, NetworkHandles networkHandles) {
//		Object vertexForNode;

        Object graphParent = graph.getDefaultParent();

		if (node.type == NodeTypes.EnumType.FEATURENODE.ordinal()) {

            FeatureNode featureNode = (FeatureNode) node;

			String featureName;

			// we try to cast it, if we suceed it is a type which wasn't learned by the system
			// so we can add the text of the type
            PlatonicPrimitiveNode featureTypeNodeAsPlatonicPrimitiveNode = (PlatonicPrimitiveNode) featureNode.featureTypeNode;

			if (featureTypeNodeAsPlatonicPrimitiveNode != null) {
				featureName = featureTypeNodeAsPlatonicPrimitiveNode.platonicType;
			} else {
				// type was learned by the system
				featureName = "?(learned)";
			}

			return graph.insertVertex(graphParent, null, "FeatureNode" + "<br>" + featureName, 20, 20, 80, 30);
		} else if (node.type == NodeTypes.EnumType.NUMEROSITYNODE.ordinal()) {

            NumeriosityNode numeriosityNode = (NumeriosityNode) node;

            String nodeTextContent = Integer.toString(numeriosityNode.numerosity);

			return graph.insertVertex(graphParent, null, "NumerosityNode" + "<br>" + nodeTextContent, 20, 20, 80, 30);
		} else if (node.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal()) {

            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode) node;

            String primitiveName = platonicPrimitiveInstanceNode.primitiveNode.platonicType;

			return graph.insertVertex(graphParent, null, "PlatonicPrimitiveInstanceNode" + "<br>" + primitiveName, 20, 20, 80, 30);
		} else if (node.type == NodeTypes.EnumType.PLATONICPRIMITIVENODE.ordinal()) {
			return graph.insertVertex(graphParent, null, "PlatonicPrimitiveNode", 20, 20, 80, 30);
		} else if (node.type == NodeTypes.EnumType.ATTRIBUTENODE.ordinal()) {
            String attributeName;

            AttributeNode attributeNode = (AttributeNode) node;

			// we try to cast it, if we suceed it is a type which wasn't learned by the system
			// so we can add the text of the type
            PlatonicPrimitiveNode attributeTypeNodeAsPlatinicPrimitiveNode = (PlatonicPrimitiveNode) attributeNode.attributeTypeNode;

			if (attributeTypeNodeAsPlatinicPrimitiveNode != null) {
				attributeName = attributeTypeNodeAsPlatinicPrimitiveNode.platonicType;
			} else {
				// type was learned by the system
				attributeName = "?(learned)";
			}

			return graph.insertVertex(graphParent, null, "AttributeNode" + "<br>" + attributeName, 20, 20, 80, 30);
		} else {
			throw new RuntimeException("internal error");
		}
	}


	private static class VertexList {
		public final ArrayList<VertexWithNode> verticesWithNode = new ArrayList<>();

		public VertexWithNode getVertexWithNodeByNode(Node node) {
			for (VertexWithNode iterationVertex : verticesWithNode)
				if (iterationVertex.node.equals(node))
					return iterationVertex;

			throw new RuntimeException("internal error");
		}

		public boolean existsVertexWthNodeByNode(Node node) {
			for (VertexWithNode iterationVertex : verticesWithNode)
				if (iterationVertex.node.equals(node))
					return true;

			return false;
		}
	}


	private static class VertexWithNode {
		public final ArrayList<VertexWithNode> outgoingVerticesWithNode = new ArrayList<>();
		Node node;
		Object vertexForNode;

		public static VertexWithNode createFromNodeAndVertex(Node node, Object vertexForNode) {

            VertexWithNode resultVertex = new VertexWithNode();
			resultVertex.node = node;
			resultVertex.vertexForNode = vertexForNode;
			return resultVertex;
		}
	}
}
