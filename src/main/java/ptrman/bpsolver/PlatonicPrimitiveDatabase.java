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

import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class PlatonicPrimitiveDatabase
{
    public interface IMaxValueCalculator
    {
        float getMaxValue();
    }

    public static class ConstantValueMaxValueCalculator implements IMaxValueCalculator
    {
        public ConstantValueMaxValueCalculator(final float value)
        {
            this.value = value;
        }

        @Override
        public float getMaxValue()
        {
            return value;
        }

        private final float value;
    }

    public float getMaxValueByPrimitiveNode(final PlatonicPrimitiveNode n)
    {
        return mapForMaxValueOfPlatonicPrimitiveNode.computeIfAbsent(n, this::calculateMaxValueForPlatonicPrimitiveNode);
//        if( !mapForMaxValueOfPlatonicPrimitiveNode.containsKey(n) )
//            calculateAndPutMaxValueForPlatonicPrimitiveNodeIntoMap(n);
//
//        return mapForMaxValueOfPlatonicPrimitiveNode.get(n);
    }

    private float calculateMaxValueForPlatonicPrimitiveNode(final PlatonicPrimitiveNode platonicPrimitiveNode)
    {

//        if( !calculatorsForMaxValueOfPlatonicPrimitiveNode.containsKey(platonicPrimitiveNode) )
//        {
//            throw new RuntimeException("Unknown IMaxValueCalculator for PlatonicPrimitiveNode " + platonicPrimitiveNode.platonicType);
//        }

        final var maxValueCalculator = calculatorsForMaxValueOfPlatonicPrimitiveNode.get(platonicPrimitiveNode);
        return maxValueCalculator.getMaxValue();
        //mapForMaxValueOfPlatonicPrimitiveNode.put(platonicPrimitiveNode, maxValue);
    }


    // NOTE ASK< is Haskmap ok? >
    private final Map<PlatonicPrimitiveNode, Float> mapForMaxValueOfPlatonicPrimitiveNode = new HashMap<>();

    // NOTE ASK< is Haskmap ok? >
    public final Map<PlatonicPrimitiveNode, IMaxValueCalculator> calculatorsForMaxValueOfPlatonicPrimitiveNode = new HashMap<>();
}
