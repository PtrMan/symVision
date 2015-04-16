package Visual;

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

    public float getMagnitude()
    {
        return (r + g + b) * 0.33333333333333f;
    }

}


