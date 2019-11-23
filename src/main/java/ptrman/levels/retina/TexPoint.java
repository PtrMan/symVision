/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.levels.retina;

import org.eclipse.collections.api.tuple.primitive.IntIntPair;

/**
 * sample to indicate texture, gradient, filling, etc
 */
public class TexPoint {
    public double value = 0.0; // how much is the texture/filling the case?

    public double gradientX = 0.0;
    public double gradientY = 0.0;

    // TODO< add  >
    public String type = ""; // type of texture, can be "f" for filling, "g" for gradient

    //public IntIntPair pos; // position in image
    public int x;
    public int y;

    public TexPoint(int x, int y, String type) {
        this.x = x; this.y = y;
        this.type = type;
    }
}
