package ptrman.Showcases;

import ptrman.Datastructures.Vector2d;
import ptrman.Gui.*;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.Parameters;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 *
 */
public class TestClustering {

    final static int RETINA_WIDTH = 100;
    final static int RETINA_HEIGHT = 100;

    static File currentFile = null;
    static BufferedImage currentFileImage = null;


    private class InputDrawer implements IImageDrawer {

        BufferedImage off_Image;

        @Override
        public BufferedImage drawToJavaImage(float time, BpSolver bpSolver) {
            time *= 0.1f;


            if (off_Image == null || off_Image.getWidth() != RETINA_WIDTH || off_Image.getHeight() != RETINA_HEIGHT) {
                off_Image = new BufferedImage(RETINA_WIDTH, RETINA_HEIGHT, BufferedImage.TYPE_INT_ARGB);
            }
            Graphics2D g2 = off_Image.createGraphics();

            if (currentFile == null) {

                g2.setColor(Color.BLACK);

                g2.drawLine(10, 10, 20, 40);

                g2.drawLine(20, 40, 30, 10);

                ///drawTestTriangle(g2, new Vector2d<>(20.0f, 60.0f), 10.0f, time, (3.0f / (float)Math.sqrt(3)));

                //drawTestTriangle(g2, new Vector2d<>(60.0f, 60.0f), 10.0f, time * 0.114f, 0.5f*(3.0f / (float)Math.sqrt(3)));

            } else {
                if (currentFileImage == null) {
                    try {
                        currentFileImage = ImageIO.read(currentFile);
                        g2.drawImage(currentFileImage, 0, 0, RETINA_WIDTH, RETINA_HEIGHT, null);
                        System.out.println("painted: "+ currentFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                        currentFile = null;
                    }
                }

            }


            return off_Image;
        }
    }

    static class ImageFileFilter implements FileFilter {

        public boolean accept(File file) {
            String extension = file.getName().toLowerCase();
            return extension.endsWith(".jpg") || extension.endsWith(".png") || extension.endsWith(".gif");
        }
    }

    public TestClustering() {
        JFrame j = new JFrame("TestClustering");


        BpSolver bpSolver = new BpSolver();
        bpSolver.setImageSize(new Vector2d<>(RETINA_WIDTH, RETINA_HEIGHT));
        bpSolver.setup();

        Parameters.init();


        GraphWindow graphWindow = new GraphWindow();

        Controller.RecalculateActionListener recalculate = new Controller.RecalculateActionListener(bpSolver, graphWindow.getNodeGraph(),
                new InputDrawer());


        Timer timer = new Timer(1000, recalculate);
        timer.setInitialDelay(0);
        timer.start();

        Container panel = j.getContentPane();


        panel.setLayout(new BorderLayout());

        {
            JSplitPane s = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            s.setLeftComponent(recalculate.setInteractive(new Interactive()));
            s.setRightComponent(graphWindow.getComponent());
            panel.add(s, BorderLayout.CENTER);
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

    }

    public static void main(String[] args) {
        new TestClustering();
    }
}
