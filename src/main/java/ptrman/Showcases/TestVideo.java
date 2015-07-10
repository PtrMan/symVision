package ptrman.Showcases;

import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.BpSolver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 *
 */
public class TestVideo extends AnimatedShowcase {

    final static int RETINA_WIDTH = 40*8;
    final static int RETINA_HEIGHT = 160;

    private class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(BpSolver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            currentFile = new File("/home/r0b3/kdenlive/"+ "output_" + String.format("%05d", 1+frameCounter) + ".jpg");

            frameCounter++;
            frameCounter = frameCounter % (2022-1);

            try {
                currentFileImage = ImageIO.read(currentFile);
                g2.drawImage(currentFileImage, 0, 0, RETINA_WIDTH, RETINA_HEIGHT, null);
                System.out.println("painted: "+ currentFile);
            } catch (IOException e) {
                e.printStackTrace();
                currentFile = null;
            }

            return off_Image;
        }

        private int frameCounter = 80;
    }

    public TestVideo() {
        super();
        setup("TestVideo", new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT), new InputDrawer(), new AnimatedShowcase.NormalModeRefreshAction());
    }

    public static void main(String[] args) {
        new TestVideo();
    }
}
