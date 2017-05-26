/*
 * Copyright (c) 2012. John May
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 */

package uk.ac.ebi.centres.graph;

import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.MutableDescriptor;

/**
 * @author John May
 */
public class Edge<A> {

    private Node<A>           beg;
    private Node<A>           end;
    private MutableDescriptor descriptor;
    private int depth = 0;


    public Edge(Node<A> beg, Node<A> head,
                MutableDescriptor descriptor) {
        this.beg = beg;
        this.end = head;
        this.descriptor = descriptor;
    }


    /**
     * @param beg
     * @param head
     * @param descriptor
     * @param depth      1 = beg is closed then end, -1 = end is closer then
     *                   end, 0 = same plane. -1 = wedge bond when beg is root
     *                   atom
     */
    public Edge(Node<A> beg,
                Node<A> head,
                MutableDescriptor descriptor,
                int depth) {
        this.beg = beg;
        this.end = head;
        this.descriptor = descriptor;
        this.depth = depth;
    }


    public int getDepth() {
        return depth;
    }


    public Descriptor getDescriptor() {
        if (descriptor == null)
            return Descriptor.None;
        return descriptor.get();
    }


    public Node<A> getEnd() {
        return this.end;
    }


    public Node<A> getBeg() {
        return this.beg;
    }


    public void transpose() {
        Node<A> tmp = beg;
        beg = end;
        end = tmp;
        depth *= -1; // invert the sign
        end.setParent(beg.getAtom());
        end.reset(); // need to reset any caches
        beg.reset();
    }


    @Override
    public String toString() {
        return beg + " -> " + end;
    }
}
