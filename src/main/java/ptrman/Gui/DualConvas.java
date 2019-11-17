/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Gui;

import javax.swing.*;
import java.awt.*;

public class DualConvas extends JPanel
{
    public DualConvas() {
        super(new GridLayout(0,1));

        leftCanvas= new GCanvas();
        //leftCanvas.setSize(new Dimension(300, 200));
        add(leftCanvas);
        
        rightCanvas = new GCanvas();
        //rightCanvas.setSize(new Dimension(300, 200));
        add(rightCanvas);
        
        
        
        setVisible(true);
        
    }
    
    public final GCanvas leftCanvas;
    public final GCanvas rightCanvas;
    
    
    public static class GCanvas extends Canvas
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

