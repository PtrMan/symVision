package bpsolver;

public class HardParameters
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
        public final static float MAXMSE = 5.04f; // max mean square error for inclusion of a point
        
        public final static float LOCKINGACTIVATIONMSESCALE = 1.0f;
        public final static float MINIMALACTIVATIONTOSUMRATIO = 0.0f; // minimal ratio of the activation of an detector to the sum of all detectors to not get discarded
        public final static float LOCKINGACTIVATIONOFFSET = 4.6f; // LOCKINGACTIVATION = LOCKINGACTIVATIONMSESCALE*MAXMSE+this; // the minimal activation of a detector to get locked
        
        public final static float LINECLUSTERINGMAXDISTANCE = 7.0f; // how many units (pixels) can be the distance of points of a line to be considered to lay on the same line
    }
    
    public static class ProcessE
    {
        public final static int NEIGHTBORHOODSEARCHRADIUS = 10;
    }
    
    public static class ProcessH
    {
        public final static float MAXDISTANCEFORCANDIDATEPOINT = 3.0f;
    }
    
    public static class FeatureWeights
    {
        public final static float LINESEGMENTFEATURELINELENGTH = 0.7f;
    }
}
