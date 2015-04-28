package bpsolver;

import bpsolver.nodes.PlatonicPrimitiveNode;

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
