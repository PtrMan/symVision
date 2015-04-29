package Visual;

import Datastructures.IMap2d;
import Datastructures.Map2d;
import Datastructures.Vector2d;
import math.Kernels;

/**
 *
 */
public class Convolution2dHelper
{
    public IMap2d<Float> calculateMarrHildrethOperator(Vector2d<Integer> size, float sigma)
    {
        IMap2d<Float> kernel;
        int x, y;

        kernel = new Map2d<>(size.x, size.y);

        for( y = 0; y < size.y; y++ )
        {
            for( x = 0; x < size.x; x++ )
            {
                float fx, fy;

                fx = (float)x*2.0f - 1.0f;
                fy = (float)y*2.0f - 1.0f;

                Kernels.calcMarrHildrethOperator(new Vector2d<>(fx, fy), sigma);
            }
        }

        return kernel;
    }
}
