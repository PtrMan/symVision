package ptrman.bpsolver;

/**
 * class to allow parameter tuning while the program is running, but also let parameters be set to constants (which the compiler or JIT hopefuly optimizes out)
 * 
 */
public class Parameters
{
    public static float getProcessdLockingActivation()
    {
        return getProcessdMaxMse()*getProcessdLockingActivationScale()+getProcessdLockingActivationOffset();
    }
    
    public static float getProcessdMaxMse()
    {
        if( ALLOWTUNINGPROCESSDMAXMSE )
        {
            return currentProcessdMaxMse;
        }
        else
        {
            return HardParameters.ProcessD.MAXMSE;
        }
    }
    
    public static float getProcessdLockingActivationOffset()
    {
        if( ALLOWTUNINGPROCESSDLOCKINGACTIVATIONOFFSET )
        {
            return currentProcessdLockingActivationOffset;
        }
        else
        {
            return HardParameters.ProcessD.LOCKINGACTIVATIONOFFSET;
        }
    }
    
    public static float getProcessdLockingActivationScale()
    {
        if( ALLOWTUNINGPROCESSDLOCKINGACTIVATIONSCALE )
        {
            return currentProcessdLockingActivationScale;
        }
        else
        {
            return HardParameters.ProcessD.LOCKINGACTIVATIONMSESCALE;
        }
    }

    public static float getPatternMatchingMinSimilarity()
    {
        if( ALLOWPATTERNMATCHINGMINSIMILARITY )
        {
            return currentPatternMatchingMinSimilarity;
        }
        else
        {
            return HardParameters.PatternMatching.MINSIMILARITY;
        }
    }

    public static void init()
    {
        currentProcessdMaxMse = HardParameters.ProcessD.MAXMSE;
        currentProcessdLockingActivationOffset = HardParameters.ProcessD.LOCKINGACTIVATIONOFFSET;
        currentProcessdLockingActivationScale = HardParameters.ProcessD.LOCKINGACTIVATIONMSESCALE;
        currentPatternMatchingMinSimilarity = HardParameters.PatternMatching.MINSIMILARITY;
    }
    
    public static float currentProcessdMaxMse;
    public static float currentProcessdLockingActivationOffset;
    public static float currentProcessdLockingActivationScale;
    public static float currentPatternMatchingMinSimilarity;
    
    private final static boolean ALLOWTUNINGPROCESSDMAXMSE = true;
    private final static boolean ALLOWTUNINGPROCESSDLOCKINGACTIVATIONOFFSET = true;
    private final static boolean ALLOWTUNINGPROCESSDLOCKINGACTIVATIONSCALE = true;
    private final static boolean ALLOWPATTERNMATCHINGMINSIMILARITY = true;
}
