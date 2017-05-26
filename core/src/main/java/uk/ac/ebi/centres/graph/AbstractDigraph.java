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
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.TooManyNodesException;
import uk.ac.ebi.centres.ligand.NonterminalNode;
import uk.ac.ebi.centres.ligand.TerminalNode;

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
    private Node<A> root;
    private ArcMap                   arcs      = new ArcMap(); // Could set expected size
    private ListMultimap<A, Node<A>> ligandMap = ArrayListMultimap.create();
    private DescriptorManager<A> manager;

    // don't differentiate Kekule useful but not correct, atypical case CHEBI:521393
    private static final boolean loose = Boolean.getBoolean("loose.mode");

    public AbstractDigraph(Node<A> root) {
        this(root, new DefaultDescriptorManager<A>());
    }

    public AbstractDigraph(Node<A> root, DescriptorManager<A> manager) {
        if (root == null)
            throw new IllegalArgumentException("Root cannot be null!");
        this.root = root;
        this.manager = manager;
    }


    @Override
    public Node<A> getRoot() {
        return root;
    }


    @Override
    public List<Node<A>> getProximal() {
        return root.getNodes();
    }


    @Override
    public List<Node<A>> ligandInstancesForAtom(A atom) {
        return ligandMap.get(atom);
    }


    /**
     * @inheritDoc
     */
    @Override
    public void reroot(Node<A> node) {

        root = node;
        node.reset();

        Queue<Edge<A>> queue = new LinkedList<Edge<A>>();

        // get parent arcs
        Edge<A> edge = arcs.getForHead(node);
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
        
        for (Node<A> l : ligands())
            l.clearOrderedBy();

        node.setParent(node.getAtom());

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

        Queue<Node<A>> queue = new LinkedList<Node<A>>();

        queue.addAll(root.getNodes());

        while (!queue.isEmpty()) {
            queue.addAll(queue.poll().getNodes());
        }

    }


    @Override
    public List<Edge<A>> getArcs(Node<A> node) {
        return arcs.getForTail(node);
    }


    @Override
    public Edge<A> getParentArc(Node<A> node) {
        return arcs.getForHead(node);
    }


    @Override
    public List<Node<A>> getLigands(Node<A> node) {

        List<Node<A>> nodes = arcs.getHeads(node);

        // lots of ligands being created
        if(ligandMap.size() > NODE_LIMIT)
            throw new TooManyNodesException();

        // ligands already determined
        if (!nodes.isEmpty())
            return nodes;


        // ligands have not be built
        for (A atom : getConnected(node.getAtom())) {

            if (node.isParent(atom))
                continue;

            MutableDescriptor descriptor = manager.getDescriptor(atom);

            // create the new ligand - terminal ligands are created in cases of cyclic molecules
            Node<A> neighbour = node.isVisited(atom)
                                  ? new TerminalNode<A>(this, descriptor, node.getVisited(), atom, node.getAtom(), node.getDistanceFromRoot() + 1)
                                  : new NonterminalNode<A>(this, descriptor, node.getVisited(), atom, node.getAtom(), node.getDistanceFromRoot() + 1);
            arcs.add(newArc(node, neighbour));
            ligandMap.put(atom, neighbour);

            nodes.add(neighbour);

            int order = getOrder(node.getAtom(), atom);

            // create ghost ligands (opened up from double bonds)
            if (order > 1 &&
                node.getDistanceFromRoot() > 0 &&
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
                    Node<A> neighbourGhost = new TerminalNode<A>(this, descriptor, node.getVisited(), atom, node.getAtom(), node.getDistanceFromRoot() + 1);
                    Node<A> ghost          = new TerminalNode<A>(this, descriptor, node.getVisited(), node.getAtom(), atom, node.getDistanceFromRoot() + 1);
                    
                    arcs.add(newArc(node, neighbourGhost));
                    arcs.add(newArc(neighbour, ghost));
                    
                    ligandMap.put(atom, neighbourGhost);
                    ligandMap.put(node.getAtom(), ghost);

                    nodes.add(neighbourGhost);
                }
            }

        }

        // TODO: now add the implicit hydrogens


        return nodes;

    }
    
    public Collection<Node<A>> ligands() {
        return ligandMap.values();
    }

    public abstract Collection<A> getConnected(A atom);

    public abstract int getOrder(A first, A second);

    public abstract int getDepth(A first, A second);


    private Edge<A> newArc(Node<A> tail, Node<A> head) {
        return new Edge<A>(tail, head,
                           manager.getDescriptor(tail.getAtom(), head.getAtom()),
                           getDepth(tail.getAtom(), head.getAtom()));
    }


    /**
     * Manages maps of ligands and thier arcs
     */
    class ArcMap {

        private final ListMultimap<Node<A>, Edge<A>> tails = ArrayListMultimap.create();
        private final Map<Node<A>, Edge<A>>          heads = new HashMap<Node<A>, Edge<A>>();


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


        public Edge<A> getForHead(Node<A> head) {
            return heads.get(head);
        }


        public List<Edge<A>> getForTail(Node<A> tail) {
            return tails.get(tail);
        }


        public List<Node<A>> getHeads(Node<A> tail) {

            // this okay for now but should create a custom list that proxyies calls
            // to the arc list
            List<Edge<A>> edges = tails.get(tail);
            List<Node<A>> nodes = new ArrayList<Node<A>>(edges.size());
            for (Edge<A> edge : edges) {
                nodes.add(edge.getEnd());
            }
            return nodes;

        }


        public Node<A> getTail(Node<A> head) {
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
