package ptrman.math;

import ptrman.Datastructures.Vector2d;

import java.lang.*;

/**
 *
 */
public class Kernels
{
    public interface IKernel<Type, CoordinateType extends Number>
    {
        Type calculateAt(Vector2d<CoordinateType> position);
    }

    public static class MarrHildrethOperator implements  IKernel<Float, Float>
    {

        public MarrHildrethOperator(float sigma)
        {
            this.sigma = sigma;
        }

        @Override
        public Float calculateAt(Vector2d<Float> position)
        {
            double gaussianFactor, scalingFactor;
            final double factor = -1.0/(java.lang.Math.PI*java.lang.Math.pow(sigma, 4.0f));

            gaussianFactor = java.lang.Math.exp(-1.0f*(Maths.squaredDistance(new double[]{position.x, position.y})/(2.0* Maths.power2(sigma))));
            scalingFactor = 1.0 - Maths.squaredDistance(new double[]{position.x, position.y})/(2.0* Maths.power2(sigma));

            return (float)(factor*gaussianFactor*scalingFactor);
        }

        private final float sigma;
    }

    /**
     * Marr Hildreth Operator
     * Mexican Hat Operator
     *
     * candidate for SUPERCOMPILATION, slow when executed
     *
     */
    public static float calcMarrHildrethOperator(Vector2d<Float> position, float sigma)
    {
        return new MarrHildrethOperator(sigma).calculateAt(position);
    }
}
