/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Showcases;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.Solver;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * http://boofcv.org/index.php?title=Example_Webcam_Capture
 */
public class TestWebcam extends AnimatedShowcase {

    final static int RETINA_WIDTH = 64;
    final static int RETINA_HEIGHT = 64;
    private final Webcam webcam;
    private final ImagePanel gui = new ImagePanel();


    BufferedImage webcamFrame = null;

    private class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;


        @Override
        public BufferedImage apply(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }

            BufferedImage webcamFrame = TestWebcam.this.webcamFrame;
            if (webcamFrame == null)
                return off_Image;

            //BufferedImage webcamFrame = webcam.getImage();
//            GrayF32 gray = ConvertBufferedImage.convertFrom(webcamFrame, (GrayF32)null);

//            tracker.process(gray);
//
//            List<PointTrack> tracks = tracker.getActiveTracks(null);
//
//            // Spawn tracks if there are too few
//            if( tracks.size() < minimumTracks ) {
//                tracker.spawnTracks();
//                tracks = tracker.getActiveTracks(null);
//                minimumTracks = tracks.size()/2;
//            }

//            // Draw the tracks
             //Graphics2D g1 = webcamFrame.createGraphics();
//
//            for( PointTrack t : tracks ) {
//                VisualizeFeatures.drawPoint(g2, (int) t.x, (int) t.y, Color.RED);
//            }


            gui.setImageRepaint(webcamFrame);

            Graphics2D g2 = off_Image.createGraphics();

            g2.drawImage(webcamFrame, 0, 0, RETINA_WIDTH, RETINA_HEIGHT, null);

            g2.dispose();

            return off_Image;
        }

    }

    public TestWebcam() {
        super();


        // tune the tracker for the image size and visual appearance
        //ConfigGeneralDetector configDetector = new ConfigGeneralDetector(-1,8,1);
        //PkltConfig configKlt = new PkltConfig(3,new int[]{1,2,4,8});

        //PointTracker<ImageFloat32> tracker = FactoryPointTracker.klt(configKlt, configDetector, ImageFloat32.class, null);

        // Open a webcam at a resolution close to 640x480
        webcam = Webcam.getDefault(); //UtilWebcamCapture.openDefault(640,480);
        webcam.open(true);
        webcam.addWebcamListener(new WebcamListener() {
            private JFrame win;

            @Override
            public void webcamOpen(WebcamEvent we) {


                // Create the panel used to display the image and
                gui.setPreferredSize(webcam.getViewSize());
                win = ShowImages.showWindow(gui, "WebCAM");
            }

            @Override
            public void webcamClosed(WebcamEvent we) {
                win.setVisible(false);
            }

            @Override
            public void webcamDisposed(WebcamEvent we) {

            }

            @Override
            public void webcamImageObtained(WebcamEvent we) {
                webcamFrame = we.getImage();
            }
        });


        //int minimumTracks = 100;

        setup("TestWebcam", new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT), new InputDrawer(), new NormalModeRefreshAction());
    }

    public static void main(String[] args) {
        new TestWebcam();
    }
}
