package ptrman.Datastructures;

public interface IMap2d<Type>
{
    Type readAt(int x, int y);
    void setAt(int x, int y, Type value);

    int getWidth();
    int getLength();

    boolean inBounds(Vector2d<Integer> position);

    Map2d<Type> copy();
}
