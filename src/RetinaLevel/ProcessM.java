package RetinaLevel;

import java.util.ArrayList;
import java.util.Random;
import misc.Assert;

public class ProcessM
{
    public class LineParsing
    {
        public LineParsing(ArrayList<SingleLineDetector> lineParsing)
        {
            this.lineParsing = lineParsing;
        }
        
        public ArrayList<SingleLineDetector> lineParsing;
        
        public float processGInterestRating = 0.0f; // rating or interest of the parsing for processG to be a candidate of a curve
        public boolean processGRated = false; // used to check for invalidated curves and rerate them if necessary
    }
    
    public void process(ArrayList<SingleLineDetector> lineDetectors)
    {
        if( lineDetectors.isEmpty() )
        {
            return;
        }
        
        tryToFindLines(lineDetectors, 1);
    }
    
    public ArrayList<LineParsing> getLineParsings()
    {
        return lineParsings;
    }
    
    private void tryToFindLines(ArrayList<SingleLineDetector> lineDetectors, int numberOfIterations)
    {
        int iteration;
        
        lineParsings.clear();
        
        for( iteration = 0; iteration < numberOfIterations; iteration++ )
        {
            resetMarkingsWithLocking(lineDetectors);
            selectRandomLineAndTryToTraceAndStoreItAwayWithLocking(lineDetectors);
        }
    }
    
    private static void resetMarkingsWithLocking(ArrayList<SingleLineDetector> lineDetectors)
    {
        // TODO< lock >
        resetMarkingsSynchronous(lineDetectors);
        // TODO< unlock >
    }
    
    private static void resetMarkingsSynchronous(ArrayList<SingleLineDetector> lineDetectors)
    {
        for( SingleLineDetector iterationDetector : lineDetectors )
        {
            iterationDetector.marked = false;
        }
    }

    private void selectRandomLineAndTryToTraceAndStoreItAwayWithLocking(ArrayList<SingleLineDetector> lineDetectors)
    {
        int startLineIndex;
        SingleLineDetector startLineDetector;
        ArrayList<SingleLineDetector> lineParsing;
        
        // TODO< lock >
        
        Assert.Assert(!lineDetectors.isEmpty(), "");
        
        startLineIndex = random.nextInt(lineDetectors.size());
        startLineDetector = lineDetectors.get(startLineIndex);
        
        lineParsing = findLineParsingForStartLine(startLineDetector);
        lineParsings.add(new LineParsing(lineParsing));
        
        // TODO< unlock >
    }
    
    /**
     * 
     * \result returns the (possible random) line parsing
     * 
     * --- the lines are locked
     */
    private ArrayList<SingleLineDetector> findLineParsingForStartLine(SingleLineDetector startLineDetector)
    {
        SingleLineDetector currentLineDetector;
        ArrayList<SingleLineDetector> resultLineParsing;
        
        resultLineParsing = new ArrayList<SingleLineDetector>();
        currentLineDetector = startLineDetector;
        
        for(;;)
        {
            ArrayList<Intersection> remainingIntersections;
            
            remainingIntersections = deepCopyIntersections(currentLineDetector.intersections);
            
            // choose from the remaining intersections one and check it if it leads to a nonmarked edge
            for(;;)
            {
                int indexOfChosenRemainingIntersections;
                Intersection currentIntersection;
                
                if( remainingIntersections.isEmpty() )
                {
                    // if we don't have any edges we can't go to any other edge/line, so the "search" is terminated
                    
                    return resultLineParsing;
                }
                
                // take out
                indexOfChosenRemainingIntersections = random.nextInt(remainingIntersections.size());
                currentIntersection = remainingIntersections.get(indexOfChosenRemainingIntersections);
                remainingIntersections.remove(indexOfChosenRemainingIntersections);
                
                Assert.Assert(currentIntersection.partners[0].type == Intersection.EnumType.LINE, "is not line");
                Assert.Assert(currentIntersection.partners[1].type == Intersection.EnumType.LINE, "is not line");
                
                // check out if the other side was already marked, if so, continue search for a unmarked edge/line
                if( currentIntersection.partners[0].line.equals(currentLineDetector) )
                {
                    if( currentIntersection.partners[1].line.marked )
                    {
                        continue;
                    }
                    // else we are here
                    
                    currentIntersection.partners[1].line.marked = true;
                    resultLineParsing.add(currentIntersection.partners[1].line);
                    currentLineDetector = currentIntersection.partners[1].line;
                }
                else
                {
                    if( currentIntersection.partners[0].line.marked )
                    {
                        continue;
                    }
                    // else we are here
                    
                    currentIntersection.partners[0].line.marked = true;
                    resultLineParsing.add(currentIntersection.partners[0].line);
                    currentLineDetector = currentIntersection.partners[0].line;
                }
            }
        }
    }
    
    private static ArrayList<Intersection> deepCopyIntersections(ArrayList<Intersection> intersections) {
        ArrayList<Intersection> copyed;
        
        copyed = new ArrayList<>();
        
        for( Intersection iterationIntersection : intersections )
        {
            copyed.add(iterationIntersection);
        }
        
        return copyed;
    }
    
    
    private Random random = new Random();
    
    private ArrayList<LineParsing> lineParsings = new ArrayList<>();
}
