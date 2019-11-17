package ptrman.Showcases;

import boofcv.gui.image.ImagePanel;
import boofcv.gui.image.ShowImages;
import com.github.sarxos.webcam.Webcam;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.Solver;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * http://boofcv.org/index.php?title=Example_Webcam_Capture
 */
public class TestWebcam extends AnimatedShowcase {

    final static int RETINA_WIDTH = 64;
    final static int RETINA_HEIGHT = 64;
    private final Webcam webcam;
    private final ImagePanel gui;


    private class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }

            BufferedImage webcamFrame = webcam.getImage();
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

            if (webcamFrame!=null) {
                gui.setImageRepaint(webcamFrame);

                Graphics2D g2 = off_Image.createGraphics();

                g2.drawImage(webcamFrame, 0, 0, RETINA_WIDTH, RETINA_HEIGHT, null);

                g2.dispose();
            }

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

        //webcam.addWebcamListener()

        // Create the panel used to display the image and
        gui = new ImagePanel();
        gui.setPreferredSize(webcam.getViewSize());

        ShowImages.showWindow(gui, "WebCAM");

        //int minimumTracks = 100;

        setup("TestWebcam", new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT), new InputDrawer(), new NormalModeRefreshAction());
    }

    public static void main(String[] args) {
        new TestWebcam();
    }
}
