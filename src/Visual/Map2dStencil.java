class Map2dStencil
{
	static public void stencilSingleValue(Map2d<Integer> input, Map2d<Boolean> result, int value)
	{
		int ix, iy;

		for( iy = 0; iy < map.getWidth(); iy++ )
		{
			for( ix = 0; ix < map.getLength(); ix++ )
			{
				int valueAtPosition;

				valueAtPosition = input.readAt(ix, iy);
				result.writeAt(ix, iy, valueAtPosition == value);
			}
		}
	}

	static public void stencilValues(Map2d<Integer> input, Map2d<Boolean> result, List<Integer> values, int maxValue)
	{
		Boolean[] stencilBooleanArray;

		stencilBooleanArray = convertValuesToBooleanArray(values, maxValue);
		stencilWithBooleanArray(input, result, stencilBooleanArray);
	}

	static private Boolean[] convertValuesToBooleanArray(List<Integer> values, int maxValue)
	{
		Boolean[] resultArray;

		resultArray = new Boolean[maxValue+1];

		for( int iterationValue : values )
		{
			resultArray[iterationValue] = true;
		}

		return resultArray;
	}

	static private void stencilWithBooleanArray(Map2d<Integer> input, Map2d<Boolean> result, boolean[] booleanArray)
	{
		int ix, iy;

		for( iy = 0; iy < map.getWidth(); iy++ )
		{
			for( ix = 0; ix < map.getLength(); ix++ )
			{
				int valueAtPosition;

				valueAtPosition = input.readAt(ix, iy);
				result.writeAt(ix, iy, booleanArray[valueAtPosition]);
			}
		}
	}
}
