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
import ptrman.bpsolver.IImageDrawer;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.Solver2;
import ptrman.misc.ImageConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/** visualize line-segments of endosceleton
 *
 */
public class VisualizeLinesegmentsAnnealing extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;

    public Solver2 solver2 = new Solver2();
    private PImage pimg;


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

            g2.setColor(Color.WHITE);

            if (chosenImage == 0) { // draw polygon
                g2.setColor(new Color(1.0f, 0.0f, 0.0f));

                Polygon poly = new Polygon();

                poly.addPoint(10, 10);
                poly.addPoint(70, 10);
                poly.addPoint(40, 50);

                g2.fillPolygon(poly);
            }
            if (chosenImage == 1) { // draw "A"
                int endpointADeltaX = (int)(Math.cos(animationFrameNumber * 0.1) * 10);
                int endpointADeltaY = (int)(Math.sin(animationFrameNumber * 0.1) * 10);

                g2.setStroke(new BasicStroke(12));
                g2.drawLine(10+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                g2.drawLine(90+endpointADeltaX, 80+endpointADeltaY, 40, 10);
                g2.drawLine(30, 40, 70, 40);
            }
            else if(chosenImage == 2){
                // draw star
                g2.setFont(new Font("TimesRoman", Font.PLAIN, 230));
                g2.drawString("*", 20, 170);
            }
            else if(chosenImage == 3){
                // text
                g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
                g2.drawString("/en-", 2, 100);
            }
            else if(chosenImage == 4) {
                // draw big boxes
                g2.fillRect(10, 10, 70, 20);

                g2.fillRect(10, 50, 70, 20);
            }
            else if(chosenImage == 5) { // chinese symbol
                // text
                g2.setFont(new Font("TimesRoman", Font.PLAIN, 90));
                g2.drawString("不", 2, 100);
            }


            return off_Image;
        }
    }

    static int chosenImage = 0; // chosen image





    static int animationFrameNumber = 0;

    int frameCountdown = 0;

    int state = 0;
    String stateName = "annealing";

    public VisualizationDrawer drawer = new VisualizationDrawer(); // used for drawing


    public void draw(){
        background(64);

        frameCountdown--;


        int framesPerStep = 10; // how many frames do we visualize one step?

        int steps = 50; // how many steps are done?

        if (frameCountdown < 0) { // do we need to visualize something new?
            frameCountdown = framesPerStep;

            if (stateName.equals("annealing") && state == 0) { // first frame of new animation
                chosenImage = new Random().nextInt(6);

                solver2.imageDrawer = new VisualizeLinesegmentsAnnealing.InputDrawer();

                solver2.preFrame(); // do all processing and setup before the actual processing of the frame

                state++;
            }
            else if (stateName.equals("annealing")) {
                solver2.frameStep(); // step of a frame

                state++;

                if (solver2.annealingStep == steps-1-1) {// is last step?
                    solver2.postFrame(); // finish off frame processing

                    {

                    }

                    stateName = "showPrimitives"; // we want to show the primitives
                    frameCountdown = 60*3;
                }
            }
            else if(stateName.equals("showPrimitives")) { // is the time over for the showing of the primitives?
                // then reset state so we continue with new random image

                frameCountdown = -1;
                state = 0;
                stateName = "annealing";
            }
        }



        { // draw processed image in the background
            pimg = ImageConverter.convBufferedImageToPImage((new InputDrawer()).apply(null), pimg);
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }


        if (stateName.equals("annealing")) { // are we annealing the image?
            drawer.drawDetectors(solver2, this);
        }
        else if(stateName.equals("showPrimitives")) { // are we shwoing the primitives?
            drawer.drawPrimitives(solver2, this, null, null, null);
        }

        // mouse cursor
        ellipse(mouseX, mouseY, 4, 4);
    }

    @Override
    public void settings() {
        size(200, 200);
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "ptrman.visualizationTests.VisualizeLinesegmentsAnnealing" };
        PApplet.main(appletArgs);
    }
}
