package ptrman.bpsolver.RetinaToWorkspaceTranslator;

import ptrman.FargGeneral.network.Node;
import ptrman.levels.retina.RetinaPrimitive;
import ptrman.bpsolver.Solver;

import java.util.List;

/**
 * All classes which derive from this implement a strategy on how to cluster retinaprimitives into Objects(represented as Nodes)
 * 
 * possible strategies include
 * * based on point distance
 * * based on (hidden) line intersection information
 * 
 */
public interface ITranslatorStrategy
{
    List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, Solver bpSolver);
}
