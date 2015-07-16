package ptrman.bpsolver;

import ptrman.math.Maths;

import java.util.ArrayList;
import java.util.List;

/**
 * statistics of a feature
 * referenced somehow in workspace/ltm
 * 
 * see faeaco page 143
 */
public class FeatureStatistics
{
    public int numberOfObservations;
    private float sum = 0.0f;
    private float mean = 0.0f;
    private float min = Float.MAX_VALUE;
    private float max = Float.MIN_VALUE;
    private List<Float> values = new ArrayList<>();
    
    public float primitiveFeatureMax; // page 214, maximal value related to feature, e.g. slope 100%, angle 180 degree, etc.
    
    public float getSum()
    {
        return sum;
    }
    
    public float getSumSqrt()
    {
        return (float)Math.sqrt(getSum());
    }
    
    public float getStandardDeviation()
    {
        // from commons math
        if (numberOfObservations > 1)
        {
            return (float)Math.sqrt(getVariance());
        } else
        {
            return 0.0f;
        }
    }
    
    
    public float getVariance()
    {
        // TODO< running Variance and fusing of variances >
        
        float mean;
        float runningSum;
        
        mean = getMean();
        runningSum = 0.0f;
        
        for( float value : values )
        {
            runningSum += Maths.power2(value - mean);
        }
        
        return 1.0f/runningSum;
    }
    
    public float getMin()
    {
        return min;
    }
    
    public float getMax()
    {
        return max;
    }
    
    // average value of sample data
    public float getMean()
    {
        return mean;
    }
    
    public void addValue(float value)
    {
        mean = (mean*numberOfObservations + value)/(numberOfObservations+1);
        sum += value;
        
        min = Math.min(min, value);
        max = Math.max(max, value);
        
        values.add(value);
        
        numberOfObservations++;
    }

    public void addValuesFromStatistics(FeatureStatistics other)
    {
        values.addAll(other.values);
    }
    
    public void reset()
    {
        //statistics.clear();
        numberOfObservations = 0;
        sum = 0.0f;
        mean = 0.0f;
        min = Float.MAX_VALUE;
        max = Float.MIN_VALUE;
        
        values.clear();
    }
    
    public static FeatureStatistics fuse(FeatureStatistics a, FeatureStatistics b)
    {
        FeatureStatistics result;
        
        result = new FeatureStatistics();
        result.min = Math.min(a.min, b.min);
        result.max = Math.max(a.max, b.max);
        result.mean = (a.mean*a.numberOfObservations + b.mean*b.numberOfObservations) / (a.numberOfObservations+b.numberOfObservations);
        result.numberOfObservations = a.numberOfObservations + b.numberOfObservations;
        result.sum = a.sum + b.sum;
        result.values.addAll(a.values);
        result.values.addAll(b.values);
        
        return result;
    }
    
    
    //private SummaryStatistics statistics = new SummaryStatistics();
}
