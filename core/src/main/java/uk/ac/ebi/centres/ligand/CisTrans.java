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

package uk.ac.ebi.centres.ligand;

import com.google.common.collect.Sets;
import uk.ac.ebi.centres.Centre;
import uk.ac.ebi.centres.ConnectionProvider;
import uk.ac.ebi.centres.Descriptor;
import uk.ac.ebi.centres.Node;
import uk.ac.ebi.centres.MutableDescriptor;
import uk.ac.ebi.centres.Priority;
import uk.ac.ebi.centres.PriorityRule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author John May
 */
public class CisTrans<A> extends AbstractNode<A> implements Centre<A> {

    private final AbstractNode<A> first;
    private final AbstractNode<A> second;
    private final Set<A>          atoms;
    private final A[] carriers;
    private final int config;

    public static final int TOGETHER = 0x1;
    public static final int OPPOSITE = 0x2;

    public CisTrans(A first, A second, A[] carriers, int config, MutableDescriptor descriptor) {
        super(descriptor, 0);
        Node<A> self = this;
        this.first  = new NonterminalNode<A>(descriptor, first, second, 0);
        this.second = new NonterminalNode<A>(descriptor, second, first, 0);
        this.atoms = Sets.newHashSet(first, second);
        this.carriers = carriers;
        this.config = config;
    }


    @Override
    public void setProvider(ConnectionProvider<A> provider) {
        super.setProvider(provider);
        first.setProvider(provider);
        second.setProvider(provider);
    }


    @Override
    public List<Node<A>> getNodes() {
        List<Node<A>> nodes = new ArrayList<Node<A>>(16);
        nodes.addAll(first.getNodes());
        nodes.addAll(second.getNodes());
        return nodes;
    }


    @Override
    public A getAtom() {
        // might need a rethink...
        throw new NoSuchMethodError("Centre does not have a single atom");
    }


    @Override
    public String toString() {
        return first.getAtom().toString() + "=" + second.getAtom().toString();
    }


    @Override
    public Boolean isParent(Object atom) {
        return atoms.contains(atom);
    }


    @Override
    public Set<A> getFoci() {
        return atoms;
    }


    @Override
    public A getParent() {
        throw new UnsupportedOperationException("Can't get parent on a planar centre");
    }


    @Override
    public void setParent(A atom) {
        throw new UnsupportedOperationException("Can't set parent on a planar centre");
    }


    @Override
    public int perceiveAuxiliary(Collection<Centre<A>> centres, PriorityRule<A> rule) {
        // System.err.println("Auxiliary perception is not currently supported on planar centres");
        return 0;
    }


    @Override
    public uk.ac.ebi.centres.Descriptor perceive(List<Node<A>> proximal, PriorityRule<A> rule) {
        // can't do this type of perception for planar centres
        return Descriptor.Unknown;
    }


    @Override
    public uk.ac.ebi.centres.Descriptor perceive(PriorityRule<A> rule) {

        List<Node<A>> firstNodes  = first.getNodes();
        List<Node<A>> secondNodes = second.getNodes();

        if (firstNodes.isEmpty() || secondNodes.isEmpty())
            return Descriptor.None;

        // check for pseudo
        Priority firstPriority  = rule.prioritise(firstNodes);
        Priority secondPriority = rule.prioritise(secondNodes);

        if (!firstPriority.isUnique() || !secondPriority.isUnique()) {
            // we don't know whether it is none yet...
            return Descriptor.Unknown;
        }

        int config = this.config;

        if (firstNodes.size() > 1 && firstNodes.get(1).getAtom().equals(carriers[0]))
            config ^= 0x3;
        //else if (!firstNodes.get(0).getAtom().equals(carriers[0]))
        //    throw new IllegalArgumentException();
        if (secondNodes.size() > 2 && secondNodes.get(1).getAtom().equals(carriers[1]))
            config ^= 0x3;
        //else if (!secondNodes.get(0).getAtom().equals(carriers[1]))
        //    throw new IllegalArgumentException();

        // this should be an or?
        boolean pseudo = firstPriority.isPseduoAsymettric() || secondPriority.isPseduoAsymettric();

        // also check for psuedo (from prioritise)
        return config == TOGETHER ? pseudo ? Descriptor.z : Descriptor.Z
                                  : pseudo ? Descriptor.E : Descriptor.E;

    }


    @Override
    public void dispose() {
        getProvider().dispose();
        setProvider(null);
    }


}
