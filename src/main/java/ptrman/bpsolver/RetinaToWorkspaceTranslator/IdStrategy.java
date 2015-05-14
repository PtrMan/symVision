package ptrman.bpsolver.RetinaToWorkspaceTranslator;

import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.BpSolver;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.misc.Assert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Translator which uses the id's of the RetinaPrimitives
 * the id of each retina primitive needs to be set!
 */
public class IdStrategy extends AbstractTranslatorStrategy {
    @Override
    public List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, BpSolver bpSolver) {
        Map<Integer, List<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = new HashMap<>();

        for( final RetinaPrimitive iterationPrimitive : primitives ) {
            Assert.Assert(iterationPrimitive.hasValidObjectId(), "RetinaPrimitive has no valid objectId");

            List<RetinaPrimitive> primitivesOfObject;
            if( objectIdToRetinaPrimitivesMap.containsKey(iterationPrimitive.objectId) ) {
                primitivesOfObject = objectIdToRetinaPrimitivesMap.get(iterationPrimitive.objectId);
            }
            else {
                primitivesOfObject = new ArrayList<>();
                objectIdToRetinaPrimitivesMap.put(iterationPrimitive.objectId, primitivesOfObject);
            }

            primitivesOfObject.add(iterationPrimitive);
        }

        return createNodesFromMap(objectIdToRetinaPrimitivesMap, bpSolver);
    }

    private static List<Node> createNodesFromMap(final Map<Integer, List<RetinaPrimitive>> map, final BpSolver bpSolver) {
        List<Node> resultNodes = new ArrayList<>();

        for( final Map.Entry<Integer, List<RetinaPrimitive>> iterationEntry : map.entrySet() ) {
            Node objectNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.objectPlatonicPrimitiveNode);

            for( final RetinaPrimitive iterationRetinaPrimitive : iterationEntry.getValue() ) {
                Node nodeForRetinaPrimitive = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, bpSolver.networkHandles);

                // linkage
                Link createdForwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive);
                objectNode.outgoingLinks.add(createdForwardLink);

                Link createdBackwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                nodeForRetinaPrimitive.outgoingLinks.add(createdBackwardLink);
            }

            resultNodes.add(objectNode);
        }

        return resultNodes;
    }
}
