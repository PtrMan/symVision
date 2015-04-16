package RetinaLevel;

import Datastructures.Map2d;
import Datastructures.Vector2d;
import Visual.Map2dBinary;
import Visual.Map2dConverter;
import Visual.Map2dTransform;
import misc.GaussianBlur;

public class ProcessZ
{
    public void setup(Vector2d<Integer> imageSize)
    {
        this.imageSize = imageSize;
    }

    public void process(Map2d<Boolean> input)
    {
        Map2d<Float> floatNotMagnified;
        Map2d<Float> floatMagnified;
        Map2d<Float> floatMagnifiedBlured;
        Map2d<Boolean> tempResult;

        floatNotMagnified = new Map2d<>(imageSize.x, imageSize.y);
        floatMagnified = new Map2d<>(imageSize.x*2, imageSize.y*2);
        tempResult = new Map2d<>(imageSize.x, imageSize.y);

        Map2dConverter.booleanToFloat(input, floatNotMagnified);
        map2dTranform.magnify(floatNotMagnified, floatMagnified, 2);
        floatMagnifiedBlured = GaussianBlur.blur(2, floatMagnified);
        Map2dConverter.floatToBoolean(floatMagnifiedBlured, tempResult, FLOATTOBOOLEANTHRESHOLD);
        magnifiedOutput = Map2dBinary.corode(tempResult);
    }

    public Map2d<Boolean> getMagnifiedOutput()
    {
        return magnifiedOutput;
    }

    private Vector2d<Integer> imageSize;

    private Map2d<Boolean> magnifiedOutput;

    private Map2dTransform<Float> map2dTranform = new Map2dTransform<>();

    private final float FLOATTOBOOLEANTHRESHOLD = 0.6f;
}
