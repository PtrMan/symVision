package ptrman.levels.visual;

import ptrman.Datastructures.Map2d;
import java.util.List;

public enum Map2dStencil
{
	;

	static public void stencilSingleValue(Map2d<Integer> input, Map2d<Boolean> result, int value)
    {
        int ix, iy;

        for( iy = 0; iy < input.getWidth(); iy++ )
        {
            for( ix = 0; ix < input.getLength(); ix++ )
            {
                int valueAtPosition;

                valueAtPosition = input.readAt(ix, iy);
                result.setAt(ix, iy, valueAtPosition == value);
            }
        }
    }

    static public void stencilValues(Map2d<Integer> input, Map2d<Boolean> result, List<Integer> values, int maxValue)
    {
        boolean[] stencilBooleanArray;

        stencilBooleanArray = convertValuesToBooleanArray(values, maxValue);
        stencilWithBooleanArray(input, result, stencilBooleanArray);
    }

    static private boolean[] convertValuesToBooleanArray(List<Integer> values, int maxValue)
    {
        boolean[] resultArray;

        resultArray = new boolean[maxValue+1];

        for( int iterationValue : values )
        {
            resultArray[iterationValue] = true;
        }

        return resultArray;
    }

    static private void stencilWithBooleanArray(Map2d<Integer> input, Map2d<Boolean> result, boolean[] booleanArray)
    {
        int ix, iy;

        for( iy = 0; iy < input.getWidth(); iy++ )
        {
            for( ix = 0; ix < input.getLength(); ix++ )
            {
                int valueAtPosition;

                valueAtPosition = input.readAt(ix, iy);
                result.setAt(ix, iy, booleanArray[valueAtPosition]);
            }
        }
    }
}
