/**
 * Copyright 2019 The SymVision authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ptrman.FargGeneral.network;

import org.eclipse.collections.impl.set.mutable.UnifiedSet;

import java.util.*;

public abstract class Node
{
    public final int type; // int because we don't know what types there can be in the specific impl., in the iml its a enum which gets casted to int
    
    public float activation; // [0.0, 1.0)
    public float activationDelta;
    
    public int conceptualDepth; // control decayrate
    
    private final EnumMap<Link.EnumType, Set<Link>> out = new EnumMap(Link.EnumType.class);

    //public ArrayList<Link> incommingLinks = new ArrayList<Link>(); // only bidirection links are in here
    
    public Node(int type)
    {
        this.type = type;
    }
    
    public void resetActivationDelta()
    {
        activationDelta = 0.0f;
    }
    
    public void addActivationDelta()
    {
        activation += activationDelta;
    }
    
    // TODO< good place to access a Map of the links >
    public Iterable<Link> getLinksByType(Link.EnumType type)
    {

        Set<Link> y = out.get(type);
        return y == null ? Collections.emptyList() : y;

        //return Iterables.filter(out, x->x.type==type);

//        List<Link> result = new ArrayList<>();
//
//        for( Link iterationLink : out)
//        {
//            if( iterationLink.type == type )
//            {
//                result.add(iterationLink);
//            }
//        }
//
//        return result;
    }

    public boolean out(Link l) {
        return out.computeIfAbsent(l.type, (x)->new UnifiedSet()).add(l);
    }

    public Iterable<Link> out() {
        return out.values().stream().flatMap(Collection::stream)::iterator;
    }
}
