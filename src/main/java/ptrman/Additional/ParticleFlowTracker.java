/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.Additional;

import boofcv.abst.flow.DenseOpticalFlow;
import boofcv.alg.distort.DistortImageOps;
import boofcv.alg.interpolate.InterpolationType;
import boofcv.factory.flow.FactoryDenseOpticalFlow;
import boofcv.struct.border.BorderType;
import boofcv.struct.flow.ImageFlow;
import boofcv.struct.image.GrayF32;
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
        //				FactoryDenseOpticalFlow.region(null,GrayF32.class);
        //				FactoryDenseOpticalFlow.hornSchunck(20, 1000, ImageFloat32.class);
        //				FactoryDenseOpticalFlow.hornSchunckPyramid(null,ImageFloat32.class);
            FactoryDenseOpticalFlow.broxWarping(null, GrayF32.class);


        // scaled down because the flow is computational expensive
        previous = new GrayF32(imageSize.x / imageDownscaleFactor, imageSize.y / imageDownscaleFactor);
        current = new GrayF32(imageSize.x/imageDownscaleFactor, imageSize.y/imageDownscaleFactor);

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

    public void firstImage(GrayF32 image) {
        DistortImageOps.scale(image, current, BorderType.ZERO, InterpolationType.BILINEAR);
        DistortImageOps.scale(image, previous,  BorderType.ZERO, InterpolationType.BILINEAR);
    }

    public void step(GrayF32 newCurrent) {
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

        for (ParticleType iterationTrackingParticle : trackingParticles) {
            if (
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

    private void setCurrent(GrayF32 newCurrent) {
        DistortImageOps.scale(newCurrent, current, BorderType.ZERO, InterpolationType.BILINEAR);
    }

    private void dragParticles() {
        // compute dense motion
        denseFlow.process(previous, current, flow);

        // drag all particles
        for( ParticleType iterationTrackingParticle : trackingParticles ) {
            Vector2d<Integer> positionAsInteger = ArrayRealVectorHelper.arrayRealVectorToInteger(new ArrayRealVector(iterationTrackingParticle.getPosition().mapMultiply(1.0 / 2.0)), ArrayRealVectorHelper.EnumRoundMode.DOWN);

            ImageFlow.D floatDirection = flow.get(positionAsInteger.x/imageDownscaleFactor, positionAsInteger.y/imageDownscaleFactor);

            if( !floatDirection.isValid() )
                continue;

            iterationTrackingParticle.setVelocity(new ArrayRealVector(new double[]{(double)floatDirection.x*imageDownscaleFactor, (double)floatDirection.y*imageDownscaleFactor}));
            iterationTrackingParticle.setPosition(iterationTrackingParticle.getPosition().add(iterationTrackingParticle.getVelocity()));
        }

    }

    public final List<ParticleType> trackingParticles = new ArrayList<>();


    private ArrayRealVector[] samplePositions;

    private final DenseOpticalFlow<GrayF32> denseFlow;
    private final ImageFlow flow;

    private final GrayF32 previous;
    private final GrayF32 current;

    private final SpatialAcceleration<ParticleType> trackingParticleAcceleration;

    private final Vector2d<Integer> imageSize;
    private final int imageDownscaleFactor;

    private final IParticleConstructorDestructor<ParticleType> particleConstructorDestructor;

    private final Semaphore particleMutex;
}
