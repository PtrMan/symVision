package ptrman.levels.retina.nonFoundalis;


import boofcv.io.image.ConvertBufferedImage;
import boofcv.struct.image.InterleavedF32;
import org.eclipse.collections.api.tuple.primitive.IntIntPair;
import org.junit.Test;
import ptrman.Datastructures.Vector2d;

import java.awt.image.BufferedImage;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

/**
 *
 */
public class ProcessFractalFillerTest {
    public static class Extended extends ProcessFractalFiller {
        public void scanGridCell(final IntIntPair gridCellPosition, final IntIntPair entryDirection, final boolean[] entryPixels, ProcessFractalFiller.FillContext fillContext) {
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

        extended.setImageSize(new Vector2d<>(64, 64));

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

        InterleavedF32 inputImage = new InterleavedF32();
        ConvertBufferedImage.convertFromInterleaved(testImage,  inputImage, true);

        extended.set(9, inputImage);

        // we start from the right most pixel which can fill it indirectly
        boolean[] entryPixelsForTest = new boolean[8];
        entryPixelsForTest[4] = true;

        ProcessFractalFiller.FillContext fillContext = new ProcessFractalFiller.FillContext();

        extended.scanGridCell(pair(0, 0), pair(0, 1), entryPixelsForTest, fillContext);
    }
}
