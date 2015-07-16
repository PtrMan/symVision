package ptrman.Datastructures;

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

    public static class IntegerHelper
    {
        public static Vector2d<Integer> add(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<Integer>(a.xInt() + b.xInt(), a.yInt() + b.yInt());
        }
        
        public static Vector2d<Integer> sub(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<Integer>(a.xInt() - b.xInt(), a.yInt() - b.yInt());
        }
        
        public static Vector2d<Integer> max(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<Integer>(Math.max(a.xInt(), b.xInt()), Math.max(a.yInt(), b.yInt()));
        }
        
        public static Vector2d<Integer> max4(Vector2d<Integer> a, Vector2d<Integer> b, Vector2d<Integer> c, Vector2d<Integer> d)
        {
            Vector2d<Integer> maxLeft = max(a, b);
            Vector2d<Integer> maxRight = max(c, d);
            return max(maxLeft, maxRight);
        }
        
        public static Vector2d<Integer> min(Vector2d<Integer> a, Vector2d<Integer> b)
        {
            return new Vector2d<Integer>(Math.min(a.xInt(), b.xInt()), Math.min(a.yInt(), b.yInt()));
        }
        
        public static Vector2d<Integer> min4(Vector2d<Integer> a, Vector2d<Integer> b, Vector2d<Integer> c, Vector2d<Integer> d)
        {
            Vector2d<Integer> minLeft = min(a, b);
            Vector2d<Integer> minRight = min(c, d);
            return min(minLeft, minRight);
        }
        
        public static Vector2d<Integer> getScaled(Vector2d<Integer> a, int value)
        {
            return new Vector2d<Integer>(a.xInt()*value, a.yInt()*value);
        }
    }
    
    public static class FloatHelper
    {
        public static float dot(Vector2d<Float> a, Vector2d<Float> b)
        {
            return a.xFloat()*b.xFloat() + a.yFloat()*b.yFloat();
        }
        
        public static Vector2d<Float> add(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<Float>(a.xFloat() + b.xFloat(), a.yFloat() + b.yFloat());
        }
        
        public static Vector2d<Float> sub(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<Float>(a.xFloat() - b.xFloat(), a.yFloat() - b.yFloat());
        }
        
        public static Vector2d<Float> normalize(Vector2d<Float> vector)
        {
            float length, invLength;
            
            length = getLength(vector);
            invLength = 1.0f/length;
            return new Vector2d<Float>(vector.xFloat()*invLength, vector.yFloat()*invLength);
        }
        
        public static float getLength(Vector2d<Float> vector)
        {
            return (float)Math.sqrt(vector.xFloat()*vector.xFloat() + vector.yFloat()*vector.yFloat());
        }
        
        public static Vector2d<Float> getScaled(Vector2d<Float> a, float value)
        {
            return new Vector2d<Float>(a.xFloat()*value, a.yFloat()*value);
        }
    }
    
    public static class ConverterHelper
    {
        public static Vector2d<Float> convertIntVectorToFloat(Vector2d<Integer> vector)
        {
            return new Vector2d<Float>((float)vector.xInt(), (float)vector.xInt());
        }
        
        public static Vector2d<Integer> convertFloatVectorToInt(Vector2d<Float> vector)
        {
            return new Vector2d<Integer>(Math.round(vector.xFloat()), Math.round(vector.yFloat()));
        }
    }
}
