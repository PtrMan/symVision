package ptrman.bpsolver.RetinaToWorkspaceTranslator;

//import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.impl.map.mutable.primitive.IntObjectHashMap;
import ptrman.FargGeneral.network.Link;
import ptrman.FargGeneral.network.Node;
import ptrman.bpsolver.Solver;
import ptrman.bpsolver.nodes.PlatonicPrimitiveInstanceNode;
import ptrman.levels.retina.RetinaPrimitive;

import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import static ptrman.bpsolver.Helper.createMapByObjectIdsFromListOfRetinaPrimitives;

/**
 * Translator which uses the id's of the RetinaPrimitives
 * the id of each retina primitive needs to be set!
 */
public class IdStrategy extends AbstractTranslatorStrategy {
    @Override
    public List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, Solver bpSolver) {
        IntObjectHashMap<Deque<RetinaPrimitive>> objectIdToRetinaPrimitivesMap = createMapByObjectIdsFromListOfRetinaPrimitives(primitives);
        return createNodesFromMap(objectIdToRetinaPrimitivesMap, bpSolver);
    }

    private static List<Node> createNodesFromMap(final IntObjectHashMap<Deque<RetinaPrimitive>> map, final Solver bpSolver) {
        List<Node> resultNodes = new ArrayList<>();

        map.forEachKeyValue( (k, v) -> {
            Node objectNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.objectPlatonicPrimitiveNode);

            for( final RetinaPrimitive iterationRetinaPrimitive : v)  {
                Node nodeForRetinaPrimitive = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, bpSolver.networkHandles);

                // linkage
                Link createdForwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive);
                objectNode.outgoingLinks.add(createdForwardLink);

                Link createdBackwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                nodeForRetinaPrimitive.outgoingLinks.add(createdBackwardLink);
            }

            resultNodes.add(objectNode);
        });

        return resultNodes;
    }
}
