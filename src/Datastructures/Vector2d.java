package Datastructures;

public class Vector2d<Type>
{
    public Vector2d(Type x, Type y)
    {
        this.x = x;
        this.y = y;
    }
    
    public Type x, y;
    
    public static class FloatHelper
    {
        public static float dot(Vector2d<Float> a, Vector2d<Float> b)
        {
            return a.x*b.x + a.y*b.y;
        }
        
        public static Vector2d<Float> add(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<Float>(a.x + b.x, a.y + b.y);
        }
        
        public static Vector2d<Float> sub(Vector2d<Float> a, Vector2d<Float> b)
        {
            return new Vector2d<Float>(a.x - b.x, a.y - b.y);
        }
        
        public static Vector2d<Float> normalize(Vector2d<Float> vector)
        {
            float length, invLength;
            
            length = getLength(vector);
            invLength = 1.0f/length;
            return new Vector2d<Float>(vector.x*invLength, vector.y*invLength);
        }
        
        public static float getLength(Vector2d<Float> vector)
        {
            return (float)Math.sqrt(vector.x*vector.x + vector.y*vector.y);
        }
    }
}
