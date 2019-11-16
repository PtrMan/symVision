package ptrman.misc;

public enum Assert
{
	;

	public static void Assert(boolean value, String message)
    {
        if( !value )
        {
            throw new RuntimeException("ASSERT: "+ message);
        }
    }
}
