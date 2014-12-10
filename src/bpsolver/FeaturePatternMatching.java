package bpsolver;

import org.apache.commons.math3.analysis.UnivariateFunction;
import org.apache.commons.math3.analysis.integration.BaseAbstractUnivariateIntegrator;

/**
 * class which encapuslates feature matching for learning/matching in the workspace/ltm
 * 
 * page 214 ff
 */
public class FeaturePatternMatching
{
    private class IntegrateTDistributionUpperIntegral implements UnivariateFunction
    {
        public float n;
        
        public double value(double d)
        {
            float x;
            float insidePower;
            
            x = (float)d;

            insidePower = (n - 1.0f)/(n - 1.0f + x*x);
            return Math.pow(insidePower, n/2.0f);
        }
    }
    
    private class IntegrateTDistributionLowerIntegral implements UnivariateFunction
    {
        public float n;
        
        public double value(double d)
        {
            float x;
            
            x = (float)d;

            return Math.pow(Math.sin(x), n);
        }
    }
    
    private class IntegrateEquation8Dot4 implements UnivariateFunction
    {
        public double value(double d)
        {
            return Math.pow(Math.E, -0.5*d*d);
        }
    }
    
    /**
     * 
     * f1 and f2 are from the Node of the same type (excluding numerosity)
     */
    public float matchSameTypeNonNumerosity(FeatureStatistics f1, FeatureStatistics f2)
    {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 )
        {
            return matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(f1, f2);
        }
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 )
        {
            return matchSameTypeNonNumerosityWithF1Equals1(f1, f2);
        }
        else
        {
            return matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(f1, f2);
        }
    }
    
    public float matchSameTypeNumerosity(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2)
    {
        if( f1.numberOfObservations == 1 && f2.numberOfObservations == 1 )
        {
            return matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(f1, numeriosity1, f2, numeriosity2);
        }
        else if( f1.numberOfObservations == 1 && f2.numberOfObservations > 1 )
        {
            return matchSameTypeNumerosityWithF1Equals1(f1, numeriosity1, f2, numeriosity2);
        }
        else
        {
            return matchSameTypeNumeroistyWithF1AndF2NotEqual1(f1, numeriosity1, f2, numeriosity2);
        }
    }
    
    private float matchSameTypeNumerosityWithBothNumberOfObservationsEquals1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2)
    {
        float z;
        float l, s;
        
        l = Math.max(numeriosity1, numeriosity2);
        s = Math.min(numeriosity1, numeriosity2);
        
        z = Math.abs(l-s)/(SIGMAZERO*(float)Math.sqrt(l+s));
        
        return calcNumeriosityD(z);
    }
    
    // maybe this is wrong implemented
    private float matchSameTypeNumerosityWithF1Equals1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2)
    {
        float z;
        float insideSqrt;
        
        insideSqrt = SIGMAZERO*SIGMAZERO*f1.getMean() + math.Math.power2(f2.getStandardDeviation())/math.Math.power2(f2.numberOfObservations);
        z = (float)(Math.abs(f1.getMean() - f2.getMean())/Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float matchSameTypeNumeroistyWithF1AndF2NotEqual1(FeatureStatistics f1, int numeriosity1, FeatureStatistics f2, int numeriosity2)
    {
        float z;
        float insideSqrt;
        
        insideSqrt = math.Math.power2(f1.getStandardDeviation())/f1.numberOfObservations + math.Math.power2(f2.getStandardDeviation())/f2.numberOfObservations;
        z = (float)(Math.abs(f1.getMean() - f2.getMean())/Math.sqrt(insideSqrt));
        
        return calcNumeriosityD(z);
    }
    
    private float calcNumeriosityD(float z)
    {
        return (float)(1.0f/(Math.sqrt(2.0*Math.PI)))*(float)integrator.integrate(INTEGRATEMAXEVAL, integrateEquation8Dot4, -z, z);
    }
    
    private static float matchSameTypeNonNumerosityWithBothNumberOfObservationsEquals1(FeatureStatistics f1, FeatureStatistics f2)
    {
        return Math.abs(f1.getMean() - f2.getMean()) / f1.primitiveFeatureMax;
    }
    
    private float matchSameTypeNonNumerosityWithF1Equals1(FeatureStatistics f1, FeatureStatistics f2)
    {
        float t;
        float n;
        
        n = (float)f2.numberOfObservations;
        t = ((f2.getMean()-f1.getMean())*(float)Math.sqrt(n))/f2.getStandardDeviation();
        
        return calcStudentTDistribution(n, t);
    }
    
    private float matchSameTypeNonNumeroistyWithF1AndF2NotEqual1(FeatureStatistics f1, FeatureStatistics f2)
    {
        float s1, s2, n1, n2;
        float nDividend;
        float nDivisorSum1, nDivisorSum2;
        float n;
        float t;
        
        s1 = f1.getStandardDeviation();
        n1 = f1.numberOfObservations;
        
        s2 = f2.getStandardDeviation();
        n2 = f2.numberOfObservations;
        
        nDividend = math.Math.power2(s1)/n1 + math.Math.power2(s2)/n2;
        nDivisorSum1 = math.Math.power2((s1*s1)/n1) / (n1 - 1.0f);
        nDivisorSum2 = math.Math.power2((s2*s2)/n2) / (n2 - 1.0f);
        
        n = nDividend / (nDivisorSum1 + nDivisorSum2);
        t = Math.abs(f1.getMean()-f2.getMean())/(float)Math.sqrt(math.Math.power2(s1)/n1 + math.Math.power2(s2)/n2);
        
        return calcStudentTDistribution(n, t);
    }
    
    private float calcStudentTDistribution(float n, float t)
    {
        
        
        float upperIntegral, lowerIntegral;

        integrateTDistributionUpperIntegral.n = n;
        integrateTDistributionLowerIntegral.n = n;
        
        upperIntegral = (float)integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionUpperIntegral, -t, t);
        lowerIntegral = (float)integrator.integrate(INTEGRATEMAXEVAL, integrateTDistributionLowerIntegral, 0, 2.0f*Math.PI);
        
        return (upperIntegral)/((float)Math.sqrt(n-1.0f)*lowerIntegral);
    }
    
    public BaseAbstractUnivariateIntegrator integrator;
    private IntegrateTDistributionUpperIntegral integrateTDistributionUpperIntegral = new IntegrateTDistributionUpperIntegral();
    private IntegrateTDistributionLowerIntegral integrateTDistributionLowerIntegral = new IntegrateTDistributionLowerIntegral();
    private IntegrateEquation8Dot4 integrateEquation8Dot4 = new IntegrateEquation8Dot4();
    
    private final static float SIGMAZERO = 1.0f; // used in the comparison of numerosity nodes
    
    private final static int INTEGRATEMAXEVAL = 50;
}
