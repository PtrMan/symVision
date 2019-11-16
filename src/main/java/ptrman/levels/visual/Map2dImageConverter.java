package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;

import java.awt.image.BufferedImage;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public enum Map2dImageConverter
{
	;

	public static IMap2d<ColorRgb> convertImageToMap(BufferedImage image)
    {
        IMap2d<ColorRgb> result;

        result = new Map2d<>(image.getWidth(), image.getHeight());

        for (int i = 0; i < image.getHeight(); i++)
        {
            for (int j = 0; j < image.getWidth(); j++)
            {
                ColorRgb colorRgb;
                int pixel;

                pixel = image.getRGB(j, i);
                colorRgb = convertPixelToColor(pixel);
                result.setAt(j, i, colorRgb);
            }
        }

        return result;
    }

    public static BufferedImage convertBooleanMapToImage(IMap2d<Boolean> map)
    {
        BufferedImage resultImage;
        int x, y;

        resultImage = new BufferedImage(map.getWidth(), map.getLength(), TYPE_INT_ARGB);

        for( y = 0; y < resultImage.getHeight(); y++ )
        {
            for( x = 0; x < resultImage.getWidth(); x++ )
            {
                boolean value;

                value = map.readAt(x, y);

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

    private static ColorRgb convertPixelToColor(int pixelValue)
    {
        int redInt = (pixelValue >> 16) & 0xff;
        int greenInt = (pixelValue >> 8) & 0xff;
        int blueInt = (pixelValue) & 0xff;

        float red, green, blue;

        red = (float)redInt/255.0f;
        green = (float)greenInt/255.0f;
        blue = (float)blueInt/255.0f;

        return new ColorRgb(red, green, blue);
    }

}
