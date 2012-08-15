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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.Digraph;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.ligand.NonterminalLigand;
import uk.ac.ebi.centres.ligand.TerminalLigand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 * A digraph with a single immutable root.
 *
 * @author John May
 */
public abstract class RootedDigraph<A> implements Digraph<A>,
                                                  ConnectionProvider<A> {

    private final Ligand<A> root;
    private final ArcMap                     arcs      = new ArcMap(); // Could set expected size
    private final ListMultimap<A, Ligand<A>> ligandMap = ArrayListMultimap.create();
    private final DescriptorManager<A> manager;


    public RootedDigraph(Ligand<A> root) {
        this(root, new DefaultDescriptorManager<A>());
    }


    public RootedDigraph(Ligand<A> root, DescriptorManager<A> manager) {
        if (root == null)
            throw new IllegalArgumentException("Root cannot be null!");
        this.root = root;
        this.manager = manager;
    }


    @Override
    public Ligand<A> getRoot() {
        return root;
    }


    @Override
    public List<Ligand<A>> getProximal() {
        return root.getLigands();
    }


    @Override
    public List<Ligand<A>> getLigands(A atom) {
        return ligandMap.get(atom);
    }


    /**
     * @inheritDoc
     */
    @Override
    public void build() {

        if (root == null)
            throw new IllegalArgumentException("Attempting build without a root");

        Queue<Ligand<A>> queue = new LinkedList<Ligand<A>>();

        queue.addAll(root.getLigands());

        while (!queue.isEmpty()) {
            queue.addAll(queue.poll().getLigands());
        }

    }


    @Override
    public List<Ligand<A>> getLigands(Ligand<A> ligand) {

        List<Ligand<A>> ligands = arcs.getHeads(ligand);

        // ligands already determined
        if (!ligands.isEmpty())
            return ligands;

        // ligands have not be built
        for (A atom : getConnected(ligand.getAtom())) {

            if (ligand.isParent(atom))
                continue;


            // collection creation...
            MutableDescriptor descriptor = manager.getDescriptor(atom);

            // create the new ligand - terminal ligands are created in cases of cyclic molecules
            Ligand<A> neighbour = ligand.isVisited(atom)
                                  ? new TerminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom())
                                  : new NonterminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom());

            arcs.add(newArc(ligand, neighbour));
            ligandMap.put(atom, neighbour);

            ligands.add(neighbour);

            int order = getOrder(ligand.getAtom(), atom);

            // create ghost ligands (opened up from double bonds)
            for (int i = 1; i < order; i++) {

                Ligand<A> ghost = new NonterminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom());
                arcs.add(newArc(ligand, ghost));
                ligandMap.put(atom, ghost);
                ligands.add(ghost);
                Ligand<A> terminalGhost = new TerminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom());
                arcs.add(newArc(ghost, terminalGhost));
                ligandMap.put(ghost.getAtom(), terminalGhost);
            }

        }

        return ligands;

    }


    public abstract Collection<A> getConnected(A atom);

    public abstract int getOrder(A first, A second);

    public abstract int getDepth(A first, A second);


    private Arc<A> newArc(Ligand<A> tail, Ligand<A> head) {
        return new Arc<A>(tail, head,
                          manager.getDescriptor(tail.getAtom(), head.getAtom()),
                          getDepth(tail.getAtom(), head.getAtom()));
    }


    /**
     * Manages maps of ligands and thier arcs
     */
    class ArcMap {

        private final ListMultimap<Ligand<A>, Arc<A>> tails = ArrayListMultimap.create();
        private final Map<Ligand<A>, Arc<A>>          heads = new HashMap<Ligand<A>, Arc<A>>();


        public void add(Arc<A> arc) {
            tails.put(arc.getTail(), arc);
            heads.put(arc.getHead(), arc);
        }


        public Arc<A> getForHead(Ligand<A> head) {
            return heads.get(head);
        }


        public List<Arc<A>> getForTail(Ligand<A> tail) {
            return tails.get(tail);
        }


        public List<Ligand<A>> getHeads(Ligand<A> tail) {

            // this okay for now but should create a custom list that proxyies calls
            // to the arc list
            List<Arc<A>> arcs = tails.get(tail);
            List<Ligand<A>> ligands = new ArrayList<Ligand<A>>(arcs.size());
            for (Arc<A> arc : arcs) {
                ligands.add(arc.getHead());
            }
            return ligands;

        }


        public Ligand<A> getTail(Ligand<A> head) {
            Arc<A> arc = heads.get(head);
            if (arc == null)
                throw new NoSuchElementException("No tail for provided head");
            return arc.getTail();
        }


    }

}
