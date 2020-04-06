/**
 * Copyright 2020 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ptrman.exp;

import processing.core.PApplet;
import processing.core.PImage;
import ptrman.bindingNars.NarsBinding;
import ptrman.bindingNars.OnaNarseseConsumer;
import ptrman.bpsolver.IImageDrawer;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.Solver2;
import ptrman.misc.ImageConverter;
import ptrman.visualizationTests.VisualizationDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Random;

/**
 * * connect to NAR
 * * test in simple world (pong, etc)
 */
public class NarConSimpleWorld extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;

    public Solver2 solver2 = new Solver2();
    private PImage pimg;

    // scene to choose
    // "pong" pong
    public static String scene = "pong";

    public static double ballX = 30.0;
    public static double ballY = 50.0;
    public static double ballVelX = 6.1;
    public static double ballVelY = 1.6;

    public static double batVel = 0.0;
    public static double batY = 50.0;

    public static int t = 0; // timer

    public static Process pro = null;
    public static OutputStream os;
    public static InputStream is;

    public static BufferedReader reader;
    public static BufferedWriter writer;

    public static String pathToOna = "C:\\Users\\R0B3\\dir\\github\\ONAmy\\OpenNARS-for-Applications";

    public static int hits = 0;
    public static int misses = 0;


    // helper to send string to ONA
    public static void sendText(String str, boolean silent) {
        if (!silent) {
            System.out.println("=>"+str);
        }

        byte[] buf = (str+"\r\n").getBytes();
        try {
            os.write(buf);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
            int here = 5;
        }
    }

    public static String queuedOp = null; // string of op which is queued

    // helper to let NAR perform inference steps
    public void narInferenceSteps(int steps) {
        sendText(""+steps, true);



        try {
            /*
            while(true) {
                int len = is.available();

                if (len > 0) {
                    int here = 5;
                }

                break;

                *
                byte[] buf = new byte[10];
                is.read(buf);

                if (buf[0] == 0) { // is empty line?
                    break;
                }

                String bufAsString = new String(buf);

                System.out.println(bufAsString);

                int here = 5;

                 *
            }

             */


            while(true) {
                //byte[] arr = new byte[1];
                //is.read(arr);

                int len = is.available();

                if(len > 0) {
                    byte[] buf = new byte[len];
                    is.read(buf);

                    String bufAsString = new String(buf);
                    for(String iLine : bufAsString.split("\\n")) {

                        if (iLine.contains("=/>") && iLine.contains("^")) { // is a seq with a op derived?
                            System.out.println("=/>^   "+iLine);
                            int here = 5;
                        }

                        if (iLine.substring(0, 9).equals("Derived: ") || iLine.substring(0, 9).equals("Revised: ")) {
                            continue; // ignore derivation messages
                        }



                        if (iLine.substring(0, 4).equals("done") || iLine.substring(0, 10).equals("performing")) {
                            // don't print
                        }
                        else {
                            System.out.println(iLine);
                        }

                        if(iLine.contains("executed with args")) {
                            int here = 5;
                        }

                        if (iLine.equals("^up executed with args ")) {
                            queuedOp = "^up";
                        }
                        else if (iLine.equals("^down executed with args ")) {
                            queuedOp = "^down";
                        }

                        if (iLine.substring(0, 4).equals("done")) {
                            return; // it is done with the steps
                        }
                    }

                    int here = 5;
                }

                try {
                    Thread.sleep(1); // give other processes time
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }

                int here = 5;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public NarConSimpleWorld() {
        /*
        try {
            socket = new DatagramSocket(50001);
            socket.setSoTimeout(1);
        } catch (SocketException e) {
            e.printStackTrace();
        }*/

        // TODO < decide path based on OS >
        String commandline = pathToOna+"\\NAR.exe";

        try {
            pro = Runtime.getRuntime().exec(new String[]{commandline, "shell"});
            os = pro.getOutputStream();
            is = pro.getInputStream();

            /*
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(is));

            //os.write("< a --> b >.\n".getBytes());

            //sendText(""+5, false);

            byte[] buf = (5+"\r\n").getBytes();
            try {
                os.write(buf);
                os.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(100); // give other processes time
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }

            String inputLine;
            while ((inputLine = stdInput.readLine()) != null)
                System.out.println(inputLine);

             */

            /*
            for(;;) {
                String x = stdInput.readLine();

                //if (a > 0) {
                    int here = 5;
                //}

            }

             */

            int here = 5;

            //int here = 5;

            //sendText("*motorbabbling=false", false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        solver2.narsBinding = new NarsBinding(new OnaNarseseConsumer());
    }



    public void setup() {
        frameRate(8);
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


            g2.setColor(Color.WHITE);

            g2.drawRect(6, (int)batY-20, 10, 20*2);

            g2.fillOval((int)ballX-10, (int)ballY-10, 20, 20);




            return off_Image;
        }
    }

    static float animationTime = 0.0f;

    public VisualizationDrawer drawer = new VisualizationDrawer(); // used for drawing

    String oldState = "";

    public void draw() {
        t++;


        if(VisualizationDrawer.rel2 != "") { // send current state
            String n = "< {"+(VisualizationDrawer.rel2)+"} --> rel >. :|:";
            sendText(n, false);
            oldState = VisualizationDrawer.rel2;
        }


        if(false){ // manual motor babbling
            Random rng = new Random();
            if(rng.nextFloat() < 0.1) {
                if (rng.nextFloat() < 0.5) {
                    batVel = -7.0;
                    String n = "^up. :|:";
                    sendText(n, false);
                }
                else {
                    batVel = 7.0;
                    String n = "^down. :|:";
                    sendText(n, false);
                }
            }
        }

        { // simulate
            if (ballX < 10) {
                ballVelX = Math.abs(ballVelX);
            }
            if (ballX > 110) {
                ballVelX = -Math.abs(ballVelX);
            }

            if (ballY < 0) {
                ballVelY = Math.abs(ballVelY);
            }
            if (ballY > 100) {
                ballVelY = -Math.abs(ballVelY);
            }

            if (ballX < 10) {
                float distY = (float)Math.abs(ballY - batY);
                if (distY <= 20+1) {
                    // hit bat -> good NAR
                    sendText("good_nar. :|:", false);
                    hits++;
                }
                else { // bat didn't hit ball, respawn ball
                    System.out.println("respawn ball");

                    Random rng = new Random();
                    ballX = 15+5 + rng.nextFloat()*80;
                    ballY = rng.nextFloat()*80;

                    ballVelY = 1.0 + rng.nextFloat()*3.0;
                    if (rng.nextFloat() < 0.5) {
                        ballVelY *= -1;
                    }

                    misses++;
                }
            }

            ballX += ballVelX;
            ballY += ballVelY;

            batY += batVel;
            batY = Math.min(batY, 100-20/2);
            batY = Math.max(batY, 20/2);
        }



        if (t%4 == 0) {
            // feed with goal
            String n = "good_nar! :|:\n\0";
            sendText(n, false);
        }

        { // give time to reason
            narInferenceSteps(5);
            // do actions
            if (queuedOp != null && queuedOp.equals("^up")) {
                batVel = -7.0;
            }
            if (queuedOp != null && queuedOp.equals("^down")) {
                batVel = 7.0;
            }
            queuedOp = null;
        }





        background(0);

        animationTime += 0.1f;

        int steps = 50; // how many steps are done?

        solver2.imageDrawer = new ptrman.exp.NarConSimpleWorld.InputDrawer();

        solver2.preFrame(); // do all processing and setup before the actual processing of the frame
        for (int iStep=0;iStep<steps;iStep++) {
            solver2.frameStep(); // step of a frame
        }
        solver2.postFrame();

        { // draw processed image in the background
            pimg = ImageConverter.convBufferedImageToPImage((new ptrman.exp.NarConSimpleWorld.InputDrawer()).apply(null), pimg);
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }

        drawer.drawPrimitives(solver2, this);

        { // draw debug
            fill(255);
            text("hits"+hits + " misses="+misses + " t="+t, 10, 120);
        }
    }

    @Override
    public void settings() {
        size(200, 200);
    }

    public static void main(String[] passedArgs) {
        PApplet.main(new String[] { "ptrman.exp.NarConSimpleWorld" });
    }
}
