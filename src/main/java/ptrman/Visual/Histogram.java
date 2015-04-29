package ptrman.Visual;

import ptrman.Datastructures.Map2d;

public class Histogram
{
    static public Integer[] count(Map2d<Integer> map, int maxValue)
    {
        Integer[] countArray;
        int ix, iy;

        countArray = new Integer[maxValue+1];

        for( iy = 0; iy < map.getWidth(); iy++ )
        {
                for( ix = 0; ix < map.getLength(); ix++ )
                {
                        int valueAtPosition;

                        valueAtPosition = map.readAt(ix, iy);
                        countArray[valueAtPosition]++;
                }
        }

        return countArray;
    }
}
