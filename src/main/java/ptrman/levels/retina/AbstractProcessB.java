package ptrman.levels.retina;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;

/**
 * calculates the altitude
 */
public abstract class AbstractProcessB implements IProcess {
    public abstract void set(IMap2d<Boolean> map, ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> outputSampleConnector);

    @Override
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
    }

    protected Vector2d<Integer> imageSize;
}
