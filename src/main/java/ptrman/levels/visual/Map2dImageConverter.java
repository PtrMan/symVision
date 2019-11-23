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

	public static IMap2d<ColorRgb> convertImageToMap(BufferedImage image) {

		return BufferedImageMap2D.rgb(image);

//		int h = image.getHeight();
//		int w = image.getWidth();
//		IMap2d<ColorRgb> result = new Map2d<>(w, h);
//
//        for (int y = 0; y < h; y++)
//            for (int x = 0; x < w; x++)
//				result.setAt(x, y, new ColorRgb(image.getRGB(x, y)));
//
//        return result;
//        DataBuffer imageBuffer = javaImage.getData().getDataBuffer();
//
//        int bufferI;
//
//        IMap2d<ColorRgb> convertedToMap = new Map2d<>(javaImage.getWidth(), javaImage.getHeight());
//
//        for( bufferI = 0; bufferI < imageBuffer.getSize(); bufferI++ )
//        {
//
//            int pixelValue = javaImage.getRGB(bufferI % convertedToMap.getWidth(), bufferI / convertedToMap.getWidth());
//
//
//            convertedToMap.setAt(bufferI%convertedToMap.getWidth(), bufferI/convertedToMap.getWidth(),
//                //new ColorRgb((float)r / 255.0f, (float)g / 255.0f, (float)b / 255.0f)
//                new ColorRgb(pixelValue)
//            );
//        }
//
//        return convertedToMap;

    }

    public static BufferedImage convertBooleanMapToImage(IMap2d<Boolean> map)
    {
		int x, y;

		BufferedImage resultImage = new BufferedImage(map.getWidth(), map.getLength(), TYPE_INT_ARGB);

        for( y = 0; y < resultImage.getHeight(); y++ )
        {
            for( x = 0; x < resultImage.getWidth(); x++ )
            {

				boolean value = map.readAt(x, y);

                if( value )
                {
                    resultImage.setRGB(x, y, 0xFFFFFFFF);
                }
                else
                {
                    resultImage.setRGB(x, y, 0);
                }
            }
        }

        return resultImage;
    }

//    private static ColorRgb convertPixelToColor(int pixelValue)
//    {
//        int redInt = (pixelValue >> 16) & 0xff;
//        int greenInt = (pixelValue >> 8) & 0xff;
//        int blueInt = (pixelValue) & 0xff;
//
//		float red = (float) redInt / 255.0f;
//		float green = (float) greenInt / 255.0f;
//		float blue = (float) blueInt / 255.0f;
//
//        return new ColorRgb(red, green, blue);
//    }

}
