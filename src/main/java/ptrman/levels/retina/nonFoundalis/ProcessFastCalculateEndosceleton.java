/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina.nonFoundalis;

//import ptrman.Algorithms.ai.gng.NeuralGasNet;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.IProcess;
import ptrman.levels.visual.Map2dBinary;

import java.util.ArrayList;
import java.util.List;

/**
 * Tries to convert the input bitmaps(which can be anotated with a boundary) to a endosceleton (which doesn't have to be exact)
 */
// not(partially) implemented!
public class ProcessFastCalculateEndosceleton implements IProcess {
    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {

    }

    @Override
    public void setup() {
        // TODO< setup gng >
    }

    @Override
    public void processData() {
        // corrode in one direction
        List<IMap2d<Boolean>> directonalCorrededInputs = corrodeDirectional(inputs);

        // or it together
        IMap2d<Boolean> combinedDirectonalCorrodedInput = booleanOperationOrOnMaps(directonalCorrededInputs);


        IMap2d<Boolean> combinedSkeletons = Map2dBinary.skeletalize(combinedDirectonalCorrodedInput);

        convertMapToVectorRepresentation(combinedSkeletons);
    }

    protected void convertMapToVectorRepresentation(IMap2d<Boolean> combinedSkeletons) {
        // TODO< sample the image >

        // TODO< call gng >

        // TODO< convert result back to symvision vector form and store in this object >
    }

    protected static List<IMap2d<Boolean>> corrodeDirectional(List<IMap2d<Boolean>> inputs) {
        List<IMap2d<Boolean>> directonalCorrededInputs = new ArrayList<>();

        // TODO< corrode in just one direction the inputs >

        // example
        // .....
        // .xxx.
        // .xxx.
        // .xxx.
        // .....

        // results in

        // .....
        // .xx..
        // .xx..
        // .....

        return directonalCorrededInputs;
    }

    protected static IMap2d<Boolean> booleanOperationOrOnMaps(final List<IMap2d<Boolean>> inputs) {
        IMap2d<Boolean> resultMap =  new Map2d<>(inputs.get(0).getWidth(), inputs.get(0).getLength());

        for( final IMap2d<Boolean> currentInput : inputs ) {
            Map2dBinary.orInplace(resultMap, currentInput, resultMap);
        }

        return resultMap;
    }

    protected static List<IMap2d<Boolean>> corrodeInOneDirection(final List<IMap2d<Boolean>> inputs) {
        // TODO
        throw new RuntimeException(); // HACK< we need to implement this maybe but throw now exception because we want to compile it >
    }


	public List<IMap2d<Boolean>> inputs;

    //protected NeuralGasNet gng;

    //public static void main(String[] args) {
    //}
}
