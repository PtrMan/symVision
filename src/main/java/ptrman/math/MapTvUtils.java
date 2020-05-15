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

import ptrman.Datastructures.IMap2d;

import java.util.ArrayList;
import java.util.List;

public class MapTvUtils {
    // compute MapTv from Map
    // conf default is 0.02
    public static List<Tv> convToMapTv(IMap2d<Boolean> img, float conf, boolean onlyTrue) {
        List<Tv> res = new ArrayList<>();
        for(int iy=0;iy<img.getLength();iy++) for(int ix=0;ix<img.getWidth();ix++) {
            Boolean val2 = img.readAt(iy,ix);
            boolean val = val2 != null ? val2 : false;
            if (val) {
                res.add(new Tv(1.0f, conf));
                res.add(new Tv(0.0f, conf));
            }
            else {
                if (!onlyTrue) {
                    res.add(new Tv(0.0f, conf));
                    res.add(new Tv(1.0f, conf));
                }
                else {
                    res.add(new Tv(0.0f, 0.0f));
                    res.add(new Tv(0.0f, 0.0f));
                }

            }
        }
        return res;
    }

    public static Tv[] resemblance(List<Tv> a, List<Tv> b) {
        Tv[] res = new Tv[a.size()];
        for(int idx=0;idx<a.size();idx++) {
            res[idx] = Tv.resemblance(a.get(idx),b.get(idx));
        }
        return res;
    }

    public static Tv[] resemblance(Tv[] a, Tv[] b) {
        Tv[] res = new Tv[a.length];
        for(int idx=0;idx<a.length;idx++) {
            res[idx] = Tv.resemblance(a[idx],b[idx]);
        }
        return res;
    }

    public static List<Tv> revision(List<Tv> a, List<Tv> b) {
        List<Tv> res = new ArrayList<>();
        for(int idx=0;idx<a.size();idx++) {
            res.add(Tv.revision(a.get(idx),b.get(idx)));
        }
        return res;
    }

    // compute merged tv by revision
    public static Tv calcMergedTv(List<Tv> arr) {
        Tv res = arr.get(0);
        for(int idx=1; idx<arr.size();idx++) {
            res = Tv.revision(res, arr.get(idx));
        }
        return res;
    }

    // compute merged tv by revision
    public static Tv calcMergedTv(Tv[] arr) {
        Tv res = arr[0];
        for(int idx=1; idx<arr.length;idx++) {
            res = Tv.revision(res, arr[idx]);
        }
        return res;
    }
}


