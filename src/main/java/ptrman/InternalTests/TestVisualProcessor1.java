package ptrman.InternalTests;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.visual.ColorRgb;
import ptrman.levels.visual.Map2dImageConverter;
import ptrman.levels.visual.VisualProcessor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TestVisualProcessor1
{
    public static void main(String[] args)
    {
        Vector2d<Integer> imageSize;
        IMap2d<ColorRgb> inputMap;
        IMap2d<Boolean> zeroCrossingsMap;
        BufferedImage resultImage;

        BufferedImage img = null;
        try {
            img = ImageIO.read(new File("/home/r0b3/flowerMini.png"));
        } catch (IOException e) {
            int x = 0;
        }

        imageSize = new Vector2d<>(img.getWidth(), img.getHeight());

        VisualProcessor.ProcessingChain processingChain;

        processingChain = new VisualProcessor.ProcessingChain();
        processingChain.setup(imageSize, new ColorRgb(1.0f, 1.0f, 1.0f), new VisualProcessor.ProcessingChain.MarrHildrethOperatorParameter(10, 0.25f));

        inputMap = Map2dImageConverter.convertImageToMap(img);

        processingChain.filterChain(inputMap);

        zeroCrossingsMap = processingChain.getZeroCrossingBinary();
        resultImage = Map2dImageConverter.convertBooleanMapToImage(zeroCrossingsMap);

        File f = new File("/home/r0b3/MyFile.png");
        try {
            ImageIO.write(resultImage, "PNG", f);
        }
        catch(IOException e) {
        }
    }
}
