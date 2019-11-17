package ptrman.visualizationTests;

import processing.core.PApplet;
import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.Gui.IImageDrawer;
import ptrman.Showcases.TestClustering;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.Parameters;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.VisualProcessor;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;

// visualize linesegments of endosceleton
public class VisualizeLinesegments extends PApplet {

    final static int RETINA_WIDTH = 128;
    final static int RETINA_HEIGHT = 128;


    public class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(Solver bpSolver) {
            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            g2.setColor(Color.BLACK);

            g2.drawRect(0, 0, off_Image.getWidth(), off_Image.getHeight());

            g2.setColor(Color.WHITE);

            //g2.drawRect(2, 2, 100, 100);
            g2.fillRect(10, 10, 50, 100);

            return off_Image;
        }

        public void settings() {
            size(500, 500);
        }
    }

    public void draw(){
        background(64);

        {
            InputDrawer imageDrawer = new InputDrawer();

            BufferedImage image;
            IMap2d<Boolean> mapBoolean;
            IMap2d<ColorRgb> mapColor;


            Solver bpSolver = new Solver();
            bpSolver.setImageSize(new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT));
            bpSolver.setup();

            Parameters.init();

            // TODO< pull image from source >
            // for now imageDrawer does this
            image = imageDrawer.drawToJavaImage(bpSolver);



            mapColor = TestClustering.translateFromImageToMap(image);





            // setup the processing chain

            VisualProcessor.ProcessingChain processingChain = new VisualProcessor.ProcessingChain();

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
                            new VisualProcessor.ProcessingChain.DitheringFilter(),
                            "dither",
                            bpSolver.getImageSize()
                    )
            );

            processingChain.filterChainDag.elements.add(newDagElement);



            processingChain.filterChain(mapColor);

            mapBoolean = ((VisualProcessor.ProcessingChain.ApplyChainElement)processingChain.filterChainDag.elements.get(1).content).result;

            bpSolver.recalculate(mapBoolean);

            int s = bpSolver.connectorDetectorsEndosceletonFromProcessD.workspace.size();

            List<RetinaPrimitive> endosceletonLines = bpSolver.connectorDetectorsEndosceletonFromProcessD.workspace;
            for(RetinaPrimitive iLine : endosceletonLines) {
                double x0 = iLine.line.a.getDataRef()[0];
                double y0 = iLine.line.a.getDataRef()[1];
                double x1 = iLine.line.b.getDataRef()[0];
                double y1 = iLine.line.b.getDataRef()[1];
                line((float)x0, (float)y0, (float)x1, (float)y1);
            }

            int here = 5;
        }


        ellipse(mouseX, mouseY, 20, 20);
    }

    public static void main(String[] passedArgs) {
        String[] appletArgs = new String[] { "ptrman.visualizationTests.VisualizeLinesegments" };
        PApplet.main(appletArgs);
    }
}
