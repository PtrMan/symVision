package bpsolver;

import Datastructures.Vector2d;
import static Datastructures.Vector2d.FloatHelper.getLength;
import static Datastructures.Vector2d.FloatHelper.sub;
import FargGeneral.Coderack;
import FargGeneral.network.Link;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.SingleLineDetector;
import bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import com.sun.org.apache.bcel.internal.generic.AALOAD;
import java.util.ArrayList;
import java.util.HashMap;
import misc.Assert;

public class RetinaToWorkspaceTranslator
{
    private static class RetinaObjectWithAssociatedPoints
    {
        private RetinaObjectWithAssociatedPoints()
        {
            
        }
        
        // TODO ASK< other types of "objects" except lines (curves, circles) >
        public enum EnumType
        {
            LINESEGMENT;
        }
        
        public EnumType type;
        
        public SingleLineDetector lineDetector;
        
        // TODO< store this in a fast access datastructure for more efficient retrival and comparison >
        // for now we store only the point positions, which is super slow
        public ArrayList<Vector2d<Float>> pointPositions;
        
        public static RetinaObjectWithAssociatedPoints makeLineSegment(SingleLineDetector lineDetector)
        {
            RetinaObjectWithAssociatedPoints result;
            
            result = new RetinaObjectWithAssociatedPoints();
            result.type = EnumType.LINESEGMENT;
            result.lineDetector = lineDetector;
            
            return result;
        }
        
        public int objectId = -1; // if -1 -> is not included in a retina level "object"
    }
    
    private static class ObjectNodeWithGroup
    {
        Node objectNode;
        GroupOfRetinaObjectWithAssociatedPoints groupOfRetinaObjects;
    }
    
    /**
     * 
     * \param lines
     * \param network
     * \return the node which is the object node 
     */
    public ArrayList<Node> createObjectFromLines(ArrayList<SingleLineDetector> lines, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup)
    {
        ArrayList<RetinaObjectWithAssociatedPoints> retinaObjectsWithAssociatedPoints;
        HashMap<Integer, PlatonicPrimitiveInstanceNode> objectNodesByGroupId;
        ArrayList<Node> resultNodes;
        
        retinaObjectsWithAssociatedPoints = new ArrayList<RetinaObjectWithAssociatedPoints>();
        
        for( SingleLineDetector iterationLine : lines )
        {
            retinaObjectsWithAssociatedPoints.add(associatePointsToLineSegmentRetinaObject(iterationLine));
        }
        
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints = createAndPropagateRetinaLevelObjects(retinaObjectsWithAssociatedPoints);
        ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints = createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(groupsOfRetinaObjectsWithAssociatedPoints, coderack, network, networkHandles, codeletLtmLookup);
        resultNodes = getNodesOfNodeAndGroupOfRetinaObject(objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints);
        return resultNodes;
    }
    
    
    private static ArrayList<Node> getNodesOfNodeAndGroupOfRetinaObject(ArrayList<ObjectNodeWithGroup> objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints)
    {
        ArrayList<Node> resultArray;
        
        resultArray = new ArrayList<>();
        
        for( ObjectNodeWithGroup iterationObjectNodeWithGroup : objectNodesWithGroupsOfRetinaObjectsWithAssociatedPoints )
        {
            resultArray.add(iterationObjectNodeWithGroup.objectNode);
        }
        
        return resultArray;
    }
    
