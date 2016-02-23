package ptrman.levels.retina.nonFoundalis;

import ptrman.Algorithms.ai.gng.NeuralGasNet;
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
public class ProcessFastCalculateEndosceleton implements IProcess {
    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {

    }

    @Override
    public void setup() {
        // TODO< setup gng >
    }

    @Override
    public void preProcessData() {

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

    protected List<IMap2d<Boolean>> corrodeDirectional(List<IMap2d<Boolean>> inputs) {
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
    }

    @Override
    public void postProcessData() {

    }


    public List<IMap2d<Boolean>> inputs;

    protected NeuralGasNet gng;

    //public static void main(String[] args) {
    //}
}
