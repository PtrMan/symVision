package ptrman.levels.retina.nonFoundalis;

import boofcv.core.image.ConvertBufferedImage;
import boofcv.struct.image.ImageFloat32;
import boofcv.struct.image.MultiSpectral;
import org.junit.Test;
import ptrman.Datastructures.Vector2d;

import java.awt.image.BufferedImage;

/**
 *
 */
public class ProcessFractalFillerTest {
    public static class Extended extends ProcessFractalFiller {
        public void scanGridCell(final Vector2d<Integer> gridCellPosition, final Vector2d<Integer> entryDirection, final boolean[] entryPixels, ProcessFractalFiller.FillContext fillContext) {
            super.scanGridCell(gridCellPosition, entryDirection, entryPixels, fillContext);
        }
    }

    /**
     *
     * this test tests if the whole shape gets filled as it should be
     *
     * result looks like this:
     * Sxxx.....
     * xxx......
     * xx.......
     * x........
     * .........
     * .........
     * .........
     * .........
     * .........
     *
     */
    @Test
    public void singleCellTriangleCase() {
        Extended extended = new Extended();

        extended.setImageSize(new Vector2d<>(9, 9));

        // TODO< create image for testing >
        BufferedImage testImage = new BufferedImage(9,9, BufferedImage.TYPE_INT_RGB);
        testImage.setRGB(0, 0, 0xFFFFFFFF);
        testImage.setRGB(1, 0, 0xFFFFFFFF);
        testImage.setRGB(2, 0, 0xFFFFFFFF);
        testImage.setRGB(3, 0, 0xFFFFFFFF);

        testImage.setRGB(0, 1, 0xFFFFFFFF);
        testImage.setRGB(1, 1, 0xFFFFFFFF);
        testImage.setRGB(2, 1, 0xFFFFFFFF);

        testImage.setRGB(0, 2, 0xFFFFFFFF);
        testImage.setRGB(1, 2, 0xFFFFFFFF);

        testImage.setRGB(0, 3, 0xFFFFFFFF);

        MultiSpectral<ImageFloat32> inputImage = ConvertBufferedImage.convertFromMulti(testImage, null, true, ImageFloat32.class);

        extended.set(9, inputImage);

        // we start from the right most pixel which can fill it indirectly
        boolean[] entryPixelsForTest = new boolean[8];
        entryPixelsForTest[4] = true;

        ProcessFractalFiller.FillContext fillContext = new ProcessFractalFiller.FillContext();

        extended.scanGridCell(new Vector2d<>(0, 0), new Vector2d<>(0, 1), entryPixelsForTest, fillContext);
    }
}
