/**
 * Copyright 2020 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.math;

public class Tv {
    public float conf;
    public float freq;

    public Tv(float freq, float conf) {
        this.freq = freq;
        this.conf = conf;
    }

    public static Tv revision(Tv a, Tv b) {
        float w1 = c2w(a.conf);
        float w2 = c2w(b.conf);
        float w = w1 + w2;

        if (w == 0.0f) {
            //return new Tv(0.0f, w2c(w));
        }
        return new Tv((w1 * a.freq + w2 * b.freq) / w, w2c(w));
    }

    public static Tv resemblance(Tv a, Tv b) {
        float f = and(a.freq, b.freq);
        float c = and3(a.conf, b.conf, or(a.freq, b.freq));
        return new Tv(f, c);
    }


    static float and(float a, float b) {
        return a*b;
    }
    static float and3(float a, float b, float c) {
        return a*b*c;
    }
    static float or(float a, float b) {
        float product = 1.0f;
        product *= (1.0f - a);
        product *= (1.0f - b);
        return 1.0f - product;
    }

    static float w2c(float w) {
        float horizon = 1.0f;
        return w / (w + horizon);
    }

    static float c2w(float c) {
        float horizon = 1.0f;
        return horizon * c / (1.0f - c);
    }
}
