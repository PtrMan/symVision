package ptrman.Additional;

import boofcv.abst.flow.DenseOpticalFlow;
import boofcv.alg.distort.DistortImageOps;
import boofcv.alg.interpolate.TypeInterpolate;
import boofcv.factory.flow.FactoryDenseOpticalFlow;
import boofcv.struct.flow.ImageFlow;
import boofcv.struct.image.ImageFloat32;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.SpatialAcceleration;
import ptrman.Datastructures.Vector2d;
import ptrman.math.ArrayRealVectorHelper;
import ptrman.math.RandomUtil;
//import sun.awt.Mutex;
import java.util.concurrent.Semaphore;

import java.util.ArrayList;
import java.util.List;

// TODO< remove particles which are too dense >
/**
 * Tracks particles with the imageflow.
 *
 * Manages the creation of new particles
 */
public class ParticleFlowTracker<ParticleType extends ParticleFlowTracker.ITrackingParticle> {
    public interface ITrackingParticle {
        void setPosition(final ArrayRealVector position);
        ArrayRealVector getPosition();

        void setVelocity(final ArrayRealVector velocity);
        ArrayRealVector getVelocity();
    }

    public interface IParticleConstructorDestructor<ParticleType> {
        ParticleType create(final ArrayRealVector position);
        void remove(final ParticleType particle);
    }

    public ParticleFlowTracker(IParticleConstructorDestructor<ParticleType> particleConstructorDestructor, Vector2d<Integer> imageSize, int imageDownscaleFactor, Semaphore particleMutex) {
        this.particleConstructorDestructor = particleConstructorDestructor;
        this.imageDownscaleFactor = imageDownscaleFactor;
        this.imageSize = imageSize;
        this.particleMutex = particleMutex;

        denseFlow =
        //				FactoryDenseOpticalFlow.flowKlt(null, 6, ImageFloat32.class, null);
        //				FactoryDenseOpticalFlow.region(null,ImageFloat32.class);
        //				FactoryDenseOpticalFlow.hornSchunck(20, 1000, ImageFloat32.class);
        //				FactoryDenseOpticalFlow.hornSchunckPyramid(null,ImageFloat32.class);
        FactoryDenseOpticalFlow.broxWarping(null, ImageFloat32.class);


        // scaled down because the flow is computational expensive
        previous = new ImageFloat32(imageSize.x/imageDownscaleFactor, imageSize.y/imageDownscaleFactor);
        current = new ImageFloat32(imageSize.x/imageDownscaleFactor, imageSize.y/imageDownscaleFactor);

        flow = new ImageFlow(previous.width, previous.height);

        final int spatialAccelerationCellsX = 500;
        final int spatialAccelerationCellsY = 300;
        trackingParticleAcceleration = new SpatialAcceleration<>(spatialAccelerationCellsX, spatialAccelerationCellsY, (float)imageSize.x, (float)imageSize.y );

        setupSamplePositions();
    }

    private void setupSamplePositions() {
        final float trackingDensity = 0.05f;

        final int numberOfTrackingPoints = (int)((float)imageSize.x * (float)imageSize.y * trackingDensity * trackingDensity);

        samplePositions = new ArrayRealVector[numberOfTrackingPoints];

        for( int newTrackingPointCounter = 0; newTrackingPointCounter < numberOfTrackingPoints; newTrackingPointCounter++ )
        {
            double samplePositionX = (((double)imageSize.x - 1.0) * RandomUtil.radicalInverse(newTrackingPointCounter, 2));
            double samplePositionY = (((double)imageSize.y - 1.0) * RandomUtil.radicalInverse(newTrackingPointCounter, 3));

            samplePositions[newTrackingPointCounter] = new ArrayRealVector(new double[]{samplePositionX, samplePositionY});
        }
    }

    public void firstImage(ImageFloat32 image) {
        DistortImageOps.scale(image, current, TypeInterpolate.BILINEAR);
        DistortImageOps.scale(image, previous, TypeInterpolate.BILINEAR);
    }

