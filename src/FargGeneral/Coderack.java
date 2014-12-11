package FargGeneral;

import FargGeneral.Codelet;
import java.util.Random;

/**
 * Propabilistic queue for all codelets
 */
public class Coderack
{
    public void enqueue(Codelet codelet, float priority)
    {
        queue.add(codelet, priority);
    }
    
    private PriorityQueue queue = new PriorityQueue(new Random());
}
