// taken from nars

package ptrman.Algorithms.ai.gng;

import com.syncleus.dann.graph.MutableBidirectedGraph;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * from: https://github.com/scadgek/NeuralGas
 * TODO use a graph for incidence structures to avoid some loops
 */
public class NeuralGasNet {
    private final int dimension;

    private int iteration;


    private int maxNodes;
    private int lambda;
    private int maxAge;
    private double alpha;
    private double beta;
    private double epsW;
    private double epsN;

    public int getLambda() {
        return lambda;
    }

    /** lifespan of a node */
    public void setLambda(int lambda) {
        this.lambda = lambda;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
    }

    public void setEpsN(double epsN) {
        this.epsN = epsN;
    }

    public void setEpsW(double epsW) {
        this.epsW = epsW;
    }

    public void setBeta(double beta) {
        this.beta = beta;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public double getAlpha() {
        return alpha;
    }

    public double getBeta() {
        return beta;
    }

    public double getEpsW() {
        return epsW;
    }

    public double getEpsN() {
        return epsN;
    }



    public NeuralGasNet(int dimension, int maxNodes) {
        this.iteration = 0;
        this.dimension = dimension;
        this.maxNodes = maxNodes;

        //default values
        setLambda(20);
        setMaxAge(20);

        setAlpha(0.8);
        setBeta(0.9);

        setEpsW(0.05);
        setEpsN(0.02);



        for (int i = 0; i < maxNodes; i++) {
            graph.add(new Node(i, dimension).randomizeUniform(-1.0, 1.0));
        }


//        pw = new PrintWriter("resources/output.txt");
//
//        for (Node node : nodes)
//        {
//            pw.println(node.getWeights()[0] + " " + node.getWeights()[1] + " " + node.getWeights()[2] + " black");
//        }
//        pw.println("*");
    }

    public Node closest(double... x) {
        //find closest nodes
        double minDist = Double.POSITIVE_INFINITY;
        Node closest = null;
        for (Node node : graph.getNodes()) {
            if (node.getDistanceSq(x) < minDist)
                closest = node;
        }

        return closest;
    }
    /** translates all nodes uniformly */
    public void addToNodes(double[] x) {
        for (Node n : graph.getNodes() ) {
            n.add(x);
        }
    }

    public Node learn(double... x) {

        //find closest nodes
        double minDist = Double.POSITIVE_INFINITY;
        double minDist2 = Double.POSITIVE_INFINITY;
        double maxDist = Double.NEGATIVE_INFINITY;
        Node closest = null;
        Node nextClosestNode = null;
        Node furthest = null;
        for (Node node : graph.getNodes()) {
            double dd = node.updateDistanceSq(x);

            if (dd > maxDist) {
                furthest = node;
                maxDist = dd;
            }
            if (dd < minDist) {
                closest = node;
                minDist = dd;
            }

        }
        for (Node node : graph.getNodes()) {
            if (node == closest) continue;
            double dd = node.getLocalDistanceSq(); //TODO cache this localDist
            if (dd < minDist2) {
                nextClosestNode = node;
                minDist2 = dd;
            }
        }


        if (closest == null || nextClosestNode == null) {
            throw new RuntimeException("closest=" + closest + ", nextClosest=" + nextClosestNode);
        }

        //update local error of the "winner"
        closest.setLocalError(closest.getLocalError() + closest.getLocalDistance());

        //update weights for "winner"
        closest.update(getEpsW(), x);

        //update weights for "winner"'s neighbours
        for (Connection connection : graph.getTraversableEdges(closest)) {
            Node toUpdate = null;
            if (connection.from == closest) {
                toUpdate = connection.to;
            } else if (connection.to == closest) {
                toUpdate = connection.from;
            }

            //if (toUpdate != null) { //should not be null
            toUpdate.update(getEpsN(), x);
            connection.age();
            //}
        }

        //remove connections with age > maxAge
        List<Connection> toRemove = new ArrayList(1);
        for (Connection c : graph.getEdges()) {
            if (c.getAge() > getMaxAge()) {
                toRemove.add(c);
            }
        }
        removeAllEdges(toRemove);

        //set connection between "winners" to age zero
        Connection nc = new Connection(closest, nextClosestNode);
        graph.remove(nc);
        addEdge(nc);



        //if iteration is lambda
        if (iteration != 0 && iteration % getLambda() == 0) {

            int nextID = furthest.id;
            graph.remove(furthest);


            //find node with maximal local error
            double maxError = Double.NEGATIVE_INFINITY;
            Node maxErrorNode = null;
            for (Node node : graph.getNodes()) {
                if (node.getLocalError() > maxError) {
                    maxErrorNode = node;
                    maxError = node.getLocalError();
                }
            }

            if (maxErrorNode == null) {
                throw new RuntimeException("maxErrorNode=null");
            }

            //find max error neighbour of the mentioned node
            maxError = Double.NEGATIVE_INFINITY;
            Node maxErrorNeighbour = null;

            for (Connection connection : graph.getTraversableEdges(maxErrorNode)) {

                Node otherNode = connection.to == maxErrorNode ? connection.from : connection.to;

                if (otherNode.getLocalError() > maxError) {
                    maxErrorNeighbour = otherNode;
                    maxError = otherNode.getLocalError();
                }

            }

            if (maxErrorNeighbour == null) {
                //throw new RuntimeException("maxErrorNeighbor=null");
                return null;
            }

            //remove connection between them
            removeEdgeBetween(maxErrorNode, maxErrorNeighbour);

            //System.out.println("creating new node " + nextID + " in: " + vertexSet());

            //create node between errorest nodes
            Node newNode = new Node(nextID, maxErrorNode, maxErrorNeighbour);
            graph.add(newNode);

            if (maxErrorNode.id == newNode.id) {
                throw new RuntimeException("new node has same id as max error node");
            }

            //create connections between them
            addEdge(new Connection(maxErrorNode, newNode));
            addEdge(new Connection(maxErrorNeighbour, newNode));

            //update errors of the error nodes
            maxErrorNode.setLocalError(maxErrorNode.getLocalError() * getAlpha());
            maxErrorNeighbour.setLocalError(maxErrorNeighbour.getLocalError() * getAlpha());
        }


        //System.out.println(vertexSet().size() + " nodes, " + edgeSet().size() + " edges in neuralgasnet");

        //update errors of the nodes
        for (Node node : graph.getNodes()) {
            node.setLocalError(node.getLocalError() * getBeta());

            //System.out.println("  "+ node);
        }

//            //save positions
//            for (Node node : nodes)
//            {
//                pw.println(node.getWeights()[0] + " " + node.getWeights()[1] + " " + node.getWeights()[2] + " black");
//            }
//            pw.println("*");


        //System.out.println("Iteration: " + iteration++);

        //pw.close();

        iteration++;

        return closest;
    }

    private void removeEdgeBetween(Node maxErrorNode, Node maxErrorNeighbour) {
        for( Connection iterationEdge : graph.getTraversableEdges(maxErrorNode) ) {
            if( iterationEdge.from.equals(maxErrorNode) && iterationEdge.to.equals(maxErrorNeighbour) ) {
                graph.remove(iterationEdge);
                return;
            }
            else if( iterationEdge.to.equals(maxErrorNode) && iterationEdge.from.equals(maxErrorNeighbour) ) {
                graph.remove(iterationEdge);
                return;
            }
        }
    }

    private void removeAllEdges(List<Connection> toRemove) {
        for( Connection iterationConnection : toRemove ) {
            graph.remove(iterationConnection);
        }
    }

    private void addEdge(Connection connection) {
        graph.add(connection);
    }

    public double[] getDimensionRange(final int dimension) {
        final double[] x = new double[] { Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};

        for (Node node :graph.getNodes()) {
            double v = node.getEntry(dimension);
            if (v < x[0]) x[0] = v;
            if (v > x[1]) x[1] = v;
        }

        return x;
    }

    /** pulls a dimension out of all the nodes, as an array, and sorts it  */
    public double[] getDimension(int dim) {
        double[] d = new double[graph.getNodes().size()];
        int i = 0;
        for (Node n : graph.getNodes()) {
            d[i++] = n.getEntry(dim);
        }
        Arrays.sort(d);
        return d;
    }

    public void clear() {
        graph.clear();
    }

    public MutableBidirectedGraph<Node, Connection> graph = new MutableDirectedAdjacencyGraph<>();


}
