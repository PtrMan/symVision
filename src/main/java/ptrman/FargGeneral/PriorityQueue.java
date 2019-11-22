/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.FargGeneral;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PriorityQueue<Type> {
    public static class QueueElement<Type> {
        public Type value;
        public float priority = 0.0f;
    }
    
    public PriorityQueue(final Random random) {
        this.random = random;
    }
    
    public QueueElement getReference() {

        final var chosenIndex = getNextQueueIndex();

        return queue.get(chosenIndex);
    }
    
    /**
    * 
    * only used for (re)filling
    */
    public void add(final Type value, final float priority) {

        final var element = new QueueElement();
        element.priority = priority;
        element.value = value;
        
        prioritySum += priority;
        
        queue.add(element);
    }
    
    public void flush() {
        queue.clear();
        prioritySum = 0.0f;
    }
    
    public int getSize() {
        return queue.size();
    }
    
    public Type dequeue() {
        return dequeueQueueElement().value;
    }
    
    public QueueElement<Type> dequeueQueueElement() {

        final var chosenIndex = getNextQueueIndex();
        final QueueElement<Type> result = queue.get(chosenIndex);
        queue.remove(chosenIndex);
        
        return result;
    }
    
    private int getNextQueueIndex() {

        // NOTE< a priority 10 times as high means the concept gets selected 10 times as much >
        // algorithm is (very) inefficient

        //System.Diagnostics.Debug.Assert(queue.Count > 0);

        final var priorityDiscreteRange = (int) (prioritySum / PRIORITYGRANULARITY);
        final var chosenDiscretePriority = random.nextInt(priorityDiscreteRange);
        var remainingPriority = (float) chosenDiscretePriority * PRIORITYGRANULARITY;

        var chosenIndex = 0;
        while (true) {
            assert chosenIndex <= queue.size() : "ASSERT: " + "";

            if( chosenIndex == queue.size() ) {
                assert queue.size() != 0 : "ASSERT: " + "";
                chosenIndex = queue.size() - 1;
                
                break;
            }
            
            if( remainingPriority < queue.get(chosenIndex).priority ) break;
            // else

            remainingPriority -= queue.get(chosenIndex).priority;
            
            chosenIndex++;
        }

        return chosenIndex;
    }

    // the queue is sorted by priority
    private final List<QueueElement> queue = new ArrayList<>();

    
    private final Random random;
    private float prioritySum = 0.0f;

    private final float PRIORITYGRANULARITY = 0.02f;
}
