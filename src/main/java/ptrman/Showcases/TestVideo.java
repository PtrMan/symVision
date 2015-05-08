package ptrman.Showcases;

import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.*;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.Parameters;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 *
 */
public class TestVideo {

    final static int RETINA_WIDTH = 40*8;
    final static int RETINA_HEIGHT = 160;

    static File currentFile = null;
    static BufferedImage currentFileImage = null;


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

        private int frameCounter = 51;
    }

    static class ImageFileFilter implements FileFilter {

        public boolean accept(File file) {
            String extension = file.getName().toLowerCase();
            return extension.endsWith(".jpg") || extension.endsWith(".png") || extension.endsWith(".gif");
        }
    }

    /**
     *
     * gets called when the next frame should be drawn
     *
     * delegates to all parts
     *
     */
    private static class TimerActionListener implements ActionListener {



        public TimerActionListener(BpSolver bpSolver, IImageDrawer imageDrawer,  IntrospectControlPanel introspectControlPanel, NodeGraph nodeGraph, DualConvas dualCanvas, VisualProcessor.ProcessingChain processingChain) {
            this.bpSolver = bpSolver;
            this.imageDrawer = imageDrawer;
            this.introspectControlPanel = introspectControlPanel;
            this.nodeGraph = nodeGraph;
            this.dualCanvas = dualCanvas;
            this.processingChain = processingChain;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            BufferedImage image;
            IMap2d<Boolean> mapBoolean;
            IMap2d<ColorRgb> mapColor;

            // TODO< pull image from source >
            // for now imageDrawer does this
            image = imageDrawer.drawToJavaImage(bpSolver);

            System.out.println("begin processing");

            mapColor = TestVideo.translateFromImageToMap(image);

            processingChain.filterChain(mapColor);

            mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement)processingChain.filterChainDag.elements.get(1).content).result;

            System.out.println("begin symVision");

            bpSolver.recalculate(mapBoolean);

            if( introspectControlPanel.getIntrospectionState() )
            {
                nodeGraph.repopulateAfterNodes(bpSolver.lastFrameObjectNodes, bpSolver.networkHandles);
            }

            System.out.println("end all");

            dualCanvas.leftCanvas.setImage(translateFromMapToImage(mapBoolean));

            BufferedImage detectorImage;

            detectorImage = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphics = (Graphics2D)detectorImage.getGraphics();

            // TODO create graphics and draw it to a created image and put the image into the canvas
            DebugDrawingHelper.drawDetectors(graphics, bpSolver.lastFrameRetinaPrimitives, bpSolver.lastFrameIntersections, bpSolver.lastFrameSamples);

            dualCanvas.rightCanvas.setImage(detectorImage);
        }

        private BpSolver bpSolver;
        private final IImageDrawer imageDrawer;
        private IntrospectControlPanel introspectControlPanel;
        private NodeGraph nodeGraph;
        private DualConvas dualCanvas;
        private final VisualProcessor.ProcessingChain processingChain;
    }

    public TestVideo() {
        JFrame j = new JFrame("TestVideo");



        BpSolver bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT));
        bpSolver.setup();

        Parameters.init();


        VisualProcessor.ProcessingChain processingChain;

        // setup the processing chain

        processingChain = new VisualProcessor.ProcessingChain();

        Dag.Element newDagElement;

        newDagElement = new Dag.Element(
                new VisualProcessor.ProcessingChain.ChainElementColorFloat(
                        new VisualProcessor.ProcessingChain.ConvertColorRgbToGrayscaleFilter(new ColorRgb(1.0f, 1.0f, 1.0f)),
                        "convertRgbToGrayscale",
                        bpSolver.getImageSize()
                )
        );
        newDagElement.childIndices.add(1);

        processingChain.filterChainDag.elements.add(newDagElement);


        newDagElement = new Dag.Element(
                new VisualProcessor.ProcessingChain.ChainElementFloatBoolean(
                        new VisualProcessor.ProcessingChain.ThresholdFilter(0.4f),
                        "threshold",
                        bpSolver.getImageSize()
                )
        );

        processingChain.filterChainDag.elements.add(newDagElement);


        GraphWindow graphWindow = new GraphWindow();


        IntrospectControlPanel introspectControlPanel;

        introspectControlPanel = new IntrospectControlPanel();

        DualConvas dualCanvas = new DualConvas();

        TimerActionListener actionListener = new TimerActionListener(bpSolver, new InputDrawer(), introspectControlPanel, graphWindow.getNodeGraph(), dualCanvas, processingChain);
        //timer.setInitialDelay(0);
        //timer.start();

        Container panel = j.getContentPane();


        panel.setLayout(new BorderLayout());

        {




            GridLayout experimentLayout = new GridLayout(3,1);

            final JPanel compsToExperiment = new JPanel();
            compsToExperiment.setLayout(experimentLayout);

            compsToExperiment.add(introspectControlPanel.getPanel());
            compsToExperiment.add(dualCanvas);
            compsToExperiment.add(graphWindow.getComponent());

            panel.add(compsToExperiment, BorderLayout.CENTER);
        }

        panel.add(new TuningWindow(), BorderLayout.SOUTH);
        {

            Component fc = FileChooser.newComponent(new File("/tmp"), new ImageFileFilter(), true, f -> {
                currentFileImage = null;
                currentFile = f;
            });
            fc.setPreferredSize(new Dimension(RETINA_WIDTH, RETINA_HEIGHT));
            panel.add(fc, BorderLayout.WEST);
        }

        j.setSize(1024, 1000);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setVisible(true);

        for(;;) {
            actionListener.actionPerformed(null);
        }

    }

    public static void main(String[] args) {
        new TestVideo();
    }



    // TODO< move this into the functionality of the visual processor >
    private static IMap2d<ColorRgb> translateFromImageToMap(BufferedImage javaImage) {
        DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

        int bufferI;
        IMap2d<ColorRgb> convertedToMap;

        convertedToMap = new Map2d<>(javaImage.getWidth(), javaImage.getHeight());

        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
        {
            int pixelValue;

            pixelValue = javaImage.getRGB(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth());

            Color c = new Color(pixelValue);

            int r = c.getRed();
            int g = c.getGreen();
            int b = c.getBlue();

            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), new ColorRgb((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f));
        }

        return convertedToMap;
    }

    private static BufferedImage translateFromMapToImage(IMap2d<Boolean> map) {
        BufferedImage result;
        int x, y;

        result = new BufferedImage(map.getWidth(), map.getLength(), BufferedImage.TYPE_INT_ARGB);

        for( y = 0; y < map.getLength(); y++ ) {
            for( x = 0; x < map.getWidth(); x++ ) {
                boolean booleanValue;

                booleanValue = map.readAt(x, y);

                if( booleanValue ) {
                    result.setRGB(x, y, 0xffffffff);
                }
                else {
                    result.setRGB(x, y, 0xff000000);
                }
            }
        }

        return result;
    }
}
