package ptrman.Gui;

import javax.swing.*;
import java.awt.*;

public class DualConvas extends JPanel
{
    public DualConvas() {
        super(new GridLayout(0,1));

        leftCanvas=new GCanvas();
        //leftCanvas.setSize(new Dimension(300, 200));
        add(leftCanvas);
        
        rightCanvas = new GCanvas();
        //rightCanvas.setSize(new Dimension(300, 200));
        add(rightCanvas);
        
        
        
        setVisible(true);
        
    }
    
    public final GCanvas leftCanvas;
    public final GCanvas rightCanvas;
    
    
    public class GCanvas extends Canvas
    {
        public GCanvas() {
            setIgnoreRepaint(true);
        }
        
        public void paint(Graphics g)
        {
            if( image == null ) {
                return;
            }
            
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
        
        public void setImage(Image image) {
            SwingUtilities.invokeLater(() -> {
                this.image = image;
                repaint();
            } );
        }
        
        private Image image;
    }
}

