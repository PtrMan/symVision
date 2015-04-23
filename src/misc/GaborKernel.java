package misc;

import Datastructures.IMap2d;
import Datastructures.Map2d;

public class GaborKernel
{
    /**
     * 
     * 
     * 
     * \param phi angle in radiants
     * \param spartialRatioAspect ellipticity of the support of the Gabor function
     */
    public static IMap2d<Float> generateGaborKernel(int width, float phi, float lambda, float phaseOffset, float spartialRatioAspect)
    {
        float xTick, yTick;
        float sigma;
        int xInt, yInt;

        // constant from http://bmia.bmt.tue.nl/Education/Courses/FEV/course/pdf/Petkov_Gabor_functions2011.pdf
        sigma = 0.56f * lambda;
        IMap2d<Float> resultMap = new Map2d<>(width, width);
        for (yInt = 0;yInt < width;yInt++)
        {
            for (xInt = 0;xInt < width;xInt++)
            {
                float x, y;
                float insideExp, insideCos;
                float filterValue;
                x = ((float)(xInt - width / 2) / (float)width) * 2.0f;
                y = ((float)(yInt - width / 2) / (float)width) * 2.0f;
                xTick = x * (float)java.lang.Math.cos(phi) + y * (float)java.lang.Math.sin(phi);
                yTick = -x * (float)java.lang.Math.sin(phi) + y * (float)java.lang.Math.cos(phi);
                insideExp = -(xTick * xTick + spartialRatioAspect * spartialRatioAspect * yTick * yTick) / (2.0f * sigma * sigma);
                insideCos = 2.0f * (float)java.lang.Math.PI * (xTick / lambda) + phaseOffset;
                filterValue = (float)java.lang.Math.exp(insideExp) * (float)java.lang.Math.cos(insideCos);
                resultMap.setAt(xInt, yInt, filterValue);
            }
        }
        return resultMap;
    }
}
