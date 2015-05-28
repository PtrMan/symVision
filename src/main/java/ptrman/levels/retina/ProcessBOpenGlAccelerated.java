package ptrman.levels.retina;

import com.jogamp.common.nio.Buffers;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.IMap2d;
import ptrman.Datastructures.Vector2d;
import ptrman.levels.retina.helper.ProcessConnector;
import ptrman.math.ArrayRealVectorHelper;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.media.opengl.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static javax.media.opengl.GL2.*;
import static ptrman.math.ArrayRealVectorHelper.arrayRealVectorToInteger;
import static ptrman.math.Math.squaredDistance;
import static ptrman.math.MatrixHelper.*;

/**
 * OpenGL accelerated ProcessB
 *
 * uses blendingmodes, polygondrawing and quaddrawing and render to texture functionality to accelerate the Process
 */
// TODO< accelerate it with the usage of a accelerated map, so we can draw large patches (typical for blocks of size 8x8), speeds up it order of magnitude >
public class ProcessBOpenGlAccelerated extends AbstractProcessB {
    private IMap2d<Boolean> inputMap;
    private ProcessConnector<ProcessA.Sample> inputSampleConnector;
    private ProcessConnector<ProcessA.Sample> outputSampleConnector;

    private static class PolygonBuffer {
        public PolygonBuffer(final int vertexLocation, final int colorLocation) {
            this.vertexLocation = vertexLocation;
            this.colorLocation = colorLocation;
        }

        private void createGL(GL3 gl, final float[] triangleArray, final float[] colorArray, final int numberOfPrimitives) {
            this.numberOfPrimitives = numberOfPrimitives;

            FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(triangleArray);
            vertexBuffer.rewind();
            final int vertexBufferSize = vertexBuffer.capacity() * Buffers.SIZEOF_FLOAT;

            FloatBuffer colorBuffer = Buffers.newDirectFloatBuffer(colorArray);
            colorBuffer.rewind();
            final int colorBufferSize = vertexBuffer.capacity() * Buffers.SIZEOF_FLOAT;


            IntBuffer buffers = IntBuffer.allocate(2);

            vao = IntBuffer.allocate(1);


            gl.glGenVertexArrays(1, vao);
            checkError(gl); ///

            gl.glBindVertexArray(vao.get(0));
            checkError(gl);

            gl.glGenBuffers(2, buffers);
            checkError(gl); ///
            // bind buffer for vertices and copy data into buffer
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, buffers.get(0));
            checkError(gl);
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, vertexBufferSize, vertexBuffer, GL3.GL_STATIC_DRAW);
            checkError(gl);
            gl.glEnableVertexAttribArray(vertexLocation);
            checkError(gl);
            gl.glVertexAttribPointer(vertexLocation, 3, GL3.GL_FLOAT, false, 0, 0);
            checkError(gl);

