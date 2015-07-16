package ptrman.Datastructures;

public interface IMap2d<Type>
{
    Type readAt(int x, int y);
    void setAt(int x, int y, Type value);

    int getWidth();
    int getLength();

    default boolean inBounds(Vector2d<Integer> position) {
        return position.xInt() < getWidth() && position.yInt() < getLength();
    }

    void clear();

    IMap2d<Type> copy();
}
