package ptrman.Showcases;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageFloat32;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Additional.CohesionParticleTracker;
import ptrman.Additional.ParticleFlowTracker;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.DualConvas;
import ptrman.Gui.IImageDrawer;
import ptrman.Gui.IntrospectControlPanel;
import ptrman.Gui.NodeGraph;
import ptrman.Gui.showcase.AnimatedShowcase;
import ptrman.bpsolver.BpSolver;
import ptrman.levels.visual.VisualProcessor;
import sun.awt.Mutex;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

// TODO
/**
 *
 */
public class TestMotion extends AnimatedShowcase {
    final static int RETINA_WIDTH = 1080;
    final static int RETINA_HEIGHT = 720;


    private class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(BpSolver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            // TODO< change path >
            currentFile = new File("/media/r0b3/Seagate Expansion Drive/"+ "output_" + String.format("%05d", 1+frameCounter) + ".jpg");

            frameCounter++;
            frameCounter = frameCounter % (14140);

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

        private int frameCounter = 0;
    }

    private static class RefreshAction extends AnimatedShowcase.RefreshAction {

        public RefreshAction(TestMotion testMotion) {
            this.testMotion = testMotion;
        }

        public void preSetupSet(BpSolver bpSolver, IImageDrawer imageDrawer,  IntrospectControlPanel introspectControlPanel, NodeGraph nodeGraph, DualConvas dualCanvas) {
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
            BufferedImage image = imageDrawer.drawToJavaImage(bpSolver);


            BufferedImage copiedImage = deepCopyBufferedImage(image);

            testMotion.stepWithInputImage(image);

            drawParticlesAndConnectionsToImage(copiedImage, testMotion.cohesionParticleTracker);


            resultLeftCanvasImage = copiedImage;



            resultRightCanvasImage = null;
        }

        private void drawParticlesAndConnectionsToImage(BufferedImage targetImage, CohesionParticleTracker cohesionParticleTracker) {

            testMotion.particleMutex.lock();

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

            testMotion.particleMutex.unlock();

        }

        private static BufferedImage deepCopyBufferedImage(BufferedImage bi) {
            ColorModel cm = bi.getColorModel();
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            WritableRaster raster = bi.copyData(null);
            return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
        }

        private BpSolver bpSolver;
        private IImageDrawer imageDrawer;
        private IntrospectControlPanel introspectControlPanel;
        private NodeGraph nodeGraph;
        private VisualProcessor.ProcessingChain processingChain;

        private final TestMotion testMotion;
    }

    public void stepWithInputImage(BufferedImage image) {
        final ImageFloat32 convertedImage = convertImageToGrayscaleImageFloat32(image);

        if( framecounter == 0 ) {
            particleFlowTracker.firstImage(convertedImage);
        }
        else {
            particleFlowTracker.step(convertedImage);
        }

        cohesionParticleTracker.step();

        framecounter++;
    }

    private static ImageFloat32 convertImageToGrayscaleImageFloat32(BufferedImage image) {
        ImageFloat32 resultImage = new ImageFloat32(image.getWidth(), image.getHeight());
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

    private ParticleFlowTracker<CohesionParticleTracker.Particle> particleFlowTracker;
    private CohesionParticleTracker cohesionParticleTracker;

    private Mutex particleMutex = new Mutex();
}
