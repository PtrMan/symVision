package ptrman.Showcases;

import ptrman.Datastructures.Vector2d;
import ptrman.Gui.*;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.Parameters;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;

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

    static class ImageFileFilter implements FileFilter {

        public boolean accept(File file) {
            String extension = file.getName().toLowerCase();
            return extension.endsWith(".jpg") || extension.endsWith(".png") || extension.endsWith(".gif");
        }
    }

    public static void main(String[] args)
    {
        JFrame j = new JFrame("TestClustering");


        BpSolver bpSolver = new BpSolver();

        bpSolver.setImageSize(new Vector2d<>(100, 100));
        bpSolver.setup();

        Parameters.init();


        GraphWindow graphWindow = new GraphWindow();

        Controller.RecalculateActionListener recalculate = new Controller.RecalculateActionListener(bpSolver, graphWindow.getNodeGraph(), new InputDrawer());




        Timer timer = new Timer(1000, recalculate);
        timer.setInitialDelay(0);
        timer.start();

        Container panel = j.getContentPane();


        panel.setLayout(new BorderLayout());

        {
            JSplitPane s = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            s.setLeftComponent(recalculate.interactive = new Interactive());
            s.setRightComponent(graphWindow.getComponent());
            panel.add(s, BorderLayout.CENTER);
        }

        panel.add(new TuningWindow(), BorderLayout.SOUTH);
        {

            Component fc = FileChooser.newComponent(new File("/home/me/"), new ImageFileFilter(), true, f -> {
                System.out.println("loading " + f);
            });
            fc.setPreferredSize(new Dimension(200, 200));
            panel.add(fc, BorderLayout.WEST);
        }

        j.setSize(1024, 1000);
        j.setVisible(true);

        j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
