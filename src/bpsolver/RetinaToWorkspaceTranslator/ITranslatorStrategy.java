package bpsolver.RetinaToWorkspaceTranslator;

import Datastructures.Vector2d;
import FargGeneral.Coderack;
import FargGeneral.network.Network;
import FargGeneral.network.Node;
import RetinaLevel.RetinaPrimitive;
import bpsolver.BpSolver;
import bpsolver.CodeletLtmLookup;
import bpsolver.NetworkHandles;

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
    List<Node> createObjectsFromRetinaPrimitives(List<RetinaPrimitive> primitives, Network network, NetworkHandles networkHandles, Coderack coderack, CodeletLtmLookup codeletLtmLookup, BpSolver bpSolver, Vector2d<Float> imageSize);
}
