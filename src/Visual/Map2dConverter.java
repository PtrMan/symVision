class Map2dConverter
{
	public static void booleanToFloat(Map2d<Boolean> input, Map2d<Float> output)
	{
		int ix, iy;

		for( iy = 0; iy < map.getWidth(); iy++ )
		{
			for( ix = 0; ix < map.getLength(); ix++ )
			{
				boolean valueAtPosition;

				valueAtPosition = input.readAt(ix, iy);
				output.writeAt(ix, iy, convertBooleanToFloat(valueAtPosition));
			}
		}
	}

	public static void floatToBoolean(Map2d<Float> input, Map2d<Boolean> output, float threshold)
	{
		int ix, iy;

		for( iy = 0; iy < map.getWidth(); iy++ )
		{
			for( ix = 0; ix < map.getLength(); ix++ )
			{
				float valueAtPosition;

				valueAtPosition = input.readAt(ix, iy);
				output.writeAt(ix, iy, floatAboveThreshold(valueAtPosition, threshold));
			}
		}
	}

	private static float convertBooleanToFloat(boolean value)
	{
		if( value )
		{
			return 1.0f;
		}
		else
		{
			return 0.0f;
		}
	}

	private static boolean floatAboveThreshold(float value, float threshold)
	{
		return value > threshold;
	}
}