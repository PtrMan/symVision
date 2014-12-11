package Gui;

import java.awt.*;
import javax.swing.*;

public class Interactive extends JFrame
{
    public Interactive()
    {
        super("");
        setBounds(50,50,300,300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container con=this.getContentPane();
        con.setBackground(Color.white);
        
        canvas=new GCanvas();
        con.add(canvas);
        setVisible(true);
        
    }
    
    public GCanvas canvas;
    
    
    
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

