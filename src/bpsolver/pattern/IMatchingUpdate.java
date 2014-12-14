package bpsolver.pattern;

import bpsolver.NetworkHandles;

/**
 * abstract interface for decoupling the implementation of the matching (belongs a exemplar to a pattern group)
 * and updating (integrating an exemplar/Pattern into a pattern)
 * 
 */
public interface IMatchingUpdate
{
    // TODO< matching >
    Pattern updateCore(Pattern orginal, Pattern additional, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching);
    
    float match(Pattern a, Pattern b, NetworkHandles networkHandles, FeaturePatternMatching featurePatternMatching);
}
