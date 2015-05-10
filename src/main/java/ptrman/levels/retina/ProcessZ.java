package ptrman.levels.retina;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.visual.Map2dBinary;
import ptrman.levels.visual.Map2dConverter;
import ptrman.levels.visual.Map2dTransform;
import ptrman.misc.GaussianBlur;

public class ProcessZ implements IProcess {
    public IMap2d<Boolean> getMagnifiedOutput() {
        return magnifiedOutput;
    }



    public void set(IMap2d<Boolean> inputMap) {
        this.inputMap = inputMap;
    }

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    @Override
    public void setup() {

    }

    @Override
    public void processData() {
        IMap2d<Float> floatNotMagnified;
        IMap2d<Float> floatMagnified;
        IMap2d<Float> floatMagnifiedBlured;
        IMap2d<Boolean> tempResult;

        floatNotMagnified = new Map2d<>(imageSize.x, imageSize.y);
        floatMagnified = new Map2d<>(imageSize.x*2, imageSize.y*2);
        tempResult = new Map2d<>(imageSize.x*2, imageSize.y*2);

        Map2dConverter.booleanToFloat(inputMap, floatNotMagnified);
        map2dTranform.magnify(floatNotMagnified, floatMagnified, 2);
        floatMagnifiedBlured = GaussianBlur.blur(2, floatMagnified);
        Map2dConverter.floatToBoolean(floatMagnifiedBlured, tempResult, FLOATTOBOOLEANTHRESHOLD);
        magnifiedOutput = Map2dBinary.corode(tempResult);
    }

    private IMap2d<Boolean> inputMap;

    private Vector2d<Integer> imageSize;

    private IMap2d<Boolean> magnifiedOutput;

    private Map2dTransform<Float> map2dTranform = new Map2dTransform<>();

    private final float FLOATTOBOOLEANTHRESHOLD = 0.6f;
}
