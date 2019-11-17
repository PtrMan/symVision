/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver;

/**
 * class to allow parameter tuning while the program is running, but also let parameters be set to constants (which the compiler or JIT hopefuly optimizes out)
 * 
 */
public enum Parameters
{
	;

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
    
    public static float currentProcessdMaxMse = 50.0f;
    public static float currentProcessdLockingActivationOffset;
    public static float currentProcessdLockingActivationScale;
    public static float currentPatternMatchingMinSimilarity;
    
    private final static boolean ALLOWTUNINGPROCESSDMAXMSE = true;
    private final static boolean ALLOWTUNINGPROCESSDLOCKINGACTIVATIONOFFSET = true;
    private final static boolean ALLOWTUNINGPROCESSDLOCKINGACTIVATIONSCALE = true;
    private final static boolean ALLOWPATTERNMATCHINGMINSIMILARITY = true;
}