            // bind buffer for colors and copy data into buffer
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, buffers.get(1));
            checkError(gl);
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, colorBufferSize, colorBuffer, GL3.GL_STATIC_DRAW);
            checkError(gl);
            gl.glEnableVertexAttribArray(colorLocation);
            checkError(gl);
            gl.glVertexAttribPointer(colorLocation, 3, GL3.GL_FLOAT, false, 0, 0);
            checkError(gl);
        }

        public void draw(GL3 gl) {
            gl.glBindVertexArray(vao.get(0));
            checkError(gl);
            gl.glDrawArrays(GL.GL_TRIANGLE_STRIP, 0, numberOfPrimitives*4);
            checkError(gl);
        }

        public void release() {
            // TODO
            throw new NotImplementedException();
        }

        private final int vertexLocation;
        private final int colorLocation;

        private IntBuffer vao;

        private int numberOfPrimitives;
    }

    private static class Program {

        private void create(GL3 gl, String vertexShaderSource, String fragmentShaderSource) {
            int[] wasCompiled = new int[1];

            String[] vertexShaderSourceArray = new String[1];
            vertexShaderSourceArray[0] = vertexShaderSource;

            String[] fragmentShaderSourceArray = new String[1];
            fragmentShaderSourceArray[0] = fragmentShaderSource;

            program = gl.glCreateProgram();
            if( program == 0 ) {
                throw new RuntimeException("can't creat opengl program");
            }

            int vertexShader = gl.glCreateShader(GL3.GL_VERTEX_SHADER);
            gl.glShaderSource(vertexShader, 1, vertexShaderSourceArray, null);
            checkError(gl);
            gl.glCompileShader(vertexShader);
            checkError(gl);

            gl.glGetShaderiv(vertexShader, GL3.GL_COMPILE_STATUS, wasCompiled, 0);
            if(wasCompiled[0] == 0) {
                int[] logLength = new int[1];
                gl.glGetShaderiv(vertexShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

                byte[] log = new byte[logLength[0]];
                gl.glGetShaderInfoLog(vertexShader, logLength[0], null, 0, log, 0);

                throw new RuntimeException("Error compiling the vertex shader: " + new String(log));
            }

            int fragmentShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
            gl.glShaderSource(fragmentShader, 1, fragmentShaderSourceArray, null);
            checkError(gl);
            gl.glCompileShader(fragmentShader);
            checkError(gl);

            gl.glGetShaderiv(fragmentShader, GL3.GL_COMPILE_STATUS, wasCompiled, 0);
            if(wasCompiled[0] == 0) {
                int[] logLength = new int[1];
                gl.glGetShaderiv(fragmentShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

                byte[] log = new byte[logLength[0]];
                gl.glGetShaderInfoLog(fragmentShader, logLength[0], null, 0, log, 0);

                throw new RuntimeException("Error compiling the fragment shader: " + new String(log));
            }

            gl.glAttachShader(program, vertexShader);
            checkError(gl);
            gl.glAttachShader(program, fragmentShader);
            checkError(gl);
            gl.glLinkProgram(program);
            checkError(gl);
        }

        public int getProgram() {
            return program;
        }

        public void useThis(GL3 gl) {
            gl.glUseProgram(program);
            checkError(gl);
        }

        public void release() {
            // TODO
            throw new NotImplementedException();
        }

        private int program;

    }

    @Override
    public void set(IMap2d<Boolean> map, ProcessConnector<ProcessA.Sample> inputSampleConnector, ProcessConnector<ProcessA.Sample> outputSampleConnector) {
        this.inputMap = map;
        this.inputSampleConnector = inputSampleConnector;
        this.outputSampleConnector = outputSampleConnector;
    }

    @Override
    public void setup() {
        GLProfile.initSingleton();

        // TODO< change to GL3 when I have learned the api... >
        GLProfile glp = GLProfile.get(GLProfile.GL3);

        final String baseClassName = glp.getGLImplBaseClassName();

        GLCapabilities caps = new GLCapabilities(glp);

        // Without line below, there is an error on Windows.
        caps.setDoubleBuffered(false);



        GLDrawableFactory factory = GLDrawableFactory.getFactory(glp);


        //makes a new buffer
        // TODO< catch exception >
        GLPbuffer pBuffer = factory.createGLPbuffer(GLProfile.getDefaultDevice(), caps, null, imageSize.x, imageSize.y, null);

        //required for drawing to the buffer
        context = pBuffer.createContext(null);

        context.makeCurrent();
        gl = (GL3)context.getCurrentGL();

        initFbo(gl);


        final String vertexShader = "#version 330\n" +
                "layout(location=0) in vec3 position;\n" +
                "layout(location=1) in vec3 color;\n" +
                "out vec3 vColor;\n" +
                "uniform mat4 viewMatrix;\n" +
                "void main(void)\n" +
                "{\n" +
                "gl_Position = viewMatrix * vec4(position, 1.0);\n" +
                "vColor = color;\n" +
                "}\n";

        final String fragmentShaderForTest = "#version 330\n" +
                "in vec4 vColor;\n" +
                "out vec4 fColor;\n" +
                "void main(void)\n" +
                "{\n" +
                "vec4 squared = vColor * vColor;\n" +
                "float value = squared.x + squared.y;\n" +
                "fColor = vec4(0,0,0,1) + vec4(value, 0,0,0);\n" +
                "}\n";

        defaultProgram = new Program();
        defaultProgram.create(gl, vertexShader, fragmentShaderForTest);



        final float[] triangleArray = {
                0, 0, 0.5f,
                2, 0, 0.5f,
                0, 2, 0.5f,
                2, 2, 0.5f,
        };

        final float[] colorArray = {
                0, 0, 1,
                10, 0, 1,
                0, 1, 1,
                10, 10, 1,
        };

        final int vertexLocation = 0;
        final int colorLocation = 1;

        polygonBufferTest = new PolygonBuffer(vertexLocation, colorLocation);
        polygonBufferTest.createGL(gl, triangleArray, colorArray, /* number of primitives*/ 1);

        projectionMatrixBuffer = FloatBuffer.allocate(16);
        viewMatrixBuffer = FloatBuffer.allocate(16);

        projMatrixLoc = gl.glGetUniformLocation(defaultProgram.getProgram(), "projMatrix");
        checkError(gl);
        viewMatrixLoc = gl.glGetUniformLocation(defaultProgram.getProgram(), "viewMatrix");
        checkError(gl);



        // set projection matrix only once because it doesn't change!
        final float[] projectionMatrix = convertMatrixToArray(getIdentityMatrix());

        projectionMatrixBuffer.rewind();
        projectionMatrixBuffer.put(projectionMatrix);
        projectionMatrixBuffer.rewind();

        defaultProgram.useThis(gl);

        gl.glUniformMatrix4fv(projMatrixLoc, 1, false, projectionMatrixBuffer);
        checkError(gl);

    }

    @Override
    public void preProcessData() {

    }

    /**
     *
     * we use the whole image, in phaeaco he worked with the incomplete image with the guiding of processA, this is not implemented that way
     */
    @Override
    public void processData() {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        final float maxDistance = (float)java.lang.Math.sqrt(squaredDistance(new double[]{imageSize.x, imageSize.y}));
        final float backgroundValue = maxDistance*maxDistance;

        gl = (GL3)context.getCurrentGL();

        context.makeCurrent();
        renderToFrameBufferBegin(gl);



        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(gl);
        gl.glClearColor(backgroundValue, 0.0f, 0.0f, 1.0f);
        checkError(gl);
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        checkError(gl);

        gl.glViewport(0, 0, fboImageSize.x, fboImageSize.y);
        checkError(gl);

        defaultProgram.useThis(gl);



        gl.glEnable(gl.GL_BLEND);
        gl.glBlendEquationSeparate(gl.GL_MIN, gl.GL_MIN);


        /*
        double translateX = -(1.0);
        double translateY = -(0.0);


        Array2DRowRealMatrix viewMatrix = getIdentityMatrix();
        viewMatrix = viewMatrix.multiply(getScaleMatrix(new ArrayRealVector(new double[]{.5, 1.0, 1.0})));
        viewMatrix = viewMatrix.multiply(getTranslationMatrix(new ArrayRealVector(new double[]{translateX, translateY, 0.0f})));
        */

        for( int y = 0; y < inputMap.getLength(); y++ ) {
            for( int x = 0; x < inputMap.getWidth(); x++ ) {
                boolean pixelValue = inputMap.readAt(x, y);

                if( !pixelValue ) {
                    Array2DRowRealMatrix viewMatrix = getMatrixForBlackPixel(0, 0, fboImageSize.x, maxDistance);
                    setUniforms(defaultProgram, viewMatrix);
                    polygonBufferTest.draw(gl);
                }
            }
        }




        // TODO< use no program >


        renderToFrameBufferEnd(gl);



        // read framebuffer
        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(gl);
        gl.glReadBuffer(GL_COLOR_ATTACHMENT0);
        checkError(gl);

        // allocate memory for texture data
        FloatBuffer data = Buffers.newDirectFloatBuffer(fboImageSize.x*fboImageSize.y*4);

        //
        gl.glClampColor(gl.GL_CLAMP_READ_COLOR, gl.GL_FALSE);

        gl.glReadPixels(0, 0, fboImageSize.x, fboImageSize.y, GL_RGBA, GL_FLOAT, data);
        checkError(gl);

        // dump of data
        float[] dataArrayRaw = new float[fboImageSize.x*fboImageSize.y*4];

        int remaining = data.remaining();

        for( int i = 0; i < remaining; i++ ) {
            dataArrayRaw[i] = data.get(i);
        }

        float[] dataArray = new float[fboImageSize.x*fboImageSize.y];

        for( int i = 0; i < dataArray.length; i++ ) {
            dataArray[i] = dataArrayRaw[i*4];
        }

        /* debug the content of dataArray
        for( int y = 0; y < fboImageSize.y; y++ ) {
            for( int x = 0; x < fboImageSize.x; x++ ) {
                float distanceSquared = dataArray[x + y*fboImageSize.x];

                boolean notSet = distanceSquared >= backgroundValue - 1000000.0f;

                if( notSet ) {
                    System.out.print(" ");
                }
                else {
                    System.out.print(".");
                }
            }

            System.out.println("");
        }
        */

        // for each sample we read out the depth and calculate the squareroot of it.
        // we calculate the squareroot because the values are squared for efficiency reasons

        final int samplesRemaining = inputSampleConnector.getSize();

        for( int sampleI = 0; sampleI < samplesRemaining; sampleI++ ) {
            final ProcessA.Sample currentSample = inputSampleConnector.poll();
            final ArrayRealVector currentSamplePosition = currentSample.position;
            final Vector2d<Integer> currentSamplePositionAsInteger = arrayRealVectorToInteger(currentSamplePosition, ArrayRealVectorHelper.EnumRoundMode.DOWN);

            final float squaredAltitudeOfSample =  dataArray[currentSamplePositionAsInteger.x + currentSamplePositionAsInteger.y * fboImageSize.x];
            final float altitudeOfSample = (float)java.lang.Math.sqrt(squaredAltitudeOfSample);

            ProcessA.Sample resultSample = currentSample.getClone();
            resultSample.altitude = altitudeOfSample;

            outputSampleConnector.add(resultSample);
        }

    }

    @Override
    public void postProcessData() {

    }


    // code from https://github.com/demoscenepassivist/SocialCoding/blob/master/code_demos_jogamp/src/framework/base/BaseFrameBufferObjectRendererExecutor.java
    private void initFbo(GL3 gl) {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        //allocate the framebuffer object ...
        int[] result = new int[1];
        gl.glGenFramebuffers(1, result, 0);
        checkError(gl);
        frameBufferObjectID = result[0];
        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(gl);
        //allocate the colour texture ...
        gl.glGenTextures(1, result, 0); // no check
        colorTextureID = result[0];
        gl.glBindTexture(GL_TEXTURE_2D, colorTextureID);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        checkError(gl);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, fboImageSize.x, fboImageSize.y, 0, GL_RGBA, GL_FLOAT, null);
        checkError(gl);
        //allocate the depth texture ...
        gl.glGenTextures(1, result, 0); // no check
        depthTextureID = result[0];
        gl.glBindTexture(GL_TEXTURE_2D, depthTextureID);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        checkError(gl);
        gl.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        checkError(gl);
        gl.glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT32, fboImageSize.x, fboImageSize.y, 0, GL_DEPTH_COMPONENT, GL_UNSIGNED_INT, null);
        checkError(gl);
        //attach the textures to the framebuffer
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, colorTextureID, 0);
        checkError(gl);
        gl.glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, depthTextureID, 0);
        checkError(gl);
        gl.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        checkError(gl);
        //check if fbo is set up correctly ...
        checkFrameBufferObjectCompleteness(gl);
    }

    private void checkFrameBufferObjectCompleteness(GL3 inGL) {
        //BaseLogging.getInstance().info("CHECKING FRAMEBUFFEROBJECT COMPLETENESS ...");
        int tError = inGL.glCheckFramebufferStatus(GL_FRAMEBUFFER);
        switch(tError) {
            case GL_FRAMEBUFFER_COMPLETE:
                //BaseLogging.getInstance().info("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_COMPLETE_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT");
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:;
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT");
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT");
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT");
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT");
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT");
            case GL_FRAMEBUFFER_UNSUPPORTED:
                throw new RuntimeException("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_UNSUPPORTED_EXT");
            default:
                throw new RuntimeException("FRAMEBUFFER CHECK RETURNED UNKNOWN RESULT ...");
        }
    }

    public void renderToFrameBufferBegin(GL3 inGL) {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        inGL.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(inGL);
        inGL.glViewport(0, 0, fboImageSize.x, fboImageSize.y);
        checkError(gl);
    }

    public void renderToFrameBufferEnd(GL3 inGL) {
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        checkError(inGL);
    }

    private static void checkError(GL3 gl) {
        if( gl.glGetError() != 0 ) {
            throw new RuntimeException("OpenGL error!");
        }
    }

    /**
     *
     * TODO shutdown
     *
     context.destroy();
     buf.destroy();
     */

    private Vector2d<Integer> getFboImageSize() {
        final int nextPowerOfTwo = ptrman.math.Math.nextPowerOfTwo(java.lang.Math.max(imageSize.x, imageSize.y));

        return new Vector2d<>(nextPowerOfTwo, nextPowerOfTwo);
    }

    // must be called after glUseProgram
    private void setUniforms(Program fromProgram, final Array2DRowRealMatrix viewMatrixInput) {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        final float[] projectionMatrix = convertMatrixToArray(getIdentityMatrix());
        final float[] viewMatrix = convertMatrixToArray(viewMatrixInput);

        projectionMatrixBuffer.rewind();
        projectionMatrixBuffer.put(projectionMatrix);
        projectionMatrixBuffer.rewind();

        viewMatrixBuffer.rewind();
        viewMatrixBuffer.put(viewMatrix);
        viewMatrixBuffer.rewind();

        // we save the time to set the matrix because its always the same and set in in the setup
        //gl.glUniformMatrix4fv(projMatrixLoc, 1, false, projectionMatrixBuffer);
        //checkError(gl);

        gl.glUniformMatrix4fv(viewMatrixLoc, 1, false, viewMatrixBuffer);
        checkError(gl);
    }


    private Array2DRowRealMatrix getMatrixForBlackPixel(int x, int y, int bufferSize, float maxDistance) {
        final int maxAltitideInteger = (int)maxDistance;

        return getMatrixForPositionAndSize(x-maxAltitideInteger, y-maxAltitideInteger, x+1+maxAltitideInteger, y+1+maxAltitideInteger, bufferSize);
    }

    private static Array2DRowRealMatrix getMatrixForPositionAndSize(final int positionX, final int positionY, final int sizeX, final int sizeY, final int bufferSize) {
        double translateX = ((double)positionX/(double)bufferSize) * 2.0 - 1.0;
        double translateY = ((double)positionY/(double)bufferSize) * 2.0 - 1.0;

        double sizeXd = (double)sizeX / (double)bufferSize;
        double sizeYd = (double)sizeY / (double)bufferSize;

        Array2DRowRealMatrix viewMatrix = getIdentityMatrix();
        viewMatrix = viewMatrix.multiply(getScaleMatrix(new ArrayRealVector(new double[]{sizeXd, sizeYd, 1.0})));
        viewMatrix = viewMatrix.multiply(getTranslationMatrix(new ArrayRealVector(new double[]{translateX, translateY, 0.0f})));

        return viewMatrix;
    }

    private GLContext context;
    private GL3 gl;

    private int frameBufferObjectID;
    private int colorTextureID, depthTextureID;

    private Program defaultProgram;
    private PolygonBuffer polygonBufferTest;

    private FloatBuffer projectionMatrixBuffer;
    private FloatBuffer viewMatrixBuffer;

    private int projMatrixLoc;
    private int viewMatrixLoc;
}
