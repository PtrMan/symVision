/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ptrman.bpsolver;

import ptrman.FargGeneral.Codelet;
import ptrman.FargGeneral.network.Network;
import ptrman.FargGeneral.network.Node;

/**
 *
 * 
 */
public abstract class SolverCodelet extends Codelet
{
    public SolverCodelet(Solver bpSolver)
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

    protected final Solver bpSolver;
    protected Node startNode;
}
