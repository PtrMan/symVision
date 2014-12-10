package bpsolver;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

/**
 * statistics of a feature
 * referenced somehow in workspace/ltm
 * 
 * see faeaco page 143
 */
public class FeatureStatistics
{
    public int numberOfObservations;
    
    
    public float primitiveFeatureMax; // page 214, maximal value related to feature, e.g. slope 100%, angle 180 degree, etc.
    
    public float getSum()
    {
        return (float)statistics.getSum();
    }
    
    public float getSumSqrt()
    {
        return (float)Math.sqrt(statistics.getSum());
    }
    
    public float getStandardDeviation()
    {
        return (float)statistics.getStandardDeviation();
    }
    
    public float getVariance()
    {
        return (float)statistics.getVariance();
    }
    
    public float getMin()
    {
        return (float)statistics.getMin();
    }
    
    public float getMax()
    {
        return (float)statistics.getMax();
    }
    
    // average value of sample data
    public float getMean()
    {
        return (float)statistics.getMean();
    }
    
    public void addValue(float value)
    {
        statistics.addValue(value);
        numberOfObservations++;
    }
    
    public void reset()
    {
        statistics.clear();
        numberOfObservations = 0;
    }
    
    private SummaryStatistics statistics = new SummaryStatistics();
}
