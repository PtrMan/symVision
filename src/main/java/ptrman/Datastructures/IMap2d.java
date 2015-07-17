package ptrman.Datastructures;

public interface IMap2d<Type>
{
    Type readAt(int x, int y);
    void setAt(int x, int y, Type value);

    int getWidth();
    int getLength();

    default boolean inBounds(Vector2d<Integer> position) {
        return inBounds(position.xInt(), position.yInt() );
        //return position.xInt() < getWidth() && position.yInt() < getLength();
    }

    default boolean inBounds(int x, int y) {
        return x>=0 && y >=0 && x < getWidth() && y < getLength();
    }

    void clear();

    IMap2d<Type> copy();
}
