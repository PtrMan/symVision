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
    public SolverCodelet(Network network, NetworkHandles networkHandles)
    {
        this.network = network;
        this.networkHandles = networkHandles;
    }
    
    public void setStartNode(Node startNode)
    {
        this.startNode = startNode;
    }
    
    // must be called before it runs
    public abstract void initialize();
    
    public abstract SolverCodelet cloneObject();
    
    protected Network network;
    protected NetworkHandles networkHandles;
    protected Node startNode;
}