    public void step(ImageFloat32 newCurrent) {
        //particleMutex.lock();

        flip();
        setCurrent(newCurrent);
        dragParticles();
        removeParticlesOutOfImage();
        reseedTrackingParticles();

        //particleMutex.unlock();
    }

    private void removeParticlesOutOfImage() {
        List<ParticleType> particlesToBeRemoved = new ArrayList<>();

        for( int particleI = 0; particleI < trackingParticles.size(); particleI++ ) {
            ParticleType iterationTrackingParticle = trackingParticles.get(particleI);

            if(
                iterationTrackingParticle.getPosition().getDataRef()[0] < 0.0 ||
                iterationTrackingParticle.getPosition().getDataRef()[0] > imageSize.x ||
                iterationTrackingParticle.getPosition().getDataRef()[1] < 0.0 ||
                iterationTrackingParticle.getPosition().getDataRef()[1] > imageSize.y
            ) {
                particlesToBeRemoved.add(iterationTrackingParticle);

                //trackingParticles.remove(particleI);
                //particleConstructorDestructor.remove(iterationTrackingParticle);
                //particleI--;
            }
        }

        for( ParticleType iterationParticle : particlesToBeRemoved ) {
            particleConstructorDestructor.remove(iterationParticle);
            trackingParticles.remove(iterationParticle);
        }
    }

    private void reseedTrackingParticles() {
        final float nextParticleMaxDistance = 25.0f;

        for( ParticleType iterationTrackingParticle : trackingParticles ) {
            SpatialAcceleration<ParticleType>.Element newElement = trackingParticleAcceleration.new Element();
            newElement.position = iterationTrackingParticle.getPosition();
            newElement.data = iterationTrackingParticle;

            trackingParticleAcceleration.addElement(newElement);
        }

        for( final ArrayRealVector iterationPosition : samplePositions ) {
            final List<SpatialAcceleration<ParticleType>.Element> nearestParticlesToParticle = trackingParticleAcceleration.getElementsNearPoint(iterationPosition, nextParticleMaxDistance);

            if( nearestParticlesToParticle.isEmpty() ) {
                ParticleType createdParticle = particleConstructorDestructor.create(iterationPosition);
                trackingParticles.add(createdParticle);
            }
        }

        trackingParticleAcceleration.flushCells();
    }

    private void flip() {
        System.arraycopy(current.data, 0, previous.data, 0, previous.data.length);
    }

    private void setCurrent(ImageFloat32 newCurrent) {
        DistortImageOps.scale(newCurrent, current, TypeInterpolate.BILINEAR);
    }

    private void dragParticles() {
        // compute dense motion
        denseFlow.process(previous, current, flow);

        // drag all particles
        for( ParticleType iterationTrackingParticle : trackingParticles ) {
            Vector2d<Integer> positionAsInteger = ArrayRealVectorHelper.arrayRealVectorToInteger(new ArrayRealVector(iterationTrackingParticle.getPosition().mapMultiply(1.0 / 2.0)), ArrayRealVectorHelper.EnumRoundMode.DOWN);

            ImageFlow.D floatDirection = flow.get(positionAsInteger.x/imageDownscaleFactor, positionAsInteger.y/imageDownscaleFactor);

            if( !floatDirection.isValid() ) {
                continue;
            }

            iterationTrackingParticle.setVelocity(new ArrayRealVector(new double[]{(double)floatDirection.x*imageDownscaleFactor, (double)floatDirection.y*imageDownscaleFactor}));
            iterationTrackingParticle.setPosition(iterationTrackingParticle.getPosition().add(iterationTrackingParticle.getVelocity()));
        }

    }

    public List<ParticleType> trackingParticles = new ArrayList<>();


    private ArrayRealVector[] samplePositions;

    private DenseOpticalFlow<ImageFloat32> denseFlow;
    private ImageFlow flow;

    private ImageFloat32 previous;
    private ImageFloat32 current;

    private SpatialAcceleration<ParticleType> trackingParticleAcceleration;

    private final Vector2d<Integer> imageSize;
    private final int imageDownscaleFactor;

    private final IParticleConstructorDestructor<ParticleType> particleConstructorDestructor;

    private Semaphore particleMutex;
}
