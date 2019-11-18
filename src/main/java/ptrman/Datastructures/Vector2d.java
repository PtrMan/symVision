/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Datastructures;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;

import static org.eclipse.collections.impl.tuple.primitive.PrimitiveTuples.pair;

public class Vector2d<Type extends Number>
{
    public Vector2d(Type x, Type y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Type x, y;

    public float xFloat() {
        return x.floatValue();
    }
    public float yFloat() {
        return y.floatValue();
    }


    public int xInt() {
        return x.intValue();
    }

    public int yInt() {
        return y.intValue();
    }

    public enum IntegerHelper
    {
        ;

        public static IntIntPair add(IntIntPair a, IntIntPair b)
        {
            return pair(a.getOne() + b.getOne(), a.getTwo() + b.getTwo());
        }
        public static IntIntPair add(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return pair(a.xInt() + b.xInt(), a.yInt() + b.yInt());
        }
        public static IntIntPair add(Vector2d<Integer> a, IntIntPair b)
        {
            return pair(a.xInt() + b.getOne(), a.yInt() + b.getTwo());
        }
        public static Vector2d<Integer> sub(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<>(a.xInt() - b.xInt(), a.yInt() - b.yInt());
        }
        
        public static Vector2d<Integer> max(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<>(Math.max(a.xInt(), b.xInt()), Math.max(a.yInt(), b.yInt()));
        }
        public static Vector2d<Integer> max(Vector2d<Integer> a, IntIntPair b)
        {
            return new Vector2d<>(Math.max(a.xInt(), b.getOne()), Math.max(a.yInt(), b.getTwo()));
        }

        public static Vector2d<Integer> max4(Vector2d<Integer> a, Vector2d<Integer> b, Vector2d<Integer> c, Vector2d<Integer> d)
        {
            Vector2d<Integer> maxLeft = max(a, b);
            Vector2d<Integer> maxRight = max(c, d);
            return max(maxLeft, maxRight);
        }
        
        public static Vector2d<Integer> min(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<>(Math.min(a.xInt(), b.xInt()), Math.min(a.yInt(), b.yInt()));
        }
        public static Vector2d<Integer> min(Vector2d<Integer> a, IntIntPair b)
        {
            return new Vector2d<>(Math.min(a.xInt(), b.getOne()), Math.min(a.yInt(), b.getTwo()));
        }

        public static Vector2d<Integer> min4(Vector2d<Integer> a, Vector2d<Integer> b, Vector2d<Integer> c, Vector2d<Integer> d)
        {
            Vector2d<Integer> minLeft = min(a, b);
            Vector2d<Integer> minRight = min(c, d);
            return min(minLeft, minRight);
        }
        public static Vector2d<Integer> min4(Vector2d<Integer> a, IntIntPair b, Vector2d<Integer> c, Vector2d<Integer> d)
        {
            Vector2d<Integer> minLeft = min(a, b);
            Vector2d<Integer> minRight = min(c, d);
            return min(minLeft, minRight);
        }

        public static IntIntPair getScaled(IntIntPair a, int value)
        {
            return pair(a.getOne() * value, a.getTwo() * value);
        }
        public static Vector2d<Integer> getScaled(Vector2d<Integer> a, int value)
        {
            return new Vector2d<>(a.xInt() * value, a.yInt() * value);
        }
    }
    
    public enum FloatHelper
    {
        ;

        public static float dot(Vector2d<Float> a, Vector2d<Float> b)
        {
            return a.xFloat()*b.xFloat() + a.yFloat()*b.yFloat();
        }
        
        public static Vector2d<Float> add(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<>(a.xFloat() + b.xFloat(), a.yFloat() + b.yFloat());
        }
        
        public static Vector2d<Float> sub(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<>(a.xFloat() - b.xFloat(), a.yFloat() - b.yFloat());
        }
        
        public static Vector2d<Float> normalize(Vector2d<Float> vector)
        {

            float length = getLength(vector);
            float invLength = 1.0f / length;
            return new Vector2d<>(vector.xFloat() * invLength, vector.yFloat() * invLength);
        }
        
        public static float getLength(Vector2d<Float> vector)
        {
            return (float)Math.sqrt(vector.xFloat()*vector.xFloat() + vector.yFloat()*vector.yFloat());
        }
        
        public static Vector2d<Float> getScaled(Vector2d<Float> a, float value)
        {
            return new Vector2d<>(a.xFloat() * value, a.yFloat() * value);
        }
    }
    
    public enum ConverterHelper
    {
        ;

        public static Vector2d<Float> convertIntVectorToFloat(Vector2d<Integer> vector)
        {
            return new Vector2d<>((float) vector.xInt(), (float) vector.xInt());
        }
        
        public static Vector2d<Integer> convertFloatVectorToInt(Vector2d<Float> vector)
        {
            return new Vector2d<>(Math.round(vector.xFloat()), Math.round(vector.yFloat()));
        }
    }
}
