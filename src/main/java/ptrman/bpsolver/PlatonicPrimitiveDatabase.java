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
        public ConstantValueMaxValueCalculator(float value)
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

    public float getMaxValueByPrimitiveNode(PlatonicPrimitiveNode platonicPrimitiveNodeForSearch)
    {
        if( !mapForMaxValueOfPlatonicPrimitiveNode.containsKey(platonicPrimitiveNodeForSearch) )
        {
            calculateAndPutMaxValueForPlatonicPrimitiveNodeIntoMap(platonicPrimitiveNodeForSearch);
        }

        return mapForMaxValueOfPlatonicPrimitiveNode.get(platonicPrimitiveNodeForSearch);
    }

    private void calculateAndPutMaxValueForPlatonicPrimitiveNodeIntoMap(PlatonicPrimitiveNode platonicPrimitiveNode)
    {
        float maxValue;
        IMaxValueCalculator maxValueCalculator;

        if( !calculatorsForMaxValueOfPlatonicPrimitiveNode.containsKey(platonicPrimitiveNode) )
        {
            throw new RuntimeException("Unknown IMaxValueCalculator for PlatonicPrimitiveNode " + platonicPrimitiveNode.platonicType);
        }

        maxValueCalculator = calculatorsForMaxValueOfPlatonicPrimitiveNode.get(platonicPrimitiveNode);
        maxValue = maxValueCalculator.getMaxValue();

        mapForMaxValueOfPlatonicPrimitiveNode.put(platonicPrimitiveNode, maxValue);
    }


    // NOTE ASK< is Haskmap ok? >
    private Map<PlatonicPrimitiveNode, Float> mapForMaxValueOfPlatonicPrimitiveNode = new HashMap<>();

    // NOTE ASK< is Haskmap ok? >
    public Map<PlatonicPrimitiveNode, IMaxValueCalculator> calculatorsForMaxValueOfPlatonicPrimitiveNode = new HashMap<>();
}
