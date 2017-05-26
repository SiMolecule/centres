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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package uk.ac.ebi.centres;

import uk.ac.ebi.centres.graph.Edge;

import java.util.Collection;
import java.util.List;

/**
 * @author John May
 */
public interface ConnectionProvider<A> {

    public void build();

    public List<Node<A>> ligandInstancesForAtom(A atom);

    public List<Node<A>> getLigands(Node<A> node);

    public List<Edge<A>> getArcs(Node<A> node);

    public Edge<A> getParentArc(Node<A> node);

    public void reroot(Node<A> node);

    public Collection<Node<A>> ligands();
    
    /**
     * Clear the digraph ready for GC
     */
    public void dispose();


}