    private ArrayList<ObjectNodeWithGroup> createObjectNodesForGroupsOfRetinaObjectsWithAssociatedPoints(ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groupsOfRetinaObjectsWithAssociatedPoints, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
        ArrayList<ObjectNodeWithGroup> resultObjectNodesWithGroup;
        
        resultObjectNodesWithGroup = new ArrayList<>();
        
        for( GroupOfRetinaObjectWithAssociatedPoints iterationGroupOfRetinaObjectWithAssociatedPoints : groupsOfRetinaObjectsWithAssociatedPoints )
        {
            PlatonicPrimitiveInstanceNode objectNode;
            ObjectNodeWithGroup objectNodeWithGroup;
            
            objectNode = new PlatonicPrimitiveInstanceNode(networkHandles.objectPlatonicPrimitiveNode);
            createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(iterationGroupOfRetinaObjectWithAssociatedPoints.arrayOfRetinaObjectWithAssociatedPoints, objectNode, coderack, network, networkHandles, codeletLtmLookup);
            
            objectNodeWithGroup = new ObjectNodeWithGroup();
            objectNodeWithGroup.groupOfRetinaObjects = iterationGroupOfRetinaObjectWithAssociatedPoints;
            objectNodeWithGroup.objectNode = objectNode;
            
            resultObjectNodesWithGroup.add(objectNodeWithGroup);
        }
        
        return resultObjectNodesWithGroup;
    }
    
    
    private void createPlatonicInstanceNodeForRetinaObjectsAndLinkToParent(ArrayList<RetinaObjectWithAssociatedPoints> arrayOfRetinaObjectWithAssociatedPoints, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
        for( RetinaObjectWithAssociatedPoints iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
        {
            createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(iterationRetinaObject, objectNode, coderack, network, networkHandles, codeletLtmLookup);
        }
    }
    
    
    
    
    private static void createPlatonicInstanceNodeForRetinaObjectAndLinkToParent(RetinaObjectWithAssociatedPoints iterationRetinaObject, PlatonicPrimitiveInstanceNode objectNode, Coderack coderack, Network network, NetworkHandles networkHandles, CodeletLtmLookup codeletLtmLookup)
    {
        PlatonicPrimitiveInstanceNode createdPlatonicInstanceNodeForRetinaObject;
        Link createdForwardLink, createdBackwardLink;
        
        createdPlatonicInstanceNodeForRetinaObject = createPlatonicInstanceNodeForRetinaObject(iterationRetinaObject, networkHandles);

        // linkage
        createdForwardLink = network.linkCreator.createLink(Link.EnumType.CONTAINS, createdPlatonicInstanceNodeForRetinaObject);
        objectNode.outgoingLinks.add(createdForwardLink);

        createdBackwardLink = network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
        createdPlatonicInstanceNodeForRetinaObject.outgoingLinks.add(createdBackwardLink);

        // add all codelet's of it
        codeletLtmLookup.lookupAndPutCodeletsAtCoderackForPrimitiveNode(createdPlatonicInstanceNodeForRetinaObject, coderack, network, networkHandles);

    }
    
    private static PlatonicPrimitiveInstanceNode createPlatonicInstanceNodeForRetinaObject(RetinaObjectWithAssociatedPoints retinaObject, NetworkHandles networkHandles)
    {
        if( retinaObject.type == RetinaObjectWithAssociatedPoints.EnumType.LINESEGMENT )
        {
            PlatonicPrimitiveInstanceNode createdLineNode;
            
            createdLineNode = new PlatonicPrimitiveInstanceNode(networkHandles.lineSegmentPlatonicPrimitiveNode);
            createdLineNode.p1 = retinaObject.lineDetector.getAProjected();
            createdLineNode.p2 = retinaObject.lineDetector.getBProjected();
            
            return createdLineNode;
        }
        else
        {
            throw new RuntimeException("internal error");
        }
    }
    
    private static class GroupOfRetinaObjectWithAssociatedPoints
    {
        public ArrayList<RetinaObjectWithAssociatedPoints> arrayOfRetinaObjectWithAssociatedPoints = new ArrayList<>();
        
        public boolean canBeIncludedInCluster(RetinaObjectWithAssociatedPoints candidate)
        {
            for( RetinaObjectWithAssociatedPoints iterationRetinaObject : arrayOfRetinaObjectWithAssociatedPoints )
            {
                if( doRetinaObjectsShareCommonPoints(iterationRetinaObject, candidate) )
                {
                    return true;
                }
            }
            
            return false;
        }
        
        public static GroupOfRetinaObjectWithAssociatedPoints createFromSingleRetinaObject(RetinaObjectWithAssociatedPoints retinaObject)
        {
            GroupOfRetinaObjectWithAssociatedPoints result;
            
            result = new GroupOfRetinaObjectWithAssociatedPoints();
            result.arrayOfRetinaObjectWithAssociatedPoints.add(retinaObject);
            return result;
        }
        
        public static boolean shareCommonPoint(GroupOfRetinaObjectWithAssociatedPoints a, GroupOfRetinaObjectWithAssociatedPoints b)
        {
            for( RetinaObjectWithAssociatedPoints outerRetinaObjectWithAssosciatedPoints : a.arrayOfRetinaObjectWithAssociatedPoints )
            {
                for( RetinaObjectWithAssociatedPoints innerRetinaObjectWithAssosciatedPoints : b.arrayOfRetinaObjectWithAssociatedPoints )
                {
                    if( RetinaToWorkspaceTranslator.doRetinaObjectsShareCommonPoints(outerRetinaObjectWithAssosciatedPoints, innerRetinaObjectWithAssosciatedPoints) )
                    {
                        return true;
                    }
                }
            }
            
            return false;
        }

        private void append(GroupOfRetinaObjectWithAssociatedPoints appendix) 
        {
            arrayOfRetinaObjectWithAssociatedPoints.addAll(appendix.arrayOfRetinaObjectWithAssociatedPoints);
        }
    }
    
    private ArrayList<GroupOfRetinaObjectWithAssociatedPoints> createAndPropagateRetinaLevelObjects(ArrayList<RetinaObjectWithAssociatedPoints> retinaObjectsWithAssociatedPoints)
    {
        ArrayList<GroupOfRetinaObjectWithAssociatedPoints> groups;
        int retinaObjectI;
        
        groups = new ArrayList<>();
        
        groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(retinaObjectsWithAssociatedPoints.get(0)));
        
        // we first try to cluster as many RetinaObjects in a lear fashion, after this we try to combine these "mini clusters" to final large clusters
        
        for( retinaObjectI = 1; retinaObjectI < retinaObjectsWithAssociatedPoints.size(); retinaObjectI++ )
        {
            RetinaObjectWithAssociatedPoints currentRetinaObject;
            boolean wasIncludedInCluster;
            
            
            currentRetinaObject = retinaObjectsWithAssociatedPoints.get(retinaObjectI);
            
            
            wasIncludedInCluster = false;
            
            for( GroupOfRetinaObjectWithAssociatedPoints iterationGroup : groups )
            {
                if( iterationGroup.canBeIncludedInCluster(currentRetinaObject) )
                {
                    wasIncludedInCluster = true;
                    iterationGroup.arrayOfRetinaObjectWithAssociatedPoints.add(currentRetinaObject);
                    break;
                }
            }
            
            if( !wasIncludedInCluster )
            {
                groups.add(GroupOfRetinaObjectWithAssociatedPoints.createFromSingleRetinaObject(currentRetinaObject));
            }
            
        }
        
        
        
        // cluster
        int lowerI, upperI;

        repeatSearch:
        for( lowerI = 0; lowerI < groups.size(); lowerI++ )
        {
            for( upperI = lowerI+1; upperI < groups.size(); upperI++ )
            {
                GroupOfRetinaObjectWithAssociatedPoints lower, upper;

                lower = groups.get(lowerI);
                upper = groups.get(upperI);

                if( GroupOfRetinaObjectWithAssociatedPoints.shareCommonPoint(lower, upper) )
                {
                    lower.append(upper);
                    groups.remove(upperI);
                    
                    break repeatSearch;
                }
            }
        }
        
        return groups;
    }
    
    
    private static class IndexTuple
    {
        public IndexTuple(int a, int b)
        {
            values = new int[]{a, b};
        }
        
