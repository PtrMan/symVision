package ptrman.openclContext;

//import com.jogamp.opencl.CLCommandQueue;
//import com.jogamp.opencl.CLContext;
//import com.jogamp.opencl.CLDevice;

/**
 *
 */
public class OpenclContext {
    /*
    private OpenclContext(CLContext clContext, CLCommandQueue queue) {
        this.clContext = clContext;
        this.queue = queue;
    }

    public static OpenclContext create() {
        CLContext clContext = CLContext.create();

        // always make sure to release the context under all circumstances
        // not needed for this particular sample but recommented

        // select fastest device
        CLDevice device = clContext.getMaxFlopsDevice();

        CLCommandQueue queue = device.createCommandQueue(0);

        return new OpenclContext(clContext, queue);
    }

    public void release() {
        queue.release();
        clContext.release();

        queue = null;
        clContext = null;
    }

    private CLContext clContext;
    private CLCommandQueue queue;
    */
}
