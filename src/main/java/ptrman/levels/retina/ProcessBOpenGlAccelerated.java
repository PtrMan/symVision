package ptrman.levels.retina;

import com.jogamp.common.nio.Buffers;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import ptrman.Datastructures.Vector2d;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import javax.media.opengl.*;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static javax.media.opengl.GL2.*;

/**
 * OpenGL accelerated ProcessB
 *
 * uses blendingmodes, polygondrawing and quaddrawing and render to texture functionality to accelerate the Process
 */
public class ProcessBOpenGlAccelerated implements IProcess {
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
            if(wasCompiled[0] != 0) {
                System.out.println("Vertex shader compiled");
            }
            else {
                int[] logLength = new int[1];
                gl.glGetShaderiv(vertexShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

                byte[] log = new byte[logLength[0]];
                gl.glGetShaderInfoLog(vertexShader, logLength[0], null, 0, log, 0);

                System.err.println("Error compiling the vertex shader: " + new String(log));
                System.exit(1);
            }

            int fragmentShader = gl.glCreateShader(GL3.GL_FRAGMENT_SHADER);
            gl.glShaderSource(fragmentShader, 1, fragmentShaderSourceArray, null);
            checkError(gl);
            gl.glCompileShader(fragmentShader);
            checkError(gl);

            gl.glGetShaderiv(fragmentShader, GL3.GL_COMPILE_STATUS, wasCompiled, 0);
            if(wasCompiled[0] != 0) {
                System.out.println("fragment shader compiled");
            }
            else {
                int[] logLength = new int[1];
                gl.glGetShaderiv(fragmentShader, GL3.GL_INFO_LOG_LENGTH, logLength, 0);

                byte[] log = new byte[logLength[0]];
                gl.glGetShaderInfoLog(fragmentShader, logLength[0], null, 0, log, 0);

                System.err.println("Error compiling the fragment shader: " + new String(log));
                System.exit(1);
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
    public void setImageSize(Vector2d<Integer> imageSize) {
        this.imageSize = imageSize;
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
                "void main(void)\n" +
                "{\n" +
                "gl_Position = vec4(position, 1.0);\n" +
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
                1, 0, 0.5f,
                0, 1, 0.5f,
                1, 1, 0.5f,
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

    }


    @Override
    public void processData() {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        final float maxDistance = 10.0f + 500.0f*500.0f;
        final float backgroundColor = maxDistance*maxDistance;

        gl = (GL3)context.getCurrentGL();

        context.makeCurrent();
        renderToFrameBufferBegin(gl);



        gl.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(gl);
        gl.glClearColor(maxDistance, 0.0f, 0.0f, 1.0f);
        checkError(gl);
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        checkError(gl);

        gl.glViewport(0, 0, fboImageSize.x, fboImageSize.y);
        checkError(gl);

        /* GL2
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0.0, (float) fboImageSize.x, 0.0, (float) fboImageSize.y, -1.0, 1.0);
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        gl.glBegin(GL_TRIANGLES);

        gl.glColor3f(0.5f, 2.0f, -0.5f);

        gl.glVertex3f(0.0f, 0.0f, 0.0f);
        gl.glVertex3f((float) 50, 0.0f, 0.0f);
        gl.glVertex3f((float) 50, (float) 50, 0.0f);

        gl.glEnd();
        */

        //gl.glClampColor(GL_CLAMP_FRAGMENT_COLOR, GL_FALSE);


        defaultProgram.useThis(gl);
        setUniforms(defaultProgram);

        gl.glEnable(gl.GL_BLEND);
        gl.glBlendEquationSeparate(gl.GL_MIN, gl.GL_MIN);

        polygonBufferTest.draw(gl);

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
        float[] dataArray = new float[fboImageSize.x*fboImageSize.y*4];

        int remaining = data.remaining();

        for( int i = 0; i < remaining; i++ ) {
            dataArray[i] = data.get(i);
        }

        /*
        while(data.hasRemaining()) {
            int curr = data.position() / 4;
            int offset = (curr%fboImageSize.x+(curr))*4;
            dataArray[offset] = data.get();
            dataArray[offset+1] = data.get();
            dataArray[offset+2] = data.get();
            dataArray[offset+3] = data.get();
        }
        */

        for( int i = 0; i < dataArray.length / 4; i++ ) {
            System.out.println(Float.toString(dataArray[i * 4 + 0]) + " " + Float.toString(dataArray[i * 4 + 1]) + " " + Float.toString(dataArray[i*4 + 2]) + " " + Float.toString(dataArray[i*4 + 3]));
        }
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
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_FORMATS:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT");
                break;
            case GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT");
                break;
            case GL_FRAMEBUFFER_UNSUPPORTED:
                //BaseLogging.getInstance().error("FRAMEBUFFEROBJECT CHECK RESULT=GL_FRAMEBUFFER_UNSUPPORTED_EXT");
                break;
            default:
                //BaseLogging.getInstance().error("FRAMEBUFFER CHECK RETURNED UNKNOWN RESULT ...");
                int debugX = 0;
        }
    }

    public void renderToFrameBufferBegin(GL3 inGL) {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        ///inGL.glPushAttrib(GL_TRANSFORM_BIT | GL_ENABLE_BIT | GL_COLOR_BUFFER_BIT);
        //bind the framebuffer ...
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, frameBufferObjectID);
        checkError(inGL);
        ///inGL.glPushAttrib(GL_VIEWPORT_BIT);
        inGL.glViewport(0, 0, fboImageSize.x, fboImageSize.y);
        checkError(gl);
    }

