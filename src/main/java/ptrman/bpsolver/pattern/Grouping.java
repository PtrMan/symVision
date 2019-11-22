/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.pattern;

import ptrman.bpsolver.NetworkHandles;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Grouping/CLustering algorithm as described in
 * Foundalis disertation chapter 8.3.2
 * Foundalis disertation chapter 8.3.3
 */
public enum Grouping
{
	;

	public static Set<Pattern> group(final Iterable<Pattern> exemplarsSet, final float clusteringThreshold, final NetworkHandles networkHandles, final IMatchingUpdate matchingUpdateImplementation, final FeaturePatternMatching featurePatternMatching)
    {

        final Set<Pattern> patterns = new LinkedHashSet<>();
        final Collection<Pattern> knownExemplars = new LinkedHashSet<>();
        
        for( final var iterationExemplarI : exemplarsSet )
        {
            if( !exemplarIsSimilarToAPattern(iterationExemplarI, patterns, clusteringThreshold, networkHandles, matchingUpdateImplementation, featurePatternMatching) )
            {

                var maxSimilarity = 0.0f;
                Pattern closest = null;
                
                for( final var iterationExemplarJ : knownExemplars )
                {

                    final var similarity = match(iterationExemplarJ, iterationExemplarI, matchingUpdateImplementation, networkHandles, featurePatternMatching);
                    
                    if( similarity > maxSimilarity )
                    {
                        maxSimilarity = similarity;
                        closest = iterationExemplarJ;
                    }
                }
                
                if( maxSimilarity > clusteringThreshold )
                {

                    final var createdPattern = formPattern(closest, iterationExemplarI, networkHandles, matchingUpdateImplementation, featurePatternMatching);
                    
                    if( !resemblesOneOf(createdPattern, patterns, clusteringThreshold, matchingUpdateImplementation, networkHandles, featurePatternMatching) )
                    {
                        patterns.add(createdPattern);
                        knownExemplars.remove(closest);
                    }
                }
            }
            
            knownExemplars.add(iterationExemplarI);
        }
        
        // ASK< why UNION, is this wrong? >
        // does the union
        patterns.addAll(knownExemplars);
        return patterns;
    }
    
    private static boolean exemplarIsSimilarToAPattern(final Pattern exemplar, final Collection<Pattern> patterns, final float clusteringThreshold, final NetworkHandles networkHandles, final IMatchingUpdate matchingUpdateImplementation, final FeaturePatternMatching featurePatternMatching)
    {

        if( patterns.isEmpty() )
            return false;
        // else here

        var maxSimilarity = 0.0f;
        Pattern closestPattern = null; // null means there is none

        for( final var iterationPattern : patterns )
        {
            if( resembles(iterationPattern, exemplar, clusteringThreshold, matchingUpdateImplementation, networkHandles, featurePatternMatching) )
            {

                final var similarity = match(iterationPattern, exemplar, matchingUpdateImplementation, networkHandles, featurePatternMatching);
                if( similarity > maxSimilarity )
                {
                    maxSimilarity = similarity;
                    closestPattern = iterationPattern;
                }
            }
            
            if( closestPattern != null )
                update(closestPattern, exemplar, networkHandles, matchingUpdateImplementation, featurePatternMatching);
        }
        
        return false;
    }
    
    private static void update(final Pattern orginal, final Pattern additional, final NetworkHandles networkHandles, final IMatchingUpdate matchingUpdateImplementation, final FeaturePatternMatching featurePatternMatching)
    {

        final var resultPattern = updateCore(orginal, additional, networkHandles, matchingUpdateImplementation, featurePatternMatching);
        orginal.exemplars = resultPattern.exemplars;
    }
    
    private static Pattern formPattern(final Pattern a, final Pattern b, final NetworkHandles networkHandles, final IMatchingUpdate matchingUpdateImplementation, final FeaturePatternMatching featurePatternMatching)
    {
        return updateCore(a, b, networkHandles, matchingUpdateImplementation, featurePatternMatching);
    }
    
    /**
     * adds the pattern additional to orginal as described in
     * foundalis disertation chapter 8.3.3
     * returns a deep copy of the updated pattern
     * 
     * update and formPattern are the same algorithm
     */
    private static Pattern updateCore(final Pattern orginal, final Pattern additional, final NetworkHandles networkHandles, final IMatchingUpdate matchingUpdateImplementation, final FeaturePatternMatching featurePatternMatching)
    {
        return matchingUpdateImplementation.updateCore(orginal, additional, networkHandles, featurePatternMatching);
    }
    
    private static boolean resemblesOneOf(final Pattern a, final Iterable<Pattern> set, final float clusteringThreshold, final IMatchingUpdate matchingUpdateImplementation, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {
        for( final var iterationPattern : set )
            if (resembles(a, iterationPattern, clusteringThreshold, matchingUpdateImplementation, networkHandles, featurePatternMatching))
                return true;
        
        return false;
    }
    
    private static boolean resembles(final Pattern a, final Pattern b, final float clusteringThreshold, final IMatchingUpdate matchingUpdateImplementation, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {
        return match(a, b, matchingUpdateImplementation, networkHandles, featurePatternMatching) > clusteringThreshold;
    }
    
    private static float match(final Pattern a, final Pattern b, final IMatchingUpdate matchingUpdateImplementation, final NetworkHandles networkHandles, final FeaturePatternMatching featurePatternMatching)
    {
        return matchingUpdateImplementation.match(a, b, networkHandles, featurePatternMatching);
    }
}
