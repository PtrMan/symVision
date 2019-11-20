/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.math;


import org.eclipse.collections.api.list.primitive.IntList;
import org.eclipse.collections.impl.set.mutable.primitive.IntHashSet;

import java.util.*;

public enum Maths {
    ;

    public static float weightFloats(final float valueA, final float weightA, final float valueB, final float weightB) {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }

    public static double weightDoubles(final double valueA, final double weightA, final double valueB, final double weightB) {
        return (valueA*weightA + valueB*weightB)/(weightA + weightB);
    }
    
    public static double power2(double x)
    {
        return x*x;
    }

    // SUPERCOMPILATION candidate
    public static double squaredDistance(double[] data)
    {
        int i;

        // we play supercompiler
        // SUPERCOMPILATION remove this when we use supercompilation
        if( data.length == 2 )
        {
            return power2(data[0]) + power2(data[1]);
        }

        double result = 0.0;

        for( i = 0; i < data.length; i++ )
        {
            result += power2(data[i]);
        }

        return result;
    }
    
    public static int faculty(int value)
    {
        int i;

        int result = 1;
        
        for( i = 1; i < value; i++ )
        {
            result *= i;
        }
        
        return result;
    }

    public static float clamp01(float value)
    {
        return java.lang.Math.min(1.0f, java.lang.Math.max(value, 0.0f));
    }

    public static int clampInt(final int value, final int min, final int max) {
        return java.lang.Math.min(max, java.lang.Math.max(value, min));
    }

    public static int modNegativeWraparound(final int value, final int max) {
        if( value >= 0 ) {
            return value % max;
        }
        else {
            final int positiveValue = -value;
            final int positiveValueMod = positiveValue % max;
            return (max - positiveValueMod) % max;
        }
    }

    // can be very slow
    public static List<Integer> getRandomIndices(final int max, final int numberOfSamples, Random random) {
        List<Integer> candidates = new ArrayList<>();
        List<Integer> result = new ArrayList<>();

        for( int i = 0; i < max; i++ ) {
            candidates.add(i);
        }

        for( int sampleNumber = 0; sampleNumber < numberOfSamples; sampleNumber++ ) {
            final int candidate = random.nextInt(candidates.size());

            final int chosenIndex = candidates.get(candidate);
            candidates.remove(candidate);

            result.add(chosenIndex);
        }

        return result;
    }

    public static IntList getRandomElements(final IntList source, final int numberOfSamples, Random random) {
        if (source.size() <= numberOfSamples)
            return source;

        IntHashSet result = new IntHashSet();

        while( result.size() < numberOfSamples ) {
            final int sampleIndex = random.nextInt(source.size());
            result.add(source.get(sampleIndex));
        }

        return result.toList();
    }

    public static<Type extends Comparable<?>> List<Type> getRandomElements(final List<Type> source, final int numberOfSamples, Random random) {
        if (source.size() <= numberOfSamples)
            return source;

        Set<Type> result = new TreeSet<>();

//        for( int i = 0; i < numberOfSamples; i++ ) {
//            final int sampleIndex = random.nextInt(source.size());
//            result.add(source.get(sampleIndex));
//        }

        while( result.size() < numberOfSamples ) {
            result.add(source.get(random.nextInt(source.size())));
        }

        return new ArrayList<>(result);
    }

    public static int nextPowerOfTwo(final int value) {
        int workingValue = value;

        int bitsFromLeft;
        for( bitsFromLeft = 0; bitsFromLeft < 32; bitsFromLeft++ ) {
            if( (value & (1 << (32-bitsFromLeft))) != 0 )
                break;
        }

        return bitsFromLeft == 0 ? 1 << 31 : 1 << (31 + 1 + 1 - bitsFromLeft);
    }

    public static boolean equals(final double a, final double b, final double epsilon) {
        //if (b > a) {
        return a >= b ? a - b <= epsilon : b - a <= epsilon;
    }

}
