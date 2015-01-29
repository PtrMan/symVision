package Gui;

import FargGeneral.network.Link;
import FargGeneral.network.Node;
import bpsolver.NetworkHandles;
import bpsolver.nodes.FeatureNode;
import bpsolver.nodes.NodeTypes;
import bpsolver.nodes.NumeriosityNode;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import com.mxgraph.layout.mxFastOrganicLayout;
import com.mxgraph.layout.mxIGraphLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.swing.util.mxMorphing;
import com.mxgraph.util.mxEvent;
import com.mxgraph.util.mxEventObject;
import com.mxgraph.util.mxEventSource.mxIEventListener;
import com.mxgraph.view.mxGraph;
import java.util.ArrayList;

public class NodeGraph
{
    private mxGraph graph = new mxGraph();
    private mxGraphComponent graphComponent;
    
    public NodeGraph()
    {
        graphComponent = new mxGraphComponent(graph);
        
        graph.setHtmlLabels(true);
    }
    
    public mxGraphComponent getGraph()
    {
        return graphComponent;
    }
    
    public void repopulateAfterNodes(ArrayList<Node> nodes, NetworkHandles networkHandles)
    {
        clear();
        populateAfterNodes(nodes, networkHandles);
    }
    
    public void clear()
    {
        graph.removeCells(graph.getChildVertices(graph.getDefaultParent()));
    }
    /*
    private void populate() {
        graph.getModel().beginUpdate();
        
        Object parent = graph.getDefaultParent();
        
        
        Object v1 = graph.insertVertex(parent, null, "Hello", 20, 20, 80, 30);
        Object v2 = graph.insertVertex(parent, null, "World!", 240, 150, 80, 30);
        graph.insertEdge(parent, null, "Edge", v1, v2);
        
        graph.getModel().endUpdate();
        
        graphComponent = new mxGraphComponent(graph);
    }
    */
    private void populateAfterNodes(ArrayList<Node> nodes, NetworkHandles networkHandles)
    {
        VertexList verticesWithNode;
        
        graph.getModel().beginUpdate();
        
        verticesWithNode = convertNodesToVertexWithNodeRecursivly(nodes, networkHandles);
        populateEdgesBetweenNodes(verticesWithNode);
        insertLinksBetweenVertexNodes(verticesWithNode);
        
        graph.getModel().endUpdate();
        
        //graphComponent = new mxGraphComponent(graph);
        
        
        
        
        mxIGraphLayout layout = new mxFastOrganicLayout(graph);

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
    }
    
    private void populateEdgesBetweenNodes(VertexList vertexList)
    {
        for( VertexWithNode iterationVertex : vertexList.verticesWithNode )
        {
            for( Link iterationLink : iterationVertex.node.outgoingLinks )
            {
                VertexWithNode vertexForTarget;

                vertexForTarget = vertexList.getVertexWithNodeByNode(iterationLink.target);
                iterationVertex.outgoingVerticesWithNode.add(vertexForTarget);
            }
        }
    }
    
    
    
    private void insertLinksBetweenVertexNodes(VertexList vertexList)
    {
        Object parent;
        
        parent = graph.getDefaultParent();
        
        for( VertexWithNode iterationVertex : vertexList.verticesWithNode )
        {
            for( VertexWithNode outgoingVertex : iterationVertex.outgoingVerticesWithNode )
            {
                // TODO< label edgetype >
                
                Link outgoingLinkForOutgoingVertex;
                
                outgoingLinkForOutgoingVertex = getLinkCorespondingToTargetNode(iterationVertex.node, outgoingVertex.node);
                
                graph.insertEdge(parent, null, outgoingLinkForOutgoingVertex.type.toString(), iterationVertex.vertexForNode, outgoingVertex.vertexForNode);
            }
        }
    }
    
    private static Link getLinkCorespondingToTargetNode(Node sourceNode, Node targetNode)
    {
        for( Link iterationOutgoingLink : sourceNode.outgoingLinks )
        {
            if( iterationOutgoingLink.target.equals(targetNode) )
            {
                return iterationOutgoingLink;
            }
        }
        
        throw new RuntimeException("internal error");
    }
    
