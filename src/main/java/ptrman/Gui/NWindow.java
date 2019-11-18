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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Specify shared properties of NARS windows
 */
public class NWindow extends JFrame {
    //http://paletton.com/#uid=70u0u0kllllaFw0g0qFqFg0w0aF
    //http://www.javacodegeeks.com/2013/07/java-7-swing-creating-translucent-and-shaped-windows.html
    //http://docs.oracle.com/javase/tutorial/displayCode.html?code=http://docs.oracle.com/javase/tutorial/uiswing/examples/misc/GradientTranslucentWindowDemoProject/src/misc/GradientTranslucentWindowDemo.java

    //static final Font NarsFont = new Font("Arial", Font.PLAIN, 13);

//    static {
//        // Determine what the GraphicsDevice can support.
//        GraphicsEnvironment ge =
//                GraphicsEnvironment.getLocalGraphicsEnvironment();
//        GraphicsDevice gd = ge.getDefaultScreenDevice();
//        boolean isPerPixelTranslucencySupported =
//                gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT);
//        boolean isPerPixelTransparencySupported =
//                gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSPARENT);
//
//        //If translucent windows aren't supported, exit.
//        if (!isPerPixelTranslucencySupported) {
//            System.err.println("Per-pixel translucency is not supported");
//        }
//        else {
//            System.err.println("Per-pixel translucency supported");
//        }
//
//
//        if (!isPerPixelTransparencySupported) {
//            System.err.println("Per-pixel transparency is not supported");
//        }
//        else {
//            System.err.println("Per-pixel transparency supported");
//        }
//
//        JFrame.setDefaultLookAndFeelDecorated(false);
//    }

    boolean transparent = false;

    /**
     * Default constructor
     */
    public NWindow() {
        this(" ");
    }

    /**
     * Constructor with title and font setting
     *
     * @param title The title displayed by the window
     */
    public NWindow(String title) {
        super(title);
        //setFont(NarsFont);
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }                        
        });

        //setContentPane(background);
        //setTransparent(true);
    }
    
    public NWindow(String title, Component component) {
        this(title);
        getContentPane().add(component, BorderLayout.CENTER);
    }
    public NWindow(String title, Container container) {
        this(title);
        setContentPane(container);
    }

    final Color transparentColor = new Color(0,0,0,0);

    final JPanel background = new JPanel(new BorderLayout()) {


        @Override protected void paintComponent(Graphics g) {

            if (transparent) {
                if (g instanceof Graphics2D) {

                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setPaint(transparentColor);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
            else {
                super.paintComponent(g);
            }
        }
    };

    /** on linux, transparent windows requries the the XComposite display extension enabled,
     * usually provided by a compositing engine like Compiz or xcompmgr */
    void setTransparent(boolean b) {

        if (this.transparent == b) return;

        this.transparent = b;

        if (b) {
            setDefaultLookAndFeelDecorated(false);
            ((JComponent) getContentPane()).setOpaque(false);
            setUndecorated(true);

            getContentPane().setBackground(transparentColor);
            setBackground(transparentColor);

            //setOpacity(0.25f);

        }
        else {
            setBackground(Color.BLACK);
        }


        repaint();
    }

    protected void close() {
        getContentPane().removeAll();
    }
    
    
    public NWindow show(int w, int h) {
        setSize(w, h);
        setVisible(true);
        return this;
    }
    
    public NWindow show(int w, int h, boolean exitOnClose) {
        show(w, h);
        if (exitOnClose)
            setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        return this;
    }


}