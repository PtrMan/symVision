package ptrman.Additional;

import com.syncleus.dann.graph.AbstractDirectedEdge;
import com.syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.SpatialAcceleration;
import ptrman.Datastructures.Vector2d;

import java.util.List;
import java.util.Set;

/**
 * Uses gestalt theory principles to find connections between (in this case tracking particles) particles.
 */
public class CohesionParticleTracker {
    public static final class CohesionParticleTrackerParticleConstructorDestructor implements ParticleFlowTracker.IParticleConstructorDestructor<CohesionParticleTracker.Particle> {
        private final CohesionParticleTracker cohesionParticleTracker;

        public CohesionParticleTrackerParticleConstructorDestructor(CohesionParticleTracker cohesionParticleTracker) {
            this.cohesionParticleTracker = cohesionParticleTracker;
        }

        @Override
        public CohesionParticleTracker.Particle create(ArrayRealVector position) {
            CohesionParticleTracker.Particle createdParticle = new Particle(position);
            cohesionParticleTracker.graph.add(createdParticle);

            return createdParticle;
        }

        @Override
        public void remove(CohesionParticleTracker.Particle particle) {
            // remove all incomming/outgoing edges
            Set<CohesionEdge> outEdgesOfNode = cohesionParticleTracker.graph.getAdjacentEdges(particle);
            for( final CohesionEdge iterationEdge : outEdgesOfNode ) {
                Set<CohesionEdge> edgesToNode = cohesionParticleTracker.graph.getAdjacentEdges(iterationEdge.getDestinationNode());

                for( final CohesionEdge iterationEdgeToNode : edgesToNode  ) {
                    if( iterationEdgeToNode.getDestinationNode().equals(particle) ) {
                        cohesionParticleTracker.graph.remove(iterationEdgeToNode);
                    }
                }

                cohesionParticleTracker.graph.remove(iterationEdge);
            }

            //outEdgesOfNode = cohesionParticleTracker.graph.getInEdges(particle);
            //for( final CohesionEdge iterationEdge : outEdgesOfNode ) {
            //    cohesionParticleTracker.graph.remove(iterationEdge);
            //}

            cohesionParticleTracker.graph.remove(particle);
        }
    }

    public static class Particle implements ParticleFlowTracker.ITrackingParticle {
        public Particle(final ArrayRealVector position) {
            this.position = position;
        }

        @Override
        public void setPosition(ArrayRealVector position) {
            this.position = position;
        }

        @Override
        public ArrayRealVector getPosition() {
            return position;
        }

        @Override
        public void setVelocity(ArrayRealVector velocity) {
            this.velocity = velocity;
        }

        @Override
        public ArrayRealVector getVelocity() {
            return velocity;
        }

        private ArrayRealVector position, velocity = new ArrayRealVector(new double[]{0.0, 0.0});

        // is the velocity which over time adapt to the average of all velocities of this particle
        public ArrayRealVector commulatedVelocity = new ArrayRealVector(new double[]{0.0, 0.0});
    }

    public static class CohesionEdge extends AbstractDirectedEdge<Particle> {
        public CohesionEdge(Particle source, Particle destination, float strength) {
            super(source, destination);
            this.strength = strength;
        }

        public float strength;
    }

    public CohesionParticleTracker(final Vector2d<Integer> imageSize) {
        final Vector2d<Integer> GRIDCOUNT = new Vector2d<>(500, 300);
        spatialAcceleration = new SpatialAcceleration<>(GRIDCOUNT.x, GRIDCOUNT.y, imageSize.x, imageSize.y);
    }

    public void step() {
        addEdgesIfNeeded();
        adjustCommulatedVelocity();
        weakenStrengthCohesion();
        removeEdgesIfCohesionTooLow();
    }

    private void removeEdgesIfCohesionTooLow() {
        // TODO
    }

    private void addEdgesIfNeeded() {
        repopulateSpatialAcceleration();

        for( Particle iterationParticle : graph.getNodes() ) {
            // TODO< get n closest particles >
            // for now we get the particles in a maximal radius
            final float NEIGHTBOR_RADIUS = 25.0f + 1.0f;
            final List<SpatialAcceleration<Particle>.Element> neightborParticles = spatialAcceleration.getElementsNearPoint(iterationParticle.getPosition(), NEIGHTBOR_RADIUS);

            for( final SpatialAcceleration<Particle>.Element neightborparticleElement : neightborParticles ) {
                Particle neightborparticle = neightborparticleElement.data;

                if( neightborparticle.equals(iterationParticle) ) {
                    continue;
                }

                // add edge if it doesnt exist

                if( existConnectionFromParticleToParticle(iterationParticle, neightborparticle) ) {
                    continue;
                }

                graph.add(new CohesionEdge(iterationParticle, neightborparticle, 0.0f));
            }
        }

        flushSpatialAcceleration();
    }

    private boolean existConnectionFromParticleToParticle(Particle source, Particle target) {
        // TODO ASK< is getAdjacentEdges correct? >
        for( final CohesionEdge iterationEdge : graph.getAdjacentEdges(source) ) {
            if( iterationEdge.getSourceNode().equals(source) && iterationEdge.getDestinationNode().equals(target) ) {
                return true;
            }
        }

        return false;
    }

    private void repopulateSpatialAcceleration() {
        for( Particle iterationParticle : graph.getNodes() ) {
            SpatialAcceleration<Particle>.Element newElement = spatialAcceleration.new Element();
            newElement.position = iterationParticle.getPosition();
            newElement.data = iterationParticle;

            spatialAcceleration.addElement(newElement);
        }
    }

    private void flushSpatialAcceleration() {
        spatialAcceleration.flushCells();
    }

    private void weakenStrengthCohesion() {
        for( Particle iterationParticle : graph.getNodes() ) {
            Set<CohesionEdge> outgoingEdges = graph.getInEdges(iterationParticle);

            for( CohesionEdge iterationEdge : outgoingEdges ) {
                final ArrayRealVector outgoingEdgeParticleCommulatedVelocity = iterationEdge.getDestinationNode().commulatedVelocity;

                adaptCohesionForEdge(iterationEdge, iterationParticle.commulatedVelocity, outgoingEdgeParticleCommulatedVelocity);
            }
        }
    }

    private void adjustCommulatedVelocity() {
        for( Particle iterationParticle : graph.getNodes() ) {
            iterationParticle.commulatedVelocity = new ArrayRealVector(iterationParticle.velocity.mapMultiply(1.0 - COMMULATEVELOCITY_FACTOR).add(new ArrayRealVector(iterationParticle.commulatedVelocity.mapMultiply(COMMULATEVELOCITY_FACTOR))));
        }
    }

    private void adaptCohesionForEdge(CohesionEdge iterationEdge, ArrayRealVector sourceParticleVelocity, ArrayRealVector outgoingEdgeParticleVelocity) {
        final double cohesionStrengthDelta = sourceParticleVelocity.getDistance(outgoingEdgeParticleVelocity) * COHESION_VELOCITYDIFFERENCE_MULTIPLIER;
        iterationEdge.strength += (float)cohesionStrengthDelta;
        iterationEdge.strength = ptrman.math.Math.clamp01(iterationEdge.strength);
    }

    public MutableDirectedAdjacencyGraph<Particle, CohesionEdge> graph = new MutableDirectedAdjacencyGraph<>();

    private SpatialAcceleration<Particle> spatialAcceleration;

    private final double COHESION_VELOCITYDIFFERENCE_MULTIPLIER = 0.1; // TODO< tune >
    private final double COMMULATEVELOCITY_FACTOR = 0.9; // TODO< tune >
}
