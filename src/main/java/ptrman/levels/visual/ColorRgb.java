package ptrman.levels.visual;

public class ColorRgb
{
    public float r;
    public float g;
    public float b;
    
    public ColorRgb(float r, float g, float b)
    {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public float getScaledNormalizedMagnitude(ColorRgb scale)
    {
        return (r*scale.r + g*scale.g + b*scale.b) * (1.0f/(scale.r+scale.g+scale.b));
    }

    public ColorRgb add(ColorRgb other)
    {
        return new ColorRgb(r + other.r, g + other.g, b + other.b);
    }

    public ColorRgb sub(ColorRgb other)
    {
        return new ColorRgb(r - other.r, g - other.g, b - other.b);
    }

    public ColorRgb mul(ColorRgb other)
    {
        return new ColorRgb(r * other.r, g * other.g, b * other.b);
    }

    public float distanceSquared(ColorRgb other)
    {
        return ptrman.math.Math.squaredDistance(new float[]{r, g, b});
    }

}


