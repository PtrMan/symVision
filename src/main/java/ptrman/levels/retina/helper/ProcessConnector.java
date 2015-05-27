package ptrman.levels.retina.helper;

import ptrman.misc.Assert;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Connector between two Processes.
 *
 * Can be either in WORKSPACE or QUEUE mode.
 */
public class ProcessConnector<Type> {

    public enum EnumMode {
        WORKSPACE,
        QUEUE,
        PRIMARY_QUEUE // workspace is used as a "logger", behaves like a queue
    }

    public static ProcessConnector createWithDefaultQueues(final EnumMode mode) {
        return new ProcessConnector(new ArrayDeque<>(), new ArrayList<>(), mode);
    }

    private ProcessConnector(Queue<Type> queueImplementation, List<Type> workspaceImplementation, EnumMode mode) {
        this.queue = queueImplementation;
        this.workspace = workspaceImplementation;
        this.mode = mode;
    }

    public List<Type> getWorkspace() {
        Assert.Assert(mode == EnumMode.WORKSPACE || mode == EnumMode.PRIMARY_QUEUE, "");
        Assert.Assert(workspace != null, "");
        return workspace;
    }

    public void add(Type element) {
        if( mode == EnumMode.WORKSPACE || mode == EnumMode.PRIMARY_QUEUE ) {
            workspace.add(element);
        }

        if( mode == EnumMode.QUEUE || mode == EnumMode.PRIMARY_QUEUE ) {
            queue.add(element);
        }
    }

    public Type poll() {
        Assert.Assert( mode == EnumMode.QUEUE || mode == EnumMode.PRIMARY_QUEUE, "");
        return queue.poll();
    }

    public int getSize() {
        if( mode == EnumMode.WORKSPACE ) {
            return workspace.size();
        }
        else {
            return queue.size();
        }
    }

    public void flush() {
        if( workspace != null ) {
            workspace.clear();
        }

        if ( queue != null ) {
            queue.clear();
        }
    }

    private EnumMode mode;

    private Queue<Type> queue;
    private List<Type> workspace;
}
