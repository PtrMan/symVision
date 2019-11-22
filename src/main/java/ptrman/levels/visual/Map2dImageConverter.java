/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public enum Map2dImageConverter
{
	;

	public static IMap2d<ColorRgb> convertImageToMap(final BufferedImage image) {

		final var h = image.getHeight();
		final var w = image.getWidth();
		final IMap2d<ColorRgb> result = new Map2d<>(w, h);

        for (var y = 0; y < h; y++)
            for (var x = 0; x < w; x++)
				result.setAt(x, y, new ColorRgb(image.getRGB(x, y)));

        return result;
    }

    public static BufferedImage convertBooleanMapToImage(final IMap2d<Boolean> map)
    {

        final var resultImage = new BufferedImage(map.getWidth(), map.getLength(), TYPE_INT_ARGB);

        for(int y = 0; y < resultImage.getHeight(); y++ )
            for (int x = 0; x < resultImage.getWidth(); x++) {

                final boolean value = map.readAt(x, y);

                if (value)
                    resultImage.setRGB(x, y, 0xFFFFFFFF);
                else
                    resultImage.setRGB(x, y, 0);
            }

        return resultImage;
    }

    private static ColorRgb convertPixelToColor(final int pixelValue)
    {
        final var redInt = (pixelValue >> 16) & 0xff;
        final var greenInt = (pixelValue >> 8) & 0xff;
        final var blueInt = (pixelValue) & 0xff;

		final var red = (float) redInt / 255.0f;
		final var green = (float) greenInt / 255.0f;
		final var blue = (float) blueInt / 255.0f;

        return new ColorRgb(red, green, blue);
    }

}
