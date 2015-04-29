package ptrman.Visual;

import ptrman.Datastructures.IMap2d;

public class Map2dTransform<Type>
{
    public void magnify(IMap2d<Type> input, IMap2d<Type> output, int factor)
    {
        int ix, iy;

        for( iy = 0; iy < input.getLength(); iy++ )
        {
            for( ix = 0; ix < input.getWidth(); ix++ )
            {
                writeMagnified(output, ix, iy, input.readAt(ix, iy), factor);
            }
        }
    }

    private void writeMagnified(IMap2d<Type> output, int ix, int iy, Type value, int factor)
    {
        int writeX, writeY;

        for( writeY = 0; writeY < factor; writeY++ )
        {
            for( writeX = 0; writeX < factor; writeX++ )
            {
                output.setAt(ix*factor + writeX, iy*factor + writeY, value);
            }
        }
    }
}
