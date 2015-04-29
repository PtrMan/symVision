package ptrman.bpsolver;

import ptrman.bpsolver.nodes.PlatonicPrimitiveNode;

/**
 *
 * holds all important Nodes in the network
 */
public class NetworkHandles
{
    public PlatonicPrimitiveNode lineSegmentPlatonicPrimitiveNode;
    public PlatonicPrimitiveNode lineSegmentFeatureLineLengthPrimitiveNode;
    public PlatonicPrimitiveNode lineSegmentFeatureLineSlopePrimitiveNode;
    
    public PlatonicPrimitiveNode barycenterPlatonicPrimitiveNode;
    public PlatonicPrimitiveNode endpointPlatonicPrimitiveNode;
    public PlatonicPrimitiveNode bayPlatonicPrimitiveNode;
    
    public PlatonicPrimitiveNode xCoordinatePlatonicPrimitiveNode;
    public PlatonicPrimitiveNode yCoordinatePlatonicPrimitiveNode;
    
    public PlatonicPrimitiveNode objectPlatonicPrimitiveNode;
    
    public PlatonicPrimitiveNode lineStructureAbstractPrimitiveNode;
    
    // has a attribute attached which indicates if it is a vertex, Touch, cross or a K-point
    public PlatonicPrimitiveNode anglePointNodePlatonicPrimitiveNode;
    public PlatonicPrimitiveNode anglePointFeatureTypePrimitiveNode;
    public PlatonicPrimitiveNode anglePointPositionPlatonicPrimitiveNode;
    public PlatonicPrimitiveNode anglePointAngleValuePrimitiveNode;
}
