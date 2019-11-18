/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.bpsolver.RetinaToWorkspaceTranslator;

//import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;
import org.eclipse.collections.api.map.primitive.IntObjectMap;
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

    private static List<Node> createNodesFromMap(final IntObjectMap<Deque<RetinaPrimitive>> map, final Solver bpSolver) {
        List<Node> resultNodes = new ArrayList<>();

        map.forEachKeyValue( (k, v) -> {
            Node objectNode = new PlatonicPrimitiveInstanceNode(bpSolver.networkHandles.objectPlatonicPrimitiveNode);

            for( final RetinaPrimitive iterationRetinaPrimitive : v)  {
                Node nodeForRetinaPrimitive = createPlatonicInstanceNodeForRetinaObject(iterationRetinaPrimitive, bpSolver.networkHandles);

                // linkage
                Link createdForwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.CONTAINS, nodeForRetinaPrimitive);
                objectNode.out.add(createdForwardLink);

                Link createdBackwardLink = bpSolver.network.linkCreator.createLink(Link.EnumType.ISPARTOF, objectNode);
                nodeForRetinaPrimitive.out.add(createdBackwardLink);
            }

            resultNodes.add(objectNode);
        });

        return resultNodes;
    }
}
