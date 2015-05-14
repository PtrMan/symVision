package ptrman.bpsolver;

import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.FargGeneral.Coderack;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.ITranslatorStrategy;
import ptrman.bpsolver.RetinaToWorkspaceTranslator.IdStrategy;
import ptrman.bpsolver.codelets.BaryCenter;
import ptrman.bpsolver.codelets.LineSegmentLength;
import ptrman.bpsolver.codelets.LineSegmentSlope;
import ptrman.bpsolver.ltm.LinkCreator;
import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;
import ptrman.bpsolver.pattern.FeaturePatternMatching;
import ptrman.levels.retina.*;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BpSolver {
    public static void main(String[] args) {
        Parameters.init();
        
        BpSolver solver = new BpSolver();
    }
    
    public BpSolver() {
    }

    public void setup() {
        initializeNetwork();
        setupLtmFactoryDefault();
        initializePlatonicPrimitiveDatabase();
        initializeCodeletLtmLookup();
    }
    
    public void cycle(int cycleCount) {
        coderack.cycle(cycleCount);
    }


    public void recalculate(IMap2d<Boolean> image) {
        final boolean enableProcessH = false;
        final boolean enableProcessE = false;
        final boolean enableProcessM = false;

        final int NUMBEROFCYCLES = 500;

        // TODO MAYBE < put this into a method in BpSolver, name "clearWorkspace()" (which cleans the ltm/workspace and the coderack) >
        coderack.flush();

        Queue<ProcessA.Sample> queueToProcessF = new ConcurrentLinkedQueue<>();

        ProcessA processA = new ProcessA();
        ProcessB processB = new ProcessB();
        ProcessC processC = new ProcessC(queueToProcessF);
        ProcessD endosceletonProcessD = new ProcessD();
        ProcessD exosceletonProcessD = new ProcessD();
        ProcessH processH = new ProcessH();
        ProcessE processE = new ProcessE();
        ProcessM processM = new ProcessM();
        ProcessF processF = new ProcessF();

        ProcessZFacade processZFacade = new ProcessZFacade();

        final int processzNumberOfPixelsToMagnifyThreshold = 8;

        final int processZGridsize = 8;

        processZFacade.preSetupSet(processZGridsize, processzNumberOfPixelsToMagnifyThreshold);
        processZFacade.setup(getImageSize());
        processZFacade.process(image); // image doesn't need to be copied

        // copy because processA changes the image
        processA.setImageSize(getImageSize());
        processA.set(image.copy(), processZFacade.getNotMagnifiedOutputObjectIds());
        List<ProcessA.Sample> endosceletonSamples = processA.sampleImage();


        ProcessSampleFilter endosceletonSampleFilter = new ProcessSampleFilter(ProcessA.Sample.EnumType.ENDOSCELETON);

        Queue<ProcessA.Sample> sampleQueueFromProcessC = new ArrayDeque<>();
        Queue<ProcessA.Sample> sampleQueueForEndosceleton = new ArrayDeque<>();

        Queue<ProcessA.Sample> processFOutputSampleQueue = new ArrayDeque<>();
        Queue<ProcessA.Sample> toExosceletonProcessDSampleQueue = new ArrayDeque<>();

        processB.process(endosceletonSamples, image);




        endosceletonSampleFilter.preSetupSet(sampleQueueFromProcessC, sampleQueueForEndosceleton);
        endosceletonSampleFilter.setup();




        processC.setImageSize(getImageSize());
        processC.preSetupSet(8 /*gridsize*/, sampleQueueFromProcessC);
        processC.setup();


        processC.set(endosceletonSamples);
        processC.recalculate();


        processC.processData();


        endosceletonSampleFilter.processData();



        processF.setImageSize(getImageSize());
        processF.preSetup(queueToProcessF, processFOutputSampleQueue);
        processF.setup();

        processF.set(image);
        processF.processData();

        System.out.println("processFOutputSampleQueue size " + Integer.toString(processFOutputSampleQueue.size()));



        endosceletonProcessD.setImageSize(getImageSize());
        endosceletonProcessD.set(sampleQueueForEndosceleton);

        endosceletonProcessD.preSetupSet(6.0f/*maximalDistanceOfPositions*/);
        endosceletonProcessD.setup();
        endosceletonProcessD.processData();
        List<RetinaPrimitive> lineDetectors = endosceletonProcessD.getResultRetinaPrimitives();

        System.out.println("endosceleton lineDetectors size " + Integer.toString(lineDetectors.size()));

        // take out the samples from a queue, put it into a list and a output queue
        // TODO< put this into a own process >

        List<ProcessA.Sample> exosceletonSamples = new ArrayList<>();
        int remainingSize = processFOutputSampleQueue.size();
        for( int i = 0; i < remainingSize; i++ ) {
            final ProcessA.Sample currentSample = processFOutputSampleQueue.poll();
            exosceletonSamples.add(currentSample);
            toExosceletonProcessDSampleQueue.add(currentSample);
        }



        exosceletonProcessD.setImageSize(getImageSize());
        exosceletonProcessD.set(toExosceletonProcessDSampleQueue);

        exosceletonProcessD.preSetupSet(6.0f/*maximalDistanceOfPositions*/);
        exosceletonProcessD.setup();
        exosceletonProcessD.processData();




        List<Intersection> lineIntersections = new ArrayList<>();



        if( enableProcessH ) {
            processH.process(lineDetectors);
        }



        if( enableProcessE ) {
            processE.process(lineDetectors, image);

            lineIntersections = getAllLineIntersections(lineDetectors);
        }

        List<ProcessM.LineParsing> lineParsings = new ArrayList<>();

        if( enableProcessM ) {
            processM.process(lineDetectors);

            lineParsings = processM.getLineParsings();
        }



        ITranslatorStrategy retinaToWorkspaceTranslatorStrategy;

        retinaToWorkspaceTranslatorStrategy = new IdStrategy();

        List<Node> objectNodes = retinaToWorkspaceTranslatorStrategy.createObjectsFromRetinaPrimitives(lineDetectors, this);

        cycle(NUMBEROFCYCLES);



        if( false ) {
            FeaturePatternMatching featurePatternMatching;

            featurePatternMatching = new FeaturePatternMatching();

            for( Node iterationNode : objectNodes ) {
                Node bestPatternNode;
                float bestPatternSimilarity;

                bestPatternNode = null;
                bestPatternSimilarity = 0.0f;

                for( Node patternNode : patternRootNodes ) {
                    List<FeaturePatternMatching.MatchingPathElement> matchingPathElements;
                    float matchingDistanceValue;
                    float matchingSimilarityValue;

                    matchingPathElements = featurePatternMatching.matchAnyRecursive(iterationNode, patternNode, networkHandles, Arrays.asList(Link.EnumType.CONTAINS), HardParameters.PatternMatching.MAXDEPTH);
                    matchingDistanceValue = FeaturePatternMatching.calculateRatingWithDefaultStrategy(matchingPathElements);
                    matchingSimilarityValue = FeaturePatternMatching.Converter.distanceToSimilarity(matchingDistanceValue);

                    if( matchingSimilarityValue > Parameters.getPatternMatchingMinSimilarity() && matchingSimilarityValue > bestPatternSimilarity ) {
                        bestPatternNode = patternNode;
                        bestPatternSimilarity = matchingSimilarityValue;
                    }
                }

                if( bestPatternNode != null ) {
                    // TODO< incorperate new pattern into old >
                    int debugPoint = 0;
                }
                else {
                    patternRootNodes.add(iterationNode);
                }
            }
        }

        lastFrameObjectNodes = objectNodes;
        lastFrameRetinaPrimitives = lineDetectors; // for now only the line detectors TODO
        lastFrameEndosceletonSamples = endosceletonSamples;
        lastFrameExosceletonSamples = exosceletonSamples;
        lastFrameIntersections = lineIntersections; // TODO< other intersections too >
    }
    
    /**
     * 
     * stores all factory preset nodes in the ltm (standard node types, linked attributes, etc)
     */
    public void setupLtmFactoryDefault() {
        ptrman.FargGeneral.network.Link link;
        
        
        networkHandles.objectPlatonicPrimitiveNode = new PlatonicPrimitiveNode("Object", null);
        networkHandles.lineStructureAbstractPrimitiveNode = new PlatonicPrimitiveNode("lineStructure", null);
        networkHandles.lineSegmentPlatonicPrimitiveNode = new PlatonicPrimitiveNode("LineSegment", null);
        
        networkHandles.bayPlatonicPrimitiveNode = new PlatonicPrimitiveNode("bay", null /* TODO "Bay" */);
        networkHandles.endpointPlatonicPrimitiveNode = new PlatonicPrimitiveNode("endpoint", "EndPoint");
        networkHandles.barycenterPlatonicPrimitiveNode = new PlatonicPrimitiveNode("barycenter", "BaryCenter");
        networkHandles.lineSegmentFeatureLineLengthPrimitiveNode = new PlatonicPrimitiveNode("LineSegmentLength", "LineSegmentLength");
        networkHandles.lineSegmentFeatureLineSlopePrimitiveNode = new PlatonicPrimitiveNode("LineSegmentSlope", "LineSegmentSlope");
        
        networkHandles.xCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("xCoordinate", null);
        networkHandles.yCoordinatePlatonicPrimitiveNode = new PlatonicPrimitiveNode("yCoordinate", null);
        
        // currently not connected to anything
        networkHandles.anglePointNodePlatonicPrimitiveNode = new PlatonicPrimitiveNode("AnglePoint", null);
        
        networkHandles.anglePointFeatureTypePrimitiveNode = new PlatonicPrimitiveNode("AnglePointFeatureType", null);
        networkHandles.anglePointPositionPlatonicPrimitiveNode = new PlatonicPrimitiveNode("AnglePointPosition", null);
        networkHandles.anglePointAngleValuePrimitiveNode = new PlatonicPrimitiveNode("AnglePointAngleValue", "Angle");
        
        networkHandles.lineStructureAbstractPrimitiveNode.isAbstract = true;
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.endpointPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.bayPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        
        
        network.nodes.add(networkHandles.lineSegmentPlatonicPrimitiveNode);
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.ISA, networkHandles.lineStructureAbstractPrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.lineSegmentFeatureLineLengthPrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.lineSegmentFeatureLineSlopePrimitiveNode);
        networkHandles.lineSegmentPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.ISA, networkHandles.objectPlatonicPrimitiveNode);
        networkHandles.lineStructureAbstractPrimitiveNode.outgoingLinks.add(link);
        
        // TODO< imagination of circle, center, tangent lines, etc >
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.endpointPlatonicPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.endpointPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.bayPlatonicPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.bayPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.barycenterPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        
        // a object has a barycenter
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.barycenterPlatonicPrimitiveNode);
        networkHandles.objectPlatonicPrimitiveNode.outgoingLinks.add(link);
        
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASATTRIBUTE, networkHandles.anglePointFeatureTypePrimitiveNode);
        networkHandles.anglePointNodePlatonicPrimitiveNode.outgoingLinks.add(link);
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.anglePointPositionPlatonicPrimitiveNode);
        networkHandles.anglePointNodePlatonicPrimitiveNode.outgoingLinks.add(link);
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.anglePointAngleValuePrimitiveNode);
        networkHandles.anglePointNodePlatonicPrimitiveNode.outgoingLinks.add(link);
        
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.xCoordinatePlatonicPrimitiveNode);
        networkHandles.anglePointPositionPlatonicPrimitiveNode.outgoingLinks.add(link);
        link = network.linkCreator.createLink(ptrman.FargGeneral.network.Link.EnumType.HASFEATURE, networkHandles.yCoordinatePlatonicPrimitiveNode);
        networkHandles.anglePointPositionPlatonicPrimitiveNode.outgoingLinks.add(link);
    }
    
    private void initializeNetwork() {
        network.linkCreator = new LinkCreator();
    }
    
    private void initializeCodeletLtmLookup() {
        CodeletLtmLookup.RegisterEntry createdRegistryEntry;
        SolverCodelet createdCodelet;
        
        codeletLtmLookup = new CodeletLtmLookup();
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new LineSegmentLength(this);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.5f));
        
        codeletLtmLookup.registry.put("LineSegmentLength", createdRegistryEntry);
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new LineSegmentSlope(this);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.5f));
        
        codeletLtmLookup.registry.put("LineSegmentSlope", createdRegistryEntry);
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new BaryCenter(this, BaryCenter.EnumRecalculate.NO);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.1f));
        
        codeletLtmLookup.registry.put("BaryCenter", createdRegistryEntry);
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new ptrman.bpsolver.codelets.EndPoint(this);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.2f));
        
        codeletLtmLookup.registry.put("EndPoint", createdRegistryEntry);
        
        
        createdRegistryEntry = new CodeletLtmLookup.RegisterEntry();
        createdCodelet = new ptrman.bpsolver.codelets.Angle(this);
        createdRegistryEntry.codeletInformations.add(new CodeletLtmLookup.RegisterEntry.CodeletInformation(createdCodelet, 0.2f));
        
        codeletLtmLookup.registry.put("Angle", createdRegistryEntry);
        
    }

    private void initializePlatonicPrimitiveDatabase() {
        platonicPrimitiveDatabase.calculatorsForMaxValueOfPlatonicPrimitiveNode.put(networkHandles.xCoordinatePlatonicPrimitiveNode, new PlatonicPrimitiveDatabase.ConstantValueMaxValueCalculator(getImageSizeAsFloat().x));
        platonicPrimitiveDatabase.calculatorsForMaxValueOfPlatonicPrimitiveNode.put(networkHandles.yCoordinatePlatonicPrimitiveNode, new PlatonicPrimitiveDatabase.ConstantValueMaxValueCalculator(getImageSizeAsFloat().y));
        platonicPrimitiveDatabase.calculatorsForMaxValueOfPlatonicPrimitiveNode.put(networkHandles.lineSegmentFeatureLineLengthPrimitiveNode, new PlatonicPrimitiveDatabase.ConstantValueMaxValueCalculator((float)Math.sqrt(getImageSizeAsFloat().x*getImageSizeAsFloat().x + getImageSizeAsFloat().y*getImageSizeAsFloat().y)));
        platonicPrimitiveDatabase.calculatorsForMaxValueOfPlatonicPrimitiveNode.put(networkHandles.lineSegmentFeatureLineSlopePrimitiveNode, new PlatonicPrimitiveDatabase.ConstantValueMaxValueCalculator(getImageSizeAsFloat().y));
        platonicPrimitiveDatabase.calculatorsForMaxValueOfPlatonicPrimitiveNode.put(networkHandles.anglePointAngleValuePrimitiveNode, new PlatonicPrimitiveDatabase.ConstantValueMaxValueCalculator(360.0f));
    }






    // TODO< refactor out >
    private static List<Intersection> getAllLineIntersections(List<RetinaPrimitive> lineDetectors) {
        List<Intersection> uniqueIntersections;

        uniqueIntersections = new ArrayList<>();

        for( RetinaPrimitive currentPrimitive : lineDetectors ) {
            if( currentPrimitive.type != RetinaPrimitive.EnumType.LINESEGMENT ) {
                continue;
            }

            findAndAddUniqueIntersections(uniqueIntersections, currentPrimitive.line.intersections);
        }

        return uniqueIntersections;
    }

    // modifies uniqueIntersections
    private static void findAndAddUniqueIntersections(List<Intersection> uniqueIntersections, List<Intersection> intersections) {
        for( Intersection currentOuterIntersection : intersections ) {
            boolean found;

            found = false;

            for( Intersection currentUnqiueIntersection : uniqueIntersections ) {
                if( currentUnqiueIntersection.equals(currentOuterIntersection) ) {
                    found = true;
                    break;
                }
            }

            if( !found ) {
                uniqueIntersections.add(currentOuterIntersection);
            }
        }


    }







    // both ltm and workspace
    // the difference is that the nodes of the workspace may all be deleted
    public Network network = new Network();
    public NetworkHandles networkHandles = new NetworkHandles();
    public Coderack coderack = new Coderack();
    public CodeletLtmLookup codeletLtmLookup;
    public PlatonicPrimitiveDatabase platonicPrimitiveDatabase = new PlatonicPrimitiveDatabase();

    // all stored patterns
    public List<Node> patternRootNodes = new ArrayList<>();

    public Vector2d<Float> getImageSizeAsFloat()
    {
        return Vector2d.ConverterHelper.convertIntVectorToFloat(imageSize);
    }
    
    public Vector2d<Integer> getImageSize()
    {
        return imageSize;
    }
    
    public void setImageSize(Vector2d<Integer> imageSize)
    {
        this.imageSize = imageSize;
    }
    
    private Vector2d<Integer> imageSize; 
    
    public List<Node> lastFrameObjectNodes;
    public List<RetinaPrimitive> lastFrameRetinaPrimitives;
    public List<ProcessA.Sample> lastFrameEndosceletonSamples;
    public List<ProcessA.Sample> lastFrameExosceletonSamples;
    public List<Intersection> lastFrameIntersections;
}
