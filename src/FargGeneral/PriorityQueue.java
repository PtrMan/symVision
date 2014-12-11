package FargGeneral;

import java.util.ArrayList;
import java.util.Random;

public class PriorityQueue<Type>
{
    private class QueueElement
    {
        public Type value;
        public float priority = 0.0f;
    }
    
    public PriorityQueue(Random random)
    {
        this.random = random;
    }
    
    public QueueElement getReference()
    {
        int chosenIndex;
        int priorityDiscreteRange;
        int chosenDiscretePriority;
        float remainingPriority;

        // NOTE< a priority 10 times as high means the concept gets selected 10 times as much >
        // algorithm is (very) inefficient

        //System.Diagnostics.Debug.Assert(queue.Count > 0);

        priorityDiscreteRange = (int)(prioritySum / PRIORITYGRANULARITY);
        chosenDiscretePriority = random.nextInt(priorityDiscreteRange);
        remainingPriority = (float)chosenDiscretePriority * PRIORITYGRANULARITY;

        chosenIndex = 0;
        for (; ; )
        {
            if( remainingPriority < queue.get(chosenIndex).priority )
            {
                break;
            }
            // else

            remainingPriority -= queue.get(chosenIndex).priority;
        }

        return queue.get(chosenIndex);
    }
    
    /**
    * 
    * only used for (re)filling
    */
    public void add(Type value, float priority)
    {
        QueueElement element;
        
        element = new QueueElement();
        element.priority = priority;
        element.value = value;

        queue.add(element);
    }

    // the queue is sorted by priority
    private ArrayList<QueueElement> queue = new ArrayList<>();

    
    private Random random;
    private float prioritySum = 0.0f;

    private final float PRIORITYGRANULARITY = 0.02f;
}
