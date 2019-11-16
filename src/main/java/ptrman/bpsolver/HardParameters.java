package ptrman.bpsolver;

public enum HardParameters
{
	;
	// is the (less than 1.0) factor between two lines to be considered to have the equal length
    // ASK< must this be a nonlinear relationship? >
    public final static float RELATIVELINELENGTHTOBECONSIDEREDEQUAL = 0.9f;
    
    public enum ProcessA
    {
		;
		public final static float MINIMALHITRATIOUNTILTERMINATION = 0.005f;
    }
    
    public enum ProcessD
    {
		;
		public final static float MAXMSE = 50.0f; // 5.04f; // max mean square error for inclusion of a point
        
        public final static float LOCKINGACTIVATIONMSESCALE = 1.0f;
        public final static float MINIMALACTIVATIONTOSUMRATIO = 0.0f; // minimal ratio of the activation of an detector to the sum of all detectors to not get discarded
        public final static float LOCKINGACTIVATIONOFFSET = 4.6f; // LOCKINGACTIVATION = LOCKINGACTIVATIONMSESCALE*MAXMSE+this; // the minimal activation of a detector to get locked
        
        public final static float LINECLUSTERINGMAXDISTANCE = 7.0f; // how many units (pixels) can be the distance of points of a line to be considered to lay on the same line
        
        public final static float EARLYCANDIDATEMAXDISTANCE = 4.0f; // maximal distance for the early candidates of a line inside a radius of a "center point"
        public final static int EARLYCANDIDATECOUNT = 5;

        public final static double MINIMAL_LINESEGMENTLENGTH = 5.0;
        public static final double LENGTH_MEAN_MULTIPLIER = 0.9;
        public static final int LAST_RECORDS_FROM_LINECANDIDATES_STACK = 2;
        public static final double SAMPLES_NUMBER_OF_TRIES_MULTIPLIER = 5.5;
    }

    public enum ProcessC {
		;
		public final static double FILLEDREGIONALTITUDETHRESHOLD = 4.0f; // after which altitude is a endosceleton sample from a filled region
        public final static float FILLEDREGIONCANDIDATEPROPABILITY = 0.8f;
    }
    
    public enum ProcessE
    {
		;
		public final static int NEIGHTBORHOODSEARCHRADIUS = 2;
    }
    
    public enum ProcessH
    {
		;
		public final static float MAXDISTANCEFORCANDIDATEPOINT = 5.0f;
    }
    
    public enum ProcessG
    {
		;
		public final static float RATINGANGLEMULTIPLIER = 0.2f;
        public final static float RATINGENDTOENDMAXDISTANCE = 5.0f;
        public final static float RATINGENDTOENDMULTIPLIER = 0.7f;
        public final static float VICINITYRADIUS = 10.0f;
        public final static float MAXIMALDISTANCEOFENDOSCELETONTOLINE = 3.0f;
    }
    
    public enum FeatureWeights
    {
		;
		public final static float LINESEGMENTFEATURELINELENGTH = 0.7f;
    }

    public enum PatternMatching
    {
		;
		public static final float MINSIMILARITY = 0.05f; // TODO< tune >
        public static final int MAXDEPTH = 3;
    }
}
