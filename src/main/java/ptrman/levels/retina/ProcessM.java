package ptrman.levels.retina;

import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProcessM {
    public class LineParsing {
        public LineParsing(List<SingleLineDetector> lineParsing) {
            this.lineParsing = lineParsing;
        }
        
        public List<SingleLineDetector> lineParsing;
        
        public float processGInterestRating = 0.0f; // rating or interest of the parsing for processG to be a candidate of a curve
        public boolean processGRated = false; // used to check for invalidated curves and rerate them if necessary
    }
    
    public void process(List<RetinaPrimitive> lineDetectors) {
        if( lineDetectors.isEmpty() ) {
            return;
        }
        
        tryToFindLines(lineDetectors, 1);
    }
    
    public List<LineParsing> getLineParsings()
    {
        return lineParsings;
    }
    
    private void tryToFindLines(List<RetinaPrimitive> lineDetectors, int numberOfIterations) {
        int iteration;
        
        lineParsings.clear();
        
        for( iteration = 0; iteration < numberOfIterations; iteration++ ) {
            resetMarkingsWithLocking(lineDetectors);
            selectRandomLineAndTryToTraceAndStoreItAwayWithLocking(lineDetectors);
        }
    }
    
    private static void resetMarkingsWithLocking(List<RetinaPrimitive> lineDetectors) {
        // TODO< lock >
        resetMarkingsSynchronous(lineDetectors);
        // TODO< unlock >
    }
    
    private static void resetMarkingsSynchronous(List<RetinaPrimitive> lineDetectors) {
        for( RetinaPrimitive iterationDetector : lineDetectors ) {
            iterationDetector.line.marked = false;
        }
    }

    private void selectRandomLineAndTryToTraceAndStoreItAwayWithLocking(List<RetinaPrimitive> lineDetectors) {
        int startLineIndex;
        SingleLineDetector startLineDetector;
        ArrayList<SingleLineDetector> lineParsing;
        
        // TODO< lock >
        
        Assert.Assert(!lineDetectors.isEmpty(), "");
        
        startLineIndex = random.nextInt(lineDetectors.size());
        startLineDetector = lineDetectors.get(startLineIndex).line;
        
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
    private ArrayList<SingleLineDetector> findLineParsingForStartLine(SingleLineDetector startLineDetector) {
        SingleLineDetector currentLineDetector;
        ArrayList<SingleLineDetector> resultLineParsing;
        
        resultLineParsing = new ArrayList<>();
        currentLineDetector = startLineDetector;
        
        for(;;) {
            List<Intersection> remainingIntersections;
            
            remainingIntersections = deepCopyIntersections(currentLineDetector.intersections);
            
            // choose from the remaining intersections one and check it if it leads to a nonmarked edge
            for(;;) {
                int indexOfChosenRemainingIntersections;
                Intersection currentIntersection;
                
                if( remainingIntersections.isEmpty() ) {
                    // if we don't have any edges we can't go to any other edge/line, so the "search" is terminated
                    
                    return resultLineParsing;
                }
                
                // take out
                indexOfChosenRemainingIntersections = random.nextInt(remainingIntersections.size());
                currentIntersection = remainingIntersections.get(indexOfChosenRemainingIntersections);
                remainingIntersections.remove(indexOfChosenRemainingIntersections);
                
                Assert.Assert(currentIntersection.partners[0].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "is not line");
                Assert.Assert(currentIntersection.partners[1].primitive.type == RetinaPrimitive.EnumType.LINESEGMENT, "is not line");
                
                // check out if the other side was already marked, if so, continue search for a unmarked edge/line
                if( currentIntersection.partners[0].primitive.line.equals(currentLineDetector) ) {
                    if( currentIntersection.partners[1].primitive.line.marked ) {
                        continue;
                    }
                    // else we are here
                    
                    currentIntersection.partners[1].primitive.line.marked = true;
                    resultLineParsing.add(currentIntersection.partners[1].primitive.line);
                    currentLineDetector = currentIntersection.partners[1].primitive.line;
                }
                else {
                    if( currentIntersection.partners[0].primitive.line.marked ) {
                        continue;
                    }
                    // else we are here
                    
                    currentIntersection.partners[0].primitive.line.marked = true;
                    resultLineParsing.add(currentIntersection.partners[0].primitive.line);
                    currentLineDetector = currentIntersection.partners[0].primitive.line;
                }
            }
        }
    }
    
    private static List<Intersection> deepCopyIntersections(List<Intersection> intersections) {
        List<Intersection> copyed;
        
        copyed = new ArrayList<>();
        
        for( Intersection iterationIntersection : intersections ) {
            copyed.add(iterationIntersection);
        }
        
        return copyed;
    }

    private Random random = new Random();
    
    private List<LineParsing> lineParsings = new ArrayList<>();
}
