package Visual;

import Visual.ColorHsl;
import Visual.ColorRgb;

public class ColorConversion   
{
    private enum EnumRgb
    {
        R,
        G,
        B
    }
    
    // http://www.rapidtables.com/convert/color/hsl-to-rgb.htm
    static public ColorRgb hslToRgb(ColorHsl input)
    {
        float r;
        float g;
        float b;
        float c;
        float x;
        float m;
        misc.Assert.Assert(input.h >= 0.0f, "");
        c = 1.0f - Math.abs(2.0f * input.l - 1.0f) * input.s;
        x = c * (1.0f - (float)Math.abs((float)((input.h / 0.25f) % 2.0f) - 1.0));
        m = input.l - c * 0.5f;
        if (input.h < 60.0f / 360.0f)
        {
            r = c;
            g = x;
            b = 0.0f;
        }
        else if (input.h < 120.0f / 360.0f)
        {
            r = x;
            g = c;
            b = 0.0f;
        }
        else if (input.h < 180.0f / 360.0f)
        {
            r = 0.0f;
            g = c;
            b = x;
        }
        else if (input.h < 240.0f / 360.0f)
        {
            r = 0.0f;
            g = x;
            b = c;
        }
        else if (input.h < 300.0f / 360.0f)
        {
            r = x;
            g = 0.0f;
            b = c;
        }
        else
        {
            r = c;
            g = 0.0f;
            b = x;
        }     
        r += m;
        g += m;
        b += m;
        return new ColorRgb(r,g,b);
    }

    static public ColorHsl rgbToHsl(ColorRgb rgb)
    {
        ColorHsl result;
        float min;
        EnumRgb rgbMin = EnumRgb.R;
        float max;
        EnumRgb rgbMax = EnumRgb.R;
        result = new ColorHsl();
        rgbMax = EnumRgb.R;
        max = rgb.r;
        if (rgb.g > max)
        {
            rgbMax = EnumRgb.G;
            max = rgb.g;
        }
         
        if (rgb.b > max)
        {
            rgbMax = EnumRgb.B;
            max = rgb.b;
        }
         
        rgbMin = EnumRgb.R;
        min = rgb.r;
        if (rgb.g < min)
        {
            rgbMin = EnumRgb.G;
            min = rgb.g;
        }
         
        if (rgb.b < min)
        {
            rgbMin = EnumRgb.B;
            min = rgb.b;
        }
         
        result.h = result.s = result.l = (max + min) * 0.5f;
        if (rgbMax == rgbMin)
        {
            // accromatic
            result.h = result.s = 0.0f;
        }
        else
        {
            float d;
            d = max - min;
            result.s = (result.l > 0.5f) ? d / (2.0f - max - min) : d / (max + min);
            if (rgbMax == EnumRgb.R)
            {
                result.h = (rgb.g - rgb.b) / d + ((rgb.g < rgb.b) ? 6.0f : 0.0f);
            }
            else if (rgbMax == EnumRgb.G)
            {
                result.h = (rgb.b - rgb.r) / d + 2.0f;
            }
            else
            {
                result.h = (rgb.r - rgb.g) / d + 4.0f;
            }  
            result.h /= 6.0f;
        } 
        return result;
    }

}


