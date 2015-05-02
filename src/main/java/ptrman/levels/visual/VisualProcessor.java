package ptrman.levels.visual;

import ptrman.Datastructures.Dag;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Map2d;
import ptrman.Datastructures.Vector2d;
import ptrman.meter.event.DurationStartMeter;
import ptrman.misc.Assert;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

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

    /**
     *
     * color to grayscale
     *
     * encapsulated for readable code
     *
     * TODO opencl
     */
    private static void convertColorToGrayImage(IMap2d<ColorRgb> inputColorImage, IMap2d<Float> outputGrayImage, ColorRgb colorScale)
    {
        class ConvertToGrayImageMap2dMapper implements Map2dMapper.IMap2dMapper<ColorRgb, Float>
        {
            private final ColorRgb colorScale;

            public ConvertToGrayImageMap2dMapper(ColorRgb colorScale)
            {
                this.colorScale = colorScale;
            }

            @Override
            public Float calculate(ColorRgb value)
            {
                return value.getScaledNormalizedMagnitude(colorScale);
            }
        }

        ConvertToGrayImageMap2dMapper imageMapper;
        Map2dMapper.Mapper<ColorRgb, Float> mapper;

        imageMapper = new ConvertToGrayImageMap2dMapper(colorScale);
        mapper = new Map2dMapper.Mapper<>();

        mapper.map(imageMapper, inputColorImage, outputGrayImage);
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

        public ProcessingChain()
        {

            durationMeters.put("zeroCrossingConvolution", new DurationStartMeter("zeroCrossingConvolution", true, 1.0, false));
            durationMeters.put("zeroCrossingToBoolean", new DurationStartMeter("zeroCrossingToBoolean", true, 1.0, false));

        }

        public void setup(Vector2d<Integer> imageSize, ColorRgb colorToGrayColorScale, MarrHildrethOperatorParameter grayImageZeroCrossingParameter)
        {
            grayImage = new Map2d<>(imageSize.x, imageSize.y);
            zeroCrossingBinary = new Map2d<>(imageSize.x, imageSize.y);

            grayImageZeroCrossingKernel = Convolution2dHelper.calculateMarrHildrethOperator(new Vector2d<>(grayImageZeroCrossingParameter.filterSize, grayImageZeroCrossingParameter.filterSize), grayImageZeroCrossingParameter.sigma);

            this.colorToGrayColorScale = colorToGrayColorScale;
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
                Dag<ChainElement>.Element currentDagElement;
                //EnumMapType filterOutputType;

                IMap2d MapForFilterOutput;

                if( chainIndicesToProcess.isEmpty() ) {
                    break;
                }

                currentDagElementIndex = chainIndicesToProcess.peek();

                currentDagElement = filterChainDag.elements.get(currentDagElementIndex);
                //filterOutputType = currentDagElement.content.outputType;

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
                    Dag<ChainElement>.Element iterationDagElement;

                    iterationDagElement = filterChainDag.elements.get(iterationChildIndex);

                    Assert.Assert(iterationDagElement.content.inputType == currentDagElement.content.outputType, "Types of filters are incompatible");

                    ((ApplyChainElement)currentDagElement.content).input = MapForFilterOutput;
                }

                chainIndicesToProcess.addAll(currentDagElement.childIndices);
            }


            /*
            IMap2d<Float> zeroCrossings;

            durationMeters.get("convertColorToGray").start();
            convertColorToGrayImage(inputColorImage, grayImage, colorToGrayColorScale);
            durationMeters.get("convertColorToGray").stop();



            durationMeters.get("zeroCrossingConvolution").start();
            zeroCrossings = Convolution2d.convolution(grayImage, grayImageZeroCrossingKernel);
            durationMeters.get("zeroCrossingConvolution").stop();

            durationMeters.get("zeroCrossingToBoolean").start();
            // threshold is 0.00045f for the MarrHildreth filter
            //convertZeroCrossingFloatToBoolean(zeroCrossings, zeroCrossingBinary);
            durationMeters.get("zeroCrossingToBoolean").stop();
            */

        }

        /*
        private void applyChainElement(ChainElement chainElement) {
            switch( chainElement.type ) {
                case COLOR_FLOAT:
                    ((ChainElementColorFloat)chainElement).apply();
            }
        }
        */


        private IMap2d<Float> grayImageZeroCrossingKernel;
        private IMap2d<Float> grayImage;

        private IMap2d<Boolean> zeroCrossingBinary;

        private ColorRgb colorToGrayColorScale;

        public Map<String, DurationStartMeter> durationMeters = new HashMap<>();

        // entry is [0]
        public Dag<ChainElement> filterChainDag = new Dag<>();
    }
}
