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

import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.Solver;

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

    private static class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            currentFile = new File("/tmp/"+ "output_" + String.format("%05d", 1+frameCounter) + ".jpg");

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
