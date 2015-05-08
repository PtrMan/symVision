package ptrman.Gui;

import javax.swing.*;
import java.awt.*;

public class DualConvas extends JPanel
{
    public DualConvas()
    {
        super();

        

        setLayout(new GridLayout(1,2));
        
        leftCanvas=new GCanvas();
        leftCanvas.setSize(new Dimension(300, 200));
        add(leftCanvas);
        
        rightCanvas = new GCanvas();
        rightCanvas.setSize(new Dimension(300, 200));
        add(rightCanvas);
        
        
        
        setVisible(true);
        
    }
    
    public final GCanvas leftCanvas;
    public final GCanvas rightCanvas;
    
    
    public class GCanvas extends Canvas
    {
        public GCanvas()
        {
        }
        
        public void paint(Graphics g)
        {
            if( image == null )
            {
                return;
            }
            
            g.drawImage(image, 0, 0, image.getWidth(null), image.getHeight(null), null);
            g.dispose();
        }
        
        public void setImage(Image image)
        {
            this.image = image;
            repaint();
        }
        
        private Image image;
    }
}

