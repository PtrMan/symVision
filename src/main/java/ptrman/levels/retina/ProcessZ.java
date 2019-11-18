/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina;

import ptrman.Datastructures.FastBooleanMap2d;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.visual.FloatMap2d;
import ptrman.levels.visual.Map2dBinary;
import ptrman.levels.visual.Map2dConverter;
import ptrman.levels.visual.Map2dTransform;
import ptrman.misc.GaussianBlur;

/**
 * zooms in to resolve small features
 */
public class ProcessZ implements IProcess {
    IMap2d<Float> floatNotMagnified;
    IMap2d<Float> floatMagnified;
    IMap2d<Float> floatMagnifiedBlured;
    IMap2d<Boolean> tempResult;


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
    public void preProcessData() {

    }

    @Override
    public void processData() {

        if ((floatNotMagnified == null || floatNotMagnified.getWidth()!=imageSize.x || floatNotMagnified.getLength()!=imageSize.y))
            floatNotMagnified = new FloatMap2d(imageSize.x, imageSize.y);
        else
            floatNotMagnified.clear();

        if ((floatMagnified == null || floatMagnified.getWidth()!=imageSize.x*2 || floatMagnified.getLength()!=imageSize.y*2))
            floatMagnified = new FloatMap2d(imageSize.x*2, imageSize.y*2);
        else
            floatMagnified.clear();

        if ((tempResult == null || tempResult.getWidth()!=imageSize.x*2 || tempResult.getLength()!=imageSize.y*2))
            tempResult = new FastBooleanMap2d(imageSize.x*2, imageSize.y*2);
        else
            tempResult.clear();


        Map2dConverter.booleanToFloat(inputMap, floatNotMagnified);
        map2dTranform.magnify(floatNotMagnified, floatMagnified, 2);
        floatMagnifiedBlured = GaussianBlur.blur(2, floatMagnified);
        Map2dConverter.floatToBoolean(floatMagnifiedBlured, tempResult, FLOATTOBOOLEANTHRESHOLD);
        magnifiedOutput = Map2dBinary.corode(tempResult);
    }

    @Override
    public void postProcessData() {

    }

    private IMap2d<Boolean> inputMap;

    private Vector2d<Integer> imageSize;

    private IMap2d<Boolean> magnifiedOutput;

    private final Map2dTransform<Float> map2dTranform = new Map2dTransform<>();

    private final float FLOATTOBOOLEANTHRESHOLD = 0.6f;
}
