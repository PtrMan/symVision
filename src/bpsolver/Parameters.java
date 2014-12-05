package bpsolver;

public class Parameters
{
    // is the (less than 1.0) factor between two lines to be considered to have the equal length
    // ASK< must this be a nonlinear relationship? >
    public final static float RELATIVELINELENGTHTOBECONSIDEREDEQUAL = 0.9f;
    
    public static class ProcessA
    {
        public final static float MINIMALHITRATIOUNTILTERMINATION = 0.005f;
    }
    
    public static class ProcessD
    {
        public final static float MAXMSE = 4.0f; // max mean square error for inclusion of a point
        
        public final static float LOCKINGACTIVATIONMSESCALE = 0.5f;
        public final static float MINIMALACTIVATIONTOSUMRATIO = 0.0f; // minimal ratio of the activation of an detector to the sum of all detectors to not get discarded
        public final static float LOCKINGACTIVATION = LOCKINGACTIVATIONMSESCALE*MAXMSE+1.0f+5.5f; // the minimal activation of a detector to get locked
    }
}
