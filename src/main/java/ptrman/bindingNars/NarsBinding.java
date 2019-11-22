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
    public final FormatedNarseseConsumer consumer;

    public NarsBinding(final FormatedNarseseConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * returns -1 if not found
     * @param primitives
     * @param primitive
     * @return
     */
    private static int retIdxOf(final List<RetinaPrimitive> primitives, final RetinaPrimitive primitive) {
        var idx = 0;
        for(final var iPrim : primitives) {
            if (iPrim.equals(primitive)) return idx;
            idx++;
        }

        return -1;
    }

    public void emitRetinaPrimitives(final List<RetinaPrimitive> primitives) {
        var primnitiveIdCntr = 0;

        for (final var iPrimitive : primitives) {
            final var axy = iPrimitive.line.a.getDataRef();
            final var ax = (int) Math.round(axy[0]); //(int) axy[0];
            final var ay = (int) Math.round(axy[1]); //axy[1];
            final var bxy = iPrimitive.line.b.getDataRef();
            final var bx = (int) Math.round(bxy[0]); //bxy[0];
            final var by = (int) Math.round(bxy[1]); //bxy[1];

            consumer.emitLineSegment("line"+primnitiveIdCntr, ax,ay,bx,by, iPrimitive.retConf());
            primnitiveIdCntr++;
        }

        // emit line intersections
        {
            for (final var iPrimitive : primitives) {
                if (iPrimitive.line == null)
                    continue;

                for(final var iIntersection :                 iPrimitive.line.intersections) {
                    final var idxA = retIdxOf(primitives, iIntersection.p0.primitive);
                    if (primitives.get(idxA).line != null) {
                        final var idxB = retIdxOf(primitives, iIntersection.p1.primitive);
                        // we only care about line-line intersections
                        if (primitives.get(idxB).line != null)
                            consumer.emitLineIntersection("line" + idxA, "line" + idxB);
                    }
                }
            }
        }
    }
}
