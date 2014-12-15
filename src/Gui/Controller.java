package Gui;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.add;
import static Datastructures.Vector2d.FloatHelper.getScaled;
import static Datastructures.Vector2d.FloatHelper.sub;
import RetinaLevel.ProcessA;
import RetinaLevel.ProcessB;
import RetinaLevel.ProcessC;
import RetinaLevel.ProcessD;
import RetinaLevel.ProcessH;
import bpsolver.Parameters;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.util.ArrayList;
import javax.swing.Timer;

public class Controller
{
    private static class RecalculateActionListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent ae) {
            recalculate();
        }
        
        private void recalculate()
        {
            BufferedImage javaImage = drawToJavaImage((float)time * 0.1f);
            Map2d<Boolean> image = drawToImage(javaImage);

            ProcessA processA = new ProcessA();
            ProcessB processB = new ProcessB();
            ProcessC processC = new ProcessC();
            ProcessD processD = new ProcessD();
            ProcessH processH = new ProcessH();

            processA.setWorkingImage(image);
            ArrayList<ProcessA.Sample> samples = processA.sampleImage();


            processB.process(samples, image);
            processC.process(samples);

            ArrayList<ProcessD.SingleLineDetector> lineDetectors = processD.detectLines(samples);
            
            // can be commented/disabled if we want to debug the raw lines
            processH.process(lineDetectors);

            BufferedImage detectorImage = drawDetectors(lineDetectors, samples);


            interactive.leftCanvas.setImage(javaImage);
            interactive.rightCanvas.setImage(detectorImage);
            
            // TODO< convert retina level information to workspace nodes >
            // TODO< process workspace nodes >
            
            time++;
        }
        
        
        
        // function in here because we don't know who should have it
        private static BufferedImage drawToJavaImage(float time)
        {
            BufferedImage off_Image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2 = off_Image.createGraphics();
            g2.setColor(Color.BLACK);

            //g2.drawLine(10, 10, 30, 20);

            //g2.drawLine(20, 30, 30, 20);
            
            drawTestTriangle(g2, new Vector2d<>(20.0f, 60.0f), 10.0f, time, (3.0f / (float)Math.sqrt(3)));
            
            //drawTestTriangle(g2, new Vector2d<>(60.0f, 60.0f), 10.0f, time * 0.114f, 0.5f*(3.0f / (float)Math.sqrt(3)));

            return off_Image;
        }
        
        private static void drawTestTriangle(Graphics2D graphics, Vector2d<Float> position, float radius, float angle, float relativeSegmentWidth)
        {
            drawTrianglePart(graphics, position, radius, angle                                      , radius * relativeSegmentWidth);
            drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (1.0f/3.0f)), radius * relativeSegmentWidth);
            //test drawTrianglePart(graphics, position, radius, angle + (2.0f*(float)Math.PI * (2.0f/3.0f)), radius * relativeSegmentWidth);
        }
        
        private static void drawTrianglePart(Graphics2D graphics, Vector2d<Float> center, float radius, float angle, float segmentWidth)
        {
            Vector2d<Float> tangent, scaledTangent;
            Vector2d<Float> normal, scaledNormal;
            Vector2d<Float> pointA, pointB;
            Vector2d<Integer> pointAInt, pointBInt;
            
            normal = new Vector2d<Float>((float)Math.sin(angle), (float)Math.cos(angle));
            tangent = new Vector2d<Float>(normal.y, -normal.x);
            
            scaledNormal = getScaled(normal, radius);
            scaledTangent = getScaled(tangent, segmentWidth);
            
            pointA = add(center, add(scaledNormal, scaledTangent));
            pointB = add(center, sub(scaledNormal, scaledTangent));
            
            pointAInt = new Vector2d<>(Math.round(pointA.x), Math.round(pointA.y));
            pointBInt = new Vector2d<>(Math.round(pointB.x), Math.round(pointB.y));
            
            graphics.drawLine(pointAInt.x, pointAInt.y, pointBInt.x, pointBInt.y);
        }

        private static Map2d<Boolean> drawToImage(BufferedImage javaImage)
        {
            DataBuffer imageBuffer = javaImage.getData().getDataBuffer();

            int bufferI;
            Map2d<Boolean> convertedToMap;

            convertedToMap = new Map2d<Boolean>(javaImage.getWidth(), javaImage.getHeight());

            for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
            {
                boolean convertedPixel;

                convertedPixel = imageBuffer.getElem(bufferI) != 0;
                convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(), convertedPixel);
            }

            return convertedToMap;
        }

        private static BufferedImage drawDetectors(ArrayList<ProcessD.SingleLineDetector> lineDetectors, ArrayList<ProcessA.Sample> samples)
        {
            BufferedImage resultImage;
            Graphics2D graphics;

            resultImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);

            graphics = resultImage.createGraphics();
            

            for( ProcessD.SingleLineDetector iterationDetector : lineDetectors )
            {
                Vector2d<Float> aProjectedFloat;
                Vector2d<Float> bProjectedFloat;

                aProjectedFloat = iterationDetector.aFloat;
                bProjectedFloat = iterationDetector.bFloat;
                
                if( iterationDetector.resultOfCombination )
                {
                    graphics.setColor(Color.ORANGE);
                }
                else
                {
                    graphics.setColor(Color.RED);
                }
                
                graphics.drawLine(Math.round(aProjectedFloat.x), Math.round(aProjectedFloat.y), Math.round(bProjectedFloat.x), Math.round(bProjectedFloat.y));
            }

            /*
            graphics.setColor(Color.GREEN);

            for( ProcessA.Sample iterationSample : samples )
            {
                graphics.drawLine(iterationSample.position.x, iterationSample.position.y, iterationSample.position.x, iterationSample.position.y);
            }
            */

            return resultImage;
        }
        
        private Interactive interactive;
        private int time;
    }
    
    public static void main(String[] args) {
        Parameters.init();
        
        RecalculateActionListener recalculate = new RecalculateActionListener();
        recalculate.interactive = new Interactive();
        
        TuningWindow tuningWindow = new TuningWindow();
        
        Timer timer = new Timer(50, recalculate);
        timer.setInitialDelay(0);
        timer.start(); 
    }
}
