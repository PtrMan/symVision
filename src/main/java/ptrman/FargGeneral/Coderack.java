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

import java.util.Random;

/**
 * Propabilistic queue for all codelets
 */
public class Coderack {
    public void enqueue(Codelet codelet, float priority) {
        queue.add(codelet, priority);
    }
    
    public void flush() {
        queue.flush();
    }
    
    public void cycle(int count) {
        int counter;

        for( counter = 0; counter < count; counter++ ) {
            if( queue.getSize() == 0 )
                break;

            PriorityQueue.QueueElement queueElement = queue.dequeueQueueElement();
            Codelet currentCodelet = (Codelet)queueElement.value;
            
            // execute codelet and examine result, enqueue it to the queue if it needs to be execute again
            if( currentCodelet.run().putback )
                queue.add(currentCodelet, queueElement.priority);
        }
    }
    
    private final PriorityQueue queue = new PriorityQueue(new Random());
}
