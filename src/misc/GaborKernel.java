package Math;

public class GaborKernel   
{
    /**
     * 
     * 
     * 
     * \param phi angle in radiants
     * \param spartialRatioAspect ellipticity of the support of the Gabor function
     */
    public static Map2d<float> generateGaborKernel(int width, float phi, float lambda, float phaseOffset, float spartialRatioAspect)
    {
        float xTick, yTick;
        float sigma;
        int xInt, yInt;

        // constant from http://bmia.bmt.tue.nl/Education/Courses/FEV/course/pdf/Petkov_Gabor_functions2011.pdf
        sigma = 0.56f * lambda;
        Map2d<float> resultMap = new Map2d<float>((uint)width, (uint)width);
        for (yInt = 0;yInt < width;yInt++)
        {
            for (xInt = 0;xInt < width;xInt++)
            {
                float x, y;
                float insideExp, insideCos;
                float filterValue;
                x = ((float)(xInt - width / 2) / (float)width) * 2.0f;
                y = ((float)(yInt - width / 2) / (float)width) * 2.0f;
                xTick = x * (float)System.Math.Cos(phi) + y * (float)System.Math.Sin(phi);
                yTick = -x * (float)System.Math.Sin(phi) + y * (float)System.Math.Cos(phi);
                insideExp = -(xTick * xTick + spartialRatioAspect * spartialRatioAspect * yTick * yTick) / (2.0f * sigma * sigma);
                insideCos = 2.0f * (float)System.Math.PI * (xTick / lambda) + phaseOffset;
                filterValue = (float)System.Math.Exp(insideExp) * (float)System.Math.Cos(insideCos);
                resultMap.writeAt(xInt, yInt, filterValue);
            }
        }
        return resultMap;
    }
}
