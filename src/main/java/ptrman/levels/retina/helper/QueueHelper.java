package ptrman.levels.retina.helper;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class QueueHelper {
    public static <Type> List<Type> getAllElementsFromQueueAsList(Queue<Type> queue) {
        final int size = queue.size();

        List<Type> result = new ArrayList<>();

        for( int i = 0; i < size; i++ ) {
            result.add(queue.peek());
        }

        return result;
    }

    public static <Type> void transferAllElementsFromListToQueue(final List<Type> elements, Queue<Type> queue) {
        for( final Type element : elements ) {
            queue.add(element);
        }
    }
}
