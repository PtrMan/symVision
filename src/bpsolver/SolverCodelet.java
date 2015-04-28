/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bpsolver;

import FargGeneral.Codelet;
import FargGeneral.network.Network;
import FargGeneral.network.Node;

/**
 *
 * 
 */
public abstract class SolverCodelet extends Codelet
{
    public SolverCodelet(BpSolver bpSolver)
    {
        this.bpSolver = bpSolver;
    }
    
    public void setStartNode(Node startNode)
    {
        this.startNode = startNode;
    }
    
    // must be called before it runs
    public abstract void initialize();
    
    public abstract SolverCodelet cloneObject();

    protected Network getNetwork()
    {
        return bpSolver.network;
    }

    protected NetworkHandles getNetworkHandles()
    {
        return bpSolver.networkHandles;
    }

    protected BpSolver bpSolver;
    protected Node startNode;
}