        public int[] values;
    }
    
    private static boolean doRetinaObjectsShareCommonPoints(RetinaObjectWithAssociatedPoints a, RetinaObjectWithAssociatedPoints b)
    {
        return getRetinaObjectSharedPoints(a, b, COMMONPOINTSMAXIMALDISTANCE).size() != 0;
    }
    
    
    private static ArrayList<IndexTuple> getRetinaObjectSharedPoints(RetinaObjectWithAssociatedPoints a, RetinaObjectWithAssociatedPoints b, float maximalDistance)
    {
        ArrayList<IndexTuple> resultIndexTuples;
        int Ia, Ib;
        
        Assert.Assert(!a.equals(b), "must be not the same");
        
        resultIndexTuples = new ArrayList<>();
        
        Ia = 0;
        
        for( Vector2d<Float> pointFromA : a.pointPositions )
        {
            Ib = 0;
            
            for( Vector2d<Float> pointFromB : b.pointPositions )
            {
                Vector2d<Float> diff;
                
                diff = sub(pointFromA, pointFromB);
                if( getLength(diff) < maximalDistance)
                {
                    resultIndexTuples.add(new IndexTuple(Ia, Ib));
                }
                
                Ib++;
            }
            
            Ia++;
        }
        
        return resultIndexTuples;
    }
    
    
    private RetinaObjectWithAssociatedPoints associatePointsToLineSegmentRetinaObject(SingleLineDetector lineSegment)
    {
        RetinaObjectWithAssociatedPoints resultAssosciation;
        
        resultAssosciation = RetinaObjectWithAssociatedPoints.makeLineSegment(lineSegment);
        resultAssosciation.pointPositions = new ArrayList<>();
        resultAssosciation.pointPositions.add(lineSegment.getAProjected());
        resultAssosciation.pointPositions.add(lineSegment.getBProjected());
        
        return resultAssosciation;
    }
    
    // TODO< move th hard parameters >
    private static final float COMMONPOINTSMAXIMALDISTANCE = 10.0f;
}
