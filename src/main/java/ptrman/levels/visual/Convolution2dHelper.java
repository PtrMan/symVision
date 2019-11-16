package ptrman.levels.visual;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.math.Kernels;

/**
 *
 */
public enum Convolution2dHelper
{
	;

	public static IMap2d<Float> calculateMarrHildrethOperator(Vector2d<Integer> size, float sigma)
    {
        IMap2d<Float> kernel;
        int x, y;

        kernel = new Map2d<>(size.x, size.y);

        for( y = 0; y < size.y; y++ )
        {
            for( x = 0; x < size.x; x++ )
            {
                float fx, fy;
                float value;

                fx = (float)x*2.0f - 1.0f;
                fy = (float)y*2.0f - 1.0f;

                value = Kernels.calcMarrHildrethOperator(new Vector2d<>(fx, fy), sigma);
                kernel.setAt(x, y, value);
            }
        }

        return kernel;
    }
}
