/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.visualizationTests;

import processing.core.PApplet;
import processing.core.PImage;
import ptrman.bindingNars.NarsBinding;
import ptrman.bindingNars.OnaNarseseConsumer;
import ptrman.bpsolver.IImageDrawer;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.Solver2;
import ptrman.misc.ImageConverter;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 */
public class VisualizeAnimation extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;

    public Solver2 solver2 = new Solver2();
    private PImage pimg;

    // scene to choose
    // "pong" pong
    // "symbols1" symbol animation 1
    public static String scene = "symbols1";//"pong";

    public static double ballX = 30.0;
    public static double ballY = 50.0;
    public static double ballVelX = 6.1;
    public static double ballVelY = 1.6;

    public VisualizeAnimation() {
        solver2.narsBinding = new NarsBinding(new OnaNarseseConsumer());
    }

    public static class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage apply(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(Color.BLACK);
            g2.drawRect(0, 0, off_Image.getWidth(), off_Image.getHeight());

            if (scene.equals("pong")) {

                ballX += ballVelX;
                ballY += ballVelY;

                if (ballX < 10) {
                    ballVelX = Math.abs(ballVelX);
                }
                if (ballX > 120) {
                    ballVelX = -Math.abs(ballVelX);
                }

                if (ballY < 0) {
                    ballVelY = Math.abs(ballVelY);
                }
                if (ballY > 100) {
                    ballVelY = -Math.abs(ballVelY);
                }

                g2.setColor(Color.WHITE);

                g2.drawRect(10, 50, 10, 30);

                g2.fillOval((int)ballX, (int)ballY, 20, 20);
            }
            else if (scene.equals("symbols1")) {
                g2.setColor(Color.WHITE);

                { // chinese symbol
                    g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
                    g2.drawString("不", (animationTime*5) % 100, 100);
                }

                { //
                    g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
                    g2.drawString("X", 100 - (animationTime*8) % 100, 120);
                }
            }


            return off_Image;
        }
    }

    static float animationTime = 0.0f;

    public VisualizationDrawer drawer = new VisualizationDrawer(); // used for drawing


    public void draw(){
        background(0);

        animationTime += 0.1f;

        int steps = 50; // how many steps are done?

        solver2.imageDrawer = new VisualizeAnimation.InputDrawer();

        solver2.preFrame(); // do all processing and setup before the actual processing of the frame
        for (int iStep=0;iStep<steps;iStep++) {
            solver2.frameStep(); // step of a frame
        }
        solver2.postFrame();

        { // draw processed image in the background
            pimg = ImageConverter.convBufferedImageToPImage((new InputDrawer()).apply(null), pimg);
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }

        drawer.drawPrimitives(solver2, this, null, null);


        // mouse cursor
        //ellipse(mouseX, mouseY, 4, 4);
    }

    @Override
    public void settings() {
        size(200, 200);
    }

    public static void main(String[] passedArgs) {
        PApplet.main(new String[] { "ptrman.visualizationTests.VisualizeAnimation" });
    }
}
