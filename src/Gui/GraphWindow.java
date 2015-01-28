package Gui;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * 
 */
public class GraphWindow extends JFrame
{
    public GraphWindow()
    {
        super("");
        setBounds(50,50,300,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        buildGui();
        setVisible(true);
    }
    
    private void buildGui()
    {
        
        setLayout(new GridLayout(1, 1));
        
        getContentPane().add(nodeGraph.getGraph());
    }
    
    public NodeGraph getNodeGraph()
    {
        return nodeGraph;
    }
    
    private NodeGraph nodeGraph = new NodeGraph();
}