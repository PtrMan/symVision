package ptrman.Gui.showcase;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.*;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.ProcessA;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.io.File;
import java.io.FileFilter;

/**
 *
 */
public class AnimatedShowcase {
    /**
     *
     * Used for pushing the input image though filters and push it into the bpsolver.
     * Can do a other thing than this...
     *
     */
    public abstract static class RefreshAction {
        public abstract void preSetupSet(BpSolver bpSolver, IImageDrawer imageDrawer, IntrospectControlPanel introspectControlPanel, NodeGraph nodeGraph, DualConvas dualCanvas);
        public abstract void setup();
        public abstract void process();

        public BufferedImage resultLeftCanvasImage;
        public BufferedImage resultRightCanvasImage;
    }

    public static class NormalModeRefreshAction extends RefreshAction {
        private BufferedImage detectorImage;

        public void preSetupSet(BpSolver bpSolver, IImageDrawer imageDrawer,  IntrospectControlPanel introspectControlPanel, NodeGraph nodeGraph, DualConvas dualCanvas) {
            this.bpSolver = bpSolver;
            this.imageDrawer = imageDrawer;
            this.introspectControlPanel = introspectControlPanel;
            this.nodeGraph = nodeGraph;
        }

        @Override
        public void setup() {
            // setup the processing chain

            processingChain = new VisualProcessor.ProcessingChain();

            Dag.Element newDagElement = new Dag.Element(
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
        }

        public synchronized void process() {
            BufferedImage image;
            IMap2d<Boolean> mapBoolean;
            IMap2d<ColorRgb> mapColor;

            // TODO< pull image from source >
            // for now imageDrawer does this
            image = imageDrawer.drawToJavaImage(bpSolver);

            System.out.print("begin processing...");

            mapColor = AnimatedShowcase.translateFromImageToMap(image);

            processingChain.filterChain(mapColor);

            mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement) processingChain.filterChainDag.elements.get(1).content).result;

            System.out.println("begin symVision");

            bpSolver.recalculate(mapBoolean);

            if (introspectControlPanel.getIntrospectionState()) {
                nodeGraph.repopulateAfterNodes(bpSolver.lastFrameObjectNodes, bpSolver.networkHandles);
            }


            resultLeftCanvasImage = translateFromMapToImage(mapBoolean, resultLeftCanvasImage);


            if (detectorImage == null || detectorImage.getWidth()!=bpSolver.getImageSize().x || detectorImage.getHeight()!=bpSolver.getImageSize().y)
                detectorImage = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);


            Graphics2D detectorImageGraphics = (Graphics2D) detectorImage.getGraphics();

            detectorImageGraphics.setColor(Color.BLACK);
            detectorImageGraphics.fillRect(0,0,detectorImage.getWidth(), detectorImage.getHeight());


            // draw whit dots where the object ids are valid
            /*
            for( int y = 0; y < bpSolver.notMagnifiedOutputObjectIdsMapDebug.getLength(); y++ ) {
                for( int x = 0; x < bpSolver.notMagnifiedOutputObjectIdsMapDebug.getWidth(); x++ ) {
                    final int readObjectId = bpSolver.notMagnifiedOutputObjectIdsMapDebug.readAt(x, y);

                    graphics.setColor(Color.WHITE);

                    if( readObjectId != -1 ) {
                        graphics.fillRect(x, y, 1, 1);
                    }
                }
            }
            */

