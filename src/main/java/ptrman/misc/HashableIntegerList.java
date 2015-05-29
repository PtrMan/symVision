package ptrman.misc;

import java.util.List;

import static ptrman.misc.BitOperations.rotateLeft;

/**
 *
 */
public class HashableIntegerList {
    public HashableIntegerList(List<Integer> list) {
        this.list = list;
    }

    @Override
    public int hashCode() {
        int result = 0;

        for( final int iterationInteger : list ) {
            result = result ^ iterationInteger;
            result = rotateLeft(result, 6);
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HashableIntegerList))
            return false;
        if (obj == this)
            return true;

        HashableIntegerList rhs = (HashableIntegerList) obj;

        if( rhs.list.size() != list.size() ) {
            return false;
        }

        for( int i = 0; i < list.size(); i++ ) {
            if( rhs.list.get(i) != list.get(i) ) {
                return false;
            }
        }

        return true;
    }

    private List<Integer> list;
}
