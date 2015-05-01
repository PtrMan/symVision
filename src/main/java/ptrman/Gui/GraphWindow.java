package ptrman.Gui;

import javax.swing.*;

/**
 *
 * 
 */
public class GraphWindow //extends JPanel
{
    public GraphWindow()
    {
        super();

        //buildGui();
        //setVisible(true);
    }
    
//    private void buildGui()
//    {
//
//        setLayout(new GridLayout(1, 1));
//
//        add(nodeGraph.getGraph(), );
//    }

    public JComponent getComponent() { return nodeGraph.getGraph(); }

    public NodeGraph getNodeGraph()
    {
        return nodeGraph;
    }
    
    private NodeGraph nodeGraph = new NodeGraph();
}