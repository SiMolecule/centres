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

import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.Digraph;
import uk.ac.ebi.centres.Ligand;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A digraph which has changeable root
 *
 * @author John May
 */
public abstract class AbstractMutableDigraph<A>
        implements Digraph<A>,
                   ConnectionProvider<A> {

    public Map<Centre<A>, RootedDigraph<A>> graphs = new HashMap<Centre<A>, RootedDigraph<A>>();
    private final DescriptorManager<A> manager;
    private       RootedDigraph<A>     active;


    public AbstractMutableDigraph(DescriptorManager<A> manager) {
        this.manager = manager;
    }


    public void setRoot(Centre<A> root) {
        if (graphs.containsKey(root)) {
            active = graphs.get(root);
        }

        // create a new graph for this centre
        final AbstractMutableDigraph<A> self = this;
        active = new RootedDigraph<A>(root, manager) {
            @Override
            public Collection<A> getConnected(A atom) {
                return self.getConnected(atom);
            }


            @Override
            public int getOrder(A first, A second) {
                return self.getOrder(first, second);
            }


            @Override
            public int getDepth(A first, A second) {
                return self.getDepth(first, second);
            }
        };

        graphs.put(root, active);

    }


    public abstract int getOrder(A first, A second);

    public abstract int getDepth(A first, A second);

    public abstract Collection<A> getConnected(A atom);


    @Override
    public List<Ligand<A>> getLigands(Ligand<A> ligand) {
        return active.getLigands(ligand);
    }


    @Override
    public Ligand<A> getRoot() {
        return active.getRoot();
    }


    @Override
    public List<Ligand<A>> getProximal() {
        return active.getProximal();
    }


    @Override
    public List<Ligand<A>> getLigands(A atom) {
        return active.getLigands(atom);
    }


    @Override
    public List<Arc<A>> getArcs(Ligand<A> ligand) {
        return active.getArcs(ligand);
    }


    @Override
    public Arc<A> getParentArc(Ligand<A> ligand) {
        return active.getParentArc(ligand);
    }


    @Override
    public void build() {
        active.build();
    }

}
