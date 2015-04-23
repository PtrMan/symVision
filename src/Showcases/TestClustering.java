package Showcases;

import Datastructures.Vector2d;
import Gui.*;
import bpsolver.BpSolver;
import bpsolver.Parameters;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 *
 */
public class TestClustering
{
    private static class InputDrawer implements IImageDrawer
    {

        @Override
        public BufferedImage drawToJavaImage(float time, BpSolver bpSolver)
        {
            time *= 0.1f;

            BufferedImage off_Image = new BufferedImage(bpSolver.getImageSize().x, bpSolver.getImageSize().y, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = off_Image.createGraphics();
            g2.setColor(Color.BLACK);

            g2.drawLine(10, 10, 20, 40);

            g2.drawLine(20, 40, 30, 10);

            ///drawTestTriangle(g2, new Vector2d<>(20.0f, 60.0f), 10.0f, time, (3.0f / (float)Math.sqrt(3)));

            //drawTestTriangle(g2, new Vector2d<>(60.0f, 60.0f), 10.0f, time * 0.114f, 0.5f*(3.0f / (float)Math.sqrt(3)));



            return off_Image;
        }
    }

    public static void main(String[] args)
    {
        BpSolver bpSolver = new BpSolver();

        bpSolver.setImageSize(new Vector2d<>(100, 100));

        Parameters.init();


        GraphWindow graphWindow = new GraphWindow();

        Controller.RecalculateActionListener recalculate = new Controller.RecalculateActionListener(bpSolver, graphWindow.getNodeGraph(), new InputDrawer());
        recalculate.interactive = new Interactive();

        TuningWindow tuningWindow = new TuningWindow();

        Timer timer = new Timer(2000, recalculate);
        timer.setInitialDelay(0);
        timer.start();
    }
}
