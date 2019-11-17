/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
