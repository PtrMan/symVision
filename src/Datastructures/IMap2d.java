package Datastructures;

public interface IMap2d<Type>
{
    Type readAt(int x, int y);
    void setAt(int x, int y, Type value);

    int getWidth();
    int getLength();

    Map2d<Type> copy();
}
