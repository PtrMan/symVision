package bpsolver.nodes;

/**
 *
 * static class with an enum for the types of the nodes used both in the workspace and ltm
 */
public class NodeTypes
{
    public enum EnumType
    {
        FEATURENODE,
        NUMEROSITYNODE,
        PLATONICPRIMITIVENODE,
        PLATONICPRIMITIVEINSTANCENODE // is a line or s curve or anything
    }
}