            // draw debugSamples, the color depends on the set objectId
            for (ProcessA.Sample iterationSample : bpSolver.debugSamples) {
                iterationSample.debugPlot(detectorImageGraphics);
            }


//            // TODO create graphics and draw it to a created image and put the image into the canvas
//            java.util.List<DebugDrawingHelper.DrawingEntity> drawingEntities = new ArrayList<>();
//            drawingEntities.add(new DebugDrawingHelper.SampleDrawingEntity(1, false, 40.0));
//
            // no drawing, we just look at the speed
//            DebugDrawingHelper.drawDetectors(detectorImageGraphics,
//                    bpSolver.lastFrameRetinaPrimitives,
//                    bpSolver.lastFrameIntersections,
//                    bpSolver.lastFrameEndosceletonSamples,
//                    //bpSolver.lastFrameExosceletonSamples);
//                    drawingEntities);
//
            resultRightCanvasImage = detectorImage;
        }

        private BpSolver bpSolver;
        private IImageDrawer imageDrawer;
        private IntrospectControlPanel introspectControlPanel;
        private NodeGraph nodeGraph;
        private VisualProcessor.ProcessingChain processingChain;
    }

    /**
     *
     * gets called when the next frame should be drawn
     *
     * delegates to the refresh action
     *
     */
    private static class FrameTask implements Runnable {
        DescriptiveStatistics frameTimes = new DescriptiveStatistics(32);

        public FrameTask(DualConvas dualCanvas, RefreshAction refreshAction) {
            this.dualCanvas = dualCanvas;
            this.refreshAction = refreshAction;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            refreshAction.process();
            long end = System.currentTimeMillis();

            float frameTime =  ((end - start) / 1000.0f);
            frameTimes.addValue(frameTime);

            System.out.println("frame: " + frameTime + "s (" + frameTimes.getMean() + " avg, " + frameTimes.getVariance() + " variance)");

            dualCanvas.leftCanvas.setImage(refreshAction.resultLeftCanvasImage);
            dualCanvas.rightCanvas.setImage(refreshAction.resultRightCanvasImage);
        }

        private DualConvas dualCanvas;
        private RefreshAction refreshAction;
    }

    static class ImageFileFilter implements FileFilter {

        public boolean accept(File file) {
            String extension = file.getName().toLowerCase();
            return extension.endsWith(".jpg") || extension.endsWith(".png") || extension.endsWith(".gif");
        }
    }

    public void setup(final String titleString, final Vector2d<Integer> imageSize, IImageDrawer inputDrawer, RefreshAction refreshAction) {
        JFrame j = new JFrame(titleString);

        BpSolver bpSolver = new BpSolver();
        bpSolver.setImageSize(imageSize);
        bpSolver.setup();

        Parameters.init();



        GraphWindow graphWindow = new GraphWindow();


        IntrospectControlPanel introspectControlPanel;

        introspectControlPanel = new IntrospectControlPanel();

        DualConvas dualCanvas = new DualConvas();

        refreshAction.preSetupSet(bpSolver, inputDrawer, introspectControlPanel, graphWindow.getNodeGraph(), dualCanvas);
        refreshAction.setup();

        FrameTask frame = new FrameTask(dualCanvas, refreshAction);
        //timer.setInitialDelay(0);
        //timer.start();

        Container panel = j.getContentPane();


        panel.setLayout(new BorderLayout());
        panel.add(dualCanvas, BorderLayout.CENTER);

        {

            final JPanel controls = new JPanel(new BorderLayout());

            final JPanel compsToExperiment = new JPanel(new GridLayout(2,1));

            compsToExperiment.add(introspectControlPanel.getPanel());
            compsToExperiment.add(graphWindow.getComponent());
            compsToExperiment.add(new TuningWindow());

            controls.add(compsToExperiment, BorderLayout.CENTER);

            Component fc = FileChooser.newComponent(new File("/tmp"), new ImageFileFilter(), true, f -> {
                currentFileImage = null;
                currentFile = f;
            });
            fc.setPreferredSize(new Dimension(imageSize.x, imageSize.y));
            controls.add(fc, BorderLayout.WEST);

            panel.add(controls, BorderLayout.SOUTH);
        }

        j.setSize(1024, 1000);
        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        j.setVisible(true);

        new Thread(() -> {
            for(;;) {
                frame.run();
            }
        }).start();
    }


    // TODO< move this into the functionality of the visual processor >
    public static IMap2d<ColorRgb> translateFromImageToMap(BufferedImage javaImage) {
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

    private static BufferedImage translateFromMapToImage(IMap2d<Boolean> map, BufferedImage result) {
        int x, y;

        if (result == null || result.getWidth()!=map.getWidth() || result.getHeight() != map.getLength())
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


    static protected File currentFile = null;
    static protected BufferedImage currentFileImage = null;

}