    public void renderToFrameBufferEnd(GL3 inGL) {
        ///inGL.glPopAttrib();
        //unbind the framebuffer ...
        inGL.glBindFramebuffer(GL_FRAMEBUFFER, 0);
        checkError(inGL);
        ///inGL.glPopAttrib();
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
    private void setUniforms(Program fromProgram) {
        final Vector2d<Integer> fboImageSize = getFboImageSize();

        //FOR TESTING
        final float[] projectionMatrix = convertMatrixToArray(getIdentityMatrix()); //getOrtographicMatrix(fboImageSize.x, fboImageSize.y, -1.0f, 1.0f);
        final float[] viewMatrix = convertMatrixToArray(getIdentityMatrix());

        FloatBuffer projectionMatrixBuffer = Buffers.newDirectFloatBuffer(projectionMatrix);
        projectionMatrixBuffer.rewind();
        FloatBuffer viewMatrixBuffer = Buffers.newDirectFloatBuffer(viewMatrix);
        viewMatrixBuffer.rewind();

        final int projMatrixLoc = gl.glGetUniformLocation(fromProgram.getProgram(), "projMatrix");
        checkError(gl);
        final int viewMatrixLoc = gl.glGetUniformLocation(fromProgram.getProgram(), "viewMatrix");
        checkError(gl);

        gl.glUniformMatrix4fv(projMatrixLoc, 1, false, projectionMatrixBuffer);
        checkError(gl);
        gl.glUniformMatrix4fv(viewMatrixLoc, 1, false, viewMatrixBuffer);
        checkError(gl);
    }

    private float[] convertMatrixToArray(final Array2DRowRealMatrix matrix) {
        float[] result = new float[16];

        for( int row = 0; row < 4; row++ ) {
            for( int column = 0; column < 4; column++ ) {
                result[row*4+column] = (float)matrix.getRow(row)[column];
            }
        }

        return new float[0];
    }

    // see https://www.opengl.org/discussion_boards/showthread.php/172280-Constructing-an-orthographic-matrix-for-2D-drawing
    private static float[] getOrtographicMatrix(float xmax, float ymax, float zNear, float zFar) {
        return new float[] {
                2.0f/xmax, 0.0f, 0.0f, -1.0f,
                0.0f -2.0f/ymax, 0.0f, 1.0f,
                0.0f, 0.0f, 2.0f/(zFar-zNear), (zNear+zFar)/(zNear-zFar),
                0.0f, 0.0f, 0.0f, 1.0f
        };
    }

    private static Array2DRowRealMatrix getIdentityMatrix() {
        Array2DRowRealMatrix result = new Array2DRowRealMatrix(4, 4);
        for( int i = 0; i < 4; i++ ) {
            result.getData()[i][i] = 1.0;
        }

        return result;
    }

    private static Array2DRowRealMatrix getTranslationMatrix(final ArrayRealVector translation) {
        Array2DRowRealMatrix result = getIdentityMatrix();
        result.getData()[3][0] = translation.getDataRef()[0];
        result.getData()[3][1] = translation.getDataRef()[1];
        result.getData()[3][2] = translation.getDataRef()[2];
        return result;
    }

    private Vector2d<Integer> imageSize;

    private GLContext context;
    private GL3 gl;

    private int frameBufferObjectID;
    private int colorTextureID, depthTextureID;

    Program defaultProgram;
    PolygonBuffer polygonBufferTest;
}
