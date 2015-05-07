package ptrman.levels.retina;

import ptrman.Datastructures.Vector2d;

public interface IProcess {
    void setImageSize(final Vector2d<Integer> imageSize);

    void setup();

    void processData();
}
