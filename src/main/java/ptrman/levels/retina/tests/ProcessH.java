package ptrman.levels.retina.tests;

import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.levels.retina.SingleLineDetector;
import ptrman.misc.Assert;

import java.util.ArrayList;

/**
 *
 * 
 */
public class ProcessH
{
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
        ArrayList<RetinaPrimitive> detectors;
        ptrman.levels.retina.ProcessH processH = new ptrman.levels.retina.ProcessH();
        
        detectors = new ArrayList<>();
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{7.0f, 5.0f+2.0f*7.0f}), new ArrayRealVector(new double[]{15.0f, 5.0f+2.0f*15.0f}))));
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f+2.0f}), new ArrayRealVector(new double[]{10.0f, 5.0f+2.0f*10.0f+2.0f}))));
        
        // TODO< modernize >
        //processH.process(detectors);
        
        Assert.Assert(detectors.size() == 1, "");
        
        // TODO< test values for >
        // new Vector2d<>(5.0f, 5.0f+2.0f*5.0f+2.0f)
        // new Vector2d<>(15.0f, 5.0f+2.0f*15.0f)
    }
    
    private static void testOverlapAEqual()
    {
        ArrayList<RetinaPrimitive> detectors;
        ptrman.levels.retina.ProcessH processH = new ptrman.levels.retina.ProcessH();
        
        detectors = new ArrayList<>();
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f}), new ArrayRealVector(new double[]{15.0f, 5.0f+2.0f*15.0f}))));
        detectors.add(RetinaPrimitive.makeLine(SingleLineDetector.createFromFloatPositions(new ArrayRealVector(new double[]{5.0f, 5.0f+2.0f*5.0f+2.0f}), new ArrayRealVector(new double[]{10.0f, 5.0f+2.0f*10.0f+2.0f}))));
        
        // TODO< modernize >
        //processH.process(detectors);
        
        Assert.Assert(detectors.size() == 1, "");
    }
}
