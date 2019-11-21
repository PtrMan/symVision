/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.InternalTests;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.retina.SingleLineDetector;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * 
 */
public enum ProcessH
{
	;

	public static void main(String[] p)
    {
        test();
    }
    
    public static void test()
    {
        testOverlapA();
        testOverlapAEqual();
    }
    
    private static void testOverlapA()
    {
		ptrman.levels.retina.ProcessH processH = new ptrman.levels.retina.ProcessH();

		Collection<RetinaPrimitive> detectors = new ArrayList<>();
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{7.0f, 5.0f+2.0f*7.0f}), new ArrayRealVector(new double[]{15.0f, 5.0f+2.0f*15.0f}), 0.2)));
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f+2.0f}), new ArrayRealVector(new double[]{10.0f, 5.0f+2.0f*10.0f+2.0f}), 0.2)));
        
        // TODO< modernize >
        //processH.process(detectors);
        
        Assert.Assert(detectors.size() == 1, "");
        
        // TODO< test values for >
        // new Vector2d<>(5.0f, 5.0f+2.0f*5.0f+2.0f)
        // new Vector2d<>(15.0f, 5.0f+2.0f*15.0f)
    }
    
    private static void testOverlapAEqual()
    {
		ptrman.levels.retina.ProcessH processH = new ptrman.levels.retina.ProcessH();

		Collection<RetinaPrimitive> detectors = new ArrayList<>();
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f}), new ArrayRealVector(new double[]{15.0f, 5.0f+2.0f*15.0f}), 0.2)));
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f+2.0f}), new ArrayRealVector(new double[]{10.0f, 5.0f+2.0f*10.0f+2.0f}), 0.2)));
        
        // TODO< modernize >
        //processH.process(detectors);
        
        Assert.Assert(detectors.size() == 1, "");
    }
}
