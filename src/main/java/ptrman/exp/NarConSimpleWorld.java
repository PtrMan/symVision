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
import ptrman.misc.Classifier;
import ptrman.misc.ImageConverter;
import ptrman.misc.TvClassifier;
import ptrman.visualizationTests.VisualizationDrawer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * * connect to NAR
 * * test in simple world (pong, etc)
 */

// TODO< sub image at position >
public class NarConSimpleWorld extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;

    public Solver2 solver2 = new Solver2();
    private PImage pimg;

    // scene to choose
    // "pong" pong
    // "shootemup" for alien invasion thingy
    public static String scene = "pong";//"shootemup";


    // SHOOTEMUP

    public static List<Proj> projectiles = new ArrayList<>();

    static class Proj {
        public float x, y, velX, velY;
        public Proj(float x, float y, float velX, float velY) {
            this.x=x;
            this.y=y;
            this.velX=velX;
            this.velY=velY;
        }
    }


    // PONG/SHOOTEMUP
    public static double batVel = 0.0;
    public static double batPos = 20.0;

    // is ball in "pong", is alien in SHOOTEMUP
    public static double ballX = 40.0;
    public static double ballY = 50.0;
    public static double ballVelX = 6.1;
    public static double ballVelY = 0;




    public static int t = 0; // timer

    public static Process pro = null;
    public static OutputStream os;
    public static InputStream is;

    public static BufferedReader reader;
    public static BufferedWriter writer;

    public static String pathToOna = "C:\\Users\\R0B3\\dir\\github\\ONAmy\\OpenNARS-for-Applications";

    public static int hits = 0;
    public static int misses = 0;

    public static TvClassifier classifier;


    public static boolean useRngAgent = false; // use random action agent? used for testing

    public static boolean verboseAgent = false;
    public static boolean verboseDscnExp = true;

    // helper to send string to ONA
    public static void sendText(String str, boolean silent) {
        if (useRngAgent) {
            return; // don't send anything to NARS if we use dummy agent
        }

        if (!silent) {
            System.out.println(""+str);
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
        sendText(""+steps, false);

        if (!useRngAgent)  {
            try {
                while(true) {
                    //byte[] arr = new byte[1];
                    //is.read(arr);

                    int len = is.available();

                    if(len > 0) {
                        byte[] buf = new byte[len];
                        is.read(buf);

                        String bufAsString = new String(buf);
                        for(String iLine : bufAsString.split("\\n")) {

                            //DEBUG System.out.println(iLine);

                            if (iLine.contains("=/>") && iLine.contains("^")) { // is a seq with a op derived?
                                if(verboseAgent) System.out.println("=/>^   "+iLine);
                                int here = 5;
                            }

                            if( iLine.length() >= 9 && iLine.substring(0, 9).equals("Derived: ") || iLine.length() >= 9 && iLine.substring(0, 9).equals("Revised: ")) {
                                continue; // ignore derivation messages
                            }


                            if (iLine.length() >= 4 && iLine.substring(0, 4).equals("done") || iLine.length() >= 10 && iLine.substring(0, 10).equals("performing")) {
                                // don't print
                            }
                            else {
                                if(verboseAgent) System.out.println(iLine);
                            }

                            if (verboseDscnExp && iLine.contains("decision expectation ")) {
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
                            else if (iLine.equals("^pick executed with args ")) {
                                queuedOp = "^pick";
                            }

                            if (iLine.length() >= 4 && iLine.substring(0, 4).equals("done")) {
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


    }

    public NarConSimpleWorld() {
        { // configure classifier
            classifier = new TvClassifier();
            classifier.minSimilarity = 0.8f;
        }

        // TODO < decide path based on OS >
        String commandline = pathToOna+"\\NAR.exe";

        try {
            pro = Runtime.getRuntime().exec(new String[]{commandline, "shell"});
            os = pro.getOutputStream();
            is = pro.getInputStream();

            int here = 5;

            //int here = 5;

            //sendText("*motorbabbling=false", false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        solver2.narsBinding = new NarsBinding(new OnaNarseseConsumer());
    }



    public void setup() {
        frameRate(15);
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

            if (scene.equals("pong")) {
                g2.drawRect(6, (int)batPos-20, 10, 20*2);

                g2.fillOval((int)ballX-10, (int)ballY-10, 20, 20);
            }
            else if (scene.equals("shootemup")) {
                g2.drawRect((int)batPos, 80, 15, 10); // spaceship

                // draw projectiles
                for(Proj iProj : projectiles) {
                    g2.drawRect((int)iProj.x, (int)iProj.y-7, 2, 14);
                }

                // draw alien
                g2.fillOval((int)ballX-10, (int)ballY-10, 20, 20);
            }




            return off_Image;
        }
    }

    static float animationTime = 0.0f;

    public VisualizationDrawer drawer = new VisualizationDrawer(); // used for drawing

    public static String lastNarsese = ""; // used to compress away changes in the wrld which are the same

    public void draw() {
        t++;

        // send hidden variables (for testing)
        if(false) {
            VisualizationDrawer.relN.clear();

            if(scene.equals("pong")) {
                double diffY = ballY - batPos;

                String relY = "c";
                if (diffY < -15.0/2.0) {
                    relY = "b"; // below
                }
                if (diffY > 15.0/2.0) {
                    relY = "a"; // above
                }

                // not scalable way, will xplode for more complicated scenes
                String n = "< {( {"+(relY)+"} * {"+0+"D"+1+"} )} --> relY >. :|:";
                n = relY+"Q"+0+"D"+1+ "QrelY. :|:";
                //n = relY+". :|:";

                VisualizationDrawer.relN.add(n);

                //n = "d"+"Q"+0+"D"+1+ "QrelX. :|:";
                //VisualizationDrawer.relN.add(n);
            }
        }

        if ((t%3) == 0) { // send status not to often
            { // send to NARS
                boolean isAnythingVisible = false;

                String thisNarsese = "";
                for(String iN : VisualizationDrawer.relN) {
                    thisNarsese += iN + "\n";
                }
                if (true||!thisNarsese.equals(lastNarsese)) {
                    // iterate over narsese sentences to send
                    for(String iN : VisualizationDrawer.relN) {
                        sendText(iN, false);
                        isAnythingVisible = true;
                    }
                }
                lastNarsese = thisNarsese;

                if (!isAnythingVisible) {
                    // HACK< send a pseudo observation to NARS so it can at least learn pong etc >
                    // we need this because NARS doesn't receive any input when there are no relations present.
                    // this is the case when bat and ball are overlaping.
                    sendText("<nothing0 --> nothing>. :|:", false);
                }
            }
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
            if (scene.equals("pong")) {
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
                    float distY = (float)Math.abs(ballY - batPos);
                    if (distY <= 20+1) {
                        // hit bat -> good NAR
                        System.out.println("good nar");
                        sendText("goodnar. :|:", false);
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

                batPos += batVel;
                batPos = Math.min(batPos, 100-20/2);
                batPos = Math.max(batPos, 20/2);
            }
            else if(scene.equals("shootemup")) {
                // check collision between projectile and alien
                for(int idx=0;idx<projectiles.size();idx++) {
                    Proj proj = projectiles.get(idx);

                    double diffX = proj.x - ballX;
                    double diffY = proj.y - ballY;
                    double dist = Math.sqrt(diffX*diffX+diffY*diffY);

                    if (dist < 10) { // did projectile hit alien?
                        // hit bat -> good NAR
                        System.out.println("hit");
                        sendText("goodnar. :|:", false);
                        hits++;

                        projectiles.remove(idx);
                        idx--;

                        // respawn alien to make it more complicated
                        Random rng = new Random();
                        ballX = rng.nextFloat()*110.0f;
                        if (rng.nextFloat() < 0.5) {
                            ballVelX = (float)Math.abs(ballVelX);
                        }
                        else {
                            ballVelX = -(float)Math.abs(ballVelX);
                        }
                    }
                }


                /*
                //change direction randomly to make it more complicated
                Random rng = new Random();
                if (rng.nextFloat() < 0.5) {
                    ballVelX = (float)Math.abs(ballVelX);
                }
                else {
                    ballVelX = -(float)Math.abs(ballVelX);
                }*/

                // alien reflects on sides of screen
                if (ballX < 10) {
                    ballVelX = Math.abs(ballVelX);
                }
                if (ballX > 110) {
                    ballVelX = -Math.abs(ballVelX);
                }

                // move projectiles
                for(Proj iProj : projectiles) {
                    iProj.x += iProj.velX;
                    iProj.y += iProj.velY;
                }

                for(int idx=0;idx<projectiles.size();idx++) {
                    if (projectiles.get(idx).y < 0) { // is projectile outside of screen
                        misses++;
                        System.out.println("miss");
                        projectiles.remove(idx);
                        idx--;
                    }
                }


                // move
                ballX += ballVelX;
                ballY += ballVelY;

                batPos += batVel;
                batPos = Math.min(batPos, 100-20/2);
                batPos = Math.max(batPos, 20/2);
            }
        }



        if (t%1 == 0) {
            // feed with goal
            String n = "goodnar! :|:";
            sendText(n, false);
        }

        { // give time to reason
            if (useRngAgent) {
                Random rng = new Random();
                if (rng.nextFloat() < 0.1) {
                    int c = rng.nextInt(3);
                    if (c == 0) {
                        queuedOp = "^up";
                    }
                    else if (c == 1) {
                        queuedOp = "^down";
                    }
                    else if (c == 2) {
                        queuedOp = "^pick";
                    }
                }
            }
            else {
                long t0 = System.nanoTime();
                narInferenceSteps(1);
                long dt = System.nanoTime() - t0;
                double timeInMs = dt / 1000000.0;
                System.out.println("reasoner time= "+timeInMs+" ms");
            }

            // do actions
            if (queuedOp != null && queuedOp.equals("^up")) {
                batVel = -7.0;
            }
            if (queuedOp != null && queuedOp.equals("^down")) {
                batVel = 7.0;
            }
            if (queuedOp != null && queuedOp.equals("^pick")) {
                // shoot
                projectiles.add(new Proj((float)batPos, 80, 0.0f, -8.0f));
            }
            queuedOp = null;
        }





        background(0);

        animationTime += 0.1f;

        int steps = 50; // how many steps are done?

        solver2.imageDrawer = new ptrman.exp.NarConSimpleWorld.InputDrawer();

        boolean useVision = true;
        if(useVision) {
            long t0 = System.nanoTime();

            solver2.preFrame(); // do all processing and setup before the actual processing of the frame
            for (int iStep=0;iStep<steps;iStep++) {
                solver2.frameStep(); // step of a frame
            }
            solver2.postFrame();

            long dt = System.nanoTime() - t0;
            double timeInMs = dt / 1000000.0;
            System.out.println("solver time= "+timeInMs+" ms");
        }


        { // draw processed image in the background
            pimg = ImageConverter.convBufferedImageToPImage((new ptrman.exp.NarConSimpleWorld.InputDrawer()).apply(null), pimg);
            tint(255.0f, 0.2f*255.0f);
            image(pimg, 0, 0); // draw image
            tint(255.0f, 255.0f); // reset tint
        }

        if(useVision) { // we only draw if we have vision
            drawer.drawPrimitives(solver2, this, classifier);
        }


        { // draw debug
            fill(255);
            text("hits"+hits + " misses="+misses + " t="+t  + "\nuseRngAgent="+useRngAgent, 10, 120);

            text(lastNarsese, 10, 160); // draw narsese state
        }
    }

    @Override
    public void settings() {
        size(400, 400);
    }

    public static void main(String[] passedArgs) {
        PApplet.main(new String[] { "ptrman.exp.NarConSimpleWorld" });
    }
}
