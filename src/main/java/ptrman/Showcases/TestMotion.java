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

import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.GrayF32;
import ptrman.Additional.CohesionParticleTracker;
import ptrman.Additional.ParticleFlowTracker;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.DualConvas;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.IntrospectControlPanel;
import ptrman.Gui.NodeGraph;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.Solver;
import ptrman.levels.visual.VisualProcessor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

// TODO
/**
 *
 */
public class TestMotion extends AnimatedShowcase {
    final static int RETINA_WIDTH = 1080;
    final static int RETINA_HEIGHT = 720;


    private static class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage apply(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            // TODO< change path >
            currentFile = null;//new File("/media/r0b3/Seagate Expansion Drive/"+ "output_" + String.format("%05d", 1+frameCounter) + ".jpg");

            frameCounter++;
            frameCounter = frameCounter % (14140);

            if (currentFile != null) { // HACK< ignore file when we didn't read a image, we can do this because we test with synthetic images anyways >
                try {
                    currentFileImage = ImageIO.read(currentFile);
                    g2.drawImage(currentFileImage, 0, 0, RETINA_WIDTH, RETINA_HEIGHT, null);
                    System.out.println("painted: "+ currentFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    currentFile = null;
                }
            }


            return off_Image;
        }

        private int frameCounter = 0;
    }

    private static class RefreshAction extends AnimatedShowcase.RefreshAction {

        public RefreshAction(TestMotion testMotion) {
            this.testMotion = testMotion;
        }

        public void preSetupSet(Solver bpSolver, Function<Solver, BufferedImage> imageDrawer, IntrospectControlPanel introspectControlPanel, NodeGraph nodeGraph, DualConvas dualCanvas) {
            this.bpSolver = bpSolver;
            this.imageDrawer = imageDrawer;
            this.introspectControlPanel = introspectControlPanel;
            this.nodeGraph = nodeGraph;
        }

        @Override
        public void setup() {
        }

        public void process(float throttle) {
            // TODO< pull image from source >
            // for now imageDrawer does this
            BufferedImage image = imageDrawer.apply(bpSolver);


            BufferedImage copiedImage = deepCopyBufferedImage(image);

            testMotion.stepWithInputImage(image);

            drawParticlesAndConnectionsToImage(copiedImage, testMotion.cohesionParticleTracker);


            resultLeftCanvasImage = copiedImage;



            resultRightCanvasImage = null;
        }

        private void drawParticlesAndConnectionsToImage(BufferedImage targetImage, CohesionParticleTracker cohesionParticleTracker) {

            //testMotion.particleMutex.lock();

            /*
            Graphics2D graphics = (Graphics2D)targetImage.getGraphics();

            graphics.setColor(Color.RED);

            for( CohesionParticleTracker.Particle iterationParticle : cohesionParticleTracker.graph.getNodes() ) {
                final ArrayRealVector particlePosition = iterationParticle.getPosition();
                int positionX = (int)particlePosition.getDataRef()[0];
                int positionY = (int)particlePosition.getDataRef()[1];

                graphics.fillOval(positionX-1, positionY-1, 2, 2);
            }

            graphics.setColor(Color.WHITE);

            for( CohesionParticleTracker.Particle iterationParticle : cohesionParticleTracker.graph.getNodes() ) {
                for( final CohesionParticleTracker.CohesionEdge iterationEdge : cohesionParticleTracker.graph.getAdjacentEdges(iterationParticle) ) {
                    if( !iterationEdge.getSourceNode().equals(iterationParticle) ) {
                        continue;
                    }

                    CohesionParticleTracker.Particle destinationParticle = iterationEdge.getDestinationNode();

                    int sourcePositionX = (int)iterationParticle.getPosition().getDataRef()[0];
                    int sourcePositionY = (int)iterationParticle.getPosition().getDataRef()[1];
                    int destinationPositionX = (int)destinationParticle.getPosition().getDataRef()[0];
                    int destinationPositionY = (int)destinationParticle.getPosition().getDataRef()[1];

                    graphics.drawLine(sourcePositionX, sourcePositionY, destinationPositionX, destinationPositionY);
                }

            }
            */

            //testMotion.particleMutex.unlock();

        }

        private static BufferedImage deepCopyBufferedImage(RenderedImage bi) {
            ColorModel cm = bi.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = bi.copyData(null);
            return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        }

        private Solver bpSolver;
        private Function<Solver, BufferedImage> imageDrawer;
        private IntrospectControlPanel introspectControlPanel;
        private NodeGraph nodeGraph;
        private VisualProcessor.ProcessingChain processingChain;

        private final TestMotion testMotion;
    }

    public void stepWithInputImage(BufferedImage image) {
        final GrayF32 convertedImage = convertImageToGrayscaleImageFloat32(image);

        if( framecounter == 0 ) {
            particleFlowTracker.firstImage(convertedImage);
        }
        else {
            particleFlowTracker.step(convertedImage);
        }

        cohesionParticleTracker.step();

        framecounter++;
    }

    private static GrayF32 convertImageToGrayscaleImageFloat32(BufferedImage image) {
        GrayF32 resultImage = new GrayF32(image.getWidth(), image.getHeight());
        ConvertBufferedImage.convertFrom(image, resultImage);
        return resultImage;
    }

    public TestMotion() {
        super();

        cohesionParticleTracker = new CohesionParticleTracker(new Vector2d(RETINA_WIDTH, RETINA_HEIGHT));

        ParticleFlowTracker.IParticleConstructorDestructor<CohesionParticleTracker.Particle> particleConstructorDestructor = new CohesionParticleTracker.CohesionParticleTrackerParticleConstructorDestructor(cohesionParticleTracker);
        final int IMAGE_DOWNSCALE_FACTOR = 8;
        particleFlowTracker = new ParticleFlowTracker(particleConstructorDestructor, new Vector2d(RETINA_WIDTH, RETINA_HEIGHT), IMAGE_DOWNSCALE_FACTOR, particleMutex);

        setup("TestMotion", new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT), new InputDrawer(), new RefreshAction(this));
    }

    public static void main(String[] args) {
        new TestMotion();
    }

    private int framecounter = 0;

    private final ParticleFlowTracker<CohesionParticleTracker.Particle> particleFlowTracker;
    private final CohesionParticleTracker cohesionParticleTracker;

    private final Semaphore particleMutex = new Semaphore(1);
}
