package ptrman.levels.retina.helper;

import ptrman.misc.Assert;

import java.util.*;

import static ptrman.levels.retina.helper.ProcessConnector.EnumMode.*;

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
        Assert.Assert(mode == WORKSPACE || mode == PRIMARY_QUEUE, "");
        Assert.Assert(workspace != null, "");
        return workspace;
    }

    public void addAll(Collection<Type> elements) {
        if( mode == WORKSPACE || mode == PRIMARY_QUEUE ) {
            workspace.addAll(elements);
        }

        if( mode == QUEUE || mode == PRIMARY_QUEUE ) {
            queue.addAll(elements);
        }

    }

    public void add(Type element) {

        if( mode == WORKSPACE || mode == PRIMARY_QUEUE ) {
            workspace.add(element);
        }

        if( mode == QUEUE || mode == PRIMARY_QUEUE ) {
            queue.add(element);
        }
    }

    public Type poll() {
        Assert.Assert( mode == QUEUE || mode == PRIMARY_QUEUE, "");
        return queue.poll();
    }

    public int getSize() {
        if( mode == WORKSPACE ) {
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

    public final EnumMode mode;

    public final Queue<Type> queue;
    public final List<Type> workspace;
}
