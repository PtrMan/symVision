/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bindingNars;

import ptrman.levels.retina.Intersection;
import ptrman.levels.retina.RetinaPrimitive;

import java.util.List;

public class NarsBinding {
    public FormatedNarseseConsumer consumer;

    public NarsBinding(FormatedNarseseConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * returns -1 if not found
     * @param primitives
     * @param primitive
     * @return
     */
    private static int retIdxOf(List<RetinaPrimitive> primitives, RetinaPrimitive primitive) {
        int idx = 0;
        for(RetinaPrimitive iPrim : primitives) {
            if (iPrim.equals(primitive)) {
                return idx;
            }
            idx++;
        }

        return -1;
    }

    public void emitRetinaPrimitives(List<RetinaPrimitive> primitives) {
        int primnitiveIdCntr = 0;

        for (RetinaPrimitive iPrimitive : primitives) {
            double[] axy = iPrimitive.line.a.getDataRef();
            int ax = (int) Math.round(axy[0]); //(int) axy[0];
            int ay = (int) Math.round(axy[1]); //axy[1];
            double[] bxy = iPrimitive.line.b.getDataRef();
            int bx = (int) Math.round(bxy[0]); //bxy[0];
            int by = (int) Math.round(bxy[1]); //bxy[1];

            consumer.emitLineSegment("line"+primnitiveIdCntr, ax,ay,bx,by, iPrimitive.retConf());
            primnitiveIdCntr++;
        }

        // emit line intersections
        {
            for (RetinaPrimitive iPrimitive : primitives) {
                if (iPrimitive.line == null)
                    continue;

                for(Intersection iIntersection :                 iPrimitive.line.intersections) {
                    int idxA = retIdxOf(primitives, iIntersection.p0.primitive);
                    if (primitives.get(idxA).line != null) {
                        int idxB = retIdxOf(primitives, iIntersection.p1.primitive);
                        if (primitives.get(idxB).line != null) {
                            // we only care about line-line intersections
                            consumer.emitLineIntersection("line" + idxA, "line" + idxB);
                        }
                    }
                }
            }
        }
    }
}
