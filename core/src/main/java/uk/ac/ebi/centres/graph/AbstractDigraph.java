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

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.DescriptorManager;
import uk.ac.ebi.centres.Digraph;
import uk.ac.ebi.centres.Ligand;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.TooManyNodesException;
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
public abstract class AbstractDigraph<A> implements Digraph<A>,
                                                    ConnectionProvider<A> {
    private static final int NODE_LIMIT = 1000000;
    private Ligand<A> root;
    private ArcMap                     arcs      = new ArcMap(); // Could set expected size
    private ListMultimap<A, Ligand<A>> ligandMap = ArrayListMultimap.create();
    private DescriptorManager<A> manager;

    // don't differentiate Kekule useful but not correct, atypical case CHEBI:521393
    private static final boolean loose = Boolean.getBoolean("loose.mode");

    public AbstractDigraph(Ligand<A> root) {
        this(root, new DefaultDescriptorManager<A>());
    }

    public AbstractDigraph(Ligand<A> root, DescriptorManager<A> manager) {
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
    public List<Ligand<A>> ligandInstancesForAtom(A atom) {
        return ligandMap.get(atom);
    }


    /**
     * @inheritDoc
     */
    @Override
    public void reroot(Ligand<A> ligand) {

        root = ligand;
        ligand.reset();

        Queue<Edge<A>> queue = new LinkedList<Edge<A>>();

        // get parent arcs
        Edge<A> edge = arcs.getForHead(ligand);
        while (edge != null) {
            arcs.remove(edge);
            Edge<A> next = arcs.getForHead(edge.getBeg());
            edge.transpose();
            queue.add(edge);
            edge = next;
        }

        for (Edge<A> transposedEdge : queue) {
            arcs.add(transposedEdge);
        }
        
        for (Ligand<A> l : ligands())
            l.clearOrderedBy();

        ligand.setParent(ligand.getAtom());

    }
    
    public String dump() {
        return arcs.toString();
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
    public List<Edge<A>> getArcs(Ligand<A> ligand) {
        return arcs.getForTail(ligand);
    }


    @Override
    public Edge<A> getParentArc(Ligand<A> ligand) {
        return arcs.getForHead(ligand);
    }


    @Override
    public List<Ligand<A>> getLigands(Ligand<A> ligand) {

        List<Ligand<A>> ligands = arcs.getHeads(ligand);

        // lots of ligands being created
        if(ligandMap.size() > NODE_LIMIT)
            throw new TooManyNodesException();

        // ligands already determined
        if (!ligands.isEmpty())
            return ligands;


        // ligands have not be built
        for (A atom : getConnected(ligand.getAtom())) {

            if (ligand.isParent(atom))
                continue;

            MutableDescriptor descriptor = manager.getDescriptor(atom);

            // create the new ligand - terminal ligands are created in cases of cyclic molecules
            Ligand<A> neighbour = ligand.isVisited(atom)
                                  ? new TerminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom(), ligand.getDistanceFromRoot() + 1)
                                  : new NonterminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom(), ligand.getDistanceFromRoot() + 1);
            arcs.add(newArc(ligand, neighbour));
            ligandMap.put(atom, neighbour);

            ligands.add(neighbour);

            int order = getOrder(ligand.getAtom(), atom);

            // create ghost ligands (opened up from double bonds)
            if (order > 1 &&
                ligand.getDistanceFromRoot() > 0 &&
                (!loose && !neighbour.isDuplicate())) {

                // preload the neighbour and add the call back ghost...
                // bit confusing but this turns -c1-c2=c3-o into:
                //          c2
                //         /
                // -c1-c2-c3-o
                //     \
                //      c3
                // when we're at c2 we preload c3 with the oxygen and then add the ghost c2
                getLigands(neighbour);  // preloading
                
                // create required number of ghost ligands
                for (int i = 1; i < order; i++) {
                    Ligand<A> neighbourGhost = new TerminalLigand<A>(this, descriptor, ligand.getVisited(), atom, ligand.getAtom(), ligand.getDistanceFromRoot() + 1);
                    Ligand<A> ghost          = new TerminalLigand<A>(this, descriptor, ligand.getVisited(), ligand.getAtom(), atom, ligand.getDistanceFromRoot() + 1);
                    
                    arcs.add(newArc(ligand, neighbourGhost));
                    arcs.add(newArc(neighbour, ghost));
                    
                    ligandMap.put(atom, neighbourGhost);
                    ligandMap.put(ligand.getAtom(), ghost);

                    ligands.add(neighbourGhost);
                }
            }

        }

        // TODO: now add the implicit hydrogens


        return ligands;

    }
    
    public Collection<Ligand<A>> ligands() {
        return ligandMap.values();
    }

    public abstract Collection<A> getConnected(A atom);

    public abstract int getOrder(A first, A second);

    public abstract int getDepth(A first, A second);


    private Edge<A> newArc(Ligand<A> tail, Ligand<A> head) {
        return new Edge<A>(tail, head,
                           manager.getDescriptor(tail.getAtom(), head.getAtom()),
                           getDepth(tail.getAtom(), head.getAtom()));
    }


    /**
     * Manages maps of ligands and thier arcs
     */
    class ArcMap {

        private final ListMultimap<Ligand<A>, Edge<A>> tails = ArrayListMultimap.create();
        private final Map<Ligand<A>, Edge<A>>          heads = new HashMap<Ligand<A>, Edge<A>>();


        public void remove(Edge<A> edge) {
            //System.out.println("\tremoving " + arc.getBeg() + ": " + arc + " and " + arc.getEnd() + ": " + arc);
            tails.remove(edge.getBeg(), edge);
            heads.remove(edge.getEnd());
        }


        public void add(Edge<A> edge) {
            tails.put(edge.getBeg(), edge);
            if (heads.containsKey(edge.getEnd()))
                System.err.println("Key clash!");
            heads.put(edge.getEnd(), edge);
        }


        public Edge<A> getForHead(Ligand<A> head) {
            return heads.get(head);
        }


        public List<Edge<A>> getForTail(Ligand<A> tail) {
            return tails.get(tail);
        }


        public List<Ligand<A>> getHeads(Ligand<A> tail) {

            // this okay for now but should create a custom list that proxyies calls
            // to the arc list
            List<Edge<A>>   edges   = tails.get(tail);
            List<Ligand<A>> ligands = new ArrayList<Ligand<A>>(edges.size());
            for (Edge<A> edge : edges) {
                ligands.add(edge.getEnd());
            }
            return ligands;

        }


        public Ligand<A> getTail(Ligand<A> head) {
            Edge<A> edge = getForHead(head);
            if (edge == null)
                throw new NoSuchElementException("No tail for provided head");
            return edge.getBeg();
        }

        @Override public String toString() {
            return Joiner.on("\n").join(heads.values());
        }
    }

    @Override public String toString() {
        return dump();
    }

    @Override
    public void dispose() {
        ligandMap.clear();
        arcs.tails.clear();
        arcs.heads.clear();
        root = null;
        arcs = null;
        ligandMap = null;
        manager = null;

    }
}
