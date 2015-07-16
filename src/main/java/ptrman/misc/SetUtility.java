package ptrman.misc;

import com.gs.collections.api.map.primitive.IntBooleanMap;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class SetUtility {

    public static<Type> Type getRandomElementFromSet(final Set<Type> set, Random random) {
        final int index = random.nextInt(set.size());

        Iterator<Type> iterator = set.iterator();

        for( int i = 0; i < index-1; i++ ) {
            iterator.next();
        }

        return iterator.next();
    }

    public static int getRandomElementFromSet(final IntBooleanMap mappedKeys, Random random) {

        final int index = random.nextInt(mappedKeys.size());

        return mappedKeys.keysView().toArray()[index];
    }

}
