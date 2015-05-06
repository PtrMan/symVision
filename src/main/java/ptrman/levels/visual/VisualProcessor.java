package ptrman.levels.visual;

import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.meter.event.DurationStartMeter;
import ptrman.misc.Assert;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 *
 */
public class VisualProcessor
{
    public static class ThresholdMap2dMapperFunction implements Map2dMapper.IMap2dMapper<Float, Boolean>
    {
        public ThresholdMap2dMapperFunction(float threshold) {
            this.threshold = threshold;
        }

        @Override
        public Boolean calculate(Float value)
        {
            return value > threshold;
        }

        private final float threshold;
    }

    public static class ConvertToGrayImageMap2dMapperFunction implements Map2dMapper.IMap2dMapper<ColorRgb, Float>
    {
        private final ColorRgb colorScale;

        public ConvertToGrayImageMap2dMapperFunction(ColorRgb colorScale)
        {
            this.colorScale = colorScale;
        }

        @Override
        public Float calculate(ColorRgb value)
        {
            return value.getScaledNormalizedMagnitude(colorScale);
        }
    }

    public static class FunctionMapperFunction implements Map2dMapper.IMap2dMapper<Float, Float> {
        public interface IFunction {
            float calculate(float value);
        }

        @Override
        public Float calculate(Float value) {
            return function.calculate(value);
        }

        public FunctionMapperFunction(IFunction function) {
            this.function = function;
        }

        private final IFunction function;

    }

    public static class ProcessingChain
    {
        public enum EnumMapType {
            COLOR,
            BOOLEAN,
            FLOAT
        }

        public static class MarrHildrethOperatorParameter
        {
            public MarrHildrethOperatorParameter(int filterSize, float sigma)
            {
                this.filterSize = filterSize;
                this.sigma = sigma;
            }

            public int filterSize;
            public float sigma;
        }

        public interface IFilter<TypeInput, TypeOutput> {
            void apply(IMap2d<TypeInput> input, IMap2d<TypeOutput> output);
        }

        public static class DitheringFilter implements IFilter<Float, Boolean> {
            @Override
            public void apply(IMap2d<Float> input, IMap2d<Boolean> output) {
                Map2dDither.Generic.floydSteinbergDitheringFloatToBoolean(input, output);
            }
        }

        public static class ThresholdFilter implements IFilter<Float, Boolean> {
            public ThresholdFilter(float threshold) {
                this.threshold = threshold;
            }

            @Override
            public void apply(IMap2d<Float> input, IMap2d<Boolean> output) {
                ThresholdMap2dMapperFunction imageMapper;
                Map2dMapper.Mapper<Float, Boolean> mapper;

                imageMapper = new ThresholdMap2dMapperFunction(threshold);
                mapper = new Map2dMapper.Mapper<>();

                mapper.map(imageMapper, input, output);
            }

            private final float threshold;

        }

        public static class ConvertColorRgbToGrayscaleFilter implements IFilter<ColorRgb, Float> {
            public ConvertColorRgbToGrayscaleFilter(ColorRgb colorToGrayColorScale) {
                this.colorToGrayColorScale = colorToGrayColorScale;
            }

            @Override
            public void apply(IMap2d<ColorRgb> input, IMap2d<Float> output) {
                ConvertToGrayImageMap2dMapperFunction mapperFunction;
                Map2dMapper.Mapper<ColorRgb, Float> mapper;

                mapperFunction = new ConvertToGrayImageMap2dMapperFunction(colorToGrayColorScale);
                mapper = new Map2dMapper.Mapper<>();

                mapper.map(mapperFunction, input, output);
            }

            private final ColorRgb colorToGrayColorScale;
        }



        public abstract static class ChainElement {
            public ChainElement(EnumMapType inputType, EnumMapType outputType, String meterName) {
                this.inputType = inputType;
                this.outputType = outputType;
                durationMeters = new DurationStartMeter(meterName, true, 1.0, false);
            }

            public abstract void apply();

            public EnumMapType inputType, outputType;
            public DurationStartMeter durationMeters;
        }

        public abstract static class ApplyChainElement<InputType, ResultType> extends ChainElement {
            public ApplyChainElement(EnumMapType inputType, EnumMapType outputType, String meterName, Vector2d<Integer> imageSize, IFilter<InputType, ResultType> filter) {
                super(inputType, outputType, meterName);
                result = new Map2d<>(imageSize.x, imageSize.y);
                this.filter = filter;
            }

            public void apply() {
                durationMeters.start();
                filter.apply(input, result);
                durationMeters.stop();
            }

            public IMap2d<InputType> input;
            public IMap2d<ResultType> result;
            private IFilter<InputType, ResultType> filter;
        }

        public static class ChainElementFloatFloat extends ApplyChainElement<Float, Float> {
            public ChainElementFloatFloat(IFilter<Float, Float> filter, String meterName, Vector2d<Integer> imageSize) {
                super(EnumMapType.FLOAT, EnumMapType.FLOAT, meterName, imageSize, filter);
            }
        }

        public static class ChainElementFloatBoolean extends ApplyChainElement<Float, Boolean> {
            public ChainElementFloatBoolean(IFilter<Float, Boolean> filter, String meterName, Vector2d<Integer> imageSize) {
                super(EnumMapType.FLOAT, EnumMapType.BOOLEAN, meterName, imageSize, filter);
            }
        }

        public static class ChainElementColorFloat extends ApplyChainElement<ColorRgb, Float> {
            public ChainElementColorFloat(IFilter<ColorRgb, Float> filter, String meterName, Vector2d<Integer> imageSize) {
                super(EnumMapType.COLOR, EnumMapType.FLOAT, meterName, imageSize, filter);
            }
        }



        public ProcessingChain() {

        }

        public void filterChain(IMap2d<ColorRgb> inputColorImage)
        {
            Deque<Integer> chainIndicesToProcess;
            boolean processFromInput;



            chainIndicesToProcess = new ArrayDeque<>();

            processFromInput = true;
            chainIndicesToProcess.add(0);

            for(;;) {
                int currentDagElementIndex;
                Dag.Element<ChainElement> currentDagElement;

                IMap2d MapForFilterOutput;

                if( chainIndicesToProcess.isEmpty() ) {
                    break;
                }

                currentDagElementIndex = chainIndicesToProcess.pollFirst();

                currentDagElement = filterChainDag.elements.get(currentDagElementIndex);

                if( processFromInput ) {
                    ChainElementColorFloat chainElement;

                    processFromInput = false;

                    Assert.Assert(currentDagElement.content.inputType == EnumMapType.COLOR, "");
                    Assert.Assert(currentDagElement.content.outputType == EnumMapType.FLOAT, "");

                    chainElement = (ChainElementColorFloat)currentDagElement.content;

                    chainElement.input = inputColorImage;
                }

                currentDagElement.content.apply();

                MapForFilterOutput = ((ApplyChainElement)currentDagElement.content).result;


                for( int iterationChildIndex : currentDagElement.childIndices ) {
                    Dag.Element<ChainElement> iterationDagElement;

                    iterationDagElement = filterChainDag.elements.get(iterationChildIndex);

                    Assert.Assert(iterationDagElement.content.inputType == currentDagElement.content.outputType, "Types of filters are incompatible");

                    ((ApplyChainElement)iterationDagElement.content).input = MapForFilterOutput;
                }

                chainIndicesToProcess.addAll(currentDagElement.childIndices);
            }


        }

        // entry is [0]
        public Dag<ChainElement> filterChainDag = new Dag<>();
    }
}
