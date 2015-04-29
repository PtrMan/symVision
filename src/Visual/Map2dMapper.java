package Visual;

import Datastructures.IMap2d;

/**
 *
 */
public class Map2dMapper
{
    public interface IMap2dMapper<InputType, ResultType>
    {
        ResultType calculate(InputType value);
    }

    public static class Mapper<InputType, ResultType>
    {
        public void map(IMap2dMapper<InputType, ResultType> mapperImplementation, IMap2d<InputType> inputMap, IMap2d<ResultType> resultMap)
        {
            int x, y;

            for( y = 0; y < inputMap.getLength(); y++ )
            {
                for( x = 0; x < inputMap.getWidth(); x++ )
                {
                    InputType tempInput;
                    ResultType tempResult;

                    tempInput = inputMap.readAt(x, y);
                    tempResult = mapperImplementation.calculate(tempInput);
                    resultMap.setAt(x, y, tempResult);
                }
            }
        }
    }
}