    private VertexList convertNodesToVertexWithNodeRecursivly(ArrayList<Node> nodes, NetworkHandles networkHandles)
    {
        VertexList resultVertexList;
        ArrayList<Node> remainingNodes;
        Object graphParent;
        
        resultVertexList = new VertexList();
        
        remainingNodes = new ArrayList<>();
        
        for( Node iterationNode : nodes )
        {
            remainingNodes.add(iterationNode);
        }
        
        for(;;)
        {
            Node currentNode;
            
            if( remainingNodes.size() == 0 )
            {
                break;
            }
            
            currentNode = remainingNodes.get(0);
            remainingNodes.remove(0);
            
            // we don't need to create/inert it if it is already in there
            if( resultVertexList.existsVertexWthNodeByNode(currentNode) )
            {
                continue;
            }
            
            // add it
            resultVertexList.verticesWithNode.add(VertexWithNode.createFromNodeAndVertex(currentNode, createVertexForNode(currentNode, networkHandles)));
            
            // look for connected childnodes
            for( Link iterationLink : currentNode.outgoingLinks )
            {
                // TODO< hyperlinks >
                
                // optimization
                if( resultVertexList.existsVertexWthNodeByNode(iterationLink.target) ) {
                    continue;
                }
                
                remainingNodes.add(iterationLink.target);
            }
        }
        
        return resultVertexList;
    }
    
    private Object createVertexForNode(Node node, NetworkHandles networkHandles)
    {
        Object vertexForNode;
        Object graphParent;
        
        graphParent = graph.getDefaultParent();
        
        if( node.type == NodeTypes.EnumType.FEATURENODE.ordinal() )
        {
            FeatureNode featureNode;
            
            featureNode = (FeatureNode)node;
            
            String featureName;
            
            if( featureNode.featureTypeNode.equals(networkHandles.lineSegmentFeatureLineLengthPrimitiveNode) )
            {
                featureName = "lineSegmentFeatureLineLength";
            }
            else if( featureNode.featureTypeNode.equals(networkHandles.xCoordinatePlatonicPrimitiveNode) )
            {
                featureName = "xCoordinate";
            }
            else if( featureNode.featureTypeNode.equals(networkHandles.yCoordinatePlatonicPrimitiveNode) )
            {
                featureName = "yCoordinate";
            }
            else
            {
                featureName = "?";
            }
            
            return graph.insertVertex(graphParent, null, "FeatureNode" + "<br>" + featureName, 20, 20, 80, 30);
        }
        else if( node.type == NodeTypes.EnumType.NUMEROSITYNODE.ordinal() )
        {
            NumeriosityNode numeriosityNode;
            String nodeTextContent;
            
            numeriosityNode = (NumeriosityNode)node;
            
            nodeTextContent = Integer.toString(numeriosityNode.numerosity);
            
            return graph.insertVertex(graphParent, null, "NumerosityNode" + "<br>" + nodeTextContent, 20, 20, 80, 30);
        }
        else if( node.type == NodeTypes.EnumType.PLATONICPRIMITIVEINSTANCENODE.ordinal() )
        {
            PlatonicPrimitiveInstanceNode platonicPrimitiveInstanceNode;
            String primitiveName;
            
            platonicPrimitiveInstanceNode = (PlatonicPrimitiveInstanceNode)node;
            
            primitiveName = platonicPrimitiveInstanceNode.primitiveNode.platonicType;
            
            return graph.insertVertex(graphParent, null, "PlatonicPrimitiveInstanceNode" + "<br>" + primitiveName, 20, 20, 80, 30);
        }
        else if( node.type == NodeTypes.EnumType.PLATONICPRIMITIVENODE.ordinal() )
        {
            return graph.insertVertex(graphParent, null, "PlatonicPrimitiveNode", 20, 20, 80, 30);
        }
        else
        {
            throw new RuntimeException("internal error");
        }
    }

    
    
    private class VertexList
    {
        public ArrayList<VertexWithNode> verticesWithNode = new ArrayList<>();
        
        public VertexWithNode getVertexWithNodeByNode(Node node)
        {
            for( VertexWithNode iterationVertex : verticesWithNode )
            {
                if( iterationVertex.node.equals(node) )
                {
                    return iterationVertex;
                }
            }
            
            throw new RuntimeException("internal error");
        }
        
        public boolean existsVertexWthNodeByNode(Node node)
        {
            for( VertexWithNode iterationVertex : verticesWithNode )
            {
                if( iterationVertex.node.equals(node) )
                {
                    return true;
                }
            }
            
            return false;
        }
    }
    
    
    private static class VertexWithNode
    {
        Node node;
        Object vertexForNode;
        
        ArrayList<VertexWithNode> outgoingVerticesWithNode = new ArrayList<>();
        
        public static VertexWithNode createFromNodeAndVertex(Node node, Object vertexForNode)
        {
            VertexWithNode resultVertex;
            
            resultVertex = new VertexWithNode();
            resultVertex.node = node;
            resultVertex.vertexForNode = vertexForNode;
            return resultVertex;
        }
    }
}
